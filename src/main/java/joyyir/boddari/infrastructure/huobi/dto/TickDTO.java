package joyyir.boddari.infrastructure.huobi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TickDTO {
    @JsonProperty("data")
    private List<TickDataDTO> data;
}
