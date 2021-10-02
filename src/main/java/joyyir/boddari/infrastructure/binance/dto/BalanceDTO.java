package joyyir.boddari.infrastructure.binance.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BalanceDTO {
    private String accountAlias;
    private String asset;
    private BigDecimal balance;
    private BigDecimal crossWalletBalance;
    private BigDecimal crossUnPnl;
    private BigDecimal availableBalance;
    private BigDecimal maxWithdrawAmount;
    private Boolean marginAvailable;
    private Long updateTime;
}
