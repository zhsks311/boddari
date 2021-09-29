package joyyir.boddari.domain.kimchi;

import joyyir.boddari.domain.exchange.OrderDetail;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TradeResult {
    private OrderDetail buyOrderDetail;
    private OrderDetail shortOrderDetail;
}
