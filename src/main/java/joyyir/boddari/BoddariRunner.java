package joyyir.boddari;

import joyyir.boddari.domain.MarketType;
import joyyir.boddari.domain.PriceRepository;
import joyyir.boddari.infrastructure.telegram.BoddariBot;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@AllArgsConstructor
@Slf4j
public class BoddariRunner implements ApplicationRunner {
    private final PriceRepository upbitPriceRepository;
    private final PriceRepository huobiPriceRepository;
    private final PriceRepository cobakPriceRepository;
    private final BoddariBot boddariBot;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        while (true) {
            // step 1. 시세 확인
            BigDecimal upbitXrpPriceByKrw = upbitPriceRepository.getCurrentPrice(MarketType.XRP_KRW);
            BigDecimal huobiXrpPriceByUsdt = huobiPriceRepository.getCurrentPrice(MarketType.XRP_USDT);
            BigDecimal usdtPriceByKrw = cobakPriceRepository.getCurrentPrice(MarketType.USDT_KRW);
            BigDecimal huobiXrpPriceByKrw = huobiXrpPriceByUsdt.multiply(usdtPriceByKrw);
            BigDecimal kimchiPremium = upbitXrpPriceByKrw.divide(huobiXrpPriceByKrw, 5, RoundingMode.HALF_UP)
                                                         .subtract(BigDecimal.ONE)
                                                         .multiply(BigDecimal.valueOf(100L))
                                                         .setScale(1, RoundingMode.HALF_UP);
            String info = "[XRP Price] upbit: " + upbitXrpPriceByKrw.setScale(0, RoundingMode.HALF_UP)
                + "원, huobi: " + huobiXrpPriceByKrw.setScale(0, RoundingMode.HALF_UP) + "원"
                + ", 김치 프리미엄: " + kimchiPremium + "%";
            log.info(info);
            if (kimchiPremium.compareTo(BigDecimal.valueOf(3L)) >= 0) {
                boddariBot.sendMessage(info);
            }

            Thread.sleep(1000 * 60); // 1분
        }
    }
}
