package joyyir.boddari.infrastructure.binance;

import joyyir.boddari.domain.exchange.FuturePlaceType;
import joyyir.boddari.domain.exchange.FutureTradeRepository;
import joyyir.boddari.domain.exchange.MarketType;
import joyyir.boddari.infrastructure.binance.dto.ChangeInitialLeverageDTO;
import joyyir.boddari.infrastructure.binance.dto.ChangeMarginTypeDTO;
import joyyir.boddari.infrastructure.binance.dto.FutureOrderDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

@Repository
public class BinanceFutureTradeRepository implements FutureTradeRepository {
    private final RestTemplate restTemplate;
    private final BinanceExchangeInfo binanceExchangeInfo;

    public BinanceFutureTradeRepository(RestTemplate restTemplate,
                                        BinanceExchangeInfo binanceExchangeInfo) {
        this.restTemplate = restTemplate;
        this.binanceExchangeInfo = binanceExchangeInfo;
    }

    @Override
    public boolean changeInitialLeverage(MarketType marketType, int leverage, String accessKey, String secretKey) {
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

    @Override
    public boolean changeMarginType(MarketType marketType, String marginType, String accessKey, String secretKey) {
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

    @Override
    public String marketLong(MarketType marketType, BigDecimal quantity, String accessKey, String secretKey) {
        return order(marketType, FuturePlaceType.LONG, null, quantity, "MARKET", accessKey, secretKey);
    }

    @Override
    public String marketShort(MarketType marketType, BigDecimal quantity, String accessKey, String secretKey) {
        return order(marketType, FuturePlaceType.SHORT, null, quantity, "MARKET", accessKey, secretKey);
    }

    @Override
    public String limitOrder(MarketType marketType, FuturePlaceType placeType, BigDecimal price, BigDecimal quantity, String accessKey, String secretKey) {
        return order(marketType, placeType, price, quantity, "LIMIT", accessKey, secretKey);
    }

    private String order(MarketType marketType, FuturePlaceType placeType, BigDecimal price, BigDecimal quantity, String orderType, String accessKey, String secretKey) {
        final String endpoint = "https://fapi.binance.com/fapi/v1/order";
        String symbol = BinanceMarketTypeConverter.convert(marketType);

        Map<String, String> params = new LinkedHashMap<>();
        params.put("symbol", symbol);
        params.put("side", FuturePlaceType.SHORT == placeType ? "SELL" : "BUY");
        params.put("type", orderType);
        if ("LIMIT".equals(orderType)) {
            params.put("timeInForce", "GTC"); // GTC - Good Till Cancel
        }
        if (quantity != null) {
            params.put("quantity", quantity.setScale(binanceExchangeInfo.getQuantityPrecision(symbol), RoundingMode.HALF_UP).toString());
        }
        if (price != null) {
            params.put("price", price.setScale(binanceExchangeInfo.getPricePrecision(symbol), RoundingMode.HALF_UP).toString());
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
