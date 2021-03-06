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
    private final String allowedIp;
    private final TelegramCommandController userController;
    private final TelegramCommandController tradeController;
    private final TelegramCommandController graphController;

    public BoddariBotHandler(@Value("${constant.telegram-boddaribot.access-token}") String token,
                             @Value("${constant.allowed-ip}") String allowedIp,
                             TelegramCommandController userController,
                             TelegramCommandController tradeController,
                             TelegramCommandController graphController) {
        this.token = token;
        this.allowedIp = allowedIp;
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
                        throw new BadRequestException("???????????? ?????? ??????????????????.");
                }
            } catch (NumberFormatException e) {
                sendMessage(chatId, "?????? ????????? ???????????? ????????????.");
            } catch (BadRequestException e) {
                sendMessage(chatId, e.getMessage());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                sendMessage(chatId, "????????? ?????? ????????? ??????????????????. ?????? ??????????????????.");
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
        return "????????????";
    }

    private void help(Long chatId) {
        String message =
                "?????? ?????? ?????? ?????? ????????? ?????? ??????????????? ???????????????.\n" +
                "\n" +
                "?????? ??????\n" +
                "1. ???????????? ?????????????????? API Key??? ???????????????.\n" +
                "  - ?????? ?????? IP??? " + allowedIp + "??? ???????????????.\n" +
                "  - Secret key ??????????????? ????????? ???????????? ?????? ??????(Withdrawals)??? ???????????? ?????????.\n" +
                "  - ????????? - ????????????, ????????????, ????????????\n" +
                "  - ???????????? - Enable Reading, Enable Futures\n" +
                "2. ????????? ????????? ????????? KRW??? ????????????, ???????????? ??????(USD???-M) ????????? ????????? USDT??? ???????????????.\n" +
                "\n" +
                "??? ??????\n" +
                "1. /user register ???????????? ?????? ??????\n" +
                "2. /user set krw-limit ???????????? ????????? KRW ?????? ??????\n" +
                "3. /user set trade-strategy ???????????? ?????? ???????????? ?????? ??????\n" +
                "4. (??????) /user set leverage ???????????? ???????????? ?????? ??? ?????? ?????? ???????????? ?????? (???????????? ?????? 1???)\n" +
                "5. /trade start ???????????? ???????????? ??????\n" +
                "\n" +
                "????????? ??????\n" +
                "/user register {????????? access key} {????????? secret key} {???????????? access key} {???????????? secret key} : ?????? ??????\n" +
                "/user unregister : ?????? ?????? ??????\n" +
                "/user info : ?????? ?????? ??????\n" +
                "/user set krw-limit {??????} : ?????? ????????? ?????? ????????? KRW ?????? ??????\n" +
                "/user set trade-strategy {?????????} : ?????? ???????????? ?????? ??????\n" +
                "/user set leverage {??????} : ???????????? ?????? ??? ?????? ?????? ???????????? ??????\n" +
                "/trade start : ????????? ???????????? ??????\n" +
                "/trade stop : ?????? ???????????? ??????\n" +
                "/trade pause : ?????? ???????????? ?????? ??????\n" +
                "/trade resume : ?????? ????????? ???????????? ??????\n" +
                "/trade cancel : ??????????????? ????????? ????????? ?????? ????????????, ?????????????????? ?????? ????????? ?????? ?????? ???, ???????????? ??????\n" +
                "/trade status : ?????? ?????? ?????? ???????????? ?????? ??????\n" +
                "/trade history : ?????? n??? ?????? ????????? ???????????? ???????????? ?????? (??????) /trade history 5\n" +
                "/graph : ?????? 7??? ?????? ????????? ??????\n" +
                "\n" +
                "?????? ?????? ???????????? ?????? ???????????? ????????? ???????????? ???????????? ????????????.\n" +
                "?????? ?????? ??? ?????? ?????? ????????? Telegram @joyyir2??? ?????? ??????????????????.\n" +
                "";
        sendMessage(chatId, message);
    }
}
