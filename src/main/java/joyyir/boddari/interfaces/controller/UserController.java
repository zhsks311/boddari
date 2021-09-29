package joyyir.boddari.interfaces.controller;

import joyyir.boddari.domain.kimchi.KimchiTradeUser;
import joyyir.boddari.interfaces.exception.BadRequestException;
import joyyir.boddari.interfaces.handler.BoddariBotHandler;
import joyyir.boddari.service.KimchiTradeUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
                "/user info : 유저 정보 조회";
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
}
