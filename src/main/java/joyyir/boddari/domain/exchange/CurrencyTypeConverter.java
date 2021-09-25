package joyyir.boddari.domain.exchange;

import java.util.Map;
import java.util.Objects;

public class CurrencyTypeConverter {
    private static final Map<CurrencyType, MarketType> usdtMap = Map.ofEntries(
        Map.entry(CurrencyType.BTC, MarketType.BTC_USDT),
        Map.entry(CurrencyType.ETH, MarketType.ETH_USDT),
        Map.entry(CurrencyType.XRP, MarketType.XRP_USDT),
        Map.entry(CurrencyType.ETC, MarketType.ETC_USDT)
    );
    private static final Map<CurrencyType, MarketType> krwMap = Map.ofEntries(
        Map.entry(CurrencyType.BTC, MarketType.BTC_KRW),
        Map.entry(CurrencyType.ETH, MarketType.ETH_KRW),
        Map.entry(CurrencyType.XRP, MarketType.XRP_KRW),
        Map.entry(CurrencyType.ETC, MarketType.ETC_KRW)
    );

    public static MarketType toMarketType(CurrencyType currencyType, CurrencyType marketCurrencyType) {
        if (marketCurrencyType == CurrencyType.KRW) {
            return Objects.requireNonNull(krwMap.get(currencyType));
        } else if (marketCurrencyType == CurrencyType.USDT) {
            return Objects.requireNonNull(usdtMap.get(currencyType));
        } else {
            throw new UnsupportedOperationException("지원되지 않는 마켓 화폐: " + marketCurrencyType);
        }
    }
}
