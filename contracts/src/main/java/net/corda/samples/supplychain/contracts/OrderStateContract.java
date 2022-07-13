package net.corda.samples.supplychain.contracts;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

public class OrderStateContract implements Contract {

    public static final String ID = "net.corda.samples.supplychain.contracts.OrderStateContract";
    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {

    }


    public interface Commands extends CommandData {
        class Create implements CargoStateContract.Commands {}
    }
}
