package stock;

import stock.model.Quote;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App {

    private static final String COMMA_DELIMITER = ",";

    public static void main(String[] args) throws IOException {
        List<Quote> quotesAll = loadData("c:\\temp\\files\\file.csv");
        List<Quote> quotes500 = quotesAll.subList(quotesAll.size() - 501, quotesAll.size() - 1);
        List<Quote> quotes250 = quotesAll.subList(quotesAll.size() - 251, quotesAll.size() - 1);

        Map<LocalDate, Long> percentProfitAll = daysToProfit(quotesAll, 1);
        Map<LocalDate, Long> percentProfit500 = daysToProfit(quotes500, 1);
        Map<LocalDate, Long> percentProfit250 = daysToProfit(quotes250, 1);

        Map<LocalDate, Long> percent2ProfitAll = daysToProfit(quotesAll, 2);
        Map<LocalDate, Long> percent2Profit500 = daysToProfit(quotes500, 2);
        Map<LocalDate, Long> percent2Profit250 = daysToProfit(quotes250, 2);

        Map<LocalDate, Long> percent3ProfitAll = daysToProfit(quotesAll, 3);
        Map<LocalDate, Long> percent3Profit500 = daysToProfit(quotes500, 3);
        Map<LocalDate, Long> percent3Profit250 = daysToProfit(quotes250, 3);

        Map<LocalDate, Long> percent4ProfitAll = daysToProfit(quotesAll, 4);
        Map<LocalDate, Long> percent4Profit500 = daysToProfit(quotes500, 4);
        Map<LocalDate, Long> percent4Profit250 = daysToProfit(quotes250, 4);

        Map<LocalDate, Long> percent5ProfitAll = daysToProfit(quotesAll, 5);
        Map<LocalDate, Long> percent5Profit500 = daysToProfit(quotes500, 5);
        Map<LocalDate, Long> percent5Profit250 = daysToProfit(quotes250, 5);

        Map<LocalDate, Long> percent7ProfitAll = daysToProfit(quotesAll, 7);
        Map<LocalDate, Long> percent7Profit500 = daysToProfit(quotes500, 7);
        Map<LocalDate, Long> percent7Profit250 = daysToProfit(quotes250, 7);

        Map<LocalDate, Long> percent10ProfitAll = daysToProfit(quotesAll, 10);
        Map<LocalDate, Long> percent10Profit500 = daysToProfit(quotes500, 10);
        Map<LocalDate, Long> percent10Profit250 = daysToProfit(quotes250, 10);

        logChanceByDay(percentProfitAll, percentProfit500, percentProfit250, "1");
        logChanceByDay(percent2ProfitAll, percent2Profit500, percent2Profit250, "2");
        logChanceByDay(percent3ProfitAll, percent3Profit500, percent3Profit250, "3");
        logChanceByDay(percent4ProfitAll, percent4Profit500, percent4Profit250, "4");
        logChanceByDay(percent5ProfitAll, percent5Profit500, percent5Profit250, "5");
        logChanceByDay(percent7ProfitAll, percent7Profit500, percent7Profit250, "7");
        logChanceByDay(percent10ProfitAll, percent10Profit500, percent10Profit250, "10");
    }

    private static void logChanceByDay(Map<LocalDate, Long> percentProfitAll, Map<LocalDate, Long> percentProfit500, Map<LocalDate, Long> percentProfit250, String profitInPercent) {
        System.out.println(" 250 | 500  | all  | " + profitInPercent + "%:");
        System.out.println("-----|------|------|----");
        System.out.print(getChanceByDay(percentProfit250, 1) + "| ");
        System.out.print(getChanceByDay(percentProfit500, 1) + "| ");
        System.out.println(getChanceByDay(percentProfitAll, 1) + "| +1d");

        System.out.print(getChanceByDay(percentProfit250, 2) + "| ");
        System.out.print(getChanceByDay(percentProfit500, 2) + "| ");
        System.out.println(getChanceByDay(percentProfitAll, 2) + "| +2d");

        System.out.print(getChanceByDay(percentProfit250, 3) + "| ");
        System.out.print(getChanceByDay(percentProfit500, 3) + "| ");
        System.out.println(getChanceByDay(percentProfitAll, 3) + "| +3d");

        System.out.print(getChanceByDay(percentProfit250, 5) + "| ");
        System.out.print(getChanceByDay(percentProfit500, 5) + "| ");
        System.out.println(getChanceByDay(percentProfitAll, 5) + "| +5d");

        System.out.print(getChanceByDay(percentProfit250, 10) + "| ");
        System.out.print(getChanceByDay(percentProfit500, 10) + "| ");
        System.out.println(getChanceByDay(percentProfitAll, 10) + "| +10d");

        System.out.print(getChanceByDay(percentProfit250, 21) + "| ");
        System.out.print(getChanceByDay(percentProfit500, 21) + "| ");
        System.out.println(getChanceByDay(percentProfitAll, 21) + "| +21d");

        System.out.print(getChanceByDay(percentProfit250, 63) + "| ");
        System.out.print(getChanceByDay(percentProfit500, 63) + "| ");
        System.out.println(getChanceByDay(percentProfitAll, 63) + "| +63d");

        System.out.print(getChanceByDay(percentProfit250, 126) + "| ");
        System.out.print(getChanceByDay(percentProfit500, 126) + "| ");
        System.out.println(getChanceByDay(percentProfitAll, 126) + "| +126d");
        System.out.println("==============================================");
        System.out.println();
    }

    private static BigDecimal getChanceByDay(Map<LocalDate, Long> percentProfit, long conditionForDays) {
        long daysInCondition = 0;
        for (Long daysToProfit : percentProfit.values()) {
            if (daysToProfit <= conditionForDays) {
                daysInCondition++;
            }
        }
        BigDecimal fullSize = BigDecimal.valueOf(percentProfit.size());
        return BigDecimal.valueOf(daysInCondition).divide(fullSize, 2,  RoundingMode.HALF_DOWN).multiply(BigDecimal.valueOf(100));
    }

    private static Map<LocalDate, Long> daysToProfit(List<Quote> quotes, int profitInPercent) {
        Map<LocalDate, Long> profit = new HashMap<>();
        for (int i = 0; i < quotes.size(); i++) {
            Quote quote = quotes.get(i);
            Long days = collectPercentProfit(quote, i, quotes, profitInPercent);
            profit.put(quote.date(), days);
        }
        return profit;
    }

    private static Long collectPercentProfit(Quote quote, int quoteIndex, List<Quote> quotes, int profitInPercent) {
        BigDecimal startPrice = quote.open().add(quote.close()).divide(BigDecimal.valueOf(2), RoundingMode.HALF_DOWN);
        int length = Math.min(quoteIndex + 1000, quotes.size());
        for (int i = quoteIndex + 1; i < length; i++) {
            Quote futureQuote = quotes.get(i);
            BigDecimal futurePrice = futureQuote.close().max(futureQuote.open());
            BigDecimal up = futurePrice.subtract(startPrice).divide(startPrice, RoundingMode.HALF_DOWN).multiply(BigDecimal.valueOf(100));
            if (up.compareTo(new BigDecimal(profitInPercent)) > 0) {
                return (long) (i - quoteIndex);
            }
        }
        return Long.MAX_VALUE;
    }

    private static List<Quote> loadData(String file) throws IOException {
        List<Quote> quotes = new ArrayList<>();
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
        boolean firstLine = true;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] values = line.split(COMMA_DELIMITER);
                LocalDate date = LocalDate.parse(values[2], dtf);
                BigDecimal open = new BigDecimal(values[4]);
                BigDecimal high = new BigDecimal(values[5]);
                BigDecimal low = new BigDecimal(values[6]);
                BigDecimal close = new BigDecimal(values[7]);
                BigDecimal volume = new BigDecimal(values[8]);
                Quote quote = new Quote(date, low, high, open, close, volume);
                quotes.add(quote);
            }
        }
        return quotes;
    }
}
