package joyyir.boddari.service;

import joyyir.boddari.domain.exchange.CurrencyType;
import joyyir.boddari.domain.kimchi.KimchiPremiumData;
import joyyir.boddari.domain.kimchi.KimchiTradeHistory;
import joyyir.boddari.domain.kimchi.KimchiTradeHistoryRepository;
import joyyir.boddari.domain.kimchi.KimchiTradeProfit;
import joyyir.boddari.domain.kimchi.KimchiTradeStatus;
import joyyir.boddari.domain.kimchi.TradeResult;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    public KimchiTradeHistory saveNewHistory(String userId, String tradeId, KimchiTradeStatus status, CurrencyType currencyType, TradeResult tradeResult, KimchiTradeProfit profit) {
        return kimchiTradeHistoryRepository.save(new KimchiTradeHistory(null,
                                                                        userId,
                                                                        tradeId,
                                                                        LocalDateTime.now(),
                                                                        status,
                                                                        currencyType,
                                                                        tradeResult.getKimchiPremium().doubleValue(),
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
        return this.kimchiTradeHistoryRepository.findAllByUserIdAndTimestampAfterOrderByTimestampAsc(userId, timeAfter);
    }

    public void deleteByUserId(String userId) {
        kimchiTradeHistoryRepository.deleteByUserId(userId);
    }

    public BigDecimal getProfitRate(KimchiTradeHistory buyHistory, KimchiPremiumData kimchiPremium) {
        return kimchiPremium.getBinancePriceByUsdt()
                            .divide(buyHistory.getShortAvgPrice()
                                              .multiply(new BigDecimal(2)), 8, RoundingMode.FLOOR)
                            .multiply(kimchiPremium.getKimchiPremium()
                                                   .add(new BigDecimal(100))
                                                   .divide(BigDecimal.valueOf(buyHistory.getKimchiPremium())
                                                                     .add(new BigDecimal(100)), 8, RoundingMode.FLOOR)
                                                   .subtract(new BigDecimal(1)))
                            .multiply(new BigDecimal(100))
                            .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getProfitAmount(KimchiTradeHistory buyHistory, BigDecimal profitRate) {
        BigDecimal krwLimit = buyHistory.getBuyAvgPrice()
                                        .multiply(buyHistory.getBuyQuantity());
        return new BigDecimal(2).multiply(krwLimit)
                                .multiply(profitRate.divide(new BigDecimal(100), 8, RoundingMode.FLOOR))
                                .setScale(0, RoundingMode.FLOOR);
    }
}
