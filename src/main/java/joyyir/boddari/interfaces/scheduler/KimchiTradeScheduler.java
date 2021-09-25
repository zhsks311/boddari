package joyyir.boddari.interfaces.scheduler;

import joyyir.boddari.domain.kimchi.KimchiTradeHistory;
import joyyir.boddari.domain.kimchi.KimchiTradeStatus;
import joyyir.boddari.domain.kimchi.KimchiTradeUser;
import joyyir.boddari.service.KimchiTradeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Component
@Slf4j
@AllArgsConstructor
public class KimchiTradeScheduler {
    private final String userId = "admin";
    private final KimchiTradeService kimchiTradeService;

    @Scheduled(fixedRate = 1000 * 60)
    @Transactional
    void kimchiTradeScheduler() {
        final BigDecimal upbitBuyLimitKrw = new BigDecimal(50000);

        KimchiTradeUser user = kimchiTradeService.findUser(userId);
        List<KimchiTradeHistory> tradeHistory = kimchiTradeService.findTradeHistory(user.getUserId(), user.getCurrentTradeId());
        KimchiTradeHistory firstHistory = tradeHistory.get(tradeHistory.size() - 1);
        KimchiTradeHistory lastHistory = tradeHistory.get(0);
        if (lastHistory.getStatus() == KimchiTradeStatus.FINISHED) {
            firstHistory = lastHistory = kimchiTradeService.startNewTrade(userId);
        }
        log.info("[jyjang] " + firstHistory.getTimestamp() + "에 시작된 trade의(tradeId: " + user.getCurrentTradeId() + ") 현재 상태: " + lastHistory.getStatus().name());
        if (lastHistory.getStatus() == KimchiTradeStatus.WAITING) {
            kimchiTradeService.checkBuyTimingAndTrade(userId, lastHistory.getTradeId(), upbitBuyLimitKrw);
        } else if (lastHistory.getStatus() == KimchiTradeStatus.STARTED) {
            kimchiTradeService.checkSellTimingAndTrade(userId, lastHistory);
        }
    }
}
