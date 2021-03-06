package joyyir.boddari.infrastructure.upbit;

import joyyir.boddari.domain.exchange.MarketType;
import joyyir.boddari.domain.exchange.PriceRepository;
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
        try {
            Thread.sleep(1000); // 초당 최대 30회 호출 가능
        } catch (InterruptedException ignored) {}
        ResponseEntity<TickerDTO[]> result = restTemplate.getForEntity("https://api.upbit.com/v1/ticker?markets=" + UpbitMarketTypeConverter.convert(marketType), TickerDTO[].class);
        return Objects.requireNonNull(result.getBody())[0].getTradePrice();
    }
}
