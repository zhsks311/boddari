package joyyir.boddari.domain;

import java.math.BigDecimal;

public interface PriceRepository {
    BigDecimal getCurrentPrice(MarketType marketType);
}
