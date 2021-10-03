package joyyir.boddari.infrastructure.strategy;

import joyyir.boddari.domain.exchange.CurrencyType;
import joyyir.boddari.domain.exchange.PlaceType;
import joyyir.boddari.domain.kimchi.KimchiPremiumData;
import joyyir.boddari.domain.kimchi.KimchiTradeHistory;
import joyyir.boddari.domain.kimchi.TradeDecision;
import joyyir.boddari.domain.kimchi.strategy.TradeStrategy;
import joyyir.boddari.service.KimchiPremiumService;
import joyyir.boddari.service.KimchiTradeHistoryService;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

import static joyyir.boddari.interfaces.scheduler.KimchiPremiumCheckScheduler.TARGET_CURRENCIES;

@AllArgsConstructor
public class LowerAndProfitLimitStrategy implements TradeStrategy {
    private final KimchiPremiumService kimchiPremiumService;
    private final KimchiTradeHistoryService kimchiTradeHistoryService;
    private final BigDecimal lowerLimit;
    private final BigDecimal profitLimit;

    @Override
    public TradeDecision decideBuy() {
        KimchiPremiumData filtered = TARGET_CURRENCIES.stream()
                                                      .map(kimchiPremiumService::getKimchiPremium)
                                                      .filter(x -> x.getKimchiPremium().doubleValue() <= lowerLimit.doubleValue())
                                                      .findFirst()
                                                      .orElse(null);
        if (filtered == null) {
            return new TradeDecision(null, null, false);
        }
        return new TradeDecision(filtered.getCurrencyType(), PlaceType.BUY, true);
    }

    @Override
    public TradeDecision decideSell(KimchiTradeHistory lastHistory) {
        CurrencyType currencyType = lastHistory.getCurrencyType();
        KimchiPremiumData kimchiPremium = kimchiPremiumService.getKimchiPremium(currencyType);
        BigDecimal profitRate = kimchiTradeHistoryService.getProfitRate(lastHistory, kimchiPremium);
        if (profitRate.doubleValue() < profitLimit.doubleValue()) {
            return new TradeDecision(null, null, false);
        }
        return new TradeDecision(currencyType, PlaceType.SELL, true);
    }

    @Override
    public String getDescription() {
        return String.format("김프가 %.2f%% 이하로 떨어지면 매수하고, 이후 이익률이 목표 이익률인 %.2f%% 이상이 되면 매도합니다.", lowerLimit.doubleValue(), profitLimit.doubleValue());
    }
}
