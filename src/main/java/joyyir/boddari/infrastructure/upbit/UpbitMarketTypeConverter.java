package joyyir.boddari.infrastructure.upbit;

import joyyir.boddari.domain.MarketType;

import java.util.Map;

public class UpbitMarketTypeConverter {
    private static final Map<MarketType, String> map = Map.ofEntries(
        Map.entry(MarketType.BTC_KRW, "KRW-BTC"),
        Map.entry(MarketType.ETH_KRW, "KRW-ETH"),
        Map.entry(MarketType.XRP_KRW, "KRW-XRP"),

        Map.entry(MarketType.BTC_USDT, "USDT-BTC"),
        Map.entry(MarketType.ETH_USDT, "USDT-ETH"),
        Map.entry(MarketType.XRP_USDT, "USDT-XRP"),
        Map.entry(MarketType.ETC_USDT, "USDT-ETC")
    );

    public static String convert(MarketType marketType) {
        String marketTypeString = map.get(marketType);
        if (marketTypeString == null) {
            throw new UnsupportedOperationException("unsupported for marketType: " + marketType.name());
        }
        return marketTypeString;
    }
}
