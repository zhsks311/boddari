package joyyir.boddari.infrastructure.upbit;

import joyyir.boddari.domain.CandleRepository;
import joyyir.boddari.domain.DailyChange;
import joyyir.boddari.domain.MarketType;
import joyyir.boddari.infrastructure.upbit.dto.CandleDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Repository
@AllArgsConstructor
public class UpbitCandleRepository implements CandleRepository {
    private final RestTemplate restTemplate;

    public DailyChange findDailyChange(MarketType marketType, LocalDateTime localDateTime) {
        LocalDate currentCandleDate =
            localDateTime.toLocalTime().isBefore(LocalTime.of(9, 0))
                ? localDateTime.toLocalDate().minusDays(1)
                : localDateTime.toLocalDate();
        List<CandleDTO> dailyCandles = getDailyCandles(marketType, 3);
        LocalDateTime currentCandleStartTime = LocalDateTime.of(currentCandleDate, LocalTime.of(9, 0));
        CandleDTO prevCandle = dailyCandles.stream()
                                            .filter(x -> x.getCandleDateTimeKst()
                                                          .equals(currentCandleStartTime.minusDays(1).format(DateTimeFormatter.ISO_DATE_TIME)))
                                            .findAny()
                                            .orElseThrow(() -> new RuntimeException("prevCandle not found"));
        CandleDTO prevPrevCandle = dailyCandles.stream()
                                                .filter(x -> x.getCandleDateTimeKst()
                                                              .equals(currentCandleStartTime.minusDays(2).format(DateTimeFormatter.ISO_DATE_TIME)))
                                                .findAny()
                                                .orElseThrow(() -> new RuntimeException("prevPrevCandle not found"));
        return new DailyChange(currentCandleStartTime,
                               marketType,
                               prevCandle.getChangeRate(),
                               (prevCandle.getCandleAccTradeVolume().subtract(prevPrevCandle.getCandleAccTradeVolume()))
                                   .divide(prevPrevCandle.getCandleAccTradeVolume(), RoundingMode.HALF_UP));
    }

    private List<CandleDTO> getDailyCandles(MarketType marketType, int count) {
        String marketTypeString;
        if (MarketType.XRP_KRW.equals(marketType)) {
            marketTypeString = "KRW-XRP";
        } else if (MarketType.ETH_KRW.equals(marketType)) {
            marketTypeString = "KRW-ETH";
        } else if (MarketType.BTC_KRW.equals(marketType)) {
            marketTypeString = "KRW-BTC";
        } else {
            throw new UnsupportedOperationException("unsupported for marketType: " + marketType.name());
        }

        ResponseEntity<CandleDTO[]> result = restTemplate.getForEntity(String.format("https://api.upbit.com/v1/candles/days?market=%s&count=%d", marketTypeString, count), CandleDTO[].class);
        return Arrays.asList(result.getBody());
    }
}
