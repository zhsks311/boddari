package joyyir.boddari.service;

import joyyir.boddari.domain.kimchi.KimchiTradeHistory;
import joyyir.boddari.domain.kimchi.KimchiTradeStatus;
import joyyir.boddari.domain.kimchi.KimchiTradeUser;
import joyyir.boddari.domain.kimchi.KimchiTradeUserRepository;
import joyyir.boddari.domain.kimchi.TradeStatus;
import joyyir.boddari.domain.user.UserAndTradeHistory;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class KimchiTradeUserService {
    private final KimchiTradeUserRepository kimchiTradeUserRepository;
    private final KimchiTradeHistoryService tradeHistoryService;

    public KimchiTradeUser findUserById(String userId) {
        return kimchiTradeUserRepository.findById(userId)
                                        .orElse(null);
    }

    public UserAndTradeHistory startNewTrade(String userId) {
        KimchiTradeUser user = findUserById(userId);
        if (user == null) {
            throw new RuntimeException("등록되지 않은 유저입니다. userId:" + userId);
        }
        String newTradeId = UUID.randomUUID().toString();

        user.setCurrentTradeId(newTradeId);
        user.setTradeStatus(TradeStatus.START);
        KimchiTradeUser savedUser = kimchiTradeUserRepository.save(user);

        KimchiTradeHistory savedTradeHistory = tradeHistoryService.saveNewHistory(userId, newTradeId, KimchiTradeStatus.WAITING);
        return new UserAndTradeHistory(savedUser, savedTradeHistory);
    }

    public KimchiTradeUser register(String userId) {
        return kimchiTradeUserRepository.save(new KimchiTradeUser(userId, null, TradeStatus.STOP));
    }

    public void delete(KimchiTradeUser user) {
        kimchiTradeUserRepository.delete(user);
    }
}
