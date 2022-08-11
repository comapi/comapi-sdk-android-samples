package com.dotdigital.deeplinksample.handlers;

import android.text.TextUtils;
import android.util.Base64;

import com.comapi.ComapiAuthenticator;
import com.comapi.internal.network.AuthClient;
import com.comapi.internal.network.ChallengeOptions;
import com.dotdigital.deeplinksample.constants.DotdigitalConstants;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class AuthChallengeHandler extends ComapiAuthenticator {

    @Override
    public void onAuthenticationChallenge(AuthClient authClient, ChallengeOptions challengeOptions) {

        final String profileId = DotdigitalConstants.PROFILE_ID;
        if (!TextUtils.isEmpty(profileId)) {

            /*
             This implementation creates the JWT token for profileId and given nonce locally. In realistic scenario this would be obtained from some auth provider service.
             The claims definitions must match configuration of the ApiSpace.
             */

            byte[] data;
            data = DotdigitalConstants.SECRET.getBytes(StandardCharsets.UTF_8);
            String base64Secret = Base64.encodeToString(data, Base64.DEFAULT);

            Map<String, Object> header = new HashMap<>();
            header.put("typ", "JWT");

            // Claims as defined in ApiSpace configuration.
            Map<String, Object> claims = new HashMap<>();
            claims.put("nonce", challengeOptions.getNonce());
            claims.put("sub", profileId);
            claims.put("aud", DotdigitalConstants.AUDIENCE);
            claims.put("iss", DotdigitalConstants.ISSUER);
            claims.put("iat", System.currentTimeMillis());
            claims.put("exp", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30));

            final String token = Jwts.builder()
                    .setHeader(header)
                    .setClaims(claims)
                    .signWith(SignatureAlgorithm.HS256, base64Secret)
                    .compact();

            // Provide auth token to the Dotdigital SDK.
            authClient.authenticateWithToken(token);

        } else {
            // Tell SDK to continue, auth failed.
            authClient.authenticateWithToken(null);
        }
    }
}
