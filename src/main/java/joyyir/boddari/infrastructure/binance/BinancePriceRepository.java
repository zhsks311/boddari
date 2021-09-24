package joyyir.boddari.infrastructure.binance;

import joyyir.boddari.domain.MarketType;
import joyyir.boddari.domain.PriceRepository;
import joyyir.boddari.infrastructure.binance.dto.PriceTickerDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Objects;

@Repository
@AllArgsConstructor
public class BinancePriceRepository implements PriceRepository {
    private final RestTemplate restTemplate;

    @Override
    public BigDecimal getCurrentPrice(MarketType marketType) {
        String marketTypeString;
        if (MarketType.BTC_USDT.equals(marketType)) {
            marketTypeString = "BTCUSDT";
        } else if (MarketType.ETH_USDT.equals(marketType)) {
            marketTypeString = "ETHUSDT";
        } else if (MarketType.XRP_USDT.equals(marketType)) {
            marketTypeString = "XRPUSDT";
        } else {
            throw new UnsupportedOperationException("unsupported for marketType: " + marketType.name());
        }

        ResponseEntity<PriceTickerDTO> result = restTemplate.getForEntity("https://api.binance.com/api/v3/ticker/price?symbol=" + marketTypeString, PriceTickerDTO.class);
        return Objects.requireNonNull(result.getBody())
                      .getPrice();
    }
}
