package joyyir.boddari.interfaces.handler;

import joyyir.boddari.interfaces.controller.TelegramCommandController;
import joyyir.boddari.interfaces.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class BoddariBotHandler extends TelegramLongPollingBot {
    private final String token;
    private final TelegramCommandController userController;
    private final TelegramCommandController tradeController;
    private final TelegramCommandController graphController;

    public BoddariBotHandler(@Value("${constant.telegram-boddaribot.access-token}") String token,
                             TelegramCommandController userController,
                             TelegramCommandController tradeController,
                             TelegramCommandController graphController) {
        this.token = token;
        this.userController = userController;
        this.tradeController = tradeController;
        this.graphController = graphController;
    }

    @Override
    public String getBotToken() {
        return this.token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.getMessage() == null) { // edited
            return;
        }
        final Long chatId = update.getMessage().getChatId();
        if (update.getMessage().isCommand()) {
            try {
                String[] commands = update.getMessage().getText().split(" ");
                switch (commands[0]) {
                    case "/start":
                    case "/help":
                        help(chatId);
                        break;
                    case "/user":
                        this.userController.runCommand(chatId, commands, this);
                        break;
                    case "/trade":
                        this.tradeController.runCommand(chatId, commands, this);
                        break;
                    case "/graph":
                        this.graphController.runCommand(chatId, commands, this);
                        break;
                    default:
                        throw new BadRequestException("지원하지 않는 명령어입니다.");
                }
            } catch (NumberFormatException e) {
                sendMessage(chatId, "숫자 형식이 올바르지 않습니다.");
            } catch (BadRequestException e) {
                sendMessage(chatId, e.getMessage());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                sendMessage(chatId, "예상치 못한 문제가 발생했습니다. 다시 시도해주세요.");
            }
        }
    }

    public void sendMessage(Long chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(false);
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(message);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public String getBotUsername() {
        return "보따리봇";
    }

    private void help(Long chatId) {
        String message =
            "처음 오신 분은 아래 절차에 따라 트레이딩을 시작하세요.\n" +
                "1. /user register 명령으로 유저 등록\n" +
                "2. /user set krw-limit 명령으로 업비트 KRW 금액 설정\n" +
                "3. /user set trade-strategy 명령으로 김프 트레이딩 전략 설정\n" +
                "4. /trade start 명령으로 트레이딩 시작\n" +
                "\n" +
                "/user register : 유저 등록\n" +
                "/user unregister : 유저 정보 제거\n" +
                "/user info : 유저 정보 조회\n" +
                "/user set krw-limit {금액} : 김프 거래를 위한 업비트 KRW 금액 변경\n" +
                "/user set trade-strategy {설정값} : 김프 트레이딩 전략 설정\n" +
                "\n" +
                "/trade start : 새로운 트레이딩 시작\n" +
                "/trade stop : 현재 트레이딩 종료\n" +
                "/trade pause : 현재 트레이딩 일시 중지\n" +
                "/trade resume : 일시 중지된 트레이딩 재개\n" +
                "/trade status : 현재 진행 중인 트레이딩 상태 확인\n" +
                "/trade history : 최근 n일 동안 진행된 트레이드 히스토리 확인 (예시) /trade history 5\n" +
                "\n" +
                "/graph : 최근 7일 김프 그래프 확인\n" +
                "\n" +
                "문의 사항은 joyyir@naver.com으로 보내주세요.\n" +
                "";
        sendMessage(chatId, message);
    }
}
