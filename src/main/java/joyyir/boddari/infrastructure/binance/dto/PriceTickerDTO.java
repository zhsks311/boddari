package joyyir.boddari.infrastructure.binance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceTickerDTO {
    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("price")
    private BigDecimal price;
}
