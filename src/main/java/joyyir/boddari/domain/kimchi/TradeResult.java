package joyyir.boddari.domain.kimchi;

import joyyir.boddari.domain.exchange.OrderDetail;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@AllArgsConstructor
public class TradeResult {
    private OrderDetail buyOrderDetail;
    private OrderDetail shortOrderDetail;
    private BigDecimal usdPriceKrw;

    public BigDecimal getKimchiPremium() {
        return buyOrderDetail.getAveragePrice()
                             .divide(shortOrderDetail.getAveragePrice()
                                                     .multiply(usdPriceKrw), 8, RoundingMode.HALF_UP)
                             .subtract(new BigDecimal(1))
                             .multiply(new BigDecimal(100))
                             .setScale(2, RoundingMode.HALF_UP);
    }
}
