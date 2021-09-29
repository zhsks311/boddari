package joyyir.boddari.infrastructure.upbit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Data
public class OrderDTO {
    @JsonProperty("uuid")
    private String uuid;

    @JsonProperty("side")
    private String side;

    @JsonProperty("ord_type")
    private String ordType;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("avg_price")
    private BigDecimal avgPrice;

    @JsonProperty("state")
    private String state;

    @JsonProperty("market")
    private String market;

    @JsonProperty("created_at")
    private ZonedDateTime createdAt;

    @JsonProperty("volume")
    private BigDecimal volume;

    @JsonProperty("remaining_volume")
    private BigDecimal remainingVolume;

    @JsonProperty("reserved_fee")
    private BigDecimal reservedFee;

    @JsonProperty("remaining_fee")
    private BigDecimal remainingFee;

    @JsonProperty("paid_fee")
    private BigDecimal paidFee;

    @JsonProperty("locked")
    private BigDecimal locked;

    @JsonProperty("executed_volume")
    private BigDecimal executedVolume;

    @JsonProperty("trades_count")
    private Integer tradesCount;

    @JsonProperty("trades")
    private List<TradeDTO> trades;
}
