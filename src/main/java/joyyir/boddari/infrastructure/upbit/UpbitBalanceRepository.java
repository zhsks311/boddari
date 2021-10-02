package joyyir.boddari.infrastructure.upbit;

import joyyir.boddari.domain.exchange.Balance;
import joyyir.boddari.domain.exchange.BalanceRepository;
import joyyir.boddari.infrastructure.upbit.dto.BalanceDTO;
import lombok.AllArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@AllArgsConstructor
public class UpbitBalanceRepository implements BalanceRepository {
    private final RestTemplate restTemplate;

    @Override
    public List<Balance> getBalance(String accessKey, String secretKey) {
        final String endpoint = "https://api.upbit.com/v1/accounts";

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", UpbitUtil.getAuthenticationToken(accessKey, secretKey));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<List<BalanceDTO>> response = restTemplate.exchange(endpoint, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});
        List<BalanceDTO> balances = response.getBody();
        if (balances == null) {
            throw new RuntimeException("response: " + response.toString());
        }
        return balances.stream()
                       .map(x -> new Balance(x.getCurrency(),
                                             x.getBalance(),
                                             x.getBalance().subtract(x.getLocked())))
                       .collect(Collectors.toList());
    }
}
