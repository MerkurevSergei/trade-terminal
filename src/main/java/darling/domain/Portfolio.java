package darling.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import static java.math.BigDecimal.ZERO;
import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_BUY;
import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_SELL;

public class Portfolio {

    private static final Comparator<Deal> CONTRACT_COMPARATOR = Comparator.comparing(Deal::getPrice).thenComparing(Deal::getDate);

    private final Map<AccountShareKey, TreeSet<Deal>> dealsByKey = new HashMap<>();

    public Portfolio(List<Deal> allDeals) {
        for (Deal deal : allDeals) {
            AccountShareKey key = new AccountShareKey(deal.getAccountId(), deal.getInstrumentUid());
            dealsByKey.putIfAbsent(key, new TreeSet<>(CONTRACT_COMPARATOR));
            TreeSet<Deal> deals = dealsByKey.get(key);
            deals.add(deal);
        }

    }

    public List<Deal> refresh(Operation operation) {
        if (!Set.of(OPERATION_TYPE_BUY, OPERATION_TYPE_SELL).contains(operation.type())) {
            return List.of();
        }
        AccountShareKey key = new AccountShareKey(operation.brokerAccountId(), operation.instrumentUid());
        TreeSet<Deal> deals = dealsByKey.getOrDefault(key, new TreeSet<>(CONTRACT_COMPARATOR));
        if (deals.isEmpty()) {
            Deal deal = new Deal(operation, operation.quantityDone(), ZERO);
            deals.add(deal);
            dealsByKey.put(key, deals);
            return List.of(deal);
        }
        Deal firstDeal = deals.first();
        if (firstDeal.getType() == operation.type()) {
            Deal deal = new Deal(operation, operation.quantityDone(), ZERO);
            deals.add(deal);
            return List.of(deal);
        } else if (OPERATION_TYPE_BUY.equals(firstDeal.getType())) {
            return removeUseQuantity(deals, operation);
        } else {
            return removeReverseUseQuantity(deals, operation);
        }
    }

    public List<Deal> getOpenDeals() {
        return dealsByKey.values().stream().flatMap(Collection::stream).toList();
    }

    public List<Deal> getOpenDeals(String instrumentUuid) {
        return dealsByKey.values().stream()
                .flatMap(Collection::stream)
                .filter(deal -> Objects.equals(deal.getInstrumentUid(), instrumentUuid))
                .toList();
    }

    public void updateDealsWithCalculatedData(List<Deal> updatedDeals) {
        updatedDeals.forEach(deal -> {
            AccountShareKey key = new AccountShareKey(deal.getAccountId(), deal.getInstrumentUid());
            TreeSet<Deal> deals = dealsByKey.get(key);
            if (deals == null) return;
            deals.remove(deal);
            deals.add(deal);
        });
    }

    private List<Deal> removeUseQuantity(TreeSet<Deal> deals, Operation operation) {
        List<Deal> forClosed = new ArrayList<>();
        long opQuantity = operation.quantityDone();
        while (opQuantity > 0) {
            if (deals.isEmpty()) {
                Deal newDeal = new Deal(operation, opQuantity, ZERO);
                deals.add(newDeal);
                forClosed.add(newDeal);
                return forClosed;
            }
            Deal first = deals.first();
            long firstQuantity = first.getQuantity();
            if (first.getQuantity() > opQuantity) {
                first.setQuantity(first.getQuantity() - opQuantity);
            } else {
                deals.remove(first);
                forClosed.add(first) ;
            }
            opQuantity = opQuantity - firstQuantity;
        }
        return forClosed;
    }

    private List<Deal> removeReverseUseQuantity(TreeSet<Deal> deals, Operation operation) {
        List<Deal> forClosed = new ArrayList<>();
        long opQuantity = operation.quantityDone();
        while (opQuantity > 0) {
            if (deals.isEmpty()) {
                Deal newDeal = new Deal(operation, opQuantity, ZERO);
                deals.add(newDeal);
                forClosed.add(newDeal);
                return forClosed;
            }
            Deal last = deals.last();
            long lastQuantity = last.getQuantity();
            if (last.getQuantity() > opQuantity) {
                last.setQuantity(last.getQuantity() - opQuantity);
            } else {
                deals.remove(last);
                forClosed.add(last);
            }
            opQuantity = opQuantity - lastQuantity;
        }
        return forClosed;
    }

    public record AccountShareKey(String accountId, String instrumentUid) {
    }

}