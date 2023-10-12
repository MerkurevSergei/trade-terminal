package darling.trash;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import darling.domain.MarketCalculator;
import darling.trash.fromfiletrash.Quote;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class HistoryRead {

    private static final String CSV_DELIMITER = ";";

    private static final MarketCalculator CALC = new MarketCalculator(new BigDecimal("0.05"),
                                                                      new BigDecimal("0.05"),
                                                                      new BigDecimal("13"));

    public static void main(String[] args) {

        List<HistoryPoint> historyPoints = getHistoryPoints();
        List<PointTtx> ttxList = new ArrayList<>();
        for (int i = 0; i < historyPoints.size(); i++) {
            HistoryPoint point = historyPoints.get(i);
            PointTtx ttx = getPointTtx(historyPoints, point, i);
            ttxList.add(ttx);
        }
    }

    private static PointTtx getPointTtx(List<HistoryPoint> historyPoints, HistoryPoint currentPoint, int currentIndex) {
        BigDecimal profitPrice020 = CALC.grossSellPrice(currentPoint.price(), new BigDecimal("0.20"));
        BigDecimal profitPrice025 = CALC.grossSellPrice(currentPoint.price(), new BigDecimal("0.25"));
        BigDecimal profitPrice030 = CALC.grossSellPrice(currentPoint.price(), new BigDecimal("0.30"));
        BigDecimal profitPrice050 = CALC.grossSellPrice(currentPoint.price(), new BigDecimal("0.50"));
        BigDecimal profitPrice075 = CALC.grossSellPrice(currentPoint.price(), new BigDecimal("0.75"));
        BigDecimal profitPrice100 = CALC.grossSellPrice(currentPoint.price(), new BigDecimal("1"));
        BigDecimal profitPrice200 = CALC.grossSellPrice(currentPoint.price(), new BigDecimal("2"));

        Integer dist020 = Integer.MAX_VALUE;
        Integer dist025 = Integer.MAX_VALUE;
        Integer dist030 = Integer.MAX_VALUE;
        Integer dist050 = Integer.MAX_VALUE;
        Integer dist075 = Integer.MAX_VALUE;
        Integer dist100 = Integer.MAX_VALUE;
        Integer dist200 = Integer.MAX_VALUE;

        for (int i = currentIndex + 1; i < historyPoints.size(); i++) {
            HistoryPoint nextPoint = historyPoints.get(i);
            dist020 = evaluate(profitPrice020, dist020, i - currentIndex, nextPoint.price());
            dist025 = evaluate(profitPrice025, dist025, i - currentIndex, nextPoint.price());
            dist030 = evaluate(profitPrice030, dist030, i - currentIndex, nextPoint.price());
            dist050 = evaluate(profitPrice050, dist050, i - currentIndex, nextPoint.price());
            dist075 = evaluate(profitPrice075, dist075, i - currentIndex, nextPoint.price());
            dist100 = evaluate(profitPrice100, dist100, i - currentIndex, nextPoint.price());
            dist200 = evaluate(profitPrice200, dist200, i - currentIndex, nextPoint.price());
            if (!dist020.equals(Integer.MAX_VALUE) && !dist025.equals(Integer.MAX_VALUE) && !dist030.equals(Integer.MAX_VALUE)
                    && !dist050.equals(Integer.MAX_VALUE) && !dist075.equals(Integer.MAX_VALUE) && !dist100.equals(Integer.MAX_VALUE)
                    && !dist200.equals(Integer.MAX_VALUE)) {
                break;
            }
        }
        return new PointTtx(currentPoint.startTime(), currentPoint.price(), dist020, dist025, dist030, dist050, dist075,
                            dist100, dist200);
    }

    private static Integer evaluate(BigDecimal profitPrice, Integer dist, Integer currentDist, BigDecimal currentPrice) {
        if (dist.equals(Integer.MAX_VALUE) && currentPrice.compareTo(profitPrice) >= 0) {
            return currentDist;
        }
        return dist;
    }


    // ========================= ИСТОРИЯ КОТИРОВОК ========================= //

    private static List<HistoryPoint> getHistoryPoints() {
        List<File> sharesHistoryFiles = getShareHistoryFiles();
        List<String> content = getLines(sharesHistoryFiles);
        return parseToPoints(content);
    }

    private static List<HistoryPoint> parseToPoints(List<String> content) {
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        ArrayList<HistoryPoint> historyPoints = new ArrayList<>();
        for (String line : content) {
            String[] values = line.split(CSV_DELIMITER);
            LocalDateTime date = LocalDateTime.parse(values[1], dtf);
            BigDecimal open = new BigDecimal(values[2]);
            BigDecimal high = new BigDecimal(values[4]);
            BigDecimal low = new BigDecimal(values[5]);
            BigDecimal volume = new BigDecimal(values[6]);

            LocalDateTime timePoint0 = date.withSecond(0);
            LocalDateTime timePoint20 = date.withSecond(20);
            LocalDateTime timePoint40 = date.withSecond(40);

            HistoryPoint point0 = new HistoryPoint("BBG004730ZJ9", timePoint0, open, volume.divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_DOWN));
            HistoryPoint point20 = new HistoryPoint("BBG004730ZJ9", timePoint20, high, volume.divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_DOWN));
            HistoryPoint point40 = new HistoryPoint("BBG004730ZJ9", timePoint40, low, volume.divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_DOWN));
            historyPoints.add(point0);
            historyPoints.add(point20);
            historyPoints.add(point40);
        }
        return historyPoints.stream().sorted(Comparator.comparing(HistoryPoint::startTime)).toList();
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

    private static List<Quote> readSharesBars() {
        return List.of();
    }
}
