package joyyir.boddari.infrastructure.upbit;

import joyyir.boddari.domain.exchange.MarketType;

public class UpbitMarketTypeConverter {
    public static String convert(MarketType marketType) {
        String[] s = marketType.name()
                               .split("_");
        return s[1] + "-" + s[0];
    }
}
