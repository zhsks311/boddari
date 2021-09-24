package joyyir.boddari.infrastructure.upbit;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UpbitUtil {
    public static String getAuthenticationToken(Map<String, String> params, String accessKey, String secretKey) {
        try {
            String queryString = toQueryString(params);
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(queryString.getBytes("UTF-8"));

            String queryHash = String.format("%0128x", new BigInteger(1, md.digest()));

            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            String jwtToken = JWT.create()
                                 .withClaim("access_key", accessKey)
                                 .withClaim("nonce", UUID.randomUUID().toString())
                                 .withClaim("query_hash", queryHash)
                                 .withClaim("query_hash_alg", "SHA512")
                                 .sign(algorithm);

            return "Bearer " + jwtToken;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String toQueryString(Map<String, String> params) {
        List<String> queryElements = new ArrayList<>();

        for(Map.Entry<String, String> entity : params.entrySet()) {
            queryElements.add(entity.getKey() + "=" + entity.getValue());
        }

        return String.join("&", queryElements.toArray(new String[0]));
    }
}
