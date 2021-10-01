package joyyir.boddari.domain.exchange;

import java.math.BigDecimal;

public interface TradeRepository {
    String marketBuy(MarketType marketType, BigDecimal price, String accessKey, String secretKey);

    String marketSell(MarketType marketType, BigDecimal volume, String accessKey, String secretKey);

    String limitOrder(MarketType marketType, PlaceType placeType, BigDecimal price, BigDecimal volume, String accessKey, String secretKey);
}
