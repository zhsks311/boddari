package joyyir.boddari.domain.exchange;

import java.util.List;

public interface BalanceRepository {
    List<Balance> getBalance(String accessKey, String secretKey);
}
