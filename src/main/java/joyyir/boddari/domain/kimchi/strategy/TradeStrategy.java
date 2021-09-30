package joyyir.boddari.domain.kimchi.strategy;

import joyyir.boddari.domain.kimchi.KimchiTradeHistory;
import joyyir.boddari.domain.kimchi.TradeDecision;

public interface TradeStrategy {
    TradeDecision decideBuy();
    TradeDecision decideSell(KimchiTradeHistory lastHistory);
}
