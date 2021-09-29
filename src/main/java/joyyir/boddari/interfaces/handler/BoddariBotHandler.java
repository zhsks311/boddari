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

    public BoddariBotHandler(@Value("${constant.telegram-boddaribot.access-token}") String token,
                             TelegramCommandController userController,
                             TelegramCommandController tradeController) {
        this.token = token;
        this.userController = userController;
        this.tradeController = tradeController;
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
                    case "/user":
                        this.userController.runCommand(chatId, commands);
                        break;
                    case "/trade":
                        this.tradeController.runCommand(chatId, commands);
                        break;
                    default:
                        throw new BadRequestException("잘못된 형식의 명령입니다. 명령어를 다시 확인해주세요.");
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

    private void sendMessage(Long chatId, String message) {
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
}
