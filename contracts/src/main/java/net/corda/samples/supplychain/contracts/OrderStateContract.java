package net.corda.samples.supplychain.contracts;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.transactions.LedgerTransaction;
import net.corda.samples.supplychain.states.InvoiceState;
import net.corda.samples.supplychain.states.OrderState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

public class OrderStateContract implements Contract {

    public static final String ID = "net.corda.samples.supplychain.contracts.OrderStateContract";

    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {

        final CommandWithParties<OrderStateContract.Commands> command = requireSingleCommand(tx.getCommands(), OrderStateContract.Commands.class);

        List<ContractState> inputs = tx.getInputStates();
        List<ContractState> outputs = tx.getOutputStates();


        if (command.getValue() instanceof OrderStateContract.Commands.Create) {

            requireThat(require -> {
                require.using("No inputs should be consumed when creating a new Invoice State.", inputs.isEmpty());
                require.using("Transaction must have exactly one output.", outputs.size() == 1);
                OrderState output = (OrderState) outputs.get(0);

                require.using("Order Value must be a valid number (Greater than zero)", output.getOrderValue() > 0.0);
                return null;
            });
        } else {
            throw new IllegalArgumentException("Command not found!");
        }


    }


    public interface Commands extends CommandData {
        class Create implements Commands {
        }
    }
}
