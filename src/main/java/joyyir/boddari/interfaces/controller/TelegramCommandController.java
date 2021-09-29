package joyyir.boddari.interfaces.controller;

public interface TelegramCommandController {
    void runCommand(Long chatId, String[] commands);
}
