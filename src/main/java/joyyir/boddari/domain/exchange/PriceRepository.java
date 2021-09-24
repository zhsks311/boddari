package joyyir.boddari.domain.exchange;

import java.math.BigDecimal;

public interface PriceRepository {
    BigDecimal getCurrentPrice(MarketType marketType);
}
