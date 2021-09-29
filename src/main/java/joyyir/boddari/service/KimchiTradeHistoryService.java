package joyyir.boddari.service;

import joyyir.boddari.domain.kimchi.KimchiTradeHistory;
import joyyir.boddari.domain.kimchi.KimchiTradeHistoryRepository;
import joyyir.boddari.domain.kimchi.KimchiTradeProfit;
import joyyir.boddari.domain.kimchi.KimchiTradeStatus;
import joyyir.boddari.domain.kimchi.TradeDecision;
import joyyir.boddari.domain.kimchi.TradeResult;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class KimchiTradeHistoryService {
    private final KimchiTradeHistoryRepository kimchiTradeHistoryRepository;

    public List<KimchiTradeHistory> findTradeHistory(String userId, String tradeId) {
        return kimchiTradeHistoryRepository.findAllByUserIdAndTradeIdOrderByTimestampDesc(userId, tradeId);
    }

    public KimchiTradeHistory saveNewHistory(String userId, String tradeId, KimchiTradeStatus status) {
        return kimchiTradeHistoryRepository.save(new KimchiTradeHistory(null, userId, tradeId, LocalDateTime.now(), status, null, null));
    }

    public KimchiTradeHistory saveNewHistory(String userId, String tradeId, KimchiTradeStatus status, TradeDecision decision, TradeResult tradeResult, KimchiTradeProfit profit) {
        return kimchiTradeHistoryRepository.save(new KimchiTradeHistory(null,
                                                                        userId,
                                                                        tradeId,
                                                                        LocalDateTime.now(),
                                                                        status,
                                                                        decision.getCurrencyType(),
                                                                        decision.getKimchiPremium().doubleValue(),
                                                                        tradeResult.getBuyOrderDetail().getOrderQty(),
                                                                        tradeResult.getBuyOrderDetail().getAveragePrice(),
                                                                        tradeResult.getBuyOrderDetail().getFee(),
                                                                        tradeResult.getShortOrderDetail().getOrderQty(),
                                                                        tradeResult.getShortOrderDetail().getAveragePrice(),
                                                                        tradeResult.getShortOrderDetail().getFee(),
                                                                        profit != null ? profit.getProfitAmount() : null,
                                                                        profit != null ? profit.getProfitRate() : null));
    }

    public List<KimchiTradeHistory> findAllByUserIdAndTimestampAfter(String userId, LocalDateTime timeAfter) {
        return this.kimchiTradeHistoryRepository.findAllByUserIdAndTimestampAfter(userId, timeAfter);
    }
}
