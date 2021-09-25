package joyyir.boddari.infrastructure.upbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import joyyir.boddari.domain.exchange.MarketType;
import joyyir.boddari.domain.exchange.OrderDetail;
import joyyir.boddari.domain.exchange.OrderRepository;
import joyyir.boddari.domain.exchange.OrderStatus;
import joyyir.boddari.infrastructure.upbit.dto.OrderDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@Repository
public class UpbitOrderRepository implements OrderRepository {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String accessKey;
    private final String secretKey;

    public UpbitOrderRepository(RestTemplate restTemplate,
                                ObjectMapper objectMapper,
                                @Value("${constant.upbit.access-key}") String accessKey,
                                @Value("${constant.upbit.secret-key}") String secretKey) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    @Override
    public OrderDetail getOrderDetail(MarketType marketType, String orderId) {
        final String endpoint = "https://api.upbit.com/v1/order";

        Map<String, String> params = new LinkedHashMap<>();
        params.put("uuid", orderId);

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", UpbitUtil.getAuthenticationToken(params, accessKey, secretKey));

        String queryString = UpbitUtil.toQueryString(params);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<OrderDTO> response = restTemplate.exchange(endpoint + "?" + queryString, HttpMethod.GET, entity, OrderDTO.class);
        OrderDTO order = response.getBody();
        if (order == null) {
            throw new RuntimeException("response: " + response.toString());
        }
        OrderStatus orderStatus = order.getState().equalsIgnoreCase("done")
            || (order.getState().equalsIgnoreCase("cancel") && order.getLocked().doubleValue() < 0.0001)
            ? OrderStatus.COMPLETED : OrderStatus.UNKNOWN;
        return new OrderDetail(orderStatus, order.getExecutedVolume());
    }
}
