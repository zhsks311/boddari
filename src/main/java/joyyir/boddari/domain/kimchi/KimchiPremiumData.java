package joyyir.boddari.domain.kimchi;

import joyyir.boddari.domain.exchange.CurrencyType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class KimchiPremiumData {
    private CurrencyType currencyType;
    private BigDecimal upbitPriceByUsdt;
    private BigDecimal binancePriceByUsdt;
    private BigDecimal kimchiPremium;
}
