package joyyir.boddari.infrastructure.strategy;

import joyyir.boddari.domain.kimchi.strategy.TradeStrategy;
import joyyir.boddari.domain.kimchi.strategy.TradeStrategyFactory;
import joyyir.boddari.domain.kimchi.strategy.TradeStrategyFactoryException;
import joyyir.boddari.service.KimchiPremiumService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@AllArgsConstructor
@Component
public class TradeStrategyFactoryImpl implements TradeStrategyFactory {
    private static String STRATEGY_UPPER_AND_LOWER_LIMIT = "upper-and-lower-limit";
    private final KimchiPremiumService kimchiPremiumService;

    @Override
    public TradeStrategy create(String tradeStrategy) throws TradeStrategyFactoryException {
        if (tradeStrategy == null) {
            throw new TradeStrategyFactoryException("트레이딩 전략이 설정되지 않았습니다.");
       }
        String[] split = tradeStrategy.split("\\|");
        if (split[0].equals(STRATEGY_UPPER_AND_LOWER_LIMIT)) {
            if (split.length < 3) {
                throw new TradeStrategyFactoryException("상한 김프와 하한 김프를 명시해야 합니다. (예시) upper-and-lower-limit|2.5|5.0");
            }
            BigDecimal lowerLimit = new BigDecimal(split[1]);
            BigDecimal upperLimit = new BigDecimal(split[2]);
            if (lowerLimit.doubleValue() > upperLimit.doubleValue()) {
                throw new TradeStrategyFactoryException("상한 김프가 하한 김프보다 적습니다. 다시 설정하세요.");
            }
            return new UpperAndLowerLimitStrategy(kimchiPremiumService, lowerLimit, upperLimit);
        }
        throw new TradeStrategyFactoryException("지원하지 않는 트레이딩 전략입니다.");
    }

    @Override
    public String format(TradeStrategy tradeStrategy) throws TradeStrategyFactoryException {
        if (tradeStrategy instanceof UpperAndLowerLimitStrategy) {
            UpperAndLowerLimitStrategy cast = (UpperAndLowerLimitStrategy) tradeStrategy;
            return STRATEGY_UPPER_AND_LOWER_LIMIT + "|" + cast.getLowerLimit() + "|" + cast.getUpperLimit();
        }
        throw new TradeStrategyFactoryException("지원하지 않는 트레이딩 전략입니다.");
    }
}