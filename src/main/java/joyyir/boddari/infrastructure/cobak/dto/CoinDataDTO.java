package joyyir.boddari.infrastructure.cobak.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoinDataDTO {
    @JsonProperty("price_krw")
    private BigDecimal priceKrw;
}
