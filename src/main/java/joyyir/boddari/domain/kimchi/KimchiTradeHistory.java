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
import java.math.BigDecimal;
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

    @Enumerated(value = EnumType.STRING)
    private CurrencyType currencyType;

    private Double kimchiPremium;

    @Column(precision = 20, scale = 8)
    private BigDecimal buyQuantity;

    @Column(precision = 20, scale = 8)
    private BigDecimal buyAvgPrice;

    @Column(precision = 20, scale = 8)
    private BigDecimal buyFee;

    @Column(precision = 20, scale = 8)
    private BigDecimal shortQuantity;

    @Column(precision = 20, scale = 8)
    private BigDecimal shortAvgPrice;

    @Column(precision = 20, scale = 8)
    private BigDecimal shortFee;

    @Column(precision = 20, scale = 8)
    private BigDecimal profitAmount;

    @Column(precision = 20, scale = 8)
    private BigDecimal profitRate;

    public KimchiTradeHistory() {
    }

    public KimchiTradeHistory(Long id,
                              String userId,
                              String tradeId,
                              LocalDateTime timestamp,
                              KimchiTradeStatus status,
                              CurrencyType currencyType,
                              Double kimchiPremium) {
        this.id = id;
        this.userId = userId;
        this.tradeId = tradeId;
        this.timestamp = timestamp;
        this.status = status;
        this.currencyType = currencyType;
        this.kimchiPremium = kimchiPremium;
    }

    public KimchiTradeHistory(Long id,
                              String userId,
                              String tradeId,
                              LocalDateTime timestamp,
                              KimchiTradeStatus status,
                              CurrencyType currencyType,
                              Double kimchiPremium,
                              BigDecimal buyQuantity,
                              BigDecimal buyAvgPrice,
                              BigDecimal buyFee,
                              BigDecimal shortQuantity,
                              BigDecimal shortAvgPrice,
                              BigDecimal shortFee,
                              BigDecimal profitAmount,
                              BigDecimal profitRate) {
        this.id = id;
        this.userId = userId;
        this.tradeId = tradeId;
        this.timestamp = timestamp;
        this.status = status;
        this.currencyType = currencyType;
        this.kimchiPremium = kimchiPremium;
        this.buyQuantity = buyQuantity;
        this.buyAvgPrice = buyAvgPrice;
        this.buyFee = buyFee;
        this.shortQuantity = shortQuantity;
        this.shortAvgPrice = shortAvgPrice;
        this.shortFee = shortFee;
        this.profitAmount = profitAmount;
        this.profitRate = profitRate;
    }
}
