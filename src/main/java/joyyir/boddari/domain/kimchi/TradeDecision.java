package joyyir.boddari.domain.kimchi;

import joyyir.boddari.domain.exchange.CurrencyType;
import joyyir.boddari.domain.exchange.PlaceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Data
@AllArgsConstructor
public class TradeDecision {
    private CurrencyType currencyType;
    private PlaceType placeType;
    private boolean trade;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("currencyType", currencyType)
            .append("placeType", placeType)
            .append("trade", trade)
            .toString();
    }
}
