package joyyir.boddari.domain.kimchi;

import joyyir.boddari.domain.exchange.CurrencyType;
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
@Table(name = "kimchi_premium", indexes = @Index(name = "kimchi_premium_timestamp_index", columnList = "timestamp"))
public class KimchiPremium {
    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "timestamp", columnDefinition = "TIMESTAMP")
    private LocalDateTime timestamp;
    @Enumerated(value = EnumType.STRING)
    private CurrencyType currency;
    private Double premium;

    public KimchiPremium() {
    }

    public KimchiPremium(LocalDateTime timestamp, CurrencyType currency, Double premium) {
        this.timestamp = timestamp;
        this.currency = currency;
        this.premium = premium;
    }
}
