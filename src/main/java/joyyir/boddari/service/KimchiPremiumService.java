package joyyir.boddari.service;

import joyyir.boddari.domain.exchange.CurrencyType;
import joyyir.boddari.domain.exchange.CurrencyTypeConverter;
import joyyir.boddari.domain.exchange.PriceRepository;
import joyyir.boddari.domain.exchange.UsdPriceRepository;
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
    private final UsdPriceRepository usdPriceRepository;

    public KimchiPremiumData getKimchiPremium(CurrencyType currency) {
        BigDecimal usdPriceKrw = usdPriceRepository.getUsdPriceKrw();
        BigDecimal upbitPriceByKrw = upbitPriceRepository.getCurrentPrice(CurrencyTypeConverter.toMarketType(currency, CurrencyType.KRW));
        BigDecimal binancePriceByUsdt = binancePriceRepository.getCurrentPrice(CurrencyTypeConverter.toMarketType(currency, CurrencyType.USDT));
        BigDecimal binancePriceByKrw = binancePriceByUsdt.multiply(usdPriceKrw);
        BigDecimal kimchiPremium = upbitPriceByKrw.divide(binancePriceByKrw, 5, RoundingMode.HALF_UP)
                                                  .subtract(BigDecimal.ONE)
                                                  .multiply(BigDecimal.valueOf(100L))
                                                  .setScale(1, RoundingMode.HALF_UP);
        return new KimchiPremiumData(currency, kimchiPremium);
    }
}
