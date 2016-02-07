package barqsoft.footballscores.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Derek on 1/31/2016.
 *
 * https://github.com/codepath/android_guides/wiki/Starting-Background-Services
 *
 */
public class ScoreAlarmReceiver extends BroadcastReceiver {
    private static String LOG_TAG = ScoreAlarmReceiver.class.getSimpleName();

    public static final int REQUEST_CODE = 1312016;
    public static final String ACTION = "barqsoft.footballscores.service.alarm";

    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent fetchScoreIntent = new Intent(context, FetchScoresService.class);
        context.startService(fetchScoreIntent);

        //Log.d(LOG_TAG, "Periodic Alarm Received - launching the FetchScoresService...");
    }
}
