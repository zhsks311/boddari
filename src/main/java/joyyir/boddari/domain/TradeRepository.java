package joyyir.boddari.domain;

import java.math.BigDecimal;

public interface TradeRepository {
    String marketBuy(MarketType marketType, BigDecimal price);

    String marketSell(MarketType marketType, BigDecimal volume);

    String limitOrder(MarketType marketType, PlaceType placeType, BigDecimal price, BigDecimal volume);
}
