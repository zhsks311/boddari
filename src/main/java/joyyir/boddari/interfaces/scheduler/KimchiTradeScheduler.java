package joyyir.boddari.interfaces.scheduler;

import joyyir.boddari.domain.kimchi.KimchiTradeUser;
import joyyir.boddari.domain.kimchi.strategy.TradeStrategyFactory;
import joyyir.boddari.domain.kimchi.strategy.TradeStrategyFactoryException;
import joyyir.boddari.interfaces.handler.BoddariBotHandler;
import joyyir.boddari.service.KimchiPremiumService;
import joyyir.boddari.service.KimchiTradeService;
import joyyir.boddari.service.KimchiTradeUserService;
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
    private final KimchiTradeUserService kimchiTradeUserService;
    private final TradeStrategyFactory tradeStrategyFactory;
    private final BoddariBotHandler botHandler;

    @Scheduled(fixedRate = 1000 * 60)
    void kimchiTradeScheduler() {
        final String userId = "1080798457";

        try {
            KimchiTradeUser user = kimchiTradeUserService.findUserById(userId);
            final BigDecimal upbitBuyLimitKrw = new BigDecimal(user.getKrwLimit());
            kimchiTradeService.kimchiTrade(userId, upbitBuyLimitKrw, tradeStrategyFactory.create(user.getTradeStrategy()), botHandler);
        } catch (TradeStrategyFactoryException e) {
            log.error(e.getMessage(), e);
        };
    }
}
