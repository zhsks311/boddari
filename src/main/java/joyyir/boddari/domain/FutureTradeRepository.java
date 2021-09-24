package joyyir.boddari.domain;

import java.math.BigDecimal;

public interface FutureTradeRepository {
    String marketLong(MarketType marketType, BigDecimal quantity);

    String marketShort(MarketType marketType, BigDecimal quantity);

    String limitOrder(MarketType marketType, FuturePlaceType placeType, BigDecimal price, BigDecimal quantity);

    boolean changeInitialLeverage(MarketType marketType, int leverage);

    boolean changeMarginType(MarketType marketType, String marginType);
}
