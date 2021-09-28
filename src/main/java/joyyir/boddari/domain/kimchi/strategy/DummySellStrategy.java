package joyyir.boddari.domain.kimchi.strategy;

import joyyir.boddari.domain.exchange.CurrencyType;
import joyyir.boddari.domain.exchange.PlaceType;
import joyyir.boddari.domain.kimchi.KimchiPremiumData;
import joyyir.boddari.domain.kimchi.KimchiTradeHistory;
import joyyir.boddari.domain.kimchi.TradeDecision;
import joyyir.boddari.service.KimchiPremiumService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DummySellStrategy implements SellStrategy {
    private final KimchiPremiumService kimchiPremiumService;
    private final KimchiTradeHistory lastHistory;

    @Override
    public TradeDecision decide() {
        CurrencyType currencyType = lastHistory.getCurrencyType();
        KimchiPremiumData kimchiPremium = kimchiPremiumService.getKimchiPremium(currencyType);
        if (kimchiPremium == null || kimchiPremium.getKimchiPremium().doubleValue() < 4.5) { // TODO : jyjang - parameterize
            return new TradeDecision(null, null, false, null);
        }
        return new TradeDecision(currencyType, PlaceType.SELL, true, kimchiPremium.getKimchiPremium());
    }
}
