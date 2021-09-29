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
        if (user.getTradeStatus() == TradeStatus.STOP) {
            UserAndTradeHistory userAndTradeHistory = userService.startNewTrade(userId);
            botHandler.sendMessage(chatId, "트레이드 상태가 " + userAndTradeHistory.getUser().getTradeStatus().name() + "으로 변경되었습니다.");
        } else if (user.getTradeStatus() == TradeStatus.PAUSE) {
            KimchiTradeUser savedUser = userService.setUserTradeStatus(user, TradeStatus.START, null);
            botHandler.sendMessage(chatId, "트레이드 상태가 " + savedUser.getTradeStatus().name() + "으로 변경되었습니다.");
        }
        throw new RuntimeException("유효하지 않은 트레이드 상태입니다. userId: " + userId + ", tradeStatus: " + user.getTradeStatus());
    }

    private void resume(BoddariBotHandler botHandler, Long chatId, String userId) {
    }

    private void stop(BoddariBotHandler botHandler, Long chatId, String userId) {
    }

    private void pause(BoddariBotHandler botHandler, Long chatId, String userId) {
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
}
