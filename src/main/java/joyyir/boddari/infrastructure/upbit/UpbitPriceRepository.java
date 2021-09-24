package joyyir.boddari.infrastructure.upbit;

import joyyir.boddari.domain.MarketType;
import joyyir.boddari.domain.PriceRepository;
import joyyir.boddari.infrastructure.upbit.dto.TickerDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

@Repository
@AllArgsConstructor
public class UpbitPriceRepository implements PriceRepository {
    private final RestTemplate restTemplate;
    private final Map<MarketType, String> map = Map.ofEntries(
        Map.entry(MarketType.BTC_KRW, "KRW-BTC"),
        Map.entry(MarketType.ETH_KRW, "KRW-ETH"),
        Map.entry(MarketType.XRP_KRW, "KRW-XRP"),

        Map.entry(MarketType.BTC_USDT, "USDT-BTC"),
        Map.entry(MarketType.ETH_USDT, "USDT-ETH"),
        Map.entry(MarketType.XRP_USDT, "USDT-XRP"),
        Map.entry(MarketType.ETC_USDT, "USDT-ETC")
    );

    @Override
    public BigDecimal getCurrentPrice(MarketType marketType) {
        String marketTypeString = map.get(marketType);
        if (marketTypeString == null) {
            throw new UnsupportedOperationException("unsupported for marketType: " + marketType.name());
        }

        ResponseEntity<TickerDTO[]> result = restTemplate.getForEntity("https://api.upbit.com/v1/ticker?markets=" + marketTypeString, TickerDTO[].class);
        return Objects.requireNonNull(result.getBody())[0].getTradePrice();
    }
}
