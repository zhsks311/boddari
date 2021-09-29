package joyyir.boddari.domain.exchange;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class OrderDetail {
    private OrderStatus orderStatus;
    private BigDecimal orderQty;
    private BigDecimal averagePrice;
    private BigDecimal fee;
}
