package joyyir.boddari.domain.kimchi;

import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "kimchi_trade_history", indexes = @Index(name = "kimchi_trade_history_userId_tradeId_index", columnList = "userId,tradeId"))
public class KimchiTradeHistory {
    @Id
    @GeneratedValue
    private Long id;

    private String userId;

    private String tradeId;

    @Column(name = "timestamp", columnDefinition = "TIMESTAMP")
    private LocalDateTime timestamp;

    @Enumerated(value = EnumType.STRING)
    private KimchiTradeStatus status;

    public KimchiTradeHistory() {
    }

    public KimchiTradeHistory(Long id,
                              String userId,
                              String tradeId,
                              LocalDateTime timestamp,
                              KimchiTradeStatus status) {
        this.id = id;
        this.userId = userId;
        this.tradeId = tradeId;
        this.timestamp = timestamp;
        this.status = status;
    }
}
