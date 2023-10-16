package darling.domain;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_BUY;
import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_SELL;

public class Portfolio {

    private static final Comparator<OpenDeal> CONTRACT_COMPARATOR = Comparator.comparing(OpenDeal::getPrice).thenComparing(OpenDeal::getDate);

    private final Map<AccountShareKey, TreeSet<OpenDeal>> dealsByKey = new HashMap<>();

    public Portfolio(List<OpenDeal> allOpenDeals) {
        for (OpenDeal openDeal : allOpenDeals) {
            AccountShareKey key = new AccountShareKey(openDeal.getAccountId(), openDeal.getInstrumentUid());
            dealsByKey.putIfAbsent(key, new TreeSet<>(CONTRACT_COMPARATOR));
            TreeSet<OpenDeal> openDeals = dealsByKey.get(key);
            openDeals.add(openDeal);
        }

    }

    public void refresh(Operation operation) {
        if (!Set.of(OPERATION_TYPE_BUY, OPERATION_TYPE_SELL).contains(operation.type())) {
            return;
        }
        AccountShareKey key = new AccountShareKey(operation.brokerAccountId(), operation.instrumentUid());
        TreeSet<OpenDeal> openDeals = dealsByKey.getOrDefault(key, new TreeSet<>(CONTRACT_COMPARATOR));
        if (openDeals.isEmpty()) {
            openDeals.add(new OpenDeal(operation));
            dealsByKey.put(key, openDeals);
            return;
        }
        OpenDeal firstOpenDeal = openDeals.first();
        if (firstOpenDeal.getType() == operation.type()) {
            openDeals.add(new OpenDeal(operation));
        } else if (OPERATION_TYPE_BUY.equals(firstOpenDeal.getType())) {
            removeUseQuantity(openDeals, operation);
        } else {
            removeReverseUseQuantity(openDeals, operation);
        }
    }

    public List<OpenDeal> getDeals() {
        return dealsByKey.values().stream().flatMap(Collection::stream).toList();
    }

    private void removeUseQuantity(TreeSet<OpenDeal> openDeals, Operation operation) {
        long opQuantity = operation.quantityDone();
        while (opQuantity > 0) {
            OpenDeal first = openDeals.first();
            long firstQuantity = first.getQuantity();
            if (first.getQuantity() > opQuantity) {
                first.setQuantity(first.getQuantity() - opQuantity);
            } else {
                openDeals.remove(first);
            }
            opQuantity = opQuantity - firstQuantity;
        }
    }

    private void removeReverseUseQuantity(TreeSet<OpenDeal> openDeals, Operation operation) {
        long opQuantity = operation.quantityDone();
        while (opQuantity > 0) {
            OpenDeal last = openDeals.last();
            long lastQuantity = last.getQuantity();
            if (last.getQuantity() > opQuantity) {
                last.setQuantity(last.getQuantity() - opQuantity);
            } else {
                openDeals.remove(last);
            }
            opQuantity = opQuantity - lastQuantity;
        }
    }

    public record AccountShareKey(String accountId, String instrumentUid) {
    }

}