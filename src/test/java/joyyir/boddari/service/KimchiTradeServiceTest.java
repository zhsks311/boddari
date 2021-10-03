package joyyir.boddari.service;

import joyyir.boddari.domain.exchange.OrderDetail;
import joyyir.boddari.domain.exchange.OrderStatus;
import joyyir.boddari.domain.kimchi.KimchiTradeHistory;
import joyyir.boddari.domain.kimchi.KimchiTradeProfit;
import joyyir.boddari.domain.kimchi.TradeResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KimchiTradeServiceTest {
    private KimchiTradeService service;

    @BeforeEach
    void setUp() {
        this.service = new KimchiTradeService(null, null, null, null, null, null, null, null, null);
    }

    @Test
    void calculateProfit() {
        TradeResult tradeResult = new TradeResult(new OrderDetail(OrderStatus.COMPLETED,
                                                                  new BigDecimal("440.52863436"),
                                                                  new BigDecimal("1115"),
                                                                  new BigDecimal("250")), // dummy
                                                  new OrderDetail(OrderStatus.COMPLETED,
                                                                  new BigDecimal("440.5"),
                                                                  new BigDecimal("0.9024"),
                                                                  new BigDecimal("0.15900287")),
                                                  null);
        KimchiTradeHistory buyHistory = new KimchiTradeHistory(null,
                                                               null,
                                                               null,
                                                               null,
                                                               null,
                                                               null,
                                                               null,
                                                               new BigDecimal("440.52863436"),
                                                               new BigDecimal("1135"),
                                                               new BigDecimal("249.99"),
                                                               new BigDecimal("440.5"),
                                                               new BigDecimal("0.9327"),
                                                               new BigDecimal("0.16434173"),
                                                               null,
                                                               null);
        KimchiTradeProfit kimchiTradeProfit = this.service.calculateProfit(tradeResult, buyHistory, new BigDecimal("1185.69"));
        assertEquals(new BigDecimal(6898), kimchiTradeProfit.getProfitAmount());
        assertEquals(new BigDecimal("0.70"), kimchiTradeProfit.getProfitRate());
    }
}