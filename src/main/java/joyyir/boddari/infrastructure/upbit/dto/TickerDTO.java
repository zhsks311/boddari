package joyyir.boddari.infrastructure.upbit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TickerDTO {
    @JsonProperty("trade_price")
    private BigDecimal tradePrice;
}
