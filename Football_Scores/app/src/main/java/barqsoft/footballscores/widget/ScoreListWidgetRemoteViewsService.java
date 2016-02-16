package barqsoft.footballscores.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.data.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities;
import barqsoft.footballscores.data.scoresAdapter;

/**
 * Created by Derek on 1/13/2016.
 */
public class ScoreListWidgetRemoteViewsService extends RemoteViewsService {
    public static final String TAG = ScoreListWidgetRemoteViewsService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor mCursor = null;

            @Override
            public void onCreate() {
                //Log.d(TAG, "onCreate");
            }

            @Override
            public void onDestroy() {
                if (mCursor != null) {
                    mCursor.close();
                    mCursor = null;
                }
            }

            @Override
            public void onDataSetChanged() {
                //Log.d(TAG, "onDataSetChanged");
                if (mCursor != null) {
                    mCursor.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // mCursor. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();

                Date cutoffDate = new Date(System.currentTimeMillis());
                SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
                String cutDate = mformat.format(cutoffDate);

                // Log.d(TAG, "onDataSetChanged: Cut off date for next game DB search: " + cutDate);

                String scoreColumns = DatabaseContract.scores_table.DATE_COL + ">=?";
                String[] scoreSpecs = {cutDate};

                mCursor = getContentResolver().query(
                        DatabaseContract.BASE_CONTENT_URI,
                        null,
                        scoreColumns,
                        scoreSpecs,
                        DatabaseContract.scores_table.DATE_COL + " ASC"
                );

                //int recordCount = mCursor.getCount();
                // Log.d(TAG, "Number of records returned by DB search: " + String.valueOf(recordCount));

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public RemoteViews getViewAt(int position) {
                String NEXT_GAME = getString(R.string.match_info);
                String description;
                String START_TIME = getString(R.string.future_start_time);
                String HOME_TEAM = getString(R.string.home);
                String AWAY_TEAM = getString(R.string.away);
                String SCORE = getString(R.string.score);

                if (position == AdapterView.INVALID_POSITION ||
                        mCursor == null || !mCursor.moveToPosition(position)) {
                    return null;
                }

                // create the list item views
                final RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_score_list_item);

                String away = mCursor.getString(scoresAdapter.COL_AWAY);
                String home = mCursor.getString(scoresAdapter.COL_HOME);
                String gameTime = mCursor.getString(scoresAdapter.COL_MATCHTIME);

                String gameDate = mCursor.getString(scoresAdapter.COL_DATE);
                int home_score = mCursor.getInt(scoresAdapter.COL_HOME_GOALS);
                int away_score = mCursor.getInt(scoresAdapter.COL_AWAY_GOALS);

                String score_text = "";

                // the 'score_text' will either display the 'Game Day' or the current score
                if (home_score < 0 || away_score < 0) {
                    // convert the date stored in the database to a game day string
                    // the date in the database is a string with the format shown below
                    SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
                    Date parseddate = null;

                    try {
                        parseddate = date_format.parse(gameDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    long milli = 0;
                    if (parseddate != null) {
                        milli = parseddate.getTime();
                        score_text = Utilities.getDayName(getApplicationContext(), milli);
                    } else {
                        score_text = getString(R.string.unknown);
                    }

                    description = NEXT_GAME + " " + score_text + ". " + START_TIME + " " + gameTime
                            + ". " + HOME_TEAM + ". " + home + ". " + AWAY_TEAM + ". " + away;
                } else {
                    score_text = Utilities.getScoresDisplay(getApplicationContext(), home_score, away_score);
                    String score_text_voice = Utilities.getScoresVoice(home_score, away_score);

                    description = NEXT_GAME + ". " + HOME_TEAM + ". " + home + ". " + SCORE + ". " +
                            score_text_voice + ". " + START_TIME + ". " + gameTime + "." + AWAY_TEAM + ". " + away;
                }

                views.setContentDescription(R.id.widget_score_list_item_container, description);
                //Log.d(TAG, "description: " + description);

                views.setTextViewText(R.id.widget_score_list_item_home_name, home);
                views.setTextViewText(R.id.widget_score_list_item_away_name, away);
                views.setTextViewText(R.id.widget_score_list_item_score, score_text);
                views.setTextViewText(R.id.widget_score_list_item_gametime, gameTime);

                views.setImageViewResource(R.id.widget_score_list_item_home_crest, Utilities.getTeamCrestByTeamName(home));
                views.setImageViewResource(R.id.widget_score_list_item_away_crest, Utilities.getTeamCrestByTeamName(away));

                // details about the FillInIntent come from this example
                //http://docs.huihoo.com/android/3.0/resources/samples/StackWidget/src/com/example/android/stackwidget/index.html

                // Next, we set a fill-intent which will be used to fill-in the pending intent template
                // which is set on the collection view in StackWidgetProvider.
                Bundle extras = new Bundle();
                extras.putInt(ScoreListWidgetProvider.EXTRA_ITEM, position);
                Intent fillInIntent = new Intent();
                fillInIntent.putExtras(extras);
                views.setOnClickFillInIntent(R.id.widget_score_list_item_container, fillInIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_score_list_item);
            }

            @Override
            public long getItemId(int position) {
                if (mCursor.moveToPosition(position))
                    return mCursor.getLong(scoresAdapter.COL_ID);
                return position;
            }

            @Override
            public int getCount() {
                return mCursor == null ? 0 : mCursor.getCount();
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
