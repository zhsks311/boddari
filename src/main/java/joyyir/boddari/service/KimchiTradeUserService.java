package joyyir.boddari.service;

import joyyir.boddari.domain.kimchi.KimchiTradeHistory;
import joyyir.boddari.domain.kimchi.KimchiTradeStatus;
import joyyir.boddari.domain.kimchi.KimchiTradeUser;
import joyyir.boddari.domain.kimchi.KimchiTradeUserRepository;
import joyyir.boddari.domain.kimchi.TradeStatus;
import joyyir.boddari.domain.user.UserAndTradeHistory;
import joyyir.boddari.interfaces.exception.BadRequestException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class KimchiTradeUserService {
    private final KimchiTradeUserRepository kimchiTradeUserRepository;
    private final KimchiTradeHistoryService tradeHistoryService;
    private final BalanceService balanceService;

    public KimchiTradeUser findUserById(String userId) {
        return kimchiTradeUserRepository.findById(userId)
                                        .orElse(null);
    }

    public List<KimchiTradeUser> findAllByTradeStatus(TradeStatus tradeStatus) {
        return kimchiTradeUserRepository.findAllByTradeStatus(tradeStatus);
    }

    public UserAndTradeHistory startNewTrade(String userId) throws BadRequestException {
        KimchiTradeUser user = findUserById(userId);
        if (user == null) {
            throw new BadRequestException("등록되지 않은 유저입니다.");
        }
        balanceService.checkSufficientBalance(user);
        String newTradeId = UUID.randomUUID().toString();
        KimchiTradeUser savedUser = setUserTradeStatus(user, TradeStatus.START, newTradeId);
        KimchiTradeHistory savedTradeHistory = tradeHistoryService.saveNewHistory(userId, newTradeId, KimchiTradeStatus.WAITING);
        return new UserAndTradeHistory(savedUser, savedTradeHistory);
    }

    public KimchiTradeUser register(String userId, String upbitAccessKey, String upbitSecretKey, String binanceAccessKey, String binanceSecretKey) {
        // TODO : jyjang - upbit key, binance key test
        return kimchiTradeUserRepository.save(new KimchiTradeUser(userId, null, TradeStatus.STOP, null, null, upbitAccessKey, upbitSecretKey, binanceAccessKey, binanceSecretKey));
    }

    @Transactional
    public void delete(String userId) throws BadRequestException {
        KimchiTradeUser user = findUserById(userId);
        if (user == null) {
            throw new BadRequestException("등록되지 않은 유저입니다.");
        }
        kimchiTradeUserRepository.delete(user);
        tradeHistoryService.deleteByUserId(userId);
    }

    public KimchiTradeUser setUserTradeStatus(KimchiTradeUser user, TradeStatus status, String tradeId) {
        user.setTradeStatus(status);
        user.setCurrentTradeId(tradeId);
        return kimchiTradeUserRepository.save(user);
    }

    public KimchiTradeUser save(KimchiTradeUser user) {
        return kimchiTradeUserRepository.save(user);
    }
}
