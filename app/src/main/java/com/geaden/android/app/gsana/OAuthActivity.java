package com.geaden.android.app.gsana;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.geaden.android.app.gsana.R;

public class OAuthActivity extends ActionBarActivity {

    public static String OAUTH_URL = "https://app.asana.com/-/oauth_authorize";
    public static String OAUTH_ACCESS_TOKEN_URL = "https://app.asana.com/-/oauth_token";

    public static String CLIENT_ID = "15434230851041";
    public static String CLIENT_SECRET = "881d9feaa4ef31bb4ecde1c924def191";
    public static String CALLBACK_URL = "http://localhost";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.oauth_container, new OAuthFragment())
                    .commit();
        }
    }

    public static class OAuthFragment extends Fragment {
        private String LOG_TAG = getClass().getSimpleName();

        public OAuthFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_oauth, container, false);
            Uri asanUri = Uri.parse(OAUTH_URL).buildUpon()
                    .appendQueryParameter("client_id", CLIENT_ID)
                        .appendQueryParameter("redirect_uri", "http://gsana.geaden.com/")
                    .appendQueryParameter("response_type", "token").build();
            WebView webview = (WebView) rootView.findViewById(R.id.webview);
            webview.getSettings().setJavaScriptEnabled(true);
            webview.setWebViewClient(new WebViewClient() {
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    String accessTokenFragment = "access_token=";
                    String accessCodeFragment = "code=";

                    // We hijack the GET request to extract the OAuth parameters

                    if (url.contains(accessTokenFragment)) {
                        // the GET request contains directly the token
                        String accessToken = url.substring(url.indexOf(accessTokenFragment));
                        Log.d(LOG_TAG, accessToken);


                    } else if(url.contains(accessCodeFragment)) {
                        // the GET request contains an authorization code
                        String accessCode = url.substring(url.indexOf(accessCodeFragment));
                        Log.d(LOG_TAG, accessCode);


                        String query = "client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET + "&code=" + accessCode;
                        view.postUrl(OAUTH_ACCESS_TOKEN_URL, query.getBytes());
                    }

                }
            });
            webview.loadUrl(asanUri.toString());
            return rootView;
        }
    }
}
