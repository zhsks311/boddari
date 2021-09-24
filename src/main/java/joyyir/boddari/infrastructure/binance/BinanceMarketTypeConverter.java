package joyyir.boddari.infrastructure.binance;

import joyyir.boddari.domain.exchange.MarketType;

import java.util.Map;

public class BinanceMarketTypeConverter {
    private static final Map<MarketType, String> map = Map.ofEntries(
        Map.entry(MarketType.BTC_USDT, "BTCUSDT"),
        Map.entry(MarketType.ETH_USDT, "ETHUSDT"),
        Map.entry(MarketType.XRP_USDT, "XRPUSDT"),
        Map.entry(MarketType.ETC_USDT, "ETCUSDT")
    );

    public static String convert(MarketType marketType) {
        String marketTypeString = map.get(marketType);
        if (marketTypeString == null) {
            throw new UnsupportedOperationException("unsupported for marketType: " + marketType.name());
        }
        return marketTypeString;
    }
}
