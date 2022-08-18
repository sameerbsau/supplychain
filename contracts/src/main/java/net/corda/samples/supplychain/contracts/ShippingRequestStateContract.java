package net.corda.samples.supplychain.contracts;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.transactions.LedgerTransaction;
import net.corda.samples.supplychain.states.InvoiceState;
import net.corda.samples.supplychain.states.ShippingRequestState;

import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

// ************
// * Contract *
// ************
public class ShippingRequestStateContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "net.corda.samples.supplychain.contracts.ShippingRequestStateContract";

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) {

        final CommandWithParties<ShippingRequestStateContract.Commands> command = requireSingleCommand(tx.getCommands(), ShippingRequestStateContract.Commands.class);

        List<ContractState> inputs = tx.getInputStates();
        List<ContractState> outputs = tx.getOutputStates();

        if (command.getValue() instanceof ShippingRequestStateContract.Commands.Create) {

            // Using Corda DSL function requireThat to replicate conditions-checks
            requireThat(require -> {
               // require.using("No inputs should be consumed when creating a new Invoice State.", inputs.isEmpty());
               // require.using("Transaction must have exactly one output.", outputs.size() == 1);
                //ShippingRequestState output = (ShippingRequestState) outputs.get(0);
               // require.using("Invoice amount must be a valid number (Greater than zero)", output.getAmount() > 0);
                System.out.println("inside the shipping contract method");
                return null;
            });

        } else {
            throw new IllegalArgumentException("Command not found!");
        }
    }

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        class Create implements Commands {}
    }
}