package com.geaden.android.gsana.app.api.toggl;

import android.content.Context;
import android.net.Uri;
import android.util.Log;


import com.geaden.android.gsana.app.api.HttpHelper;
import com.geaden.android.gsana.app.api.toggl.util.DateUtil;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Custom implementation of Toggle API
 */
public class GToggl {
    public static final String DATA = "data";
    public static final String PLACEHOLDER = "{0}";
    private final static String TIME_ENTRIES = "/time_entries";
    private final static String TIME_ENTRY = "/time_entries/{0}";
    private final static String TIME_ENTRY_START = "/time_entries/start";
    private final static String TIME_ENTRY_STOP = "/time_entries/{0}/stop";

    private final String user;
    private final String password;
    private boolean log = false;

    private GTogglApiBridge mTogglApiBridge;
    private Context mContext;
    private final String LOG_TAG = getClass().getSimpleName();

    /**
     * Constructor to create an instance of JToggl that uses an api token to connect to toggl.
     *
     * @param apiToken the api token to connect to toggl
     */
    public GToggl(Context context, String apiToken) {
        this(context, apiToken, "api_token");
    }

    /**
     * Constructor to create an instance of JToggl.
     *
     * @param user username or api_token
     * @param password password or string "api_token"
     */
    public GToggl(Context context, String user, String password) {
        this.user = user;
        this.password = password;
        prepareClient(context);
    }

    private void prepareClient(Context context) {
        // Setup Toggl API Bridge
        mTogglApiBridge = GTogglApiBridge.getInstance(context);
    }

    /**
     * Get latest time entries.
     *
     * @return list of {@link TimeEntry}
     */
    public List<TimeEntry> getTimeEntries() {
        return this.getTimeEntries(null, null);
    }

    /**
     * Get time entries started in a specific time range.
     * By default, the number of days from the field "How long are time entries
     * visible in the timer" under "My settings" in Toggl is used to determine
     * which time entries to return but you can specify another date range using
     * start_date and end_date parameters.
     *
     * @param startDate
     * @param endDate
     * @return list of {@link TimeEntry}
     */
    public List<TimeEntry> getTimeEntries(Date startDate, Date endDate) {
        String queryString = "";
        if (startDate != null && endDate != null) {
            queryString += "?";
            queryString += "start_date=" + DateUtil.convertDateToString(startDate);
            queryString += "end_date=" + DateUtil.convertDateToString(endDate);
            queryString = Uri.encode(queryString);
        }
        final List<TimeEntry> timeEntries = new ArrayList<TimeEntry>();
        mTogglApiBridge.request(HttpHelper.Method.GET, TIME_ENTRIES + queryString, null, new GTogglApiBridge.GTogglCallback<GTogglApiBridge.GTogglResponse>() {
            @Override
            public void onResult(GTogglApiBridge.GTogglResponse value) {
                if (value == null) {
                    return;
                }
                JSONArray timeEntriesJson = (JSONArray) value.getData();
                for (int i = 0; i < timeEntriesJson.size(); i++) {
                    timeEntries.add(new TimeEntry((JSONObject) timeEntriesJson.get(i)));
                }
            }

            @Override
            public void onError(Throwable exception) {
                Log.e(LOG_TAG, "Error", exception);

            }
        });
        return timeEntries;
    }

    /**
     * Get a time entry.
     *
     * @param id
     * @return TimeEntry or null if no Entry is found.
     */
    public TimeEntry getTimeEntry(Long id) {
        final TimeEntry timeEntry = new TimeEntry();
        mTogglApiBridge.request(HttpHelper.Method.GET, TIME_ENTRY.replace(PLACEHOLDER, "" + id),
                null, new GTogglApiBridge.GTogglCallback<GTogglApiBridge.GTogglResponse>() {
            @Override
            public void onResult(GTogglApiBridge.GTogglResponse value) {
                if (value == null) {
                    return;
                }
                JSONObject timeEntryJson = (JSONObject) value.getData();
                TimeEntry timeEntryResult = new TimeEntry(timeEntryJson);
                timeEntry.setDuration(timeEntryResult.getDuration());
                timeEntry.setStart(timeEntryResult.getStart());
                timeEntry.setDescription(timeEntryResult.getDescription());
            }

            @Override
            public void onError(Throwable exception) {
                Log.e(LOG_TAG, "Error", exception);

            }
        });
        return timeEntry;
    }

    /**
     * Create a new time entry.
     *
     * @param timeEntry
     * @return created {@link TimeEntry}
     */
    public TimeEntry createTimeEntry(final TimeEntry timeEntry) {
        mTogglApiBridge.request(HttpHelper.Method.POST, TIME_ENTRIES, timeEntry.toJSONString(), new GTogglApiBridge.GTogglCallback<GTogglApiBridge.GTogglResponse>() {
            @Override
            public void onResult(GTogglApiBridge.GTogglResponse value) {
                if (value == null) {
                    return;
                }
                JSONObject timeEntryJson = (JSONObject) value.getData();
                TimeEntry timeEntryResult = new TimeEntry(timeEntryJson);
                timeEntry.setId(timeEntryResult.getId());
            }

            @Override
            public void onError(Throwable exception) {
                Log.e(LOG_TAG, "Error", exception);

            }
        });
        return timeEntry;
    }

    /**
     * Create and then start the given time entry.
     *
     * @param timeEntry
     *            the time entry to start
     * @return created {@link TimeEntry}
     */
    public TimeEntry startTimeEntry(final TimeEntry timeEntry) {
        mTogglApiBridge.request(HttpHelper.Method.POST, TIME_ENTRY_START, timeEntry.toJSONString(),
                new GTogglApiBridge.GTogglCallback<GTogglApiBridge.GTogglResponse>() {
            @Override
            public void onResult(GTogglApiBridge.GTogglResponse value) {
                if (value == null) {
                    return;
                }
                JSONObject timeEntryJson = (JSONObject) value.getData();
                Log.d(LOG_TAG, timeEntryJson.toJSONString().toString());
                TimeEntry timeEntryResult = new TimeEntry(timeEntryJson);
                timeEntry.setId(timeEntryResult.getId());
            }

            @Override
            public void onError(Throwable exception) {
                Log.e(LOG_TAG, "Error", exception);

            }
        });
        return timeEntry;
    }

    /**
     * Stop the given time entry.
     *
     * @param timeEntry
     *            to time entry to stop
     * @return the stopped {@link TimeEntry}
     */
    public TimeEntry stopTimeEntry(final TimeEntry timeEntry) {
        mTogglApiBridge.request(HttpHelper.Method.PUT, TIME_ENTRY_STOP.replace(PLACEHOLDER, timeEntry.getId().toString()),
                timeEntry.toJSONString(), new GTogglApiBridge.GTogglCallback<GTogglApiBridge.GTogglResponse>() {
                    @Override
                    public void onResult(GTogglApiBridge.GTogglResponse value) {
                        if (value == null) {
                            return;
                        }
                        JSONObject timeEntryJson = (JSONObject) value.getData();
                        TimeEntry timeEntryResult = new TimeEntry(timeEntryJson);
                        timeEntry.setStop(timeEntryResult.getStop());
                    }

                    @Override
                    public void onError(Throwable exception) {

                    }
                });
        return timeEntry;
    }
}
