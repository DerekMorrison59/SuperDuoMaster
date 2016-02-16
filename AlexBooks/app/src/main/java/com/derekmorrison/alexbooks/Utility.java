package com.derekmorrison.alexbooks;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Derek on 1/24/2016.
 */
public class Utility {

    public static boolean isNetworkAvailable(Context context) {

        ConnectivityManager connectivityManager = null;
        boolean isAvailable = false;

        if (context != null){
            connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }

        // Taken from  http://developer.android.com/training/monitoring-device-state/connectivity-monitoring.html
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

            // the active network must exist and it must be connected (or in the process of connecting)
            isAvailable = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }

        return isAvailable;
    }

}
