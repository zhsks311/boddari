package joyyir.boddari.interfaces.handler;

import joyyir.boddari.domain.bot.BotConfig;
import joyyir.boddari.domain.bot.BotConfigRepository;
import joyyir.boddari.domain.exchange.CandleRepository;
import joyyir.boddari.domain.exchange.DailyChange;
import joyyir.boddari.domain.exchange.MarketType;
import joyyir.boddari.interfaces.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class BoddariBotHandler extends TelegramLongPollingBot {
    private final String token;
    private final BotConfigRepository botConfigRepository;
    private final CandleRepository candleRepository;

    public BoddariBotHandler(@Value("${constant.telegram-boddaribot.access-token}") String token,
                             BotConfigRepository botConfigRepository,
                             CandleRepository upbitCandleRepository) {
        this.token = token;
        this.botConfigRepository = botConfigRepository;
        this.candleRepository = upbitCandleRepository;
    }

    @Scheduled(cron = "0 1 9 * * *")
    public void newDailyCandleScheduler() {
        DailyChange dailyChange = candleRepository.findDailyChange(MarketType.BTC_KRW, LocalDateTime.now());
        List<BotConfig> botConfigs = botConfigRepository.findAll();
        for (BotConfig botConfig : botConfigs) {
            String message = "🔔 " + dailyChange.getLocalDateTime().toLocalDate().format(DateTimeFormatter.ISO_DATE) + " 알람 🔔\n" +
                             "- 마켓: " + dailyChange.getMarketType().name() + "\n" +
                             "- 가격 변동: " + dailyChange.getDailyPriceChangeRate().multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP) + "%\n" +
                             "- 거래량 변동: " + dailyChange.getDailyVolumeChangeRate().multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP) + "%";
            sendMessage(botConfig.getChatId(), message);
        }
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
                        commandStart(chatId);
                        break;
                    case "/volume":
                        commandVolume(chatId, commands);
                        break;
                    case "/price":
                        commandPrice(chatId, commands);
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

    private void commandStart(Long chatId) {
        sendMessage(chatId, "환영합니다!");
        botConfigRepository.save(new BotConfig(chatId, 1.2, 100.0, 1.2, 100.0));
    }

    private void commandVolume(Long chatId, String[] commands) throws BadRequestException {
        if (commands.length < 3) {
            throw new BadRequestException("올바르지 않은 명령어입니다.\n(예시) /volume 1.2 5");
        }
        Double minVolumeMultiplier = Double.valueOf(commands[1]);
        Double maxVolumeMultiplier = Double.valueOf(commands[2]);
        if (minVolumeMultiplier > maxVolumeMultiplier) {
            throw new BadRequestException("첫번째 수는 두번째 수보다 작아야합니다.");
        }
        BotConfig botConfig = botConfigRepository.findById(chatId)
                                                 .orElseThrow(() -> new BadRequestException("등록되지 않은 chatId 입니다. /start 명령어로 시작하세요."));
        botConfig.setMinVolumeMultiplier(minVolumeMultiplier);
        botConfig.setMaxVolumeMultiplier(maxVolumeMultiplier);
        botConfigRepository.save(botConfig);
        sendMessage(chatId, String.format("거래량 기준을 %.2f배 이상 %.2f배 이하로 조정했습니다.", minVolumeMultiplier, maxVolumeMultiplier));
    }

    private void commandPrice(Long chatId, String[] commands) throws BadRequestException {
        if (commands.length < 3) {
            throw new BadRequestException("올바르지 않은 명령어입니다.\n(예시) /price 1.2 5");
        }
        Double minPriceMultiplier = Double.valueOf(commands[1]);
        Double maxPriceMultiplier = Double.valueOf(commands[2]);
        if (minPriceMultiplier > maxPriceMultiplier) {
            throw new BadRequestException("첫번째 수는 두번째 수보다 작아야합니다.");
        }
        BotConfig botConfig = botConfigRepository.findById(chatId)
                                                 .orElseThrow(() -> new BadRequestException("등록되지 않은 chatId 입니다. /start 명령어로 시작하세요."));
        botConfig.setMinPriceMultiplier(minPriceMultiplier);
        botConfig.setMaxPriceMultiplier(maxPriceMultiplier);
        botConfigRepository.save(botConfig);
        sendMessage(chatId, String.format("가격 기준을 %.2f배 이상 %.2f배 이하로 조정했습니다.", minPriceMultiplier, maxPriceMultiplier));
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
