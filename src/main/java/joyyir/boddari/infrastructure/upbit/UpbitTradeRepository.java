package joyyir.boddari.infrastructure.upbit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import joyyir.boddari.domain.exchange.MarketType;
import joyyir.boddari.domain.exchange.PlaceType;
import joyyir.boddari.domain.exchange.TradeRepository;
import joyyir.boddari.infrastructure.upbit.dto.OrderDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Repository
public class UpbitTradeRepository implements TradeRepository {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public UpbitTradeRepository(RestTemplate restTemplate,
                                ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * @param marketType 거래할 마켓
     * @param price      시장가 매수에 사용할 금액
     * @return           주문의 고유 아이디
     */
    @Override
    public String marketBuy(MarketType marketType, BigDecimal price, String accessKey, String secretKey) {
        return order(marketType, PlaceType.BUY, price, null, "price", accessKey, secretKey);
    }

    /**
     * @param marketType 거래할 마켓
     * @param volume     사장가 매도할 코인 개수
     * @return           주문의 고유 아이디
     */
    @Override
    public String marketSell(MarketType marketType, BigDecimal volume, String accessKey, String secretKey) {
        return order(marketType, PlaceType.SELL, null, volume, "market", accessKey, secretKey);
    }

    @Override
    public String limitOrder(MarketType marketType, PlaceType placeType, BigDecimal price, BigDecimal volume, String accessKey, String secretKey) {
        return order(marketType, placeType, price, volume, "limit", accessKey, secretKey);
    }

    private String order(MarketType marketType, PlaceType placeType, BigDecimal price, BigDecimal volume, String orderType, String accessKey, String secretKey) {
        final String endpoint = "https://api.upbit.com/v1/orders";

        Map<String, String> params = new LinkedHashMap<>();
        params.put("market", UpbitMarketTypeConverter.convert(marketType));
        params.put("side", PlaceType.SELL == placeType ? "ask" : "bid");
        if (volume != null) {
            params.put("volume", volume.toString());
        }
        if (price != null) {
            params.put("price", price.toString());
        }
        params.put("ord_type", orderType);

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", UpbitUtil.getAuthenticationToken(params, accessKey, secretKey));

        String paramJson;
        try {
            paramJson = objectMapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        HttpEntity<String> entity = new HttpEntity<>(paramJson, headers);
        ResponseEntity<OrderDTO> response = restTemplate.postForEntity(endpoint, entity, OrderDTO.class);
        if (response.getBody() == null) {
            throw new RuntimeException("response: " + response.toString());
        }
        return response.getBody().getUuid();
    }
}
