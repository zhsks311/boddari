package joyyir.boddari.infrastructure.cobak;

import joyyir.boddari.domain.MarketType;
import joyyir.boddari.domain.PriceRepository;
import joyyir.boddari.infrastructure.cobak.dto.CoinDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Objects;

@Repository
@AllArgsConstructor
public class CobakPriceRepository implements PriceRepository {
    private final RestTemplate restTemplate;

    @Override
    public BigDecimal getCurrentPrice(MarketType marketType) {
        String marketTypeString;
        if (MarketType.USDT_KRW.equals(marketType)) {
            marketTypeString = "coin-tether";
        } else {
            throw new UnsupportedOperationException("unsupported for marketType: " + marketType.name());
        }

        ResponseEntity<CoinDTO> result = restTemplate.getForEntity("https://cobak.co.kr/api/v1/coins/" + marketTypeString, CoinDTO.class);
        return Objects.requireNonNull(result.getBody())
                      .getData().getPriceKrw();
    }
}
