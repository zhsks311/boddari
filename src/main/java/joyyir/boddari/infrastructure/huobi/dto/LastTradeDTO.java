package joyyir.boddari.infrastructure.huobi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LastTradeDTO {
    @JsonProperty("tick")
    private TickDTO tick;
}
