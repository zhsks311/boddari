package joyyir.boddari.infrastructure.binance;

import joyyir.boddari.domain.MarketType;
import joyyir.boddari.domain.PriceRepository;
import joyyir.boddari.infrastructure.binance.dto.PriceTickerDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

@Repository
@AllArgsConstructor
public class BinancePriceRepository implements PriceRepository {
    private final RestTemplate restTemplate;
    private final Map<MarketType, String> map = Map.ofEntries(
        Map.entry(MarketType.BTC_USDT, "BTCUSDT"),
        Map.entry(MarketType.ETH_USDT, "ETHUSDT"),
        Map.entry(MarketType.XRP_USDT, "XRPUSDT"),
        Map.entry(MarketType.ETC_USDT, "ETCUSDT")
    );

    @Override
    public BigDecimal getCurrentPrice(MarketType marketType) {
        String marketTypeString = map.get(marketType);
        if (marketTypeString == null) {
            throw new UnsupportedOperationException("unsupported for marketType: " + marketType.name());
        }

        ResponseEntity<PriceTickerDTO> result = restTemplate.getForEntity("https://api.binance.com/api/v3/ticker/price?symbol=" + marketTypeString, PriceTickerDTO.class);
        return Objects.requireNonNull(result.getBody())
                      .getPrice();
    }
}
