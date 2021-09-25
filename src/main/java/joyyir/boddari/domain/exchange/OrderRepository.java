package joyyir.boddari.domain.exchange;

public interface OrderRepository {
    OrderDetail getOrderDetail(MarketType marketType, String orderId);
}
