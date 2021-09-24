package joyyir.boddari.infrastructure.binance.dto;

import lombok.Data;

@Data
public class ChangeInitialLeverageDTO {
    private Integer leverage;
    private String maxNotionalValue;
    private String symbol;
}
