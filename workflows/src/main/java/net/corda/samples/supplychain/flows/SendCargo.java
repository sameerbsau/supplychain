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
import net.corda.samples.supplychain.contracts.CargoStateContract;
import net.corda.samples.supplychain.states.CargoState;
import net.corda.core.crypto.TransactionSignature;
import net.corda.core.flows.*;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.samples.supplychain.states.InvoiceState;
import net.corda.samples.supplychain.states.OrderState;
import net.corda.samples.supplychain.states.ShippingRequestState;

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
public class SendCargo extends FlowLogic<String> {

    //private variables
    private String pickupFrom;
    private String whereTo;

    private UUID orderId;


    //public constructor
    public SendCargo(String pickupFrom, String shipTo, UUID orderId) {
        this.pickupFrom = pickupFrom;
        this.whereTo = shipTo;
        this.orderId = orderId;
    }

    @Suspendable
    @Override
    public String call() throws FlowException {
        //grab account service
        AccountService accountService = getServiceHub().cordaService(KeyManagementBackedAccountService.class);
        //grab the account information
        AccountInfo myAccount = accountService.accountInfo(pickupFrom).get(0).getState().getData();
        PublicKey myKey = subFlow(new NewKeyForAccount(myAccount.getIdentifier().getId())).getOwningKey();

        AccountInfo targetAccount = accountService.accountInfo(whereTo).get(0).getState().getData();
        AnonymousParty targetAcctAnonymousParty = subFlow(new RequestKeyForAccount(targetAccount));

        QueryCriteria.VaultQueryCriteria criteria = new QueryCriteria.VaultQueryCriteria()
                .withExternalIds(Arrays.asList(targetAccount.getIdentifier().getId()));
        System.out.println("1--------"+orderId);
        OrderState orderState=null;
        try {
//            ShippingRequestState shippingRequestState = getServiceHub().getVaultService().queryBy(ShippingRequestState.class).getStates().stream()
//                    .filter(orderStateStateAndRef -> orderStateStateAndRef.getState().getData().getShippingId().equals(shippingId))
//                    .findFirst().get().getState().getData();
            System.out.println("2");
            System.out.println(getServiceHub());
            System.out.println(getServiceHub().getVaultService());
            System.out.println(getServiceHub().getVaultService().queryBy(OrderState.class));
            System.out.println(getServiceHub().getVaultService().queryBy(OrderState.class).getStates());
             orderState = getServiceHub().getVaultService().queryBy(OrderState.class).getStates().stream()
                    .filter(orderStateStateAndRef -> orderStateStateAndRef.getState().getData().getOrderID()
                            .equals(orderId)).findFirst().get().getState().getData();
        }catch (Exception e){
            e.printStackTrace();
        }



        System.out.println("3");
        String create = "randomString";
//        Timestamp create = new Timestamp(System.currentTimeMillis());
        System.out.println("4");
        //generating State for transfer
        CargoState output = new CargoState(new AnonymousParty(myKey), targetAcctAnonymousParty, orderState, getOurIdentity(), create);
        System.out.println("5");
        // Obtain a reference to a notary we wish to use.
        /** Explicit selection of notary by CordaX500Name - argument can by coded in flows or parsed from config (Preferred)*/
        final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"));
        System.out.println("6");
        TransactionBuilder txbuilder = new TransactionBuilder(notary)
                .addOutputState(output)
                .addCommand(new CargoStateContract.Commands.Create(), Arrays.asList(targetAcctAnonymousParty.getOwningKey(), getOurIdentity().getOwningKey()));
        System.out.println("7");
        //self sign Transaction
        SignedTransaction locallySignedTx = null;
        System.out.println("8");
        locallySignedTx = getServiceHub().signInitialTransaction(txbuilder, Arrays.asList(getOurIdentity().getOwningKey()));

        System.out.println("9");
        //Collect sigs
        FlowSession sessionForAccountToSendTo = initiateFlow(targetAccount.getHost());
        System.out.println("10");
        List<TransactionSignature> accountToMoveToSignature = (List<TransactionSignature>) subFlow(new CollectSignatureFlow(locallySignedTx,
                sessionForAccountToSendTo, targetAcctAnonymousParty.getOwningKey()));
        System.out.println("11");
        SignedTransaction signedByCounterParty = locallySignedTx.withAdditionalSignatures(accountToMoveToSignature);
        System.out.println("12");
        //Finalize
        subFlow(new FinalityFlow(signedByCounterParty,
                Arrays.asList(sessionForAccountToSendTo).stream().filter(it -> it.getCounterparty() != getOurIdentity()).collect(Collectors.toList())));
        System.out.println("13");
        return "send " + orderState.getOrderID() + " to " + targetAccount.getHost().getName().getOrganisation() + "'s " + targetAccount.getName() + " team";

    }
}


@InitiatedBy(SendCargo.class)
class SendCargoResponder extends FlowLogic<Void> {
    //private variable
    private FlowSession counterpartySession;

    //Constructor
    public SendCargoResponder(FlowSession counterpartySession) {
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
                    System.out.println("-----1");
                    ContractState output = stx.getTx().getOutputs().get(0).getData();
                    System.out.println("-----2");
                    CargoState cargoState = (CargoState) output;
                    System.out.println("-----3");
//                    OrderState orderState = getServiceHub().getVaultService().queryBy(OrderState.class).getStates().stream()
//                            .filter(orderStateStateAndRef -> orderStateStateAndRef.getState().getData().getOrderID()
//                                    .equals(cargoState.getCargo().getOrderID())).findFirst().get().getState().getData();
                    System.out.println("-----4");
                    require.using("Invoice amount is not matching with the order value",cargoState.getCargo().getOrderDetails().size()!=0);
                    //create state with the order status delivered
                    return null;
                });
            }
        });
        subFlow(new ReceiveFinalityFlow(counterpartySession));
        return null;
    }
}

