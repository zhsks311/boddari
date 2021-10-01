package joyyir.boddari.interfaces.controller;

import joyyir.boddari.interfaces.exception.BadRequestException;
import joyyir.boddari.interfaces.handler.BoddariBotHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

@Slf4j
@RestController
public class GraphController implements TelegramCommandController {
    private final String graphImagePath;

    public GraphController(@Value("${constant.graph-image-path}") String graphImagePath) {
        this.graphImagePath = graphImagePath;
    }

    @Override
    public void runCommand(Long chatId, String[] commands, BoddariBotHandler botHandler) throws BadRequestException {
        String userId = String.valueOf(chatId);
        File file = new File(graphImagePath);
        File pngFile = Arrays.stream(Objects.requireNonNull(file.listFiles()))
                             .filter(x -> x.getName().endsWith(".png"))
                             .findFirst()
                             .orElse(null);
        if (pngFile == null) {
            throw new BadRequestException("현재 그래프 데이터 확인이 불가능합니다. 양해 부탁드립니다.");
        }
        botHandler.sendMessage(chatId, "이미지 업로드 중입니다...");
        try {
            botHandler.execute(new SendPhoto(userId, new InputFile(pngFile)));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
