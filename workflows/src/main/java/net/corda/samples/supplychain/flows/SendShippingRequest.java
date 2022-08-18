package net.corda.samples.supplychain.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount;
import com.r3.corda.lib.accounts.workflows.services.AccountService;
import com.r3.corda.lib.accounts.workflows.services.KeyManagementBackedAccountService;
import com.sun.istack.NotNull;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.samples.supplychain.accountUtilities.NewKeyForAccount;
import net.corda.samples.supplychain.contracts.ShippingRequestStateContract;
import net.corda.samples.supplychain.states.PaymentState;
import net.corda.samples.supplychain.states.ShippingRequestState;
import net.corda.core.crypto.TransactionSignature;
import net.corda.core.flows.*;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.corda.core.contracts.ContractsDSL.requireThat;


// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class SendShippingRequest extends FlowLogic<String> {

    private final ProgressTracker progressTracker = tracker();

    private static final ProgressTracker.Step GENERATING_TRANSACTION = new ProgressTracker.Step("Generating a HeartState transaction");
    private static final ProgressTracker.Step SIGNING_TRANSACTION = new ProgressTracker.Step("Signing transaction with out private key.");
    private static final ProgressTracker.Step FINALISING_TRANSACTION = new ProgressTracker.Step("Recording transaction") {
        @Override
        public ProgressTracker childProgressTracker() {
            return FinalityFlow.tracker();
        }
    };

    private static ProgressTracker tracker() {
        return new ProgressTracker(
                GENERATING_TRANSACTION,
                SIGNING_TRANSACTION,
                FINALISING_TRANSACTION
        );
    }

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    //private variables
    private String whoAmI ;
    private String whereTo;
    private String shipper;
    private UUID orderId;


    //public constructor
    public SendShippingRequest(String whoAmI, String whereTo, String shipper, UUID orderId){
        this.whoAmI = whoAmI;
        this.whereTo = whereTo;
        this.shipper = shipper;
        this.orderId = orderId;
    }

    @Suspendable
    @Override
    public String call() throws FlowException {
        //grab account service
        AccountService accountService = getServiceHub().cordaService(KeyManagementBackedAccountService.class);
        //grab the account information
        AccountInfo myAccount = accountService.accountInfo(whoAmI).get(0).getState().getData();
        PublicKey myKey = subFlow(new NewKeyForAccount(myAccount.getIdentifier().getId())).getOwningKey();

        AccountInfo targetAccount = accountService.accountInfo(whereTo).get(0).getState().getData();

        AccountInfo shipperAccount = accountService.accountInfo(shipper).get(0).getState().getData();
        AnonymousParty targetAcctAnonymousParty = subFlow(new RequestKeyForAccount(shipperAccount));

        //verify the payment is completed for the shipping order
        System.out.println("1");
        PaymentState paymentState = getServiceHub().getVaultService().queryBy(PaymentState.class).getStates().stream()
                .filter(orderStateStateAndRef -> orderStateStateAndRef.getState().getData().getOrderId().equals(orderId))
                .findFirst().get().getState().getData();
        System.out.println("2");
        System.out.println("payment state-------->"+paymentState.getPaymentState());
        UUID shippingId = UUID.randomUUID();
        //generating State for transfer
        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        ShippingRequestState output = new ShippingRequestState(shippingId, new AnonymousParty(myKey),whereTo,targetAcctAnonymousParty, orderId, paymentState.getPaymentState());
        System.out.println("3");
        // Obtain a reference to a notary we wish to use.
        /** Explicit selection of notary by CordaX500Name - argument can by coded in flows or parsed from config (Preferred)*/
        final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"));
        System.out.println("4");
        TransactionBuilder txbuilder = new TransactionBuilder(notary)
                .addOutputState(output)
                .addCommand(new ShippingRequestStateContract.Commands.Create(), Arrays.asList(targetAcctAnonymousParty.getOwningKey(),myKey));
        txbuilder.verify(getServiceHub());
        System.out.println("5");
        //self sign Transaction
        SignedTransaction locallySignedTx = getServiceHub().signInitialTransaction(txbuilder,Arrays.asList(getOurIdentity().getOwningKey(),myKey));
        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        System.out.println("6");
        //Collect sigs
        FlowSession sessionForAccountToSendTo = initiateFlow(shipperAccount.getHost());
        System.out.println("7");
        List<TransactionSignature> accountToMoveToSignature = (List<TransactionSignature>) subFlow(new CollectSignatureFlow(locallySignedTx,
                sessionForAccountToSendTo,targetAcctAnonymousParty.getOwningKey()));
        System.out.println("8");
        SignedTransaction signedByCounterParty = locallySignedTx.withAdditionalSignatures(accountToMoveToSignature);
        progressTracker.setCurrentStep(FINALISING_TRANSACTION);
        System.out.println("9");
        //Finalize
        subFlow(new FinalityFlow(signedByCounterParty,
                Arrays.asList(sessionForAccountToSendTo).stream().filter(it -> it.getCounterparty() != getOurIdentity()).collect(Collectors.toList())));
        System.out.println("10");
        return "Request"+shipperAccount.getHost().getName().getOrganisation() +" to send " + orderId+ " to "
                + targetAccount.getHost().nameOrNull().getOrganisation() + "'s "+ targetAccount.getName() + " team ShippingId: "+shippingId;
    }
}


@InitiatedBy(SendShippingRequest.class)
class SendShippingRequestResponder extends FlowLogic<Void> {
    //private variable
    private FlowSession counterpartySession;

    //Constructor
    public SendShippingRequestResponder(FlowSession counterpartySession) {
        this.counterpartySession = counterpartySession;
    }

    @Override
    @Suspendable
    public Void call() throws FlowException {
        subFlow(new SignTransactionFlow(counterpartySession) {
            @Override
            protected void checkTransaction(@NotNull SignedTransaction stx) throws FlowException {
                // Custom Logic to validate transaction.

                requireThat(require -> {
                    ContractState output = stx.getTx().getOutputs().get(0).getData();
                    ShippingRequestState shippingRequestState = (ShippingRequestState) output;
                    require.using("Payment state is not valid",shippingRequestState.getPaymentState().equalsIgnoreCase("COMPLETED") );
                    return null;
                });
            }
        });
        subFlow(new ReceiveFinalityFlow(counterpartySession));
        return null;
    }
}

