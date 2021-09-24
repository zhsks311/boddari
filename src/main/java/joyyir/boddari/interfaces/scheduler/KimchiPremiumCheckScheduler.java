package joyyir.boddari.interfaces.scheduler;

import joyyir.boddari.domain.Bot;
import joyyir.boddari.domain.CurrencyType;
import joyyir.boddari.domain.KimchiPremium;
import joyyir.boddari.domain.KimchiPremiumRepository;
import joyyir.boddari.domain.MarketType;
import joyyir.boddari.domain.PriceRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Component
@Slf4j
@AllArgsConstructor
public class KimchiPremiumCheckScheduler {
    private final PriceRepository upbitPriceRepository;
    private final PriceRepository binancePriceRepository;
    private final KimchiPremiumRepository kimchiPremiumRepository;
    private final Bot boddariBot;

//    @Scheduled(fixedRate = 1000 * 60)
    void checkKimchiPremium() {
        check(MarketType.BTC_USDT, MarketType.BTC_USDT, CurrencyType.BTC);
        check(MarketType.ETH_USDT, MarketType.ETH_USDT, CurrencyType.ETH);
        check(MarketType.XRP_USDT, MarketType.XRP_USDT, CurrencyType.XRP);
    }

    private void check(MarketType upbitMarketType, MarketType binanceMarketType, CurrencyType currency) {
        BigDecimal upbitXrpPriceByUsdt = upbitPriceRepository.getCurrentPrice(upbitMarketType);
        BigDecimal binanceXrpPriceByUsdt = binancePriceRepository.getCurrentPrice(binanceMarketType);
        BigDecimal kimchiPremium = upbitXrpPriceByUsdt.divide(binanceXrpPriceByUsdt, 5, RoundingMode.HALF_UP)
                                                      .subtract(BigDecimal.ONE)
                                                      .multiply(BigDecimal.valueOf(100L))
                                                      .setScale(1, RoundingMode.HALF_UP);
        kimchiPremiumRepository.save(new KimchiPremium(LocalDateTime.now(), currency, kimchiPremium.doubleValue()));
        String info = "[" + currency.name() + " Price] upbit: " + upbitXrpPriceByUsdt.setScale(4, RoundingMode.HALF_UP)
            + "달러, binance: " + binanceXrpPriceByUsdt.setScale(4, RoundingMode.HALF_UP) + "달러"
            + ", 김치 프리미엄: " + kimchiPremium + "%";
        log.info(info);
        if (kimchiPremium.compareTo(BigDecimal.valueOf(3L)) >= 0 || kimchiPremium.compareTo(BigDecimal.valueOf(-3L)) <= 0 ) {
            boddariBot.sendMessage(info);
        }
    }
}
