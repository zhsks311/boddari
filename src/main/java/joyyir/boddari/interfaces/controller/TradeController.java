package joyyir.boddari.interfaces.controller;

import joyyir.boddari.interfaces.handler.BoddariBotHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TradeController implements TelegramCommandController {
    @Override
    public void runCommand(Long chatId, String[] commands, BoddariBotHandler botHandler) {
        // TODO : jyjang - develop
        log.info("trade command: {} from: {}", commands, chatId);
    }
}
