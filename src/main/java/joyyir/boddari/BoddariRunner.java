package joyyir.boddari;

import joyyir.boddari.domain.MarketType;
import joyyir.boddari.domain.PriceRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class BoddariRunner implements ApplicationRunner {
    private final PriceRepository upbitPriceRepository;
    private final PriceRepository huobiPriceRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // step 1. 시세 확인
        System.out.println(upbitPriceRepository.getCurrentPrice(MarketType.KRW_XRP));
        System.out.println(huobiPriceRepository.getCurrentPrice(MarketType.BTC_USDT));
    }
}
