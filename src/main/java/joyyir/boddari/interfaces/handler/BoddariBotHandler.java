package joyyir.boddari.interfaces.handler;

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

    public BoddariBotHandler(@Value("${constant.telegram-boddaribot.access-token}") String token) {
        this.token = token;
    }

    @Override
    public String getBotToken() {
        return this.token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        final Long chatId = update.getMessage().getChatId();

        if (update.getMessage().isCommand()) {
            try {
                String[] commands = update.getMessage().getText().split(" ");
                switch (commands[0]) {
                    case "/start":
                        sendMessage(chatId, "환영합니다!");
                        break;
                    case "/volume":
                        sendMessage(chatId, "거래량 기준을 바꿔볼까요?");
                        break;
                    case "/price":
                        sendMessage(chatId, "가격 기준을 바꿔볼까요?");
                        break;
                    default:
                        throw new BadRequestException("잘못된 형식의 명령입니다. 명령어를 다시 확인해주세요.");
                }
            } catch (BadRequestException e) {
                sendMessage(chatId, e.getMessage());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                sendMessage(chatId, "예상치 못한 문제가 발생했습니다. 다시 시도해주세요.");
            }
        }
    }

    private void sendMessage(Long chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
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
}
