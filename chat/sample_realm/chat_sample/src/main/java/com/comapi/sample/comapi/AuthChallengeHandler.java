/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Comapi (trading name of Dynmark International Limited)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons
 * to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.comapi.sample.comapi;

import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;

import com.comapi.ComapiAuthenticator;
import com.comapi.internal.network.AuthClient;
import com.comapi.internal.network.ChallengeOptions;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * Provides JWT token for Comapi SDK.
 *
 * @author Marcin Swierczek
 */
public class AuthChallengeHandler extends ComapiAuthenticator {

    /**
     * Shared preferences storing profile id for which app generates JWT token.
     */
    private SharedPreferences internalAppData;

    /**
     * Recommended constructor.
     *
     * @param internalAppData Shared preferences storing profile id for which app generates JWT token.
     */
    public AuthChallengeHandler(SharedPreferences internalAppData) {
        this.internalAppData = internalAppData;
    }

    @Override
    public void onAuthenticationChallenge(AuthClient authClient, ChallengeOptions challengeOptions) {

        // Get profile id for which app generates JWT token.
        String profileId = internalAppData.getString("profileId", null);

        if (!TextUtils.isEmpty(profileId)) {

            /*
             This implementation creates the JWT token for profileId and given nonce locally. In realistic scenario this would be obtained from some auth provider service.
             The claims definitions must match configuration of the ApiSpace.
             */

            try {

                byte[] data;
                data = "secret".getBytes("UTF-8");

                String base64Secret = Base64.encodeToString(data, Base64.DEFAULT);

                Map<String, Object> header = new HashMap<>();
                header.put("typ", "JWT");

                // Claims as defined in ApiSpace configuration.
                Map<String, Object> claims = new HashMap<>();
                claims.put("nonce", challengeOptions.getNonce());
                claims.put("sub", profileId);
                claims.put("aud", "local");
                claims.put("iss", "local");
                claims.put("iat", System.currentTimeMillis());
                claims.put("exp", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30));

                final String token = Jwts.builder()
                        .setHeader(header)
                        .setClaims(claims)
                        .signWith(SignatureAlgorithm.HS256, base64Secret)
                        .compact();

                // Provide auth token to the Comapi SDK.
                authClient.authenticateWithToken(token);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                // Tell SDK to continue, auth failed.
                authClient.authenticateWithToken(null);
            }

        } else {
            // Tell SDK to continue, auth failed.
            authClient.authenticateWithToken(null);
        }
    }
}
