package joyyir.boddari.interfaces.controller;

import joyyir.boddari.domain.exchange.FutureTradeRepository;
import joyyir.boddari.domain.kimchi.KimchiTradeUser;
import joyyir.boddari.domain.kimchi.strategy.TradeStrategy;
import joyyir.boddari.domain.kimchi.strategy.TradeStrategyFactory;
import joyyir.boddari.domain.kimchi.strategy.TradeStrategyFactoryException;
import joyyir.boddari.interfaces.exception.BadRequestException;
import joyyir.boddari.interfaces.handler.BoddariBotHandler;
import joyyir.boddari.service.BalanceService;
import joyyir.boddari.service.KimchiTradeUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Slf4j
@RestController
@AllArgsConstructor
public class UserController implements TelegramCommandController {
    private final KimchiTradeUserService userService;
    private final TradeStrategyFactory tradeStrategyFactory;
    private final BalanceService balanceService;
    private final FutureTradeRepository binanceFutureTradeRepository;

    @Override
    public void runCommand(Long chatId, String[] commands, BoddariBotHandler botHandler) throws BadRequestException {
        String userId = String.valueOf(chatId);
        if (commands.length < 2) {
            String helpMessage =
                "/user register {업비트 access key} {업비트 secret key} {바이낸스 access key} {바이낸스 secret key} : 유저 등록\n" +
                "/user unregister : 유저 정보 제거\n" +
                "/user info : 유저 정보 조회\n" +
                "/user set krw-limit {금액} : 김프 거래를 위한 업비트 KRW 금액 변경\n" +
                "/user set trade-strategy {설정값} : 김프 트레이딩 전략 설정\n" +
                "/user set leverage {배수} : 바이낸스 선물 숏 거래 시에 레버리지 설정";
            botHandler.sendMessage(chatId, helpMessage);
            return;
        }
        switch (commands[1]) {
            case "register":
                register(botHandler, chatId, userId, commands);
                break;
            case "unregister":
                unregister(botHandler, chatId, userId);
                break;
            case "info":
                info(botHandler, chatId, userId);
                break;
            case "set":
                String helpMessage = "/user set krw-limit {금액} : 김프 거래를 위한 업비트 KRW 금액 변경\n" +
                                     "/user set trade-strategy {설정값} : 김프 트레이딩 전략 설정\n" +
                                     "/user set leverage {배수} : 바이낸스 선물 숏 거래 시에 레버리지 설정";
                if (commands.length == 2) {
                    botHandler.sendMessage(chatId, helpMessage);
                    return;
                }
                switch (commands[2]) {
                    case "krw-limit":
                        setKrwLimit(chatId, userId, commands, botHandler);
                        break;
                    case "trade-strategy":
                        setTradeStrategy(chatId, userId, commands, botHandler);
                        break;
                    case "leverage":
                        setLeverage(chatId, userId, commands, botHandler);
                        break;
                    default:
                        throw new BadRequestException("지원하지 않는 명령어입니다.\n" + helpMessage);
                }
                break;
            default:
                throw new BadRequestException("지원하지 않는 명령어입니다.");
        }
    }

    private void register(BoddariBotHandler botHandler, Long chatId, String userId, String[] commands) throws BadRequestException {
        KimchiTradeUser user = userService.findUserById(userId);
        if (user != null) {
            throw new BadRequestException("이미 등록된 유저입니다.");
        }
        if (commands.length != 6) {
            throw new BadRequestException("잘못된 명령입니다.\n(사용법) /user register {업비트 access key} {업비트 secret key} {바이낸스 access key} {바이낸스 secret key}");
        }
        String upbitAccessKey = commands[2];
        String upbitSecretKey = commands[3];
        String binanceAccessKey = commands[4];
        String binanceSecretKey = commands[5];
        BigDecimal upbitKrwAvailBalance;
        BigDecimal binanceUsdtAvailBalance;
        try {
            upbitKrwAvailBalance = balanceService.getUpbitKrwAvailBalance(upbitAccessKey, upbitSecretKey);
            binanceUsdtAvailBalance = balanceService.getBinanceUsdtAvailBalance(binanceAccessKey, binanceSecretKey);
        } catch (Exception e) {
            throw new BadRequestException("access key와 secret key가 올바른지, API Key 권한이 적절하게 설정 되었는지, 허용시킬 IP를 올바르게 설정했는지 확인해주세요. 오류 내용: " + e.getMessage());
        }
        KimchiTradeUser newUser = userService.register(userId, upbitAccessKey, upbitSecretKey, binanceAccessKey, binanceSecretKey);
        botHandler.sendMessage(chatId, "유저 등록 성공. 환영합니다.\n" +
            "유저 ID: " + newUser.getUserId() + "\n" +
            "업비트 KRW 이용 가능 잔고: " + upbitKrwAvailBalance + "원\n" +
            "바이낸스 선물 USDT 이용 가능 잔고: " + binanceUsdtAvailBalance + "달러");
    }

    private void unregister(BoddariBotHandler botHandler, Long chatId, String userId) throws BadRequestException {
        userService.delete(userId);
        botHandler.sendMessage(chatId, "유저 제거 성공. 이용해주셔서 감사합니다.");
    }

    private void info(BoddariBotHandler botHandler, Long chatId, String userId) throws BadRequestException {
        KimchiTradeUser user = userService.findUserById(userId);
        if (user == null) {
            throw new BadRequestException("등록되지 않은 유저입니다.");
        }
        botHandler.sendMessage(chatId, "유저 정보: " + user);
    }

    private void setKrwLimit(Long chatId, String userId, String[] commands, BoddariBotHandler botHandler) throws BadRequestException {
        if (commands.length != 4 || NumberUtils.toInt(commands[3]) <= 0) {
            throw new BadRequestException("잘못된 명령입니다. (예시) /user set krw-limit 1000000");
        }
        KimchiTradeUser user = userService.findUserById(userId);
        if (user == null) {
            throw new BadRequestException("등록되지 않은 유저입니다.");
        }
        Integer beforeKrwLimit = user.getKrwLimit();
        user.setKrwLimit(NumberUtils.toInt(commands[3]));
        KimchiTradeUser savedUser = userService.save(user);
        botHandler.sendMessage(chatId, "김프 거래를 위한 업비트 KRW 금액이 변경되었습니다. 기존: " + beforeKrwLimit + ", 변경: " + savedUser.getKrwLimit());
    }

    private void setTradeStrategy(Long chatId, String userId, String[] commands, BoddariBotHandler botHandler) throws BadRequestException {
        try {
            KimchiTradeUser user = userService.findUserById(userId);
            if (user == null) {
                throw new BadRequestException("등록되지 않은 유저입니다.");
            }
            if (commands.length <= 3) {
                throw new BadRequestException("트레이드 전략을 지정하세요.\n" +
                                                  "\n" +
                                                  "1. 하한선, 상한선 기반 매매\n" +
                                                  "아래 예시처럼 설정하면 김프가 2.5% 이하일 경우 매수하고 5.0% 이상일 경우 매도합니다. /graph 명령으로 최근 김프 추이를 살펴본 뒤 적절한 값으로 설정하세요.\n" +
                                                  "(예시) /user set trade-strategy lower-and-upper-limit|2.5|5.0" +
                                                  "\n\n" +
                                                  "2. 하한선, 목표 수익률 기반 매매\n" +
                                                  "아래 예시처럼 설정하면 김프가 2.5% 이하로 떨어지면 매수하고, 이후 이익률이 목표 이익률인 1.0% 이상이 되면 매도합니다. /graph 명령으로 최근 김프 추이를 살펴본 뒤 적절한 값으로 설정하세요.\n" +
                                                  "(예시) /user set trade-strategy lower-and-profit-limit|2.5|1.0");
            }
            TradeStrategy tradeStrategy = tradeStrategyFactory.create(commands[3]);
            user.setTradeStrategy(commands[3]);
            KimchiTradeUser savedUser = userService.save(user);
            botHandler.sendMessage(chatId, "트레이딩 전략이 변경되었습니다.\n" + tradeStrategy.getDescription());
        } catch (TradeStrategyFactoryException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    private void setLeverage(Long chatId, String userId, String[] commands, BoddariBotHandler botHandler) throws BadRequestException {
        KimchiTradeUser user = userService.findUserById(userId);
        if (user == null) {
            throw new BadRequestException("등록되지 않은 유저입니다.");
        }
        if (commands.length != 4 || NumberUtils.toInt(commands[3], 0) <= 0) {
            throw new BadRequestException("레버리지는 자연수로 설정해주세요. (예시) /user set leverage 2");
        }
        int leverage = NumberUtils.toInt(commands[3], 1);
        user.setLeverage(leverage);
        userService.save(user);
        botHandler.sendMessage(chatId, "레버리지가 " + leverage + "배로 설정되었습니다.");
    }
}
