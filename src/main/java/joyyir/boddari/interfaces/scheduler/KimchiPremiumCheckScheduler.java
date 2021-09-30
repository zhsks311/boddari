package joyyir.boddari.interfaces.scheduler;

import joyyir.boddari.domain.exchange.CurrencyType;
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
import java.util.List;

@Component
@Slf4j
@AllArgsConstructor
public class KimchiPremiumCheckScheduler {
    private final static List<CurrencyType> TARGET_CURRENCIES
        = List.of(CurrencyType.BTC,
                  CurrencyType.ETH,
                  CurrencyType.XRP,
                  CurrencyType.ETC,
                  CurrencyType.ADA,
                  CurrencyType.XTZ,
                  CurrencyType.ATOM,
                  CurrencyType.SRM,
                  CurrencyType.DOT,
                  CurrencyType.MANA);
    private final KimchiPremiumService kimchiPremiumService;
    private final KimchiPremiumRepository kimchiPremiumRepository;

    @Scheduled(fixedRate = 1000 * 60)
    void checkKimchiPremium() {
        TARGET_CURRENCIES.forEach(this::check);
    }

    private void check(CurrencyType currency) {
        KimchiPremiumData kimchiPremiumData = kimchiPremiumService.getKimchiPremium(currency);
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
