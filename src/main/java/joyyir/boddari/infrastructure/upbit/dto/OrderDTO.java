package joyyir.boddari.infrastructure.upbit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OrderDTO {
    @JsonProperty("uuid")
    private String uuid;

    @JsonProperty("side")
    private String side;

    @JsonProperty("ord_type")
    private String ordType;

    @JsonProperty("price")
    private String price;

    @JsonProperty("avg_price")
    private String avgPrice;

    @JsonProperty("state")
    private String state;

    @JsonProperty("market")
    private String market;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("volume")
    private String volume;

    @JsonProperty("remaining_volume")
    private String remainingVolume;

    @JsonProperty("reserved_fee")
    private String reservedFee;

    @JsonProperty("remaining_fee")
    private String remainingFee;

    @JsonProperty("paid_fee")
    private String paidFee;

    @JsonProperty("locked")
    private String locked;

    @JsonProperty("executed_volume")
    private String executedVolume;

    @JsonProperty("trades_count")
    private Integer tradesCount;
}
