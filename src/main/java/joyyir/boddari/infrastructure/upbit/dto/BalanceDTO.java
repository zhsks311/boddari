package joyyir.boddari.infrastructure.upbit.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BalanceDTO {
    private String currency;
    private BigDecimal balance;
    private BigDecimal locked;
    private BigDecimal avgBuyPrice;
    private Boolean avgBuyPriceModified;
    private String unitCurrency;
}
