package joyyir.boddari.infrastructure.strategy;

import joyyir.boddari.domain.exchange.CurrencyType;
import joyyir.boddari.domain.exchange.PlaceType;
import joyyir.boddari.domain.kimchi.KimchiPremiumData;
import joyyir.boddari.domain.kimchi.KimchiTradeHistory;
import joyyir.boddari.domain.kimchi.TradeDecision;
import joyyir.boddari.domain.kimchi.strategy.TradeStrategy;
import joyyir.boddari.service.KimchiPremiumService;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
public class LowerAndUpperLimitStrategy implements TradeStrategy {
    private final static List<CurrencyType> TARGET_CURRENCIES = List.of(CurrencyType.BTC, CurrencyType.ETH, CurrencyType.XRP, CurrencyType.ETC);
    private final KimchiPremiumService kimchiPremiumService;
    private final BigDecimal lowerLimit;
    private final BigDecimal upperLimit;

    @Override
    public TradeDecision decideBuy() {
        KimchiPremiumData filtered = TARGET_CURRENCIES.stream()
                                                      .map(kimchiPremiumService::getKimchiPremium)
                                                      .filter(x -> x.getKimchiPremium().doubleValue() <= lowerLimit.doubleValue())
                                                      .findFirst()
                                                      .orElse(null);
        if (filtered == null) {
            return new TradeDecision(null, null, false, null);
        }
        return new TradeDecision(filtered.getCurrencyType(), PlaceType.BUY, true, filtered.getKimchiPremium());
    }

    @Override
    public TradeDecision decideSell(KimchiTradeHistory lastHistory) {
        CurrencyType currencyType = lastHistory.getCurrencyType();
        KimchiPremiumData kimchiPremium = kimchiPremiumService.getKimchiPremium(currencyType);
        if (kimchiPremium == null || kimchiPremium.getKimchiPremium().doubleValue() <= upperLimit.doubleValue()) {
            return new TradeDecision(null, null, false, null);
        }
        return new TradeDecision(currencyType, PlaceType.SELL, true, kimchiPremium.getKimchiPremium());
    }

    @Override
    public String getDescription() {
        return String.format("하한 김프 %.2f%% 밑으로 떨어지면 매수하고 상한 김프 %.2f%% 위로 올라가면 매도합니다.", lowerLimit.doubleValue(), upperLimit.doubleValue());
    }

    public BigDecimal getLowerLimit() {
        return lowerLimit;
    }

    public BigDecimal getUpperLimit() {
        return upperLimit;
    }
}
