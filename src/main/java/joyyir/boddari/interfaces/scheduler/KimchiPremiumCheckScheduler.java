package joyyir.boddari.interfaces.scheduler;

import joyyir.boddari.domain.bot.Bot;
import joyyir.boddari.domain.exchange.CurrencyType;
import joyyir.boddari.domain.exchange.MarketType;
import joyyir.boddari.domain.kimchi.KimchiPremium;
import joyyir.boddari.domain.kimchi.KimchiPremiumData;
import joyyir.boddari.domain.kimchi.KimchiPremiumRepository;
import joyyir.boddari.service.KimchiPremiumService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@Slf4j
@AllArgsConstructor
public class KimchiPremiumCheckScheduler {
    private final KimchiPremiumService kimchiPremiumService;
    private final KimchiPremiumRepository kimchiPremiumRepository;
    private final Bot boddariBot;

    @Scheduled(fixedRate = 1000 * 60)
    void checkKimchiPremium() {
        check(MarketType.BTC_USDT, MarketType.BTC_USDT, CurrencyType.BTC);
        check(MarketType.ETH_USDT, MarketType.ETH_USDT, CurrencyType.ETH);
        check(MarketType.XRP_USDT, MarketType.XRP_USDT, CurrencyType.XRP);
        check(MarketType.ETC_USDT, MarketType.ETC_USDT, CurrencyType.ETC);
    }

    private void check(MarketType upbitMarketType, MarketType binanceMarketType, CurrencyType currency) {
        KimchiPremiumData kimchiPremiumData = kimchiPremiumService.getKimchiPremium(upbitMarketType, binanceMarketType, currency);
        BigDecimal kimchiPremium = kimchiPremiumData.getKimchiPremium();
        kimchiPremiumRepository.save(new KimchiPremium(LocalDateTime.now(), currency, kimchiPremium.doubleValue()));
//        String info = "[" + currency.name() + " Price] upbit: " + kimchiPremiumData.getUpbitPriceByUsdt().setScale(4, RoundingMode.HALF_UP)
//            + "달러, binance: " + kimchiPremiumData.getBinancePriceByUsdt().setScale(4, RoundingMode.HALF_UP) + "달러"
//            + ", 김치 프리미엄: " + kimchiPremium + "%";
//        log.info(info);
//        if (kimchiPremium.compareTo(BigDecimal.valueOf(3L)) >= 0 || kimchiPremium.compareTo(BigDecimal.valueOf(-3L)) <= 0 ) {
//            boddariBot.sendMessage(info);
//        }
    }
}
