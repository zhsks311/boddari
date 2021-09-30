package joyyir.boddari.domain.kimchi;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "kimchi_trade_user")
public class KimchiTradeUser {
    @Id
    private String userId;

    private String currentTradeId;

    @Enumerated(value = EnumType.STRING)
    private TradeStatus tradeStatus;

    private Integer krwLimit;

    public KimchiTradeUser() {
    }

    public KimchiTradeUser(String userId, String currentTradeId, TradeStatus tradeStatus, Integer krwLimit) {
        this.userId = userId;
        this.currentTradeId = currentTradeId;
        this.tradeStatus = tradeStatus;
        this.krwLimit = krwLimit;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("userId", userId)
            .append("currentTradeId", currentTradeId)
            .append("tradeStatus", tradeStatus)
            .append("krwLimit", krwLimit)
            .toString();
    }
}
