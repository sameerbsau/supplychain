package net.corda.samples.supplychain.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.services.AccountService;
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount;
import com.r3.corda.lib.accounts.workflows.services.KeyManagementBackedAccountService;
import com.sun.istack.NotNull;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.samples.supplychain.accountUtilities.NewKeyForAccount;
import net.corda.samples.supplychain.contracts.InvoiceStateContract;
import net.corda.samples.supplychain.states.InvoiceState;
import net.corda.core.crypto.TransactionSignature;
import net.corda.core.flows.*;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.samples.supplychain.states.OrderState;

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
public class SendInvoice extends FlowLogic<String> {
    private UUID orderId ;
    //private variables
    private String whoAmI ;
    private String whereTo;
    private Double amount;

    //public constructor
    public SendInvoice(UUID orderId, String whoAmI, String whereTo, Double amount){
        this.orderId = orderId;
        this.whoAmI = whoAmI;
        this.whereTo = whereTo;
        this.amount = amount;
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
        AnonymousParty targetAcctAnonymousParty = subFlow(new RequestKeyForAccount(targetAccount));
        //take the order as input....query the states and get the order details...verify the invoice amount and the order total amount
        QueryCriteria.VaultQueryCriteria criteria = new QueryCriteria.VaultQueryCriteria()
                .withExternalIds(Arrays.asList(myAccount.getIdentifier().getId()));
        List<UUID> orderIds = getServiceHub().getVaultService().queryBy(OrderState.class).getStates().stream().map(it ->it.getState().getData().getOrderID() ).collect(Collectors.toList());
        System.out.println(Arrays.toString(orderIds.toArray()));

        OrderState orderState = getServiceHub().getVaultService().queryBy(OrderState.class).getStates().stream().filter(orderStateStateAndRef -> orderStateStateAndRef.getState().getData().getOrderID().equals(orderId)).findFirst().get().getState().getData();

                //.filter(s -> s.getOrderID().equals(orderId)).findFirst().get();

        UUID invoiceId = UUID.randomUUID();
        //generating State for transfer
        InvoiceState output = new InvoiceState(amount,new AnonymousParty(myKey),targetAcctAnonymousParty,invoiceId ,orderState.getOrderValue(), orderId);

        // Obtain a reference to a notary we wish to use.
        /** Explicit selection of notary by CordaX500Name - argument can by coded in flows or parsed from config (Preferred)*/
        final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"));

        TransactionBuilder txbuilder = new TransactionBuilder(notary)
                .addOutputState(output)
                .addCommand(new InvoiceStateContract.Commands.Create(), Arrays.asList(targetAcctAnonymousParty.getOwningKey(),myKey));

        //self sign Transaction
        SignedTransaction locallySignedTx = getServiceHub().signInitialTransaction(txbuilder,Arrays.asList(getOurIdentity().getOwningKey(),myKey));

        //Collect sigs
        FlowSession sessionForAccountToSendTo = initiateFlow(targetAccount.getHost());
        List<TransactionSignature> accountToMoveToSignature = (List<TransactionSignature>) subFlow(new CollectSignatureFlow(locallySignedTx,
                sessionForAccountToSendTo,targetAcctAnonymousParty.getOwningKey()));
        SignedTransaction signedByCounterParty = locallySignedTx.withAdditionalSignatures(accountToMoveToSignature);

        //Finalize
        subFlow(new FinalityFlow(signedByCounterParty,
                Arrays.asList(sessionForAccountToSendTo).stream().filter(it -> it.getCounterparty() != getOurIdentity()).collect(Collectors.toList())));
        return "Invoice send to " + targetAccount.getHost().getName().getOrganisation() + "'s "+ targetAccount.getName() + " team. InvoiceId: "+invoiceId;
    }
}


@InitiatedBy(SendInvoice.class)
class SendInvoiceResponder extends FlowLogic<Void> {
    //private variable
    private FlowSession counterpartySession;

    //Constructor
    public SendInvoiceResponder(FlowSession counterpartySession) {
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
                    InvoiceState invoiceState = (InvoiceState) output;
                    require.using("Invoice amount is not matching with the order value",invoiceState.getAmount().equals(invoiceState.getOrderValue()) );
                    return null;
                });
            }
        });
        subFlow(new ReceiveFinalityFlow(counterpartySession));
        return null;
    }
}

