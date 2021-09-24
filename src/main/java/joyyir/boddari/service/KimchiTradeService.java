package joyyir.boddari.service;

import joyyir.boddari.domain.kimchi.KimchiTradeHistory;
import joyyir.boddari.domain.kimchi.KimchiTradeHistoryRepository;
import joyyir.boddari.domain.kimchi.KimchiTradeStatus;
import joyyir.boddari.domain.kimchi.KimchiTradeUser;
import joyyir.boddari.domain.kimchi.KimchiTradeUserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class KimchiTradeService {
    private final KimchiTradeUserRepository kimchiTradeUserRepository;
    private final KimchiTradeHistoryRepository kimchiTradeHistoryRepository;

    public KimchiTradeUser findUser(String userId) {
        return kimchiTradeUserRepository.findById(userId)
                                        .orElseGet(() -> registerNewUser(userId));
    }

    public List<KimchiTradeHistory> findTradeHistory(String userId, String tradeId) {
        return kimchiTradeHistoryRepository.findAllByUserIdAndTradeIdOrderByTimestampDesc(userId, tradeId);
    }

    public KimchiTradeHistory startNewTrade(String userId) {
        String newTradeId = UUID.randomUUID().toString();
        kimchiTradeUserRepository.save(new KimchiTradeUser(userId, newTradeId));
        KimchiTradeHistory tradeHistory = new KimchiTradeHistory(null, userId, newTradeId, LocalDateTime.now(), KimchiTradeStatus.WAITING);
        kimchiTradeHistoryRepository.save(tradeHistory);
        return tradeHistory;
    }

    private KimchiTradeUser registerNewUser(String userId) {
        String newTradeId = UUID.randomUUID().toString();
        KimchiTradeUser user = new KimchiTradeUser(userId, newTradeId);
        kimchiTradeUserRepository.save(user);
        kimchiTradeHistoryRepository.save(new KimchiTradeHistory(null, userId, newTradeId, LocalDateTime.now(), KimchiTradeStatus.WAITING));
        return user;
    }

    public void checkBuyTimingAndTrade() {
        // TODO : jyjang - develop
    }

    public void checkSellTimingAndTrade() {
        // TODO : jyjang - develop
    }
}

