package stocks.trash.fromfiletrash;

import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class HistoryRead {

    private static final String CSV_DELIMITER = ";";

    public static void main(String[] args) {


//        BetBook betBook = new BetBook();
        Map<LocalDate, BigDecimal> profitByDay = new HashMap<>();
        List<HistoryCandle> historyCandles = getHistoryCandles();
        for (int i = 0; i < historyCandles.size(); i++) {
            HistoryCandle candle = historyCandles.get(i);

        }
    }

    private static void trade( HistoryCandle candle) {
        // Ставим ставку, если еще нет
//        if (betBook.isEmpty()) {
//            betBook.add(new Point(candle.startTime(), candle.open()), Bet.Direction.UP);
//            return;
//        }
//
//        // Получаем разницу цены с последней ставкой
//        Bet lastIsActive = betBook.getLastIsActive();
//        Bet.Direction direction = lastIsActive.getDirection();
//        BigDecimal currentPrice = candle.open();
//        BigDecimal income;
//        if (direction.equals(Bet.Direction.UP)) {
//            income = currentPrice.subtract(lastIsActive.getPoint().price());
//        } else {
//            income = lastIsActive.getPoint().price().subtract(currentPrice);
//        }
//
//        // Проверим границу прибыли/убытка и выполним алгоритмы
//        if (income.compareTo(new BigDecimal("0.7")) > 0) {
//            lastIsActive.setActive(false);
//            lastIsActive.setTakeProfit(lastIsActive.getPoint().price().add(new BigDecimal("0.7")));
//            // new Order add to book on price
//        } else if (income.compareTo(new BigDecimal("-0.7")) < 0) {
//            // new Order add to book against
//        }
//
//        // Проверим стопы и зафиксируем результат
//
//        // Если день кончился, закрываем книгу

    }

    private static List<Point> evaluateProfit(HistoryCandle currentCandle, List<HistoryCandle> historyCandles) {
        LocalDateTime timePoint0 = currentCandle.startTime().withSecond(0);
        LocalDateTime timePoint20 = currentCandle.startTime().withSecond(20);
        LocalDateTime timePoint40 = currentCandle.startTime().withSecond(40);
        LocalDateTime timePoint59 = currentCandle.startTime().withSecond(59);
        return List.of();
    }


    // ========================= ИСТОРИЯ КОТИРОВОК ========================= //

    private static List<HistoryCandle> getHistoryCandles() {
        List<File> sharesHistoryFiles = getShareHistoryFiles();
        List<String> content = getLines(sharesHistoryFiles);
        return parseToCandles(content);
    }

    private static List<HistoryCandle> parseToCandles(List<String> content) {
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        ArrayList<HistoryCandle> candles = new ArrayList<>();
        for (String line : content) {
            String[] values = line.split(CSV_DELIMITER);
            LocalDateTime date = LocalDateTime.parse(values[1], dtf);
            BigDecimal open = new BigDecimal(values[2]);
            BigDecimal close = new BigDecimal(values[3]);
            BigDecimal high = new BigDecimal(values[4]);
            BigDecimal low = new BigDecimal(values[5]);
            BigDecimal volume = new BigDecimal(values[6]);
            HistoryCandle candle = new HistoryCandle("BBG004730ZJ9", date, open, close, high, low, volume);
            candles.add(candle);
        }
        return candles.stream().sorted(Comparator.comparing(HistoryCandle::startTime)).toList();
    }

    private static List<String> getLines(List<File> sharesHistoryFiles) {
        List<String> lines = new ArrayList<>();
        for (File file : sharesHistoryFiles) {
            try (ZipFile zipFile = new ZipFile(file)) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (!entry.isDirectory()) {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8))) {
                            lines.addAll(reader.lines().toList());
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
        return lines;
    }

    private static List<File> getShareHistoryFiles() {
        File dir = new File("C:\\Temp\\stocks_history");
        FileFilter fileFilter = WildcardFileFilter.builder().setWildcards("BBG004730ZJ9_*.zip").get();
        File[] files = dir.listFiles(fileFilter);
        return files == null ? List.of() : Arrays.asList(files);
    }
}
