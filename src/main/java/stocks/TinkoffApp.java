package stocks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tinkoff.piapi.contract.v1.InstrumentStatus;
import ru.tinkoff.piapi.contract.v1.MarketDataResponse;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.contract.v1.SubscriptionStatus;
import ru.tinkoff.piapi.core.InvestApi;
import ru.tinkoff.piapi.core.stream.StreamProcessor;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TinkoffApp {

    static final Logger log = LoggerFactory.getLogger(TinkoffApp.class);


    static final Executor delayedExecutor = CompletableFuture.delayedExecutor(225, TimeUnit.SECONDS);



    public static void main(String[] args) {
        var token = "t._qVKudqLJNPnr9lSaPG93sbVMPyqAf4D985DzrNawqqB764SJwmGTRtLlxsWU3QPVKRWrfhE0CmZjyjJe8olEQ";
        var api = InvestApi.create(token);
        marketdataStreamExample(api);
        CompletableFuture.runAsync(()->{log.info("starting shutdown");}, delayedExecutor)
                .thenAcceptAsync(__ -> api.destroy(3), delayedExecutor)
                .join();
    }

    private static void marketdataStreamExample(InvestApi api) {
        var randomFigi = randomFigi(api, 300);
        System.out.println("Init Market");
        System.out.println("size: " + randomFigi.size());
        //Описываем, что делать с приходящими в стриме данными
        StreamProcessor<MarketDataResponse> processor = response -> {
            if (response.hasTradingStatus()) {
                log.info("Новые данные по статусам: {}", response);
            } else if (response.hasPing()) {
                log.info("пинг сообщение");
            } else if (response.hasCandle()) {
                log.info("Новые данные по свечам: {}", response);
            } else if (response.hasOrderbook()) {
                log.info("Новые данные по стакану: {}", response);
            } else if (response.hasTrade()) {
                log.info("Новые данные по сделкам: {}", response);
            } else if (response.hasSubscribeCandlesResponse()) {
                var subscribeResult = response.getSubscribeCandlesResponse().getCandlesSubscriptionsList().stream()
                        .collect(Collectors.groupingBy(el -> el.getSubscriptionStatus().equals(SubscriptionStatus.SUBSCRIPTION_STATUS_SUCCESS), Collectors.counting()));
                logSubscribeStatus("свечи", subscribeResult.getOrDefault(true, 0L), subscribeResult.getOrDefault(false, 0L));
            } else if (response.hasSubscribeInfoResponse()) {
                var subscribeResult = response.getSubscribeInfoResponse().getInfoSubscriptionsList().stream()
                        .collect(Collectors.groupingBy(el -> el.getSubscriptionStatus().equals(SubscriptionStatus.SUBSCRIPTION_STATUS_SUCCESS), Collectors.counting()));
                logSubscribeStatus("статусы", subscribeResult.getOrDefault(true, 0L), subscribeResult.getOrDefault(false, 0L));
            } else if (response.hasSubscribeOrderBookResponse()) {
                var subscribeResult = response.getSubscribeOrderBookResponse().getOrderBookSubscriptionsList().stream()
                        .collect(Collectors.groupingBy(el -> el.getSubscriptionStatus().equals(SubscriptionStatus.SUBSCRIPTION_STATUS_SUCCESS), Collectors.counting()));
                logSubscribeStatus("стакан", subscribeResult.getOrDefault(true, 0L), subscribeResult.getOrDefault(false, 0L));
            } else if (response.hasSubscribeTradesResponse()) {
                var subscribeResult = response.getSubscribeTradesResponse().getTradeSubscriptionsList().stream()
                        .collect(Collectors.groupingBy(el -> el.getSubscriptionStatus().equals(SubscriptionStatus.SUBSCRIPTION_STATUS_SUCCESS), Collectors.counting()));
                logSubscribeStatus("сделки", subscribeResult.getOrDefault(true, 0L), subscribeResult.getOrDefault(false, 0L));
            } else if (response.hasSubscribeLastPriceResponse()) {
                var subscribeResult = response.getSubscribeLastPriceResponse().getLastPriceSubscriptionsList().stream()
                        .collect(Collectors.groupingBy(el -> el.getSubscriptionStatus().equals(SubscriptionStatus.SUBSCRIPTION_STATUS_SUCCESS), Collectors.counting()));
                logSubscribeStatus("последние цены", subscribeResult.getOrDefault(true, 0L), subscribeResult.getOrDefault(false, 0L));
            }
        };
        Consumer<Throwable> onErrorCallback = error -> log.error(error.toString());

        //Подписка на список инструментов. Не блокирующий вызов
        //При необходимости обработки ошибок (реконнект по вине сервера или клиента), рекомендуется сделать onErrorCallback
//        api.getMarketDataStreamService().newStream("trades_stream", processor, onErrorCallback).subscribeTrades(randomFigi);
        api.getMarketDataStreamService().newStream("candles_stream", processor, onErrorCallback).subscribeCandles(randomFigi);
//        api.getMarketDataStreamService().newStream("info_stream", processor, onErrorCallback).subscribeInfo(randomFigi);
//        api.getMarketDataStreamService().newStream("orderbook_stream", processor, onErrorCallback).subscribeOrderbook(randomFigi);
//        api.getMarketDataStreamService().newStream("last_prices_stream", processor, onErrorCallback).subscribeLastPrices(randomFigi);


        //Для стримов стаканов и свечей есть перегруженные методы с дефолтными значениями
        //глубина стакана = 10, интервал свечи = 1 минута
//        api.getMarketDataStreamService().getStreamById("trades_stream").subscribeOrderbook(randomFigi);
//        api.getMarketDataStreamService().getStreamById("candles_stream").subscribeCandles(randomFigi);
//        api.getMarketDataStreamService().getStreamById("candles_stream").cancel();
//        //отписываемся от стримов с задержкой
//        CompletableFuture.runAsync(()->{
//
//                    //Отписка на список инструментов. Не блокирующий вызов
//                    api.getMarketDataStreamService().getStreamById("trades_stream").unsubscribeTrades(randomFigi);
//                    api.getMarketDataStreamService().getStreamById("candles_stream").unsubscribeCandles(randomFigi);
//                    api.getMarketDataStreamService().getStreamById("info_stream").unsubscribeInfo(randomFigi);
//                    api.getMarketDataStreamService().getStreamById("orderbook_stream").unsubscribeOrderbook(randomFigi);
//                    api.getMarketDataStreamService().getStreamById("last_prices_stream").unsubscribeLastPrices(randomFigi);
//
//                    //закрытие стрима
//                    api.getMarketDataStreamService().getStreamById("candles_stream").cancel();
//
//                }, delayedExecutor)
//                .thenRun(()->log.info("market data unsubscribe done"));


        //Каждый marketdata стрим может отдавать информацию максимум по 300 инструментам
        //Если нужно подписаться на большее количество, есть 2 варианта:
        // - открыть новый стрим
//        api.getMarketDataStreamService().newStream("new_stream", processor, onErrorCallback).subscribeCandles(randomFigi);
//        // - отписаться от инструментов в существующем стриме, освободив место под новые
//        api.getMarketDataStreamService().getStreamById("new_stream").unsubscribeCandles(randomFigi);

//        //При вызове newStream с id уже подписаного приведет к пересозданию стрима с версии 1.4
//        api.getMarketDataStreamService().newStream("candles_stream", processor, onErrorCallback)
//                .subscribeCandles(randomFigi);
    }

    private static void logSubscribeStatus(String eventType, Long successed, Long failed) {
        log.info("удачных подписок на {}: {}. неудачных подписок на {}: {}.", eventType, successed, eventType, failed);
    }

    private static List<String> randomFigi(InvestApi api, int count) {
        return api.getInstrumentsService().getSharesSync(InstrumentStatus.INSTRUMENT_STATUS_BASE)
                .stream()
                .filter(el -> RealExchange.REAL_EXCHANGE_MOEX.equals(el.getRealExchange()))
                .map(Share::getFigi)
                .limit(count)
                .collect(Collectors.toList());
    }
}
