package joyyir.boddari.service;

import joyyir.boddari.domain.kimchi.KimchiTradeHistory;
import joyyir.boddari.domain.kimchi.KimchiTradeHistoryRepository;
import joyyir.boddari.domain.kimchi.KimchiTradeStatus;
import joyyir.boddari.domain.kimchi.KimchiTradeUser;
import joyyir.boddari.domain.kimchi.KimchiTradeUserRepository;
import joyyir.boddari.domain.user.UserAndTradeHistory;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class KimchiTradeUserService {
    private final KimchiTradeUserRepository kimchiTradeUserRepository;
    private final KimchiTradeHistoryRepository kimchiTradeHistoryRepository;

    public KimchiTradeUser findUserOrElseRegister(String userId) {
        return kimchiTradeUserRepository.findById(userId)
                                        .orElseGet(() -> saveUserAndStartNewTrade(userId).getUser());
    }

    public UserAndTradeHistory saveUserAndStartNewTrade(String userId) {
        String newTradeId = UUID.randomUUID().toString();
        KimchiTradeUser user = new KimchiTradeUser(userId, newTradeId);
        KimchiTradeUser savedUser = kimchiTradeUserRepository.save(user);
        KimchiTradeHistory savedTradeHistory = kimchiTradeHistoryRepository.save(new KimchiTradeHistory(null, userId, newTradeId, LocalDateTime.now(), KimchiTradeStatus.WAITING, null, null));
        return new UserAndTradeHistory(savedUser, savedTradeHistory);
    }
}
