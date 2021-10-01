package joyyir.boddari.domain.kimchi;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KimchiTradeUserRepository extends JpaRepository<KimchiTradeUser, String> {
    List<KimchiTradeUser> findAllByTradeStatus(TradeStatus tradeStatus); // TODO : jyjang - 유저 많아지면 인덱스 필요
}
