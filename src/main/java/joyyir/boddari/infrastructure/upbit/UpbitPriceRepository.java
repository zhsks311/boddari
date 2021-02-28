package joyyir.boddari.infrastructure.upbit;

import joyyir.boddari.domain.MarketType;
import joyyir.boddari.domain.PriceRepository;
import joyyir.boddari.infrastructure.upbit.dto.TickerDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Objects;

@Repository
@AllArgsConstructor
public class UpbitPriceRepository implements PriceRepository {
    private final RestTemplate restTemplate;

    @Override
    public BigDecimal getCurrentPrice(MarketType marketType) {
        String marketTypeString;
        if (MarketType.KRW_XRP.equals(marketType)) {
            marketTypeString = "KRW-XRP";
        } else {
            throw new UnsupportedOperationException("unsupported for marketType: " + marketType.name());
        }

        ResponseEntity<TickerDTO[]> result = restTemplate.getForEntity("https://api.upbit.com/v1/ticker?markets=" + marketTypeString, TickerDTO[].class);
        return Objects.requireNonNull(result.getBody())[0].getTradePrice();
    }
}
