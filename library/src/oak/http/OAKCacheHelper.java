package oak.http;

// User: jnye
// Date: 9/18/12
// Time: 10:50 AM
// Copyright (c) 2012 WillowTree Apps, Inc. All rights reserved.

import com.integralblue.httpresponsecache.HttpResponseCache;

import android.util.Log;

import java.io.File;

public class OAKCacheHelper {

    public static final String TAG = OAKCacheHelper.class.getSimpleName();

    private static HttpResponseCache backportCache;

    public static void installIfNeeded(File cacheDir, long cacheSizeInBytes) {
        try {
            backportCache = HttpResponseCache.getInstalled();
            if (backportCache == null) {
                enableCache(cacheDir, cacheSizeInBytes);
            }
        } catch(Exception e) {
            // Nothin' yet
        }
    }

    public static boolean enableCache(File cacheDir, long cacheSizeInBytes) {
        try {
            backportCache = HttpResponseCache.install(cacheDir, cacheSizeInBytes);
            return true;
        }catch(Exception e) {
            Log.d("OAKCacheHelper", "Failed to set up integralblue HttpResponseCache");
            return false;
        }
    }

    public static boolean disableCache() {
        try {
            backportCache.close();
            return true;
        } catch(Exception e) {
            Log.d(TAG, "Disable cache exception " + e.getClass().getSimpleName());
            return false;
        }
    }

    public static boolean uninstallCache() {
        try {
            backportCache.delete();
            return true;
        } catch(Exception e) {
            Log.d(TAG, "Uninstall cache exception " + e.getClass().getSimpleName());
            return false;
        }
    }

}
