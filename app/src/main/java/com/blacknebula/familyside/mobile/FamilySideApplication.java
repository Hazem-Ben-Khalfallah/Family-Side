package com.blacknebula.familyside.mobile;

import android.app.Application;
import android.content.Context;

public class FamilySideApplication extends Application {

    private static Application context;

    public static Context getAppContext() {
        return FamilySideApplication.context;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.context = this;
    }
}