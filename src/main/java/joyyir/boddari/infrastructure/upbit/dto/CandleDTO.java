package joyyir.boddari.infrastructure.upbit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CandleDTO {
    @JsonProperty("market")
    private String market;

    @JsonProperty("candle_date_time_utc")
    private String candleDateTimeUtc;

    @JsonProperty("candle_date_time_kst")
    private String candleDateTimeKst;

    @JsonProperty("opening_price")
    private BigDecimal openingPrice;

    @JsonProperty("high_price")
    private BigDecimal highPrice;

    @JsonProperty("low_price")
    private BigDecimal lowPrice;

    @JsonProperty("trade_price")
    private BigDecimal tradePrice;

    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("candle_acc_trade_price")
    private BigDecimal candleAccTradePrice;

    @JsonProperty("candle_acc_trade_volume")
    private BigDecimal candleAccTradeVolume;

    @JsonProperty("prev_closing_price")
    private BigDecimal prevClosingPrice;

    @JsonProperty("change_price")
    private BigDecimal changePrice;

    @JsonProperty("change_rate")
    private BigDecimal changeRate;
}
