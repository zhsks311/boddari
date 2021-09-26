package joyyir.boddari.infrastructure.binance;

import joyyir.boddari.domain.exchange.MarketType;

public class BinanceMarketTypeConverter {
    public static String convert(MarketType marketType) {
        return marketType.name().replace("_", "");
    }
}
