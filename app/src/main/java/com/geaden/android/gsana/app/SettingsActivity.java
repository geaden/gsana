package com.geaden.android.gsana.app;

import android.app.Activity;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;

import com.geaden.android.gsana.app.data.GsanaContract;
import com.geaden.android.gsana.app.sync.GsanaSyncAdapter;


/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends ActionBarActivity {
    public final static String GENERAL_PREFS = "com.geaden.android.gsana.app.GENERAL_PREFS";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Display the fragment as the main content.
        setContentView(R.layout.activity_settings);
        getFragmentManager().beginTransaction()
                .replace(R.id.preference_container, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment implements
            Preference.OnPreferenceChangeListener {
        // since we use the preference change initially to populate the summary
        // field, we'll ignore that change at start of the activity
        boolean mBindingPreference;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Add 'general' preferences, defined in the XML file
            addPreferencesFromResource(R.xml.pref_general);

            // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
            // updated when the preference changes.
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_toggl_api_key)));
//            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_units_key)));
            mBindingPreference = true;
        }


        /**
         * Attaches a listener so the summary is always updated with the preference value.
         * Also fires the listener once, to initialize the summary (so it shows up before the value
         * is changed.)
         */
        private void bindPreferenceSummaryToValue(Preference preference) {
            mBindingPreference = true;

            // Set the listener to watch for value changes.
            preference.setOnPreferenceChangeListener(this);

            // Trigger the listener immediately with the preference's
            // current value.
            onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
            mBindingPreference = false;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if (!mBindingPreference) {
                if (preference.getKey().equals(getString(R.string.pref_toggl_api_key))) {
                    GsanaSyncAdapter.syncImmediately(getActivity());
                } else {
                    // TODO: notify change...
                    getActivity().getContentResolver().notifyChange(GsanaContract.TaskEntry.CONTENT_URI,
                            null);
                }
            }
            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list (since they have separate labels/values).
                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue(stringValue);
                if (prefIndex >= 0) {
                    preference.setSummary(listPreference.getEntries()[prefIndex]);
                }
            } else if (preference instanceof EditTextPreference) {
                // TODO: set summary
            } else {
                // For other preferences, set the summary to the value's simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    }
}