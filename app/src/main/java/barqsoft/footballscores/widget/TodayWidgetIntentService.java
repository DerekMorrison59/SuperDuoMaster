package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.widget.RemoteViews;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.data.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities;
import barqsoft.footballscores.data.scoresAdapter;

/**
 * Created by Derek on 1/11/2016.
 *
 * Structure copied from Sunshine app
 *
 * IntentService which handles updating all Today widgets with the latest data
 */
public class TodayWidgetIntentService extends IntentService {

    private static final String LOG_TAG = TodayWidgetIntentService.class.getSimpleName();

    public TodayWidgetIntentService() {
        super("TodayWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String NEXT_GAME = this.getString(R.string.match_info);
        String description;
        String START_TIME = getString(R.string.future_start_time);
        String HOME_TEAM = getString(R.string.home);
        String AWAY_TEAM = getString(R.string.away);

        // Retrieve all of the Today widget ids: these are the widgets to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                TodayWidgetProvider.class));

        //Log.d(LOG_TAG, "Call to onHandleIntent - widget count: " + String.valueOf(appWidgetIds.length));

        Date cutoffDate = new Date(System.currentTimeMillis());
        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
        String cutDate = mformat.format(cutoffDate);

        //Log.d(LOG_TAG, "Cut off date for next game DB search: " + cutDate);

        // show all future matches from the database (today, tomorrow and the next day)
        String scoreColumns = DatabaseContract.scores_table.DATE_COL + ">=?";
        String[] scoreSpecs = {cutDate};

        Cursor cursor = getContentResolver().query(
                //DatabaseContract.scores_table.buildScoreWithDate(),
                DatabaseContract.BASE_CONTENT_URI,
                null,
                scoreColumns,
                scoreSpecs,
                DatabaseContract.scores_table.DATE_COL + " ASC"
        );

        // no point in continuing if there is no data
        if (null == cursor) return;
        boolean cursorValid = cursor.moveToFirst();
        if (false == cursorValid) return;

        //int recordCount = cursor.getCount();
        //Log.d(LOG_TAG, "Number of records returned by DB search: " + String.valueOf(recordCount));

        // get the variables ready to find the next match
        int matchTime = 0;
        int currentTime = 0;

        SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
        String todaysDate = date_format.format(new Date());

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm"); // 24 hour clock
        String now = timeFormat.format(new Date());
        String match_time_string = "";
        String match_date_string = "";

        currentTime = Integer.parseInt(now.replace(":", ""));

        // search for the 'next' game by comparing the Match Start Time with the current time
        for (int i = 0; i < cursor.getCount(); i++){
            match_time_string = cursor.getString(scoresAdapter.COL_MATCHTIME);
            match_date_string = cursor.getString(scoresAdapter.COL_DATE);

            matchTime = Integer.parseInt(match_time_string.replace(":", ""));

            //Log.d(LOG_TAG, "onHandleIntent: time integer: " + String.valueOf(matchTime) + " now: " + String.valueOf(currentTime));

            // stop looking if the date changes (to tomorrow) or the match starts later than now
            if (!todaysDate.equals(match_date_string) || matchTime > currentTime) break;

            cursor.moveToNext();
        }

        // just in case the loop above goes all the way to the end of the cursor data
        if (cursor.isAfterLast()) cursor.moveToLast();

        String away = cursor.getString(scoresAdapter.COL_AWAY);
        String home = cursor.getString(scoresAdapter.COL_HOME);
        String time = cursor.getString(scoresAdapter.COL_MATCHTIME);
        String gameDate = cursor.getString(scoresAdapter.COL_DATE);
        int away_goals = cursor.getInt(scoresAdapter.COL_AWAY_GOALS);
        int home_goals = cursor.getInt(scoresAdapter.COL_HOME_GOALS);

        String score_text = "";

        // the 'score_text' will either display the 'Game Day' or the current score
        if (home_goals < 0 || away_goals < 0) {
            // convert the date stored in the database to a game day string
            // the date in the database is a string with the format shown below

            Date parseddate = null;

            try {
                parseddate = date_format.parse(gameDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            long milli = 0;
            if (parseddate != null) {
                milli = parseddate.getTime();
                score_text = Utilities.getDayName(this, milli);
            } else {
                score_text = getString(R.string.unknown);
            }

            description = NEXT_GAME + " " + score_text + ". " + START_TIME + " " + time
                    + ". " + HOME_TEAM + ". " + home + ". " + AWAY_TEAM + ". " + away;
        } else {

            score_text = Utilities.getScoresDisplay(getApplicationContext(), home_goals, away_goals);
            String score_text_voice = Utilities.getScoresVoice(home_goals, away_goals);

            description = HOME_TEAM + ". " + home + ". " + score_text_voice + ". " + AWAY_TEAM + ". " + away;
        }

        //Log.d(LOG_TAG, "onHandleIntent: description: " + description);

        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_today);

            views.setTextViewText(R.id.widget_away_name, away);
            views.setImageViewResource(R.id.widget_away_crest, Utilities.getTeamCrestByTeamName(away));

            views.setTextViewText(R.id.widget_home_name, home);
            views.setImageViewResource(R.id.widget_home_crest, Utilities.getTeamCrestByTeamName(home));

            views.setTextViewText(R.id.widget_score_textview, score_text);
            views.setTextViewText(R.id.widget_data_textview, time);

            // Content Descriptions for RemoteViews were only added in ICS MR1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, description);
            }

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);

            // Make sure that the user can tap anywhere on the small widget to launch the app
            views.setOnClickPendingIntent(R.id.widget_today_small, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

        // all data has been extracted from the cursor now
        cursor.close();
    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
        views.setContentDescription(R.id.widget_today_small, description);
    }
}
