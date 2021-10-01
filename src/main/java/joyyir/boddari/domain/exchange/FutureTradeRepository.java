package joyyir.boddari.domain.exchange;

import java.math.BigDecimal;

public interface FutureTradeRepository {
    String marketLong(MarketType marketType, BigDecimal quantity, String accessKey, String secretKey);

    String marketShort(MarketType marketType, BigDecimal quantity, String accessKey, String secretKey);

    String limitOrder(MarketType marketType, FuturePlaceType placeType, BigDecimal price, BigDecimal quantity, String accessKey, String secretKey);

    boolean changeInitialLeverage(MarketType marketType, int leverage, String accessKey, String secretKey);

    boolean changeMarginType(MarketType marketType, String marginType, String accessKey, String secretKey);
}
