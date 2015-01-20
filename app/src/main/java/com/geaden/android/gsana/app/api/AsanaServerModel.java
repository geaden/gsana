package com.geaden.android.gsana.app.api;

import android.net.Uri;

import com.geaden.android.gsana.app.models.AsanaTask;


/**
 * Contains methods for basic Asana APIs, which are
 * loaded into the background.
 *
 * Some of these functions are asynchronous, because they may have to talk
 * to the Asana API to get results.
 */
public class AsanaServerModel {
    private AsanaApiBridge apiBridge;

    public AsanaServerModel(AsanaApiBridge apiBridge) {
        this.apiBridge = apiBridge;
    }

//    /**
//     * Get the URL of a task given some of its data.
//     * @param task {@link com.geaden.android.gsana.app.models.AsanaTask} task data
//     * @return url of the given task
//     */
//    public String getTaskViewUrl(AsanaTask task) {
//        return Uri.parse("https://" + AsanaApiBridge.ASANA_HOST + ":" + AsanaApiBridge.ASANA_PORT)
//                .buildUpon().appendPath("0").appendPath(task.getId()).build().toString();
//    }

    public void getWorkspaces(AsanaCallback asanaCallback) {
        apiBridge.request("GET", "workspaces", null, null);
    }
}
