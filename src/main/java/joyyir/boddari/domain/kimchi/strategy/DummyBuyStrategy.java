package joyyir.boddari.domain.kimchi.strategy;

import joyyir.boddari.domain.exchange.CurrencyType;
import joyyir.boddari.domain.exchange.MarketType;
import joyyir.boddari.domain.exchange.PlaceType;
import joyyir.boddari.domain.kimchi.KimchiPremiumData;
import joyyir.boddari.domain.kimchi.TradeDecision;
import joyyir.boddari.service.KimchiPremiumService;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class DummyBuyStrategy implements BuyStrategy {
    private static final List<MarketType> TARGET_MARKET = List.of(MarketType.BTC_USDT, MarketType.ETH_USDT, MarketType.XRP_USDT, MarketType.ETC_USDT);
    private static final List<CurrencyType> TARGET_CURRENCY = List.of(CurrencyType.BTC, CurrencyType.ETH, CurrencyType.XRP, CurrencyType.ETC);
    private final KimchiPremiumService kimchiPremiumService;

    @Override
    public TradeDecision decide() {
        List<KimchiPremiumData> kimchiPremiumData = new ArrayList<>();
        for (int i = 0; i < TARGET_MARKET.size(); i++) {
            kimchiPremiumData.add(kimchiPremiumService.getKimchiPremium(TARGET_MARKET.get(i), TARGET_MARKET.get(i), TARGET_CURRENCY.get(i)));
        }
        KimchiPremiumData filtered = kimchiPremiumData.stream()
                                                      .filter(x -> x.getKimchiPremium().doubleValue() < -2.5)
                                                      .findFirst()
                                                      .orElse(null);
        if (filtered == null) {
            return new TradeDecision(null, null, false, null);
        }
        return new TradeDecision(filtered.getCurrencyType(), PlaceType.BUY, true, filtered.getKimchiPremium());
    }
}
