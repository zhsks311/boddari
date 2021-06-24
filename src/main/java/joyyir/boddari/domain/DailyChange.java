package joyyir.boddari.domain;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class DailyChange {
    private LocalDateTime localDateTime;
    private MarketType marketType;
    private BigDecimal dailyPriceChangeRate;
    private BigDecimal dailyVolumeChangeRate;

    public DailyChange(LocalDateTime localDateTime, MarketType marketType, BigDecimal dailyPriceChangeRate, BigDecimal dailyVolumeChangeRate) {
        this.localDateTime = localDateTime;
        this.marketType = marketType;
        this.dailyPriceChangeRate = dailyPriceChangeRate;
        this.dailyVolumeChangeRate = dailyVolumeChangeRate;
    }
}
