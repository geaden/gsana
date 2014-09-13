package com.geaden.android.gsana.app.oauth;

import android.util.Log;

import net.smartam.leeloo.client.OAuthClient;
import net.smartam.leeloo.client.URLConnectionClient;
import net.smartam.leeloo.client.request.OAuthClientRequest;
import net.smartam.leeloo.client.response.OAuthAuthzResponse;
import net.smartam.leeloo.client.response.OAuthJSONAccessTokenResponse;
import net.smartam.leeloo.common.exception.OAuthProblemException;
import net.smartam.leeloo.common.exception.OAuthSystemException;
import net.smartam.leeloo.common.message.types.GrantType;
import net.smartam.leeloo.common.message.types.ResponseType;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Asana OAuth2.0 client
 */
public class AsanaOAuthClient {
    public final String LOG_TAG = getClass().getSimpleName();

    /**
     * Used in Authorization Code Grant flow
     * @return redirect uri to request authorization code
     */
    public String getAuthCodeLocation() {
        String locationUri = null;
        try {
            OAuthClientRequest request = OAuthClientRequest
                    .authorizationLocation(Settings.AUTH_LOCATION)
                    .setClientId(Settings.CLIENT_ID)
                    .setRedirectURI(Settings.REDIECT_URI)
                    .setResponseType(ResponseType.CODE.toString())
                    .buildQueryMessage();
            locationUri = request.getLocationUri();
        } catch (OAuthSystemException e) {
            Log.e(LOG_TAG, "Get authorization code location error", e);
        }
        return locationUri;
    }

    /**
     * Gets Access token from provided data
     *
     * @param code the code to get access token for
     * @return asana token response
     */
    public AsanaTokenResponse getAccessToken(String code) {
        AsanaTokenResponse asanaTokenResponse = new AsanaTokenResponse();
        try {
            OAuthClientRequest request = OAuthClientRequest
                    .tokenLocation(Settings.TOKEN_LOCATION)
                    .setGrantType(GrantType.AUTHORIZATION_CODE)
                    .setClientId(Settings.CLIENT_ID)
                    .setClientSecret(Settings.CLIENT_SECRET)
                    .setRedirectURI(Settings.REDIECT_URI)
                    .setCode(code)
                    .buildBodyMessage();

            //create OAuth client that uses custom http client under the hood
            OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

            //Facebook is not fully compatible with OAuth 2.0 draft 10, access token response is
            //application/x-www-form-urlencoded, not json encoded so we use dedicated response class for that
            //Custom response classes are an easy way to deal with oauth providers that introduce modifications to
            //OAuth 2.0 specification
            OAuthJSONAccessTokenResponse oAuthResponse = oAuthClient.accessToken(request);

            asanaTokenResponse.setAccessToken(oAuthResponse.getAccessToken());
            asanaTokenResponse.setRefreshToken(oAuthResponse.getRefreshToken());
            asanaTokenResponse.setExpiresIn(oAuthResponse.getExpiresIn());
            Log.v(LOG_TAG, "User: " + oAuthResponse.getParam("data"));
            AsanaTokenResponse.AsanaUser user = new AsanaTokenResponse.AsanaUser(oAuthResponse.getParam("data"));
            asanaTokenResponse.setUser(user);
        } catch (OAuthSystemException e) {
            Log.e(LOG_TAG, "Get Access Token error", e);
        } catch (OAuthProblemException e) {
            Log.e(LOG_TAG, "Problem with OAuth", e);
        }
        return asanaTokenResponse;
    }

    /**
     * Refreshes access token with provided refresh token.
     *
     * @param refreshToken the reresh token to update access token
     * @return updated access token
     */
    public String refreshToken(String refreshToken) {
        String asanaAccessToken = null;
        try {
            OAuthClientRequest request = OAuthClientRequest
                    .tokenLocation(Settings.TOKEN_LOCATION)
                    .setGrantType(GrantType.REFRESH_TOKEN)
                    .setClientId(Settings.CLIENT_ID)
                    .setClientSecret(Settings.CLIENT_SECRET)
                    .setRefreshToken(refreshToken)
                    .buildBodyMessage();

            //create OAuth client that uses custom http client under the hood
            OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

            OAuthJSONAccessTokenResponse oAuthResponse = oAuthClient.accessToken(request);
            asanaAccessToken = oAuthResponse.getAccessToken();
        } catch (OAuthSystemException e) {
            Log.e(LOG_TAG, "Get Access Token error", e);
        } catch (OAuthProblemException e) {
            Log.e(LOG_TAG, "Problem with OAuth", e);
        }
        return asanaAccessToken;
    }

    /**
     * Asana token response
     */
    public static class AsanaTokenResponse  {
        private String accessToken;
        private String expiresIn;
        private String refreshToken;
        private AsanaUser user;

        public static class AsanaUser {
            private final String LOG_TAG = getClass().getSimpleName();

            public static final String ID = "id";
            public static final String NAME = "name";
            public static final String EMAIL = "email";

            private String id;
            private String name;
            private String email;

            public AsanaUser(String userData) {
                try {
                    JSONObject data = new JSONObject(userData);
                    id = data.getString(ID);
                    name = data.getString(NAME);
                    email = data.getString(EMAIL);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error get user data", e);
                }
            }

            public String getId() {
                return id;
            }

            public String getName() {
                return name;
            }

            public String getEmail() {
                return email;
            }
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public void setExpiresIn(String expiresIn) {
            this.expiresIn = expiresIn;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getExpiresIn() {
            return expiresIn;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public AsanaUser getUser() {
            return user;
        }

        public void setUser(AsanaUser user) {
            this.user = user;
        }
    }
}
