package joyyir.boddari.domain.exchange;

import java.time.LocalDateTime;

public interface CandleRepository {
    DailyChange findDailyChange(MarketType marketType, LocalDateTime localDateTime);
}
