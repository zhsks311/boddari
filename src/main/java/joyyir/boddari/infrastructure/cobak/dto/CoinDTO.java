package joyyir.boddari.infrastructure.cobak.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CoinDTO {
    @JsonProperty("data")
    private CoinDataDTO data;
}
