package joyyir.boddari.infrastructure.binance;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BinanceUtil {
    public static String getSignature(Map<String, String> params, String secretKey) {
        Mac hmacSha256;
        try {
            hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secKey = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            hmacSha256.init(secKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String queryString = toQueryString(params);
        return new String(Hex.encodeHex(hmacSha256.doFinal(queryString.getBytes())));
    }

    public static String toQueryString(Map<String, String> params) {
        List<String> queryElements = new ArrayList<>();

        for(Map.Entry<String, String> entity : params.entrySet()) {
            queryElements.add(entity.getKey() + "=" + entity.getValue());
        }

        return String.join("&", queryElements.toArray(new String[0]));
    }
}
