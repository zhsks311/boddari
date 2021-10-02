package joyyir.boddari.infrastructure.binance;

import joyyir.boddari.domain.exchange.MarketType;
import joyyir.boddari.domain.exchange.PriceRepository;
import joyyir.boddari.infrastructure.binance.dto.PriceTickerDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Objects;

@Repository
@AllArgsConstructor
public class BinanceFuturePriceRepository implements PriceRepository {
    private final RestTemplate restTemplate;

    @Override
    public BigDecimal getCurrentPrice(MarketType marketType) {
        String marketTypeString = BinanceMarketTypeConverter.convert(marketType);
        ResponseEntity<PriceTickerDTO> result = restTemplate.getForEntity("https://fapi.binance.com/fapi/v1/ticker/price?symbol=" + marketTypeString, PriceTickerDTO.class);
        return Objects.requireNonNull(result.getBody())
                      .getPrice();
    }
}
