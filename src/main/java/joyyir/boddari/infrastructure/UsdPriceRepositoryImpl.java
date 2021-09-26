package joyyir.boddari.infrastructure;

import joyyir.boddari.domain.exchange.UsdPriceRepository;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public class UsdPriceRepositoryImpl implements UsdPriceRepository {
    private final RestTemplate restTemplate;
    private BigDecimal usdPriceKrw;
    private LocalDateTime lastUpdateDate;

    public UsdPriceRepositoryImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public BigDecimal getUsdPriceKrw() {
        if (usdPriceKrw == null || lastUpdateDate == null || lastUpdateDate.plusMinutes(10L).isBefore(LocalDateTime.now())) { // 10분 주기로 업데이트
            this.usdPriceKrw = get();
            this.lastUpdateDate = LocalDateTime.now();
        }
        if (usdPriceKrw == null || usdPriceKrw.doubleValue() < 1) {
            throw new RuntimeException("invalid usdPriceKrw: " + usdPriceKrw);
        }
        return usdPriceKrw;
    }

    private BigDecimal get() {
        ResponseEntity<String> response = restTemplate.getForEntity("https://exchange.jaeheon.kr:23490/query/USDKRW", String.class);
        JSONObject responseJson = new JSONObject(response.getBody());
        return responseJson.getJSONArray("USDKRW").getBigDecimal(0);
    }
}
