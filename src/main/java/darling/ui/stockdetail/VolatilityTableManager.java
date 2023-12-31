package darling.ui.stockdetail;

import darling.context.MarketContext;
import darling.domain.HistoricCandle;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ru.tinkoff.piapi.contract.v1.Share;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import static java.math.RoundingMode.HALF_UP;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;

public record VolatilityTableManager(TableView<List<String>> volatilityTableView, MarketContext marketContext) {

    public VolatilityTableManager {
        volatilityTableView.getColumns().clear();
        TableColumn<List<String>, String> tableColumn = new TableColumn<>("Волатильность, %");
        TableColumn<List<String>, String> tableColumn2 = new TableColumn<>("Вероятность получения");
        tableColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(0)));
        tableColumn2.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(1)));
        volatilityTableView.getColumns().add(tableColumn);
        volatilityTableView.getColumns().add(tableColumn2);
    }

    public void calculateVolatility(Share selectedItem) {
        if (selectedItem == null) {
            throw new IllegalArgumentException("Не выбрана акция для загрузки котировок");
        }

        LocalDate start = LocalDate.of(2023, Month.JUNE, 1);
        BigDecimal volatility01 = BigDecimal.ZERO;
        BigDecimal volatility02 = BigDecimal.ZERO;
        BigDecimal volatility03 = BigDecimal.ZERO;
        BigDecimal volatilityByDay;
        List<HistoricCandle> historicCandles = marketContext.getDailyCandles(selectedItem.getFigi(), start.atStartOfDay(), start.plusDays(120).atStartOfDay());
        for (HistoricCandle candle : historicCandles) {
            LocalDateTime currentDate = candle.time();

            if (currentDate.getDayOfWeek().equals(SATURDAY) || currentDate.getDayOfWeek().equals(SUNDAY)) {
                continue;
            }
            BigDecimal high = candle.high();
            BigDecimal low = candle.low();
            volatilityByDay = high.subtract(low).setScale(7, HALF_UP);
            BigDecimal price = historicCandles.get(0).open();
            volatilityByDay = volatilityByDay.divide(price, 7, HALF_UP).multiply(BigDecimal.valueOf(100));
            if (volatilityByDay.compareTo(BigDecimal.valueOf(1)) < 0) {
                volatility01 = volatility01.add(BigDecimal.ONE);
            } else if (volatilityByDay.compareTo(BigDecimal.valueOf(2)) < 0) {
                volatility02 = volatility02.add(BigDecimal.ONE);
            } else {
                volatility03 = volatility03.add(BigDecimal.ONE);
            }
        }

        ArrayList<String> value1 = new ArrayList<>(List.of("0-1", volatility01.toString()));
        ArrayList<String> value2 = new ArrayList<>(List.of("1-2", volatility02.toString()));
        ArrayList<String> value3 = new ArrayList<>(List.of("2-999", volatility03.toString()));
        List<List<String>> volatility = List.of(value1, value2, value3);
        volatilityTableView.setItems(FXCollections.observableArrayList(volatility));
    }
}
