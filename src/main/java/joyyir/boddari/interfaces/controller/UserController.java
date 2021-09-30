package joyyir.boddari.interfaces.controller;

import joyyir.boddari.domain.kimchi.KimchiTradeUser;
import joyyir.boddari.domain.kimchi.TradeStatus;
import joyyir.boddari.interfaces.exception.BadRequestException;
import joyyir.boddari.interfaces.handler.BoddariBotHandler;
import joyyir.boddari.service.KimchiTradeUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@AllArgsConstructor
public class UserController implements TelegramCommandController {
    private final KimchiTradeUserService userService;

    @Override
    public void runCommand(Long chatId, String[] commands, BoddariBotHandler botHandler) throws BadRequestException {
        String userId = String.valueOf(chatId);
        if (commands.length < 2) {
            String helpMessage =
                "/user register : 유저 등록\n" +
                "/user unregister : 유저 정보 제거\n" +
                "/user info : 유저 정보 조회\n" +
                "/user set krw-limit {금액} : 김프 거래를 위한 업비트 KRW 금액 변경\n" +
                "/user set trade-strategy : ";
            botHandler.sendMessage(chatId, helpMessage);
            return;
        }
        switch (commands[1]) {
            case "register":
                register(botHandler, chatId, userId);
                break;
            case "unregister":
                unregister(botHandler, chatId, userId);
                break;
            case "info":
                info(botHandler, chatId, userId);
                break;
            case "set":
                String helpMessage = "/user set krw-limit {금액} : 김프 거래를 위한 업비트 KRW 금액 변경\n" +
                                     "/user set trade-strategy : ";
                if (commands.length == 2) {
                    botHandler.sendMessage(chatId, helpMessage);
                    return;
                }
                switch (commands[2]) {
                    case "krw-limit":
                        setKrwLimit(chatId, userId, commands, botHandler);
                        break;
                    case "trade-strategy":
                        break;
                    default:
                        throw new BadRequestException("지원하지 않는 명령어입니다.\n" + helpMessage);
                }
                break;
            default:
                throw new BadRequestException("지원하지 않는 명령어입니다.");
        }
    }

    private void register(BoddariBotHandler botHandler, Long chatId, String userId) throws BadRequestException {
        KimchiTradeUser user = userService.findUserById(userId);
        if (user != null) {
            throw new BadRequestException("이미 등록된 유저입니다.");
        }
        KimchiTradeUser newUser = userService.register(userId);
        botHandler.sendMessage(chatId, "유저 등록 성공. 환영합니다. " + newUser);
    }

    private void unregister(BoddariBotHandler botHandler, Long chatId, String userId) throws BadRequestException {
        KimchiTradeUser user = userService.findUserById(userId);
        if (user == null) {
            throw new BadRequestException("등록되지 않은 유저입니다.");
        }
        userService.delete(user);
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
        if (user.getTradeStatus() != TradeStatus.STOP) {
            throw new BadRequestException("트레이드 상태가 STOP 상태일 때만 변경할 수 있습니다.");
        }
        Integer beforeKrwLimit = user.getKrwLimit();
        user.setKrwLimit(NumberUtils.toInt(commands[3]));
        KimchiTradeUser savedUser = userService.save(user);
        botHandler.sendMessage(chatId, "김프 거래를 위한 업비트 KRW 금액이 변경되었습니다. 기존: " + beforeKrwLimit + ", 변경: " + savedUser.getKrwLimit());
    }
}
