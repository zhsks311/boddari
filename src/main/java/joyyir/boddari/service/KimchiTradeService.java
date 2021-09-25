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
import joyyir.boddari.domain.kimchi.TradeResult;
import joyyir.boddari.domain.kimchi.strategy.BuyStrategy;
import joyyir.boddari.domain.kimchi.strategy.DummyBuyStrategy;
import joyyir.boddari.domain.kimchi.strategy.DummySellStrategy;
import joyyir.boddari.domain.kimchi.strategy.SellStrategy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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

    @Transactional
    public void kimchiTrade(String userId, BigDecimal upbitBuyLimitKrw) {
        KimchiTradeUser user = findUser(userId);
        List<KimchiTradeHistory> tradeHistory = findTradeHistory(user.getUserId(), user.getCurrentTradeId());

        try {
            KimchiTradeHistory firstHistory = tradeHistory.get(tradeHistory.size() - 1);
            KimchiTradeHistory lastHistory = tradeHistory.get(0);
            if (lastHistory.getStatus() == KimchiTradeStatus.ERROR) {
                log.info("마지막 트레이드에서 오류가 발생했습니다. 오류를 확인하세요.");
                return;
            }
            if (lastHistory.getStatus() == KimchiTradeStatus.FINISHED) {
                firstHistory = lastHistory = startNewTrade(userId);
            }
            log.info("[jyjang] " + firstHistory.getTimestamp() + "에 시작된 trade의(tradeId: " + user.getCurrentTradeId() + ") 현재 상태: " + lastHistory.getStatus().name());
            if (lastHistory.getStatus() == KimchiTradeStatus.WAITING) {
                checkBuyTimingAndTrade(userId, lastHistory.getTradeId(), upbitBuyLimitKrw);
            } else if (lastHistory.getStatus() == KimchiTradeStatus.STARTED) {
                checkSellTimingAndTrade(userId, lastHistory);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            kimchiTradeHistoryRepository.save(new KimchiTradeHistory(null,
                                                                     userId,
                                                                     user.getCurrentTradeId(),
                                                                     LocalDateTime.now(),
                                                                     KimchiTradeStatus.ERROR,
                                                                     null,
                                                                     null));
        }
    }

    private KimchiTradeUser findUser(String userId) {
        return kimchiTradeUserRepository.findById(userId)
                                        .orElseGet(() -> registerNewUser(userId));
    }

    private List<KimchiTradeHistory> findTradeHistory(String userId, String tradeId) {
        return kimchiTradeHistoryRepository.findAllByUserIdAndTradeIdOrderByTimestampDesc(userId, tradeId);
    }

    private KimchiTradeHistory startNewTrade(String userId) {
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

    private void checkBuyTimingAndTrade(String userId, String tradeId, BigDecimal upbitBuyLimitKrw) {
        BuyStrategy buyStrategy = findBuyStrategy();
        TradeDecision decision = buyStrategy.decide();
        if (decision.isTrade()) {
            log.info("[jyjang] 조건이 충족되어 김프 거래를 시작합니다. {}", decision);
            TradeResult tradeResult = kimchiTradeBuy(decision.getCurrencyType(), upbitBuyLimitKrw);
            kimchiTradeHistoryRepository.save(new KimchiTradeHistory(null,
                                                                     userId,
                                                                     tradeId,
                                                                     LocalDateTime.now(),
                                                                     KimchiTradeStatus.STARTED,
                                                                     decision.getCurrencyType(),
                                                                     decision.getKimchiPremium().doubleValue(),
                                                                     tradeResult.getBuyQuantity(),
                                                                     tradeResult.getShortQuantity()));
        }
    }

    private void checkSellTimingAndTrade(String userId, KimchiTradeHistory lastHistory) {
        SellStrategy sellStrategy = findSellStrategy(lastHistory);
        TradeDecision decision = sellStrategy.decide();
        if (decision.isTrade()) {
            log.info("[jyjang] 조건이 충족되어 김프 거래를 마무리합니다. {}", decision);
            String tradeId = lastHistory.getTradeId();
            TradeResult tradeResult = kimchiTradeSell(decision.getCurrencyType(),
                                                      lastHistory.getBuyQuantity(),
                                                      lastHistory.getShortQuantity());
            kimchiTradeHistoryRepository.save(new KimchiTradeHistory(null,
                                                                     userId,
                                                                     tradeId,
                                                                     LocalDateTime.now(),
                                                                     KimchiTradeStatus.FINISHED,
                                                                     decision.getCurrencyType(),
                                                                     decision.getKimchiPremium().doubleValue(),
                                                                     tradeResult.getBuyQuantity(),
                                                                     tradeResult.getShortQuantity()));
        }
    }

    private TradeResult kimchiTradeBuy(CurrencyType currencyType, BigDecimal upbitBuyLimitKrw) {
        MarketType upbitMarket = CurrencyTypeConverter.toMarketType(currencyType, CurrencyType.KRW);
        String upbitOrderId = upbitTradeRepository.marketBuy(upbitMarket, upbitBuyLimitKrw);
        OrderDetail upbitOrderDetail = null;
        for (int i = 0; i < 5; i++) {
            upbitOrderDetail = upbitOrderRepository.getOrderDetail(null, upbitOrderId);
            if (upbitOrderDetail.getOrderStatus() == OrderStatus.COMPLETED) {
                log.info("[jyjang] 업비트 시장가 매수 완료. {} 마켓에서 {}개 매수", upbitMarket.name(), upbitOrderDetail.getOrderQty());
                break;
            }
            if (i == 4) {
                throw new RuntimeException("업비트 시장가 매수가 5초 이내에 완료되지 않음. 업비트 앱에서 확인 필요");
            }
            sleep(1000);
        }
        MarketType binanceMarket = CurrencyTypeConverter.toMarketType(currencyType, CurrencyType.USDT);
        String binanceOrderId = binanceFutureTradeRepository.marketShort(binanceMarket, upbitOrderDetail.getOrderQty());
        OrderDetail binanceOrderDetail = null;
        for (int i = 0; i < 5; i++) {
            binanceOrderDetail = binanceFutureOrderRepository.getOrderDetail(binanceMarket, binanceOrderId);
            if (binanceOrderDetail.getOrderStatus() == OrderStatus.COMPLETED) {
                log.info("[jyjang] 바이낸스 시장가 숏 완료. {} 마켓에서 {}개 매도", binanceMarket.name(), binanceOrderDetail.getOrderQty());
                break;
            }
            if (i == 4) {
                throw new RuntimeException("바이낸스 시장가 숏이 5초 이내에 완료되지 않음. 바이낸스 앱에서 확인 필요");
            }
            sleep(1000);
        }
        return new TradeResult(upbitOrderDetail.getOrderQty(), binanceOrderDetail.getOrderQty());
    }

    private TradeResult kimchiTradeSell(CurrencyType currencyType, BigDecimal buyQuantity, BigDecimal shortQuantity) {
        MarketType upbitMarket = CurrencyTypeConverter.toMarketType(currencyType, CurrencyType.KRW);
        String upbitOrderId = upbitTradeRepository.marketSell(upbitMarket, buyQuantity);
        OrderDetail upbitOrderDetail = null;
        for (int i = 0; i < 5; i++) {
            upbitOrderDetail = upbitOrderRepository.getOrderDetail(null, upbitOrderId);
            if (upbitOrderDetail.getOrderStatus() == OrderStatus.COMPLETED) {
                log.info("[jyjang] 업비트 시장가 매도 완료. {} 마켓에서 {}개 매도", upbitMarket.name(), upbitOrderDetail.getOrderQty());
                break;
            }
            if (i == 4) {
                throw new RuntimeException("업비트 시장가 매도가 5초 이내에 완료되지 않음. 업비트 앱에서 확인 필요");
            }
            sleep(1000);
        }
        MarketType binanceMarket = CurrencyTypeConverter.toMarketType(currencyType, CurrencyType.USDT);
        String binanceOrderId = binanceFutureTradeRepository.marketLong(binanceMarket, shortQuantity);
        OrderDetail binanceOrderDetail = null;
        for (int i = 0; i < 5; i++) {
            binanceOrderDetail = binanceFutureOrderRepository.getOrderDetail(binanceMarket, binanceOrderId);
            if (binanceOrderDetail.getOrderStatus() == OrderStatus.COMPLETED) {
                log.info("[jyjang] 바이낸스 시장가 롱 완료. {} 마켓에서 {}개 매수", binanceMarket.name(), binanceOrderDetail.getOrderQty());
                break;
            }
            if (i == 4) {
                throw new RuntimeException("바이낸스 시장가 롱이 5초 이내에 완료되지 않음. 바이낸스 앱에서 확인 필요");
            }
            sleep(1000);
        }
        return new TradeResult(upbitOrderDetail.getOrderQty(), binanceOrderDetail.getOrderQty());
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

    private BuyStrategy findBuyStrategy() {
        // TODO : jyjang - develop
        return new DummyBuyStrategy(kimchiPremiumService);
    }

    private SellStrategy findSellStrategy(KimchiTradeHistory lastHistory) {
        // TODO : jyjang - develop
        return new DummySellStrategy(kimchiPremiumService, lastHistory);
    }
}

