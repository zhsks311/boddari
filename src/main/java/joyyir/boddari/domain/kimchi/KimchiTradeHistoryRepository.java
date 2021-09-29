package joyyir.boddari.domain.kimchi;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface KimchiTradeHistoryRepository extends JpaRepository<KimchiTradeHistory, Long> {
    List<KimchiTradeHistory> findAllByUserIdAndTradeIdOrderByTimestampDesc(String userId, String tradeId);
    List<KimchiTradeHistory> findAllByUserIdAndTimestampAfter(String userId, LocalDateTime timeAfter);
}
