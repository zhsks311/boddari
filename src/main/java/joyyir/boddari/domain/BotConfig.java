package joyyir.boddari.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "bot_config")
public class BotConfig {
    @Id
    private Long chatId;
    private Double minVolumeMultiplier;
    private Double maxVolumeMultiplier;
    private Double minPriceMultiplier;
    private Double maxPriceMultiplier;

    public BotConfig() {}

    public BotConfig(Long chatId,
                     Double minVolumeMultiplier,
                     Double maxVolumeMultiplier,
                     Double minPriceMultiplier,
                     Double maxPriceMultiplier) {
        this.chatId = chatId;
        this.minVolumeMultiplier = minVolumeMultiplier;
        this.maxVolumeMultiplier = maxVolumeMultiplier;
        this.minPriceMultiplier = minPriceMultiplier;
        this.maxPriceMultiplier = maxPriceMultiplier;
    }
}
