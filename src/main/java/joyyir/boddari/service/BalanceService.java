package joyyir.boddari.service;

import joyyir.boddari.domain.exchange.Balance;
import joyyir.boddari.domain.exchange.BalanceRepository;
import joyyir.boddari.domain.exchange.CurrencyType;
import joyyir.boddari.domain.exchange.UsdPriceRepository;
import joyyir.boddari.domain.kimchi.KimchiTradeUser;
import joyyir.boddari.interfaces.exception.BadRequestException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@AllArgsConstructor
public class BalanceService {
    private final BalanceRepository upbitBalanceRepository;
    private final BalanceRepository binanceFutureBalanceRepository;
    private final UsdPriceRepository usdPriceRepository;

    public BigDecimal getUpbitKrwAvailBalance(String upbitAccessKey, String upbitSecretKey) {
        return upbitBalanceRepository.getBalance(upbitAccessKey, upbitSecretKey)
                                     .stream()
                                     .filter(x -> CurrencyType.KRW.name().equals(x.getAsset()))
                                     .findFirst()
                                     .map(Balance::getAvailableBalance)
                                     .orElse(new BigDecimal(0))
                                     .setScale(0, RoundingMode.FLOOR);
    }

    public BigDecimal getBinanceUsdtAvailBalance(String binanceAccessKey, String binanceSecretKey) {
        return binanceFutureBalanceRepository.getBalance(binanceAccessKey, binanceSecretKey)
                                             .stream()
                                             .filter(x -> CurrencyType.USDT.name().equals(x.getAsset()))
                                             .findFirst()
                                             .map(Balance::getAvailableBalance)
                                             .orElse(new BigDecimal(0))
                                             .setScale(4, RoundingMode.FLOOR);
    }

    public boolean checkSufficientBalance(KimchiTradeUser user) throws BadRequestException {
        int krwLimit = user.getKrwLimit();
        BigDecimal usdPriceKrw = usdPriceRepository.getUsdPriceKrw();
        BigDecimal upbitKrwAvailBalance = getUpbitKrwAvailBalance(user.getUpbitAccessKey(), user.getUpbitSecretKey());
        BigDecimal binanceUsdtAvailBalance = getBinanceUsdtAvailBalance(user.getBinanceAccessKey(), user.getBinanceSecretKey());
        BigDecimal binanceKrwAvailBalance = binanceUsdtAvailBalance.multiply(usdPriceKrw).setScale(0, RoundingMode.FLOOR);
        String errorMessage = "";
        if (upbitKrwAvailBalance.longValue() < krwLimit) {
            errorMessage += "업비트 KRW 잔고가 부족합니다.\n" +
                "설정된 금액 limit: " + krwLimit + "원" +
                ", 업비트 이용 가능한 KRW 잔고: " + upbitKrwAvailBalance.longValue();
            errorMessage += "\n\n";
        }
        Integer leverage = user.getLeverage();
        int binanceKrwNeededBalance = krwLimit / leverage;
        if (binanceKrwAvailBalance.longValue() < binanceKrwNeededBalance) {
            errorMessage += "바이낸스 USDT 잔고 부족합니다.\n" +
                "설정된 금액 limit: " + krwLimit + "원" +
                ", 레버리지: " + leverage + "배" +
                ", 필요한 USDT: " + new BigDecimal(binanceKrwNeededBalance).divide(usdPriceKrw, 4, RoundingMode.UP) + "달러 (약 " + binanceKrwNeededBalance + "원)" +
                ", 바이낸스 이용 가능한 USDT 잔고: " + binanceUsdtAvailBalance + "달러 (약 " + binanceKrwAvailBalance + "원)";
        }
        if (errorMessage.length() > 0) {
            throw new BadRequestException(errorMessage);
        }
        return true;
    }
}
