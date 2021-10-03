package joyyir.boddari.infrastructure.strategy;

import joyyir.boddari.domain.kimchi.strategy.TradeStrategy;
import joyyir.boddari.domain.kimchi.strategy.TradeStrategyFactory;
import joyyir.boddari.domain.kimchi.strategy.TradeStrategyFactoryException;
import joyyir.boddari.service.KimchiPremiumService;
import joyyir.boddari.service.KimchiTradeHistoryService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TradeStrategyFactoryImpl implements TradeStrategyFactory {
    private final static String STRATEGY_LOWER_AND_UPPER_LIMIT = "lower-and-upper-limit";
    private final static String STRATEGY_LOWER_AND_PROFIT_LIMIT = "lower-and-profit-limit";
    private final KimchiPremiumService kimchiPremiumService;
    private final KimchiTradeHistoryService kimchiTradeHistoryService;

    public TradeStrategyFactoryImpl(KimchiPremiumService kimchiPremiumService,
                                    KimchiTradeHistoryService kimchiTradeHistoryService) {
        this.kimchiPremiumService = kimchiPremiumService;
        this.kimchiTradeHistoryService = kimchiTradeHistoryService;
    }

    @Override
    public TradeStrategy create(String tradeStrategy) throws TradeStrategyFactoryException {
        if (tradeStrategy == null) {
            throw new TradeStrategyFactoryException("트레이딩 전략이 설정되지 않았습니다.");
       }
        String[] split = tradeStrategy.split("\\|");
        if (split[0].equals(STRATEGY_LOWER_AND_UPPER_LIMIT)) {
            if (split.length < 3) {
                throw new TradeStrategyFactoryException("하한 김프와 상한 김프를 명시해야 합니다. (예시) lower-and-upper-limit|2.5|5.0");
            }
            BigDecimal lowerLimit = new BigDecimal(split[1]);
            BigDecimal upperLimit = new BigDecimal(split[2]);
            if (lowerLimit.doubleValue() > upperLimit.doubleValue()) {
                throw new TradeStrategyFactoryException("상한 김프가 하한 김프보다 적습니다. 다시 설정하세요.");
            }
            return new LowerAndUpperLimitStrategy(kimchiPremiumService, lowerLimit, upperLimit);
        } else if (split[0].equals(STRATEGY_LOWER_AND_PROFIT_LIMIT)) {
            if (split.length < 3) {
                throw new TradeStrategyFactoryException("하한 김프와 목표 이익률을 명시해야 합니다. (예시) lower-and-profit-limit|2.5|1.0");
            }
            BigDecimal lowerLimit = new BigDecimal(split[1]);
            BigDecimal upperLimit = new BigDecimal(split[2]);
            return new LowerAndProfitLimitStrategy(kimchiPremiumService, kimchiTradeHistoryService, lowerLimit, upperLimit);
        }
        throw new TradeStrategyFactoryException("지원하지 않는 트레이딩 전략입니다.");
    }
}
