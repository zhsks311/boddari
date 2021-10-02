package joyyir.boddari.infrastructure.binance;

import joyyir.boddari.domain.exchange.Balance;
import joyyir.boddari.domain.exchange.BalanceRepository;
import joyyir.boddari.infrastructure.binance.dto.BalanceDTO;
import lombok.AllArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@AllArgsConstructor
public class BinanceFutureBalanceRepository implements BalanceRepository {
    private final RestTemplate restTemplate;

    @Override
    public List<Balance> getBalance(String accessKey, String secretKey) {
        final String endpoint = "https://fapi.binance.com/fapi/v2/balance";

        Map<String, String> params = new LinkedHashMap<>();
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("signature", BinanceUtil.getSignature(params, secretKey));

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        headers.add("X-MBX-APIKEY", accessKey);

        String queryString = BinanceUtil.toQueryString(params);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<List<BalanceDTO>> response = restTemplate.exchange(endpoint + "?" + queryString, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});
        List<BalanceDTO> balances = response.getBody();
        if (balances == null) {
            throw new RuntimeException("response: " + response.toString());
        }
        return balances.stream()
                       .map(x -> new Balance(x.getAsset(), x.getBalance(), x.getAvailableBalance()))
                       .collect(Collectors.toList());
    }
}
