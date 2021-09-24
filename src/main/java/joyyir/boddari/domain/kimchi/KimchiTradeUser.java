package joyyir.boddari.domain.kimchi;

import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Entity
@Table(name = "kimchi_trade_user")
public class KimchiTradeUser {
    @Id
    private String userId;

    private String currentTradeId;

    public KimchiTradeUser() {
    }

    public KimchiTradeUser(String userId, String currentTradeId) {
        this.userId = userId;
        this.currentTradeId = currentTradeId;
    }
}
