package joyyir.boddari.domain.kimchi;

import joyyir.boddari.domain.exchange.CurrencyType;
import joyyir.boddari.domain.exchange.PlaceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TradeDecision {
    private CurrencyType currencyType;
    private PlaceType placeType;
    private boolean trade;
    private BigDecimal kimchiPremium;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("currencyType", currencyType)
            .append("placeType", placeType)
            .append("trade", trade)
            .append("kimchiPremium", kimchiPremium)
            .toString();
    }
}
