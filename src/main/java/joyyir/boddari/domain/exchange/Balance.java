package joyyir.boddari.domain.exchange;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Balance {
    private String asset;
    private BigDecimal balance;
    private BigDecimal availableBalance;

    public Balance(String asset, BigDecimal balance, BigDecimal availableBalance) {
        this.asset = asset;
        this.balance = balance;
        this.availableBalance = availableBalance;
    }
}
