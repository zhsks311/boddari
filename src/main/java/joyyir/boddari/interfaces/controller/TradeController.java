package joyyir.boddari.interfaces.controller;

import joyyir.boddari.domain.kimchi.KimchiTradeUser;
import joyyir.boddari.domain.kimchi.TradeStatus;
import joyyir.boddari.domain.user.UserAndTradeHistory;
import joyyir.boddari.interfaces.exception.BadRequestException;
import joyyir.boddari.interfaces.handler.BoddariBotHandler;
import joyyir.boddari.service.KimchiTradeHistoryService;
import joyyir.boddari.service.KimchiTradeUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@AllArgsConstructor
@RestController
public class TradeController implements TelegramCommandController {
    private final KimchiTradeUserService userService;
    private final KimchiTradeHistoryService tradeHistoryService;

    @Override
    public void runCommand(Long chatId, String[] commands, BoddariBotHandler botHandler) throws BadRequestException {
        String userId = String.valueOf(chatId);

        switch (commands[1]) {
            case "start":
                start(botHandler, chatId, userId);
                break;
            case "resume":
                resume(botHandler, chatId, userId);
                break;
            case "stop":
                stop(botHandler, chatId, userId);
                break;
            case "pause":
                pause(botHandler, chatId, userId);
                break;
            default:
                throw new BadRequestException("지원하지 않는 명령어입니다.");
        }
    }

    private void start(BoddariBotHandler botHandler, Long chatId, String userId) throws BadRequestException {
        TradeStatus targetStatus = TradeStatus.START;
        KimchiTradeUser user = findUser(userId, targetStatus);
        if (user.getTradeStatus() != TradeStatus.STOP) {
            throw new BadRequestException("start 명령은 stop 상태일 때만 할 수 있습니다.");
        }
        UserAndTradeHistory userAndTradeHistory = userService.startNewTrade(userId);
        notifyTradeStatusChange(botHandler, chatId, userAndTradeHistory.getUser());
    }

    private void resume(BoddariBotHandler botHandler, Long chatId, String userId) throws BadRequestException {
        TradeStatus targetStatus = TradeStatus.START;
        KimchiTradeUser user = findUser(userId, targetStatus);
        if (user.getTradeStatus() != TradeStatus.PAUSE) {
            throw new BadRequestException("resume 명령은 PAUSE 상태일 때만 할 수 있습니다.");
        }
        KimchiTradeUser savedUser = userService.setUserTradeStatus(user, TradeStatus.START, user.getCurrentTradeId());
        notifyTradeStatusChange(botHandler, chatId, savedUser);
    }

    private void stop(BoddariBotHandler botHandler, Long chatId, String userId) throws BadRequestException {
        TradeStatus targetStatus = TradeStatus.STOP;
        KimchiTradeUser user = findUser(userId, targetStatus);
        if (user.getTradeStatus() != TradeStatus.START && user.getTradeStatus() != TradeStatus.PAUSE) {
            throw new BadRequestException("stop 명령은 START, PAUSE 상태일 때만 할 수 있습니다. 현재 상태: " + user.getTradeStatus());
        }
        KimchiTradeUser savedUser = userService.setUserTradeStatus(user, targetStatus, null);
        notifyTradeStatusChange(botHandler, chatId, savedUser);
    }

    private void pause(BoddariBotHandler botHandler, Long chatId, String userId) throws BadRequestException {
        TradeStatus targetStatus = TradeStatus.PAUSE;
        KimchiTradeUser user = findUser(userId, targetStatus);
        if (user.getTradeStatus() != TradeStatus.START) {
            throw new BadRequestException("pause 명령은 START 상태일 때만 할 수 있습니다. 현재 상태: " + user.getTradeStatus());
        }
        KimchiTradeUser savedUser = userService.setUserTradeStatus(user, targetStatus, user.getCurrentTradeId());
        notifyTradeStatusChange(botHandler, chatId, savedUser);
    }

    private KimchiTradeUser findUser(String userId, TradeStatus targetStatus) throws BadRequestException {
        KimchiTradeUser user = userService.findUserById(userId);
        if (user == null) {
            throw new BadRequestException("등록되지 않은 유저입니다. userId:" + userId);
        }
        if (user.getTradeStatus() == targetStatus) {
            throw new BadRequestException("이미 " + targetStatus.name() + " 상태입니다.");
        }
        return user;
    }

    private void notifyTradeStatusChange(BoddariBotHandler botHandler, Long chatId, KimchiTradeUser savedUser) {
        botHandler.sendMessage(chatId, "트레이드 상태가 " + savedUser.getTradeStatus().name() + "으로 변경되었습니다.");
    }
}
