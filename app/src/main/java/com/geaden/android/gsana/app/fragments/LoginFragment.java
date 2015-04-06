package com.geaden.android.gsana.app.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.geaden.android.gsana.app.MainActivity;
import com.geaden.android.gsana.app.R;
import com.geaden.android.gsana.app.Utility;
import com.geaden.android.gsana.app.oauth.AsanaOAuthClient;

/**
 * Login fragment for Asana client
 */
public class LoginFragment extends Fragment {
    private final String LOG_TAG = getClass().getSimpleName();
    private AsanaOAuthClient mAsanaOAuthClient;
    private AsanaOAuthClient.AsanaTokenResponse mTokenResponse;

    public LoginFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_oauth, container, false);
        mAsanaOAuthClient = AsanaOAuthClient.getInstance();
        Intent intent = getActivity().getIntent();
        ImageButton asanaLogin = (ImageButton) rootView.findViewById(R.id.imageButton);
        Uri data = intent.getData();
        AsanaAuthTask authTask = new AsanaAuthTask();
        if (data != null) {
            Log.v(LOG_TAG, "Data " + data.toString());
            String code = data.getQueryParameter("code");
            authTask.execute(Utility.ACCESS_TOKEN_KEY, code);
        }
        if (intent.hasExtra(Utility.REFRESH_TOKEN_KEY)) {
            String refreshToken = intent.getStringExtra(Utility.REFRESH_TOKEN_KEY);
            authTask.execute(Utility.REFRESH_TOKEN_KEY, refreshToken);
        }
        asanaLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(LOG_TAG, "Authenticating...");
                String authCodeLocation = mAsanaOAuthClient.getAuthCodeLocation();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(authCodeLocation));
                startActivity(browserIntent);
            }
        });
        return rootView;
    }

    private class AsanaAuthTask extends AsyncTask<String, Void, AsanaOAuthClient.AsanaTokenResponse> {
        private ProgressDialog dialog = new ProgressDialog(getActivity());
        private String mAuthType;

        /** progress dialog to show user that authentication is in progress. */
        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Please wait");
            this.dialog.show();
        }

        @Override
        protected AsanaOAuthClient.AsanaTokenResponse doInBackground(String... params) {
            mTokenResponse = new AsanaOAuthClient.AsanaTokenResponse();
            mAuthType = params[0];
            String code = params[1];
            if (mAuthType.equals(Utility.ACCESS_TOKEN_KEY)) {
                mTokenResponse = mAsanaOAuthClient.getAccessToken(code);
            } else if (mAuthType.equals(Utility.REFRESH_TOKEN_KEY)) {
                mTokenResponse = mAsanaOAuthClient.refreshToken(code);
            }
            return mTokenResponse;
        }

        @Override
        protected void onPostExecute(AsanaOAuthClient.AsanaTokenResponse asanaTokenResponse) {
            Utility.putAccessToken(getActivity(), asanaTokenResponse.getAccessToken());
            if (!mAuthType.equals(Utility.REFRESH_TOKEN_KEY)) {
                Log.d(LOG_TAG, "Saving refresh token " + asanaTokenResponse.getRefreshToken());
                Utility.putRefreshToken(getActivity(), asanaTokenResponse.getRefreshToken());
                Log.d(LOG_TAG, "Saved refresh token " + Utility.getRefreshToken(getActivity()));
            }
            Intent mainIntent = new Intent(getActivity(), MainActivity.class);
            startActivity(mainIntent);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }
}
