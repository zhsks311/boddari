package joyyir.boddari.service;

import joyyir.boddari.domain.exchange.CurrencyType;
import joyyir.boddari.domain.exchange.MarketType;
import joyyir.boddari.domain.exchange.PriceRepository;
import joyyir.boddari.domain.kimchi.KimchiPremiumData;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@AllArgsConstructor
public class KimchiPremiumService {
    private final PriceRepository upbitPriceRepository;
    private final PriceRepository binancePriceRepository;

    public KimchiPremiumData getKimchiPremium(MarketType upbitMarketType, MarketType binanceMarketType, CurrencyType currency) {
        BigDecimal upbitPriceByUsdt = upbitPriceRepository.getCurrentPrice(upbitMarketType);
        BigDecimal binancePriceByUsdt = binancePriceRepository.getCurrentPrice(binanceMarketType);
        BigDecimal kimchiPremium = upbitPriceByUsdt.divide(binancePriceByUsdt, 5, RoundingMode.HALF_UP)
                                                   .subtract(BigDecimal.ONE)
                                                   .multiply(BigDecimal.valueOf(100L))
                                                   .setScale(1, RoundingMode.HALF_UP);
        return new KimchiPremiumData(currency, upbitPriceByUsdt, binancePriceByUsdt, kimchiPremium);
    }
}
