package joyyir.boddari.infrastructure.binance.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FutureOrderDTO {
    private String clientOrderId;
    private BigDecimal cumQty;
    private BigDecimal cumQuote;
    private BigDecimal executedQty;
    private String orderId;
    private BigDecimal avgPrice;
    private BigDecimal origQty;
    private BigDecimal price;
    private Boolean reduceOnly;
    private String side;
    private String positionSide;
    private String status;
    private BigDecimal stopPrice;
    private Boolean closePosition;
    private String symbol;
    private String timeInForce;
    private String type;
    private String origType;
    private BigDecimal activatePrice;
    private BigDecimal priceRate;
    private Long updateTime;
    private String workingType;
    private Boolean priceProtect;
}
