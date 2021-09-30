package joyyir.boddari.interfaces.scheduler;

import joyyir.boddari.domain.kimchi.strategy.UpperAndLowerLimitStrategy;
import joyyir.boddari.service.KimchiPremiumService;
import joyyir.boddari.service.KimchiTradeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
@AllArgsConstructor
public class KimchiTradeScheduler {
    private final KimchiTradeService kimchiTradeService;
    private final KimchiPremiumService kimchiPremiumService;

    @Scheduled(fixedRate = 1000 * 60)
    void kimchiTradeScheduler() {
        final String userId = "admin";
        final BigDecimal upbitBuyLimitKrw = new BigDecimal(50000);

        kimchiTradeService.kimchiTrade(userId, upbitBuyLimitKrw, new UpperAndLowerLimitStrategy(kimchiPremiumService, new BigDecimal("2.3"), new BigDecimal("3.6")));
    }
}
