package joyyir.boddari.infrastructure.binance;

import joyyir.boddari.domain.exchange.MarketType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BinancePriceRepositoryTest {
    private final BinancePriceRepository repository;

    public BinancePriceRepositoryTest(@Autowired BinancePriceRepository repository) {
        this.repository = repository;
    }

    @Test
    void test() {
        BigDecimal currentPrice = this.repository.getCurrentPrice(MarketType.BTC_USDT);
        assertThat(currentPrice).isGreaterThan(new BigDecimal(0L));
    }
}