package joyyir.boddari.interfaces.scheduler;

import joyyir.boddari.domain.Bot;
import joyyir.boddari.domain.MarketType;
import joyyir.boddari.domain.PriceRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@Slf4j
@AllArgsConstructor
public class KimchiPremiumCheckScheduler {
    private final PriceRepository upbitPriceRepository;
    private final PriceRepository huobiPriceRepository;
    private final PriceRepository cobakPriceRepository;
    private final Bot boddariBot;

    @Scheduled(fixedRate = 1000 * 60)
    void checkKimchiPremium() {
        check(MarketType.BTC_KRW, MarketType.BTC_USDT, "BTC");
        check(MarketType.ETH_KRW, MarketType.ETH_USDT, "ETH");
        check(MarketType.XRP_KRW, MarketType.XRP_USDT, "XRP");
    }

    private void check(MarketType upbitMarketType, MarketType huobiMarketType, String currencyName) {
        BigDecimal upbitXrpPriceByKrw = upbitPriceRepository.getCurrentPrice(upbitMarketType);
        BigDecimal huobiXrpPriceByUsdt = huobiPriceRepository.getCurrentPrice(huobiMarketType);
        BigDecimal usdtPriceByKrw = cobakPriceRepository.getCurrentPrice(MarketType.USDT_KRW);
        BigDecimal huobiXrpPriceByKrw = huobiXrpPriceByUsdt.multiply(usdtPriceByKrw);
        BigDecimal kimchiPremium = upbitXrpPriceByKrw.divide(huobiXrpPriceByKrw, 5, RoundingMode.HALF_UP)
                                                     .subtract(BigDecimal.ONE)
                                                     .multiply(BigDecimal.valueOf(100L))
                                                     .setScale(1, RoundingMode.HALF_UP);
        String info = "[" + currencyName + " Price] upbit: " + upbitXrpPriceByKrw.setScale(0, RoundingMode.HALF_UP)
            + "원, huobi: " + huobiXrpPriceByKrw.setScale(0, RoundingMode.HALF_UP) + "원"
            + ", 김치 프리미엄: " + kimchiPremium + "%";
        log.info(info);
        if (kimchiPremium.compareTo(BigDecimal.valueOf(3L)) >= 0 || kimchiPremium.compareTo(BigDecimal.valueOf(-3L)) <= 0 ) {
            boddariBot.sendMessage(info);
        }
    }
}
