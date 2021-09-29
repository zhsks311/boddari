package joyyir.boddari.interfaces.controller;

import joyyir.boddari.interfaces.exception.BadRequestException;
import joyyir.boddari.interfaces.handler.BoddariBotHandler;

public interface TelegramCommandController {
    void runCommand(Long chatId, String[] commands, BoddariBotHandler botHandler) throws BadRequestException;
}
