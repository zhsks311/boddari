package joyyir.boddari.infrastructure.binance;

import lombok.AllArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Repository
@AllArgsConstructor
public class BinanceExchangeInfo {
    private static JSONObject exchangeInfo;
    private final RestTemplate restTemplate;
    private final Map<String, Integer> quantityPrecisionMap = new HashMap<>();
    private final Map<String, Integer> pricePrecisionMap = new HashMap<>();

    public int getQuantityPrecision(String binanceSymbol) {
        loadExchangeInfo();
        return Objects.requireNonNull(quantityPrecisionMap.get(binanceSymbol));
    }

    public int getPricePrecision(String binanceSymbol) {
        loadExchangeInfo();
        return Objects.requireNonNull(pricePrecisionMap.get(binanceSymbol));
    }

    private void loadExchangeInfo() {
        if (exchangeInfo != null) {
            return;
        }
        ResponseEntity<String> response = restTemplate.getForEntity("https://fapi.binance.com/fapi/v1/exchangeInfo", String.class);
        String responseBody = response.getBody();
        if (responseBody != null) {
            JSONObject jsonObject = new JSONObject(responseBody);
            JSONArray symbols = jsonObject.getJSONArray("symbols");
            for (int i = 0; i < symbols.length(); i++) {
                JSONObject symbolObject = symbols.getJSONObject(i);
                String symbol = symbolObject.getString("symbol");
                Integer pricePrecision = symbolObject.getInt("pricePrecision");
                Integer quantityPrecision = symbolObject.getInt("quantityPrecision");
                pricePrecisionMap.put(symbol, pricePrecision);
                quantityPrecisionMap.put(symbol, quantityPrecision);
            }
            exchangeInfo = jsonObject;
            return;
        }
        throw new RuntimeException("loading exchange info failed!");
    }

}
