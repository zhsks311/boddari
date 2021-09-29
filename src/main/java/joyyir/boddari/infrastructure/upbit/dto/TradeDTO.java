package joyyir.boddari.infrastructure.upbit.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TradeDTO {
    private String market;
    private String uuid;
    private BigDecimal price;
    private BigDecimal volume;
    private BigDecimal funds;
    private String side;
}
