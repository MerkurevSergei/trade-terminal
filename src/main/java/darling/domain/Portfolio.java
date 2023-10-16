package darling.domain;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_BUY;
import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_SELL;

public class Portfolio {

    private static final Comparator<Contract> CONTRACT_COMPARATOR = Comparator.comparing(Contract::price).thenComparing(Contract::date);

    private final TreeMap<Share, Group> groups = new TreeMap<>(Comparator.comparing(Share::ticker));
    private final Map<AccountShareKey, TreeSet<Contract>> contractsByKey = new HashMap<>();

    public List<Contract> toPrint() {
//        for (Map.Entry<AccountShareKey, TreeSet<Contract>> e : contractsByKey.entrySet()) {
//
//        }
        return contractsByKey.values().stream().flatMap(Collection::stream).toList();
    }

    private void removeUseQuantity(TreeSet<Contract> contracts, Operation operation) {
        long opQuantity = operation.quantityDone();
        while (opQuantity > 0) {
            Contract first = contracts.first();
            long firstQuantity = first.quantity();
            if (first.quantity() > opQuantity) {
                first.setQuantity(first.quantity() - opQuantity);
            } else {
                contracts.remove(first);
            }
            opQuantity = opQuantity - firstQuantity;
        }
    }

    private void removeReverseUseQuantity(TreeSet<Contract> contracts, Operation operation) {
        long opQuantity = operation.quantityDone();
        while (opQuantity > 0) {
            Contract last = contracts.last();
            long lastQuantity = last.quantity();
            if (last.quantity() > opQuantity) {
                last.setQuantity(last.quantity() - opQuantity);
            } else {
                contracts.remove(last);
            }
            opQuantity = opQuantity - lastQuantity;
        }
    }

    public void refresh(Operation operation) {
        if (!Set.of(OPERATION_TYPE_BUY, OPERATION_TYPE_SELL).contains(operation.type())) {
            return;
        }
        AccountShareKey key = new AccountShareKey(operation.brokerAccountId(), operation.instrumentUid());
        TreeSet<Contract> contracts = contractsByKey.getOrDefault(key, new TreeSet<>(CONTRACT_COMPARATOR));
        if (contracts.isEmpty()) {
            contracts.add(new Contract(operation));
            contractsByKey.put(key, contracts);
            return;
        }
        Contract firstContract = contracts.first();
        if (firstContract.type() == operation.type()) {
            contracts.add(new Contract(operation));
        } else if (OPERATION_TYPE_BUY.equals(firstContract.type())) {
            removeUseQuantity(contracts, operation);
        } else {
            removeReverseUseQuantity(contracts, operation);
        }
    }

    public record Group(String instrumentUid, String figi, BigDecimal price, BigDecimal payment, long quantity,
                        List<Contract> contracts) {
    }

    public record AccountShareKey(String accountId, String instrumentUid) {
    }
}