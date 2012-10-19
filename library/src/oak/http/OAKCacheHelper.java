package oak.http;

// User: jnye
// Date: 9/18/12
// Time: 10:50 AM
// Copyright (c) 2012 WillowTree Apps, Inc. All rights reserved.

import android.net.http.HttpResponseCache;
import android.util.Log;

import java.io.File;

public class OAKCacheHelper {

    public static final String TAG = OAKCacheHelper.class.getSimpleName();

    private static HttpResponseCache androidCache;
    private static com.integralblue.httpresponsecache.HttpResponseCache backportCache;

    public static void initialize() {
        try {
            androidCache = HttpResponseCache.getInstalled();
        } catch (NoClassDefFoundError err) {
            // Nothin' yet
        } catch (Exception e) {
            // Nothin' yet
        }

        try {
            backportCache = com.integralblue.httpresponsecache.HttpResponseCache.getInstalled();
        } catch(Exception e) {
            // Nothin' yet
        }
    }

    public static boolean enableCache(File cacheDir, long cacheSizeInBytes) {

        try {
            HttpResponseCache.install(cacheDir, cacheSizeInBytes);
            androidCache = HttpResponseCache.getInstalled();
            return true;
        } catch (NoClassDefFoundError err) {
            return enableBackportCache(cacheDir, cacheSizeInBytes);
        } catch(Exception e) {
            return enableBackportCache(cacheDir, cacheSizeInBytes);
        }
    }

    private static boolean enableBackportCache(File cacheDir, long cacheSizeInBytes) {
        Log.d("OAKCacheHelper", "Trying integralblue HttpResponseCache");
        try {
            com.integralblue.httpresponsecache.HttpResponseCache.install(cacheDir, cacheSizeInBytes);
            backportCache = com.integralblue.httpresponsecache.HttpResponseCache.getInstalled();
            return true;
        }catch(Exception e) {
            Log.d("OAKCacheHelper", "Failed to set up integralblue HttpResponseCache");
            return false;
        }
    }

    public static boolean disableCache() {
        try {
            if(androidCache != null) {
                androidCache.close();
                return true;
            } else if(backportCache != null) {
                backportCache.close();
                return true;
            }
        } catch(Exception e) {
            Log.d(TAG, "Disable cache exception " + e.getClass().getSimpleName());
        }
        return false;
    }

    public static boolean uninstallCache() {
        try {
            if(androidCache != null) {
                androidCache.delete();
                return true;
            } else if(backportCache != null) {
                backportCache.close();
                return true;
            }
        } catch(Exception e) {
            Log.d(TAG, "Uninstall cache exception " + e.getClass().getSimpleName());

        }
        return false;
    }

}
