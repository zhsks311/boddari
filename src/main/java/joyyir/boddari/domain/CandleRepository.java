package joyyir.boddari.domain;

import java.time.LocalDateTime;

public interface CandleRepository {
    DailyChange findDailyChange(MarketType marketType, LocalDateTime localDateTime);
}
