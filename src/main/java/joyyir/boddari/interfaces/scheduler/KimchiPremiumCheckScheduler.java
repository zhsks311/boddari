package joyyir.boddari.interfaces.scheduler;

import joyyir.boddari.domain.exchange.CurrencyType;
import joyyir.boddari.domain.kimchi.KimchiPremium;
import joyyir.boddari.domain.kimchi.KimchiPremiumData;
import joyyir.boddari.domain.kimchi.KimchiPremiumRepository;
import joyyir.boddari.service.KimchiPremiumService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class KimchiPremiumCheckScheduler {
    public final static List<CurrencyType> TARGET_CURRENCIES
        = List.of(CurrencyType.BTC,
                  CurrencyType.ETH,
                  CurrencyType.XRP,
                  CurrencyType.ETC,
                  CurrencyType.ADA,
                  CurrencyType.XTZ,
                  CurrencyType.ATOM,
                  CurrencyType.SRM,
                  CurrencyType.DOT);
    private final KimchiPremiumService kimchiPremiumService;
    private final KimchiPremiumRepository kimchiPremiumRepository;
    private final RestTemplate restTemplate;
    private final String graphImagePath;
    private final String graphUrl;
    private final String grafanaApiKey;

    public KimchiPremiumCheckScheduler(KimchiPremiumService kimchiPremiumService,
                                       KimchiPremiumRepository kimchiPremiumRepository,
                                       RestTemplate restTemplate,
                                       @Value("${constant.graph-image-path}") String graphImagePath,
                                       @Value("${constant.graph-url}") String graphUrl,
                                       @Value("${constant.grafana.api-key}") String grafanaApiKey) {
        this.kimchiPremiumService = kimchiPremiumService;
        this.kimchiPremiumRepository = kimchiPremiumRepository;
        this.restTemplate = restTemplate;
        this.graphImagePath = graphImagePath;
        this.graphUrl = graphUrl;
        this.grafanaApiKey = grafanaApiKey;
    }

    @Scheduled(fixedRate = 1000 * 60)
    void checkKimchiPremium() {
        TARGET_CURRENCIES.forEach(this::check);
        loadGraphImageAndSaveImageFile();
    }

    private void check(CurrencyType currency) {
        KimchiPremiumData kimchiPremiumData = kimchiPremiumService.getKimchiPremium(currency);
        BigDecimal kimchiPremium = kimchiPremiumData.getKimchiPremium();
        kimchiPremiumRepository.save(new KimchiPremium(LocalDateTime.now(), currency, kimchiPremium.doubleValue()));
    }

    private void loadGraphImageAndSaveImageFile() {
        try {
            LinkedMultiValueMap<String, String> header = new LinkedMultiValueMap<>();
            header.add("Authorization", "Bearer " + grafanaApiKey);
            ResponseEntity<Resource> response = restTemplate.exchange(graphUrl.replace("{from}", String.valueOf(ZonedDateTime.now().minusDays(7).toInstant().toEpochMilli()))
                                                                              .replace("{to}", String.valueOf(ZonedDateTime.now())),
                                                                      HttpMethod.GET, new HttpEntity<>(header), Resource.class);
            FileUtils.copyInputStreamToFile(Objects.requireNonNull(response.getBody()).getInputStream(),
                                            new File(graphImagePath + "/current.png"));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
