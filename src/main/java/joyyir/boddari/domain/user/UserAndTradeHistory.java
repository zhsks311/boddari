package joyyir.boddari.domain.user;

import joyyir.boddari.domain.kimchi.KimchiTradeHistory;
import joyyir.boddari.domain.kimchi.KimchiTradeUser;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserAndTradeHistory {
    private KimchiTradeUser user;
    private KimchiTradeHistory tradeHistory;
}
