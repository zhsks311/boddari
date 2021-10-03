package joyyir.boddari.domain.exchange;

public enum CurrencyType {
    KRW(null, null),
    USDT(null, null),
    BTC(MarketType.BTC_USDT, MarketType.BTC_KRW),
    ETH(MarketType.ETH_USDT, MarketType.ETH_KRW),
    XRP(MarketType.XRP_USDT, MarketType.XRP_KRW),
    ETC(MarketType.ETC_USDT, MarketType.ETC_KRW),
    ADA(MarketType.ADA_USDT, MarketType.ADA_KRW),
    XTZ(MarketType.XTZ_USDT, MarketType.XTZ_KRW),
    ATOM(MarketType.ATOM_USDT, MarketType.ATOM_KRW),
    SRM(MarketType.SRM_USDT, MarketType.SRM_KRW),
    DOT(MarketType.DOT_USDT, MarketType.DOT_KRW),
    ;

    private final MarketType usdtMarket;
    private final MarketType krwMarket;

    CurrencyType(MarketType usdtMarket, MarketType krwMarket) {
        this.usdtMarket = usdtMarket;
        this.krwMarket = krwMarket;
    }

    public MarketType getUsdtMarket() {
        return usdtMarket;
    }

    public MarketType getKrwMarket() {
        return krwMarket;
    }
}
