package joyyir.boddari.infrastructure.huobi;

import joyyir.boddari.domain.MarketType;
import joyyir.boddari.domain.PriceRepository;
import joyyir.boddari.infrastructure.huobi.dto.LastTradeDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Objects;

@Repository
@AllArgsConstructor
public class HuobiPriceRepository implements PriceRepository {
    private final RestTemplate restTemplate;

    @Override
    public BigDecimal getCurrentPrice(MarketType marketType) {
        String marketTypeString;
        if (MarketType.XRP_USDT.equals(marketType)) {
            marketTypeString = "xrpusdt";
        } else {
            throw new UnsupportedOperationException("unsupported for marketType: " + marketType.name());
        }

        ResponseEntity<LastTradeDTO> result = restTemplate.getForEntity("https://api.huobi.pro/market/trade?symbol=" + marketTypeString, LastTradeDTO.class);
        return Objects.requireNonNull(result.getBody())
                      .getTick().getData().get(0).getPrice();
    }
}
