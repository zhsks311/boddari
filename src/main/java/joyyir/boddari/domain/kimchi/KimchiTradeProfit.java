package joyyir.boddari.domain.kimchi;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class KimchiTradeProfit {
    private BigDecimal profitAmount;
    private BigDecimal profitRate;
}
