package net.corda.samples.supplychain.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount;
import com.r3.corda.lib.accounts.workflows.services.AccountService;
import com.r3.corda.lib.accounts.workflows.services.KeyManagementBackedAccountService;
import com.sun.istack.NotNull;
import net.corda.core.contracts.ContractState;
import net.corda.core.crypto.TransactionSignature;
import net.corda.core.flows.*;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.samples.supplychain.accountUtilities.NewKeyForAccount;

import net.corda.samples.supplychain.contracts.OrderStateContract;

import net.corda.samples.supplychain.states.OrderState;
import net.corda.samples.supplychain.states.models.ProductDetails;


import java.security.PublicKey;
import java.time.Instant;
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
public class PlaceOrder extends FlowLogic<String> {

    private String buyer;
    private String seller;
    private List<ProductDetails> orderDetails;


    public PlaceOrder(String buyer, String seller, List<ProductDetails> orderDetails) {
        this.buyer = buyer;
        this.seller = seller;
        this.orderDetails = orderDetails;

    }

    @Suspendable
    @Override
    public String call() throws FlowException {
        //grab account service
        AccountService accountService = getServiceHub().cordaService(KeyManagementBackedAccountService.class);
        //grab the account information
        AccountInfo myAccount = accountService.accountInfo(buyer).get(0).getState().getData();
        PublicKey myKey = subFlow(new NewKeyForAccount(myAccount.getIdentifier().getId())).getOwningKey();

        AccountInfo targetAccount = accountService.accountInfo(seller).get(0).getState().getData();
        AnonymousParty targetAcctAnonymousParty = subFlow(new RequestKeyForAccount(targetAccount));
        List<Double> totalOrderValue = orderDetails.stream().map(productDetails -> productDetails.getPrice()).collect(Collectors.toList());
        Double sum = totalOrderValue.stream().mapToDouble(Double::doubleValue).sum();
        //generating State for transfer
        UUID orderId = UUID.randomUUID();
        OrderState output = new OrderState(orderId, new AnonymousParty(myKey), targetAcctAnonymousParty, orderDetails, sum, Instant.now(), "OrderCreated");

        // Obtain a reference to a notary we wish to use.
        /** Explicit selection of notary by CordaX500Name - argument can by coded in flows or parsed from config (Preferred)*/
        final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"));

        TransactionBuilder txbuilder = new TransactionBuilder(notary)
                .addOutputState(output)
                .addCommand(new OrderStateContract.Commands.Create(), Arrays.asList(targetAcctAnonymousParty.getOwningKey(), myKey));

        //verify the transaction

        txbuilder.verify(getServiceHub());


        //self sign Transaction
        SignedTransaction locallySignedTx = getServiceHub().signInitialTransaction(txbuilder, Arrays.asList(getOurIdentity().getOwningKey(), myKey));

        //Collect signs
        FlowSession sessionForAccountToSendTo = initiateFlow(targetAccount.getHost());
        List<TransactionSignature> accountToMoveToSignature = (List<TransactionSignature>) subFlow(new CollectSignatureFlow(locallySignedTx,
                sessionForAccountToSendTo, targetAcctAnonymousParty.getOwningKey()));
        SignedTransaction signedByCounterParty = locallySignedTx.withAdditionalSignatures(accountToMoveToSignature);

        //Finalize
        subFlow(new FinalityFlow(signedByCounterParty,
                Arrays.asList(sessionForAccountToSendTo).stream().filter(it -> it.getCounterparty() != getOurIdentity()).collect(Collectors.toList())));
        return "Order send to " + targetAccount.getHost().getName().getOrganisation() + "'s " + targetAccount.getName() + " team. Order Value: " + sum + " OrderID: " + orderId;
    }
}


@InitiatedBy(PlaceOrder.class)
class PlaceOrderResponder extends FlowLogic<Void> {
    //private variable
    private FlowSession counterpartySession;

    //Constructor
    public PlaceOrderResponder(FlowSession counterpartySession) {
        this.counterpartySession = counterpartySession;
    }

    @Override
    @Suspendable
    public Void call() throws FlowException {
        subFlow(new SignTransactionFlow(counterpartySession) {
            @Override
            protected void checkTransaction(@NotNull SignedTransaction stx) throws FlowException {
                // Custom Logic to validate transaction.
                //seller can write the custom checks
                //check the weight and quantity

                requireThat(require -> {
                    ContractState output = stx.getTx().getOutputs().get(0).getData();
                    OrderState orderState = (OrderState) output;
                    require.using("From Seller:Product details should not be empty)", orderState.getOrderDetails() != null && orderState.getOrderDetails().size() > 0);
                    require.using("From Seller:Order Value must be Greater than 100)", orderState.getOrderValue() > 100);
                    List<String> UOMS = orderState.getOrderDetails().stream().map(productDetails -> productDetails.getUOM()).collect(Collectors.toList());
                    boolean uomcheck = UOMS.stream().allMatch(s -> s.equalsIgnoreCase("Weight") || s.equalsIgnoreCase("Quantity"));
                    require.using("From Seller:Invalid UOM", uomcheck);
                    return null;
                });

            }
        });
        subFlow(new ReceiveFinalityFlow(counterpartySession));
        return null;
    }
}
