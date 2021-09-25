package joyyir.boddari.service;

import joyyir.boddari.domain.exchange.CurrencyType;
import joyyir.boddari.domain.exchange.CurrencyTypeConverter;
import joyyir.boddari.domain.exchange.FutureTradeRepository;
import joyyir.boddari.domain.exchange.MarketType;
import joyyir.boddari.domain.exchange.OrderDetail;
import joyyir.boddari.domain.exchange.OrderRepository;
import joyyir.boddari.domain.exchange.OrderStatus;
import joyyir.boddari.domain.exchange.TradeRepository;
import joyyir.boddari.domain.kimchi.KimchiTradeHistory;
import joyyir.boddari.domain.kimchi.KimchiTradeHistoryRepository;
import joyyir.boddari.domain.kimchi.KimchiTradeStatus;
import joyyir.boddari.domain.kimchi.KimchiTradeUser;
import joyyir.boddari.domain.kimchi.KimchiTradeUserRepository;
import joyyir.boddari.domain.kimchi.TradeDecision;
import joyyir.boddari.domain.kimchi.strategy.BuyStrategy;
import joyyir.boddari.domain.kimchi.strategy.DummyBuyStrategy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class KimchiTradeService {
    private final KimchiTradeUserRepository kimchiTradeUserRepository;
    private final KimchiTradeHistoryRepository kimchiTradeHistoryRepository;
    private final KimchiPremiumService kimchiPremiumService;
    private final TradeRepository upbitTradeRepository;
    private final OrderRepository upbitOrderRepository;
    private final OrderRepository binanceFutureOrderRepository;
    private final FutureTradeRepository binanceFutureTradeRepository;

    public KimchiTradeUser findUser(String userId) {
        return kimchiTradeUserRepository.findById(userId)
                                        .orElseGet(() -> registerNewUser(userId));
    }

    public List<KimchiTradeHistory> findTradeHistory(String userId, String tradeId) {
        return kimchiTradeHistoryRepository.findAllByUserIdAndTradeIdOrderByTimestampDesc(userId, tradeId);
    }

    public KimchiTradeHistory startNewTrade(String userId) {
        String newTradeId = UUID.randomUUID().toString();
        kimchiTradeUserRepository.save(new KimchiTradeUser(userId, newTradeId));
        KimchiTradeHistory tradeHistory = new KimchiTradeHistory(null, userId, newTradeId, LocalDateTime.now(), KimchiTradeStatus.WAITING, null, null);
        kimchiTradeHistoryRepository.save(tradeHistory);
        return tradeHistory;
    }

    private KimchiTradeUser registerNewUser(String userId) {
        String newTradeId = UUID.randomUUID().toString();
        KimchiTradeUser user = new KimchiTradeUser(userId, newTradeId);
        kimchiTradeUserRepository.save(user);
        kimchiTradeHistoryRepository.save(new KimchiTradeHistory(null, userId, newTradeId, LocalDateTime.now(), KimchiTradeStatus.WAITING, null, null));
        return user;
    }

    public void checkBuyTimingAndTrade(String userId, String tradeId, BigDecimal upbitBuyLimitKrw) {
        BuyStrategy buyStrategy = findBuyStrategy(userId);
        TradeDecision decision = buyStrategy.decide();
        if (decision.isTrade()) {
            log.info("조건이 충족되어 김프 거래를 시작합니다. {}", decision);
            kimchiTradeBuy(decision.getCurrencyType(), upbitBuyLimitKrw, userId, tradeId);
            kimchiTradeHistoryRepository.save(new KimchiTradeHistory(null, userId, tradeId, LocalDateTime.now(), KimchiTradeStatus.STARTED, decision.getCurrencyType(), decision.getKimchiPremium().doubleValue()));
        }
    }

    private void kimchiTradeBuy(CurrencyType currencyType, BigDecimal upbitBuyLimitKrw, String userId, String tradeId) {
        MarketType upbitMarket = CurrencyTypeConverter.toMarketType(currencyType, CurrencyType.KRW);
        MarketType binanceMarket = CurrencyTypeConverter.toMarketType(currencyType, CurrencyType.USDT);
        String upbitOrderId = upbitTradeRepository.marketBuy(upbitMarket, upbitBuyLimitKrw);
        OrderDetail upbitOrderDetail = null;
        for (int i = 0; i < 5; i++) {
            upbitOrderDetail = upbitOrderRepository.getOrderDetail(null, upbitOrderId);
            if (upbitOrderDetail.getOrderStatus() == OrderStatus.COMPLETED) {
                log.info("업비트 시장가 매수 완료. {} 마켓에서 {}개 매수", upbitMarket.name(), upbitOrderDetail.getOrderQty());
                break;
            }
            if (i == 4) {
                throw new RuntimeException("업비트 시장가 매수가 5초 이내에 완료되지 않음. 업비트 앱에서 확인 필요");
            }
            sleep(1000);
        }
        String binanceOrderId = binanceFutureTradeRepository.marketShort(binanceMarket, upbitOrderDetail.getOrderQty());
        OrderDetail binanceOrderDetail = null;
        for (int i = 0; i < 5; i++) {
            binanceOrderDetail = binanceFutureOrderRepository.getOrderDetail(binanceMarket, binanceOrderId);
            if (binanceOrderDetail.getOrderStatus() == OrderStatus.COMPLETED) {
                log.info("바이낸스 시장가 숏 완료. {} 마켓에서 {}개 매수", binanceMarket.name(), binanceOrderDetail.getOrderQty());
                break;
            }
            if (i == 4) {
                throw new RuntimeException("바이낸스 시장가 숏이 5초 이내에 완료되지 않음. 바이낸스 앱에서 확인 필요");
            }
            sleep(1000);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

    private BuyStrategy findBuyStrategy(String userId) {
        // TODO : jyjang - develop
        return new DummyBuyStrategy(kimchiPremiumService);
    }

    public void checkSellTimingAndTrade(String userId) {
        // TODO : jyjang - develop
    }
}

