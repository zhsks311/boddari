package joyyir.boddari.service;

import joyyir.boddari.domain.exchange.CurrencyType;
import joyyir.boddari.domain.exchange.FutureTradeRepository;
import joyyir.boddari.domain.exchange.MarketType;
import joyyir.boddari.domain.exchange.OrderDetail;
import joyyir.boddari.domain.exchange.OrderRepository;
import joyyir.boddari.domain.exchange.OrderStatus;
import joyyir.boddari.domain.exchange.PriceRepository;
import joyyir.boddari.domain.exchange.TradeRepository;
import joyyir.boddari.domain.exchange.UsdPriceRepository;
import joyyir.boddari.domain.kimchi.KimchiTradeHistory;
import joyyir.boddari.domain.kimchi.KimchiTradeProfit;
import joyyir.boddari.domain.kimchi.KimchiTradeStatus;
import joyyir.boddari.domain.kimchi.KimchiTradeUser;
import joyyir.boddari.domain.kimchi.TradeDecision;
import joyyir.boddari.domain.kimchi.TradeResult;
import joyyir.boddari.domain.kimchi.TradeStatus;
import joyyir.boddari.domain.kimchi.strategy.TradeStrategy;
import joyyir.boddari.domain.kimchi.strategy.TradeStrategyFactory;
import joyyir.boddari.interfaces.handler.BoddariBotHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class KimchiTradeService {
    private final KimchiTradeUserService kimchiTradeUserService;
    private final KimchiTradeHistoryService tradeHistoryService;
    private final TradeStrategyFactory tradeStrategyFactory;
    private final TradeRepository upbitTradeRepository;
    private final OrderRepository upbitOrderRepository;
    private final OrderRepository binanceFutureOrderRepository;
    private final FutureTradeRepository binanceFutureTradeRepository;
    private final UsdPriceRepository usdPriceRepository;
    private final PriceRepository binanceFuturePriceRepository;

    @Transactional
    public void kimchiTrade(KimchiTradeUser user, BoddariBotHandler botHandler) {
        if (user == null || user.getTradeStatus() != TradeStatus.START) {
            return;
        }
        final String userId = user.getUserId();

        try {
            TradeStrategy tradeStrategy = tradeStrategyFactory.create(user.getTradeStrategy());
            List<KimchiTradeHistory> tradeHistory = tradeHistoryService.findTradeHistory(userId, user.getCurrentTradeId());

            KimchiTradeHistory firstHistory = tradeHistory.get(tradeHistory.size() - 1);
            KimchiTradeHistory lastHistory = tradeHistory.get(0);
            if (lastHistory.getStatus() == KimchiTradeStatus.ERROR) {
//                log.info("[jyjang] ????????? ?????????????????? ????????? ??????????????????. ????????? ???????????????.");
                return;
            }
            if (lastHistory.getStatus() == KimchiTradeStatus.FINISHED) {
                KimchiTradeHistory startedTradeHistory = kimchiTradeUserService.startNewTrade(userId)
                                                                               .getTradeHistory();
                firstHistory = lastHistory = startedTradeHistory;
            }
//            log.info("[jyjang] " + firstHistory.getTimestamp() + "??? ????????? trade???(tradeId: " + user.getCurrentTradeId() + ") ?????? ??????: " + lastHistory.getStatus().name());
            if (lastHistory.getStatus() == KimchiTradeStatus.WAITING) {
                checkBuyTimingAndTrade(user, tradeStrategy, lastHistory.getTradeId(), botHandler);
            } else if (lastHistory.getStatus() == KimchiTradeStatus.STARTED) {
                checkSellTimingAndTrade(user, tradeStrategy, lastHistory, botHandler);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tradeHistoryService.saveNewHistory(userId, user.getCurrentTradeId(), KimchiTradeStatus.ERROR);
            botHandler.sendMessage(Long.valueOf(userId), "????????? ??????????????????! ?????? ?????? ?????? trade??? STOP?????? ?????? START ????????????.\n?????? ??????: " + e.getMessage());
        }
    }

    private void checkBuyTimingAndTrade(KimchiTradeUser user,
                                        TradeStrategy tradeStrategy,
                                        String tradeId,
                                        BoddariBotHandler botHandler) {
        TradeDecision decision = tradeStrategy.decideBuy();
        if (decision.isTrade()) {
//            log.info("[jyjang] ????????? ???????????? ?????? ????????? ???????????????. {}", decision);
            tradeBuy(decision, user, tradeId, botHandler);
        }
    }

    private void tradeBuy(TradeDecision decision, KimchiTradeUser user, String tradeId, BoddariBotHandler botHandler) {
        TradeResult tradeResult = kimchiTradeBuy(decision.getCurrencyType(), new BigDecimal(user.getKrwLimit()), user);
        KimchiTradeHistory buyHistory = tradeHistoryService.saveNewHistory(user.getUserId(),
                                                                           tradeId,
                                                                           KimchiTradeStatus.STARTED,
                                                                           decision.getCurrencyType(),
                                                                           tradeResult,
                                                                           null);
        botHandler.sendMessage(Long.valueOf(user.getUserId()), buyHistory.buyDescription());
    }

    private void checkSellTimingAndTrade(KimchiTradeUser user,
                                         TradeStrategy tradeStrategy,
                                         KimchiTradeHistory buyHistory,
                                         BoddariBotHandler botHandler) {
        TradeDecision decision = tradeStrategy.decideSell(buyHistory);
        if (decision.isTrade()) {
//            log.info("[jyjang] ????????? ???????????? ?????? ????????? ??????????????????. {}", decision);
            tradeSell(user, buyHistory, botHandler);
        }
    }

    public void tradeSell(KimchiTradeUser user,
                          KimchiTradeHistory buyHistory,
                          BoddariBotHandler botHandler) {
        String tradeId = buyHistory.getTradeId();
        TradeResult tradeResult = kimchiTradeSell(buyHistory.getCurrencyType(),
                                                  buyHistory.getBuyQuantity(),
                                                  buyHistory.getShortQuantity(),
                                                  user);
        KimchiTradeProfit profit = calculateProfit(tradeResult, buyHistory, usdPriceRepository.getUsdPriceKrw());
        KimchiTradeHistory sellHistory = tradeHistoryService.saveNewHistory(user.getUserId(),
                                                                            tradeId,
                                                                            KimchiTradeStatus.FINISHED,
                                                                            buyHistory.getCurrencyType(),
                                                                            tradeResult,
                                                                            profit);
        botHandler.sendMessage(Long.valueOf(user.getUserId()), sellHistory.sellDescription());
    }

    KimchiTradeProfit calculateProfit(TradeResult tradeResult, KimchiTradeHistory buyHistory, BigDecimal usdPriceKrw) {
        if (buyHistory == null) {
            return null;
        }
        OrderDetail upbitSellOrderDetail = tradeResult.getBuyOrderDetail();
        OrderDetail binanceLongOrderDetail = tradeResult.getShortOrderDetail();
        BigDecimal upbitBuyCost = buyHistory.getBuyAvgPrice()
                                            .multiply(buyHistory.getBuyQuantity())
                                            .add(buyHistory.getBuyFee());
        BigDecimal upbitSellProfit = upbitSellOrderDetail.getAveragePrice()
                                                         .multiply(upbitSellOrderDetail.getOrderQty())
                                                         .subtract(upbitSellOrderDetail.getFee());
        BigDecimal binanceShortCost = buyHistory.getShortAvgPrice()
                                                .multiply(buyHistory.getShortQuantity())
                                                .add(buyHistory.getShortFee());
        BigDecimal binanceLongProfit = binanceLongOrderDetail.getAveragePrice()
                                                             .multiply(binanceLongOrderDetail.getOrderQty())
                                                             .subtract(binanceLongOrderDetail.getFee());
        BigDecimal profitAmount = upbitSellProfit.subtract(upbitBuyCost)
                                                 .add(binanceShortCost.subtract(binanceLongProfit)
                                                                      .multiply(usdPriceKrw))
                                                 .setScale(0, RoundingMode.FLOOR);
        BigDecimal profitRate = profitAmount.multiply(new BigDecimal(100))
                                            .divide(upbitBuyCost.add(binanceShortCost.multiply(usdPriceKrw)), 2, RoundingMode.HALF_UP);
        return new KimchiTradeProfit(profitAmount, profitRate);
    }

    private TradeResult kimchiTradeBuy(CurrencyType currencyType, BigDecimal upbitBuyLimitKrw, KimchiTradeUser user) {
        MarketType upbitMarket = currencyType.getKrwMarket();
        String upbitOrderId = upbitTradeRepository.marketBuy(upbitMarket, upbitBuyLimitKrw, user.getUpbitAccessKey(), user.getUpbitSecretKey());
        OrderDetail upbitOrderDetail = null;
        for (int i = 0; i < 5; i++) {
            upbitOrderDetail = upbitOrderRepository.getOrderDetail(null, upbitOrderId, user.getUpbitAccessKey(), user.getUpbitSecretKey());
            if (upbitOrderDetail.getOrderStatus() == OrderStatus.COMPLETED) {
//                log.info("[jyjang] ????????? ????????? ?????? ??????. {} ???????????? {}??? ??????", upbitMarket.name(), upbitOrderDetail.getOrderQty());
                break;
            }
            if (i == 4) {
                throw new RuntimeException("????????? ????????? ????????? 5??? ????????? ???????????? ??????. ????????? ????????? ?????? ??????");
            }
            sleep(1000);
        }
        MarketType binanceMarket = currencyType.getUsdtMarket();
        BigDecimal currentPrice = binanceFuturePriceRepository.getCurrentPrice(binanceMarket);
        BigDecimal usdPriceKrw = usdPriceRepository.getUsdPriceKrw();
        BigDecimal orderQuantity = upbitBuyLimitKrw.divide(usdPriceKrw, 8, RoundingMode.FLOOR)
                                                   .divide(currentPrice, 8, RoundingMode.FLOOR);
        boolean isSuccessChangeMarginType = binanceFutureTradeRepository.changeMarginType(binanceMarket,
                                                                                          "ISOLATED",
                                                                                          user.getBinanceAccessKey(),
                                                                                          user.getBinanceSecretKey());
        if (!isSuccessChangeMarginType) {
            throw new RuntimeException("?????? ????????? '??????'??? ??????????????? ??????????????????. ???????????? ????????? ???????????????.");
        }
        Integer leverage = ObjectUtils.firstNonNull(user.getLeverage(), 1);
        boolean isSuccessChangeLeverage = binanceFutureTradeRepository.changeInitialLeverage(binanceMarket,
                                                                                             leverage,
                                                                                             user.getBinanceAccessKey(),
                                                                                             user.getBinanceSecretKey());
        if (!isSuccessChangeLeverage) {
            throw new RuntimeException("??????????????? " + leverage + "?????? ??????????????? ??????????????????. ???????????? ????????? ???????????????.");
        }
        String binanceOrderId = binanceFutureTradeRepository.marketShort(binanceMarket, orderQuantity, user.getBinanceAccessKey(), user.getBinanceSecretKey());
        OrderDetail binanceOrderDetail = null;
        for (int i = 0; i < 5; i++) {
            binanceOrderDetail = binanceFutureOrderRepository.getOrderDetail(binanceMarket, binanceOrderId, user.getBinanceAccessKey(), user.getBinanceSecretKey());
            if (binanceOrderDetail.getOrderStatus() == OrderStatus.COMPLETED) {
//                log.info("[jyjang] ???????????? ????????? ??? ??????. {} ???????????? {}??? ??????", binanceMarket.name(), binanceOrderDetail.getOrderQty());
                break;
            }
            if (i == 4) {
                throw new RuntimeException("???????????? ????????? ?????? 5??? ????????? ???????????? ??????. ???????????? ????????? ?????? ??????");
            }
            sleep(1000);
        }
        return new TradeResult(upbitOrderDetail, binanceOrderDetail, usdPriceKrw);
    }

    private TradeResult kimchiTradeSell(CurrencyType currencyType, BigDecimal buyQuantity, BigDecimal shortQuantity, KimchiTradeUser user) {
        MarketType upbitMarket = currencyType.getKrwMarket();
        String upbitOrderId = upbitTradeRepository.marketSell(upbitMarket, buyQuantity, user.getUpbitAccessKey(), user.getUpbitSecretKey());
        OrderDetail upbitOrderDetail = null;
        for (int i = 0; i < 5; i++) {
            upbitOrderDetail = upbitOrderRepository.getOrderDetail(null, upbitOrderId, user.getUpbitAccessKey(), user.getUpbitSecretKey());
            if (upbitOrderDetail.getOrderStatus() == OrderStatus.COMPLETED) {
//                log.info("[jyjang] ????????? ????????? ?????? ??????. {} ???????????? {}??? ??????", upbitMarket.name(), upbitOrderDetail.getOrderQty());
                break;
            }
            if (i == 4) {
                throw new RuntimeException("????????? ????????? ????????? 5??? ????????? ???????????? ??????. ????????? ????????? ?????? ??????");
            }
            sleep(1000);
        }
        MarketType binanceMarket = currencyType.getUsdtMarket();
        String binanceOrderId = binanceFutureTradeRepository.marketLong(binanceMarket, shortQuantity, user.getBinanceAccessKey(), user.getBinanceSecretKey());
        OrderDetail binanceOrderDetail = null;
        for (int i = 0; i < 5; i++) {
            binanceOrderDetail = binanceFutureOrderRepository.getOrderDetail(binanceMarket, binanceOrderId, user.getBinanceAccessKey(), user.getBinanceSecretKey());
            if (binanceOrderDetail.getOrderStatus() == OrderStatus.COMPLETED) {
//                log.info("[jyjang] ???????????? ????????? ??? ??????. {} ???????????? {}??? ??????", binanceMarket.name(), binanceOrderDetail.getOrderQty());
                break;
            }
            if (i == 4) {
                throw new RuntimeException("???????????? ????????? ?????? 5??? ????????? ???????????? ??????. ???????????? ????????? ?????? ??????");
            }
            sleep(1000);
        }
        return new TradeResult(upbitOrderDetail, binanceOrderDetail, usdPriceRepository.getUsdPriceKrw());
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }
}

