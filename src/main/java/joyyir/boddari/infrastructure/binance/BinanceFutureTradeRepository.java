package joyyir.boddari.infrastructure.binance;

import com.fasterxml.jackson.databind.ObjectMapper;
import joyyir.boddari.domain.FuturePlaceType;
import joyyir.boddari.domain.FutureTradeRepository;
import joyyir.boddari.domain.MarketType;
import joyyir.boddari.infrastructure.binance.dto.ChangeInitialLeverageDTO;
import joyyir.boddari.infrastructure.binance.dto.ChangeMarginTypeDTO;
import joyyir.boddari.infrastructure.binance.dto.FutureOrderDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Repository
public class BinanceFutureTradeRepository implements FutureTradeRepository {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String accessKey;
    private final String secretKey;

    public BinanceFutureTradeRepository(RestTemplate restTemplate,
                                        ObjectMapper objectMapper,
                                        @Value("${constant.binance.access-key}") String accessKey,
                                        @Value("${constant.binance.secret-key}") String secretKey) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    public boolean changeInitialLeverage(MarketType marketType, int leverage) {
        final String endpoint = "https://fapi.binance.com/fapi/v1/leverage";

        Map<String, String> params = new LinkedHashMap<>();
        params.put("symbol", BinanceMarketTypeConverter.convert(marketType));
        params.put("leverage", String.valueOf(leverage));
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("signature", BinanceUtil.getSignature(params, secretKey));

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        headers.add("X-MBX-APIKEY", accessKey);

        String queryString = BinanceUtil.toQueryString(params);
        HttpEntity<String> entity = new HttpEntity<>(queryString, headers);
        ResponseEntity<ChangeInitialLeverageDTO> response = restTemplate.postForEntity(endpoint, entity, ChangeInitialLeverageDTO.class);
        if (response.getBody() == null) {
            throw new RuntimeException("response: " + response.toString());
        }
        return response.getBody().getLeverage() == leverage;
    }

    public boolean changeMarginType(MarketType marketType, String marginType) {
        final String endpoint = "https://fapi.binance.com/fapi/v1/marginType";

        Map<String, String> params = new LinkedHashMap<>();
        params.put("symbol", BinanceMarketTypeConverter.convert(marketType));
        params.put("marginType", marginType);
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("signature", BinanceUtil.getSignature(params, secretKey));

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        headers.add("X-MBX-APIKEY", accessKey);

        String queryString = BinanceUtil.toQueryString(params);
        HttpEntity<String> entity = new HttpEntity<>(queryString, headers);
        ResponseEntity<ChangeMarginTypeDTO> response = null;
        try {
            response = restTemplate.postForEntity(endpoint, entity, ChangeMarginTypeDTO.class);
        } catch (HttpClientErrorException.BadRequest e) {
            if (e.getResponseBodyAsString().contains("-4046")) { // "No need to change margin type."
                return true;
            }
            throw e;
        }
        if (response.getBody() == null) {
            throw new RuntimeException("response: " + response.toString());
        }
        return response.getBody().getCode() == 200;
    }

    public String order(MarketType marketType, FuturePlaceType placeType, BigDecimal price, BigDecimal quantity, String orderType) {
        final String endpoint = "https://fapi.binance.com/fapi/v1/order";

        Map<String, String> params = new LinkedHashMap<>();
        params.put("symbol", BinanceMarketTypeConverter.convert(marketType));
        params.put("side", FuturePlaceType.SHORT == placeType ? "SELL" : "BUY");
        params.put("type", orderType);
        if ("LIMIT".equals(orderType)) {
            params.put("timeInForce", "GTC"); // GTC - Good Till Cancel
        }
        if (quantity != null) {
            params.put("quantity", quantity.toString());
        }
        if (price != null) {
            params.put("price", price.toString());
        }
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("signature", BinanceUtil.getSignature(params, secretKey));

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        headers.add("X-MBX-APIKEY", accessKey);

        String queryString = BinanceUtil.toQueryString(params);
        HttpEntity<String> entity = new HttpEntity<>(queryString, headers);
        ResponseEntity<FutureOrderDTO> response = restTemplate.postForEntity(endpoint, entity, FutureOrderDTO.class);
        if (response.getBody() == null) {
            throw new RuntimeException("response: " + response.toString());
        }
        return response.getBody().getOrderId();
    }
}
