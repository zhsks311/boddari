package joyyir.boddari.infrastructure.binance;

import joyyir.boddari.domain.exchange.MarketType;
import joyyir.boddari.domain.exchange.OrderDetail;
import joyyir.boddari.domain.exchange.OrderRepository;
import joyyir.boddari.domain.exchange.OrderStatus;
import joyyir.boddari.infrastructure.binance.dto.FutureOrderDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Repository
public class BinanceFutureOrderRepository implements OrderRepository {
    private final RestTemplate restTemplate;

    public BinanceFutureOrderRepository(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public OrderDetail getOrderDetail(MarketType marketType, String orderId, String accessKey, String secretKey) {
        final String endpoint = "https://fapi.binance.com/fapi/v1/order";

        Map<String, String> params = new LinkedHashMap<>();
        params.put("symbol", BinanceMarketTypeConverter.convert(marketType));
        params.put("orderId", orderId);
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("signature", BinanceUtil.getSignature(params, secretKey));

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        headers.add("X-MBX-APIKEY", accessKey);

        String queryString = BinanceUtil.toQueryString(params);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<FutureOrderDTO> response = restTemplate.exchange(endpoint + "?" + queryString, HttpMethod.GET, entity, FutureOrderDTO.class);
        FutureOrderDTO order = response.getBody();
        if (order == null) {
            throw new RuntimeException("response: " + response.toString());
        }
        OrderStatus orderStatus = order.getStatus().equals("FILLED") ? OrderStatus.COMPLETED : OrderStatus.UNKNOWN;
        if (orderStatus != OrderStatus.COMPLETED) {
            return new OrderDetail(orderStatus, null, null, null);
        }
        return new OrderDetail(orderStatus,
                               order.getExecutedQty(),
                               order.getAvgPrice(),
                               order.getExecutedQty()
                                    .multiply(order.getAvgPrice())
                                    .multiply(new BigDecimal("0.0004"))); // VIP 0 레벨에서 Taker의 수수료는 0.04%
    }
}
