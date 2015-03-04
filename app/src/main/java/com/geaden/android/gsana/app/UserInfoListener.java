package com.geaden.android.gsana.app;

/**
 * Interface that notifies activities about
 * received user info
 */
public interface UserInfoListener {
    /**
     * Notifies other fragments with user information
     * @param userInfo user information, for now just a string
     */
    public void notifyUserInfo(String userInfo);
}
