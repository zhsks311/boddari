package joyyir.boddari.interfaces.controller;

import joyyir.boddari.domain.kimchi.KimchiTradeHistory;
import joyyir.boddari.domain.kimchi.KimchiTradeStatus;
import joyyir.boddari.domain.kimchi.KimchiTradeUser;
import joyyir.boddari.domain.kimchi.TradeStatus;
import joyyir.boddari.domain.user.UserAndTradeHistory;
import joyyir.boddari.interfaces.exception.BadRequestException;
import joyyir.boddari.interfaces.handler.BoddariBotHandler;
import joyyir.boddari.service.KimchiTradeHistoryService;
import joyyir.boddari.service.KimchiTradeUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@RestController
public class TradeController implements TelegramCommandController {
    private final KimchiTradeUserService userService;
    private final KimchiTradeHistoryService tradeHistoryService;

    @Override
    public void runCommand(Long chatId, String[] commands, BoddariBotHandler botHandler) throws BadRequestException {
        String userId = String.valueOf(chatId);
        if (commands.length < 2) {
            String helpMessage =
                "/trade start : 새로운 트레이딩 시작\n" +
                "/trade stop : 현재 트레이딩 종료\n" +
                "/trade pause : 현재 트레이딩 일시 중지\n" +
                "/trade resume : 일시 중지된 트레이딩 재개\n" +
                "/trade status : 현재 진행 중인 트레이딩 상태 확인\n" +
                "/trade history : 최근 n일 동안 진행된 트레이드 히스토리 확인 (예시) /trade history 5";
            botHandler.sendMessage(chatId, helpMessage);
            return;
        }
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
            case "status":
                status(botHandler, chatId, userId);
                break;
            case "history":
                if (commands.length != 3 || NumberUtils.toInt(commands[2], -1) < 0) {
                    throw new BadRequestException("잘못된 명령입니다. (예시) /trade history 5");
                }
                history(botHandler, chatId, userId, NumberUtils.toInt(commands[2]));
                break;
            default:
                throw new BadRequestException("지원하지 않는 명령어입니다.");
        }
    }

    private void start(BoddariBotHandler botHandler, Long chatId, String userId) throws BadRequestException {
        TradeStatus targetStatus = TradeStatus.START;
        KimchiTradeUser user = findUser(userId, targetStatus);
        if (user.getTradeStatus() != TradeStatus.STOP) {
            throw new BadRequestException("start 명령은 STOP 상태일 때만 할 수 있습니다.");
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
        botHandler.sendMessage(chatId, "김프 트레이딩 중에 업비트 매수 및 바이낸스 숏을 했던 자산이 있다면 직접 정리해주세요.");
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

    private void status(BoddariBotHandler botHandler, Long chatId, String userId) throws BadRequestException {
        KimchiTradeUser user = userService.findUserById(userId);
        if (user == null) {
            throw new BadRequestException("등록되지 않은 유저입니다.");
        }
        if (StringUtils.isEmpty(user.getCurrentTradeId())) {
            botHandler.sendMessage(chatId, "진행 중인 트레이드가 없습니다. 트레이드 상태: " + user.getTradeStatus().name());
            return;
        }
        List<KimchiTradeHistory> tradeHistory = tradeHistoryService.findTradeHistory(userId, user.getCurrentTradeId());
        Collections.reverse(tradeHistory);
        botHandler.sendMessage(chatId,
                               "현재 트레이드 id: " + user.getCurrentTradeId() + "\n" +
                               "현재 트레이드 상태: " + tradeHistory.get(tradeHistory.size() - 1).getStatus() + "\n" +
                               "\n" +
                               "현재 트레이드 히스토리\n" +
                               toTradeHistoryString(tradeHistory));
    }

    private void history(BoddariBotHandler botHandler, Long chatId, String userId, int days) {
        List<KimchiTradeHistory> tradeHistory = tradeHistoryService.findAllByUserIdAndTimestampAfter(userId, LocalDateTime.now().minusDays(days));
        botHandler.sendMessage(chatId,
                               "트레이드 히스토리\n" +
                               toTradeHistoryString(tradeHistory));
    }

    private KimchiTradeUser findUser(String userId, TradeStatus targetStatus) throws BadRequestException {
        KimchiTradeUser user = userService.findUserById(userId);
        if (user == null) {
            throw new BadRequestException("등록되지 않은 유저입니다.");
        }
        if (user.getTradeStatus() == targetStatus) {
            throw new BadRequestException("이미 " + targetStatus.name() + " 상태입니다.");
        }
        if (user.getKrwLimit() == null || user.getKrwLimit() <= 0) {
            throw new BadRequestException("업비트 KRW 금액이 설정되어 있지 않습니다. /user set krw-limit 명령어로 설정하세요.");
        }
        if (user.getTradeStrategy() == null) {
            throw new BadRequestException("트레이딩 전략이 설정되어 있지 않습니다. /user set trade-strategy 명령어로 설정하세요.");
        }
        return user;
    }

    private void notifyTradeStatusChange(BoddariBotHandler botHandler, Long chatId, KimchiTradeUser savedUser) {
        botHandler.sendMessage(chatId, "트레이드 상태가 " + savedUser.getTradeStatus().name() + "으로 변경되었습니다.");
    }

    private String toTradeHistoryString(List<KimchiTradeHistory> tradeHistory) {
        if (tradeHistory == null || tradeHistory.isEmpty()) {
            return "내역 없음";
        }
        return tradeHistory.stream()
                           .map(x -> {
                               if (x.getStatus() == KimchiTradeStatus.WAITING || x.getStatus() == KimchiTradeStatus.ERROR) {
                                   return String.format("%s | %s", x.getTimestamp(), x.getStatus());
                               } else if (x.getStatus() == KimchiTradeStatus.STARTED) {
                                   return x.buyDescription();
                               } else if (x.getStatus() == KimchiTradeStatus.FINISHED) {
                                   return x.sellDescription();
                               } else {
                                   return String.format("%s | %s | unknown", x.getTimestamp(), x.getStatus());
                               }
                           })
                           .collect(Collectors.joining("\n"));
    }
}
