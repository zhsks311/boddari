package joyyir.boddari.interfaces.scheduler;

import joyyir.boddari.domain.kimchi.KimchiTradeUser;
import joyyir.boddari.domain.kimchi.TradeStatus;
import joyyir.boddari.interfaces.handler.BoddariBotHandler;
import joyyir.boddari.service.KimchiTradeService;
import joyyir.boddari.service.KimchiTradeUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Component
@Slf4j
@AllArgsConstructor
public class KimchiTradeScheduler {
    private final KimchiTradeService kimchiTradeService;
    private final KimchiTradeUserService kimchiTradeUserService;
    private final ExecutorService tradeExecutorService;
    private final BoddariBotHandler botHandler;

    @Scheduled(fixedRate = 1000 * 60)
    void kimchiTradeScheduler() {
        List<KimchiTradeUser> tradeUsers = kimchiTradeUserService.findAllByTradeStatus(TradeStatus.START);
        String tradingUsers = tradeUsers.stream()
                                        .map(KimchiTradeUser::getUserId)
                                        .collect(Collectors.joining(", "));
        log.info("[jyjang] 트레이딩 중인 유저 목록: " + tradingUsers);
        for (KimchiTradeUser tradeUser : tradeUsers) {
            tradeExecutorService.execute(() -> kimchiTradeService.kimchiTrade(tradeUser, botHandler));
        }
    }
}
