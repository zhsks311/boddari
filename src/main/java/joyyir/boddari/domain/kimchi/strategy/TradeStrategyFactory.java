package joyyir.boddari.domain.kimchi.strategy;

public interface TradeStrategyFactory {
    TradeStrategy create(String tradeStrategy) throws TradeStrategyFactoryException;
    String format(TradeStrategy tradeStrategy) throws TradeStrategyFactoryException;
}
