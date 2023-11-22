package darling.trash.bouncer1;

import darling.context.BeanFactory;
import darling.service.HistoryService;
import darling.service.LastPriceService;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

public class App {

    private static final List<String> printList = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        BeanFactory beanFactory = new BeanFactory(true);
        HistoryService historyService = beanFactory.getHistoryService();
        LastPriceService lastPriceService = beanFactory.getLastPriceService();

        LocalDate currentDay = LocalDate.of(2023, Month.NOVEMBER, 14);
        LocalDate previousDay = LocalDate.of(2023, Month.NOVEMBER, 13);

        Bouncer0 vtb = new Bouncer0(historyService, lastPriceService, currentDay, previousDay,
                                    "8e2b0325-0292-4654-8a18-4f63ed3b0e09", "ВТБ");
        Bouncer0 fees = new Bouncer0(historyService, lastPriceService, currentDay, previousDay,
                                     "88e130e8-5b68-4b05-b9ae-baf32f5a3f21", "ФСК Россети (FEES)");
        Bouncer0 mtlr = new Bouncer0(historyService, lastPriceService, currentDay, previousDay,
                                     "eb4ba863-e85f-4f80-8c29-f2627938ee58", "Мечел (MTLR)");

        Bouncer0 irao = new Bouncer0(historyService, lastPriceService, currentDay, previousDay,
                                     "2dfbc1fd-b92a-436e-b011-928c79e805f2", "Интер РАО ЕЭС (IRAO)");

        Bouncer0 sgzh = new Bouncer0(historyService, lastPriceService, currentDay, previousDay,
                                     "7bedd86b-478d-4742-a28c-29d27f8dbc7d", "Сегежа (SGZH)");

        Bouncer0 sber = new Bouncer0(historyService, lastPriceService, currentDay, previousDay,
                                     "e6123145-9665-43e0-8413-cd61b8aa9b13", "Сбербанк (SBER)");

        Bouncer0 gazp = new Bouncer0(historyService, lastPriceService, currentDay, previousDay,
                                     "962e2a95-02a9-4171-abd7-aa198dbe643a", "Газпром (GAZP)");

        Bouncer0 moex = new Bouncer0(historyService, lastPriceService, currentDay, previousDay,
                                     "5e1c2634-afc4-4e50-ad6d-f78fc14a539a", "Московская Биржа (MOEX)");

        while (true) {
            printList.clear();
            toPrintList(vtb.step());
            toPrintList(fees.step());
            toPrintList(mtlr.step());
            toPrintList(irao.step());
            toPrintList(sgzh.step());
            toPrintList(sber.step());
            toPrintList(gazp.step());
            toPrintList(moex.step());
            print();
            Thread.sleep(10000);
        }
    }

    private static void toPrintList(String info) {
        if (!info.isBlank()) printList.add(info);
    }

    private static void print() {
        App.printList.forEach(System.out::println);
    }
}
