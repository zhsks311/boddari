package joyyir.boddari.domain.kimchi.strategy;

import joyyir.boddari.domain.exchange.CurrencyType;
import joyyir.boddari.domain.exchange.PlaceType;
import joyyir.boddari.domain.kimchi.KimchiPremiumData;
import joyyir.boddari.domain.kimchi.TradeDecision;
import joyyir.boddari.service.KimchiPremiumService;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class DummyBuyStrategy implements BuyStrategy {
    private final static List<CurrencyType> TARGET_CURRENCIES = List.of(CurrencyType.BTC, CurrencyType.ETH, CurrencyType.XRP, CurrencyType.ETC);
    private final KimchiPremiumService kimchiPremiumService;

    @Override
    public TradeDecision decide() {
        KimchiPremiumData filtered = TARGET_CURRENCIES.stream()
                                                      .map(kimchiPremiumService::getKimchiPremium)
                                                      .filter(x -> x.getKimchiPremium().doubleValue() < 2.5) // TODO : jyjang - parameterize
                                                      .findFirst()
                                                      .orElse(null);
        if (filtered == null) {
            return new TradeDecision(null, null, false, null);
        }
        return new TradeDecision(filtered.getCurrencyType(), PlaceType.BUY, true, filtered.getKimchiPremium());
    }
}
