package barqsoft.footballscores;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by yehya khaled on 2/26/2015.
 */
public class scoresAdapter extends CursorAdapter
{
    public final String LOG_TAG = scoresAdapter.class.getSimpleName();

    public static final int COL_DATE = 1;
    public static final int COL_MATCHTIME = 2;
    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_LEAGUE = 5;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_ID = 8;
    public static final int COL_MATCHDAY = 9;

    private String FOOTBALL_SCORES_HASHTAG = mContext.getString(R.string.scores_hashtag);
    public scoresAdapter(Context context,Cursor cursor,int flags)
    {
        super(context,cursor,flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        View mItem;

        Configuration config = context.getResources().getConfiguration();

        if (config.getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL){
            mItem = LayoutInflater.from(context).inflate(R.layout.scores_list_item_rtl, parent, false);
            //Log.w(LOG_TAG, "*** layout_direction CONFIG: LAYOUT_DIRECTION_RTL: " + ViewCompat.LAYOUT_DIRECTION_RTL + " ***");
        } else {
            mItem = LayoutInflater.from(context).inflate(R.layout.scores_list_item, parent, false);
        }

        ViewHolder mHolder = new ViewHolder(mItem);
        mItem.setTag(mHolder);
        //Log.v(FetchScoreTask.LOG_TAG,"new View inflated");
        return mItem;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor)
    {
        final ViewHolder mHolder = (ViewHolder) view.getTag();

        String home_name = cursor.getString(COL_HOME);
        String away_name = cursor.getString(COL_AWAY);
        String gameTime = cursor.getString(COL_MATCHTIME);
        int home_score =  cursor.getInt(COL_HOME_GOALS);
        int away_score = cursor.getInt(COL_AWAY_GOALS);


        mHolder.home_name.setText(home_name);
        mHolder.away_name.setText(away_name);
        mHolder.date.setText(gameTime);
        mHolder.score.setText(Utilities.getScoresDisplay(context, home_score, away_score));

        //Log.v(LOG_TAG, "Match Data: " + cursor.getString(COL_HOME) + " " + cursor.getInt(COL_HOME_GOALS) + "   " + cursor.getString(COL_AWAY) + " " + cursor.getInt(COL_AWAY_GOALS));

        // ERROR - The previous code set match_id as a Double but it's defined as Int in the database
        mHolder.match_id = cursor.getInt(COL_ID);

        mHolder.home_crest.setImageResource(Utilities.getTeamCrestByTeamName(cursor.getString(COL_HOME)));
        mHolder.away_crest.setImageResource(Utilities.getTeamCrestByTeamName(cursor.getString(COL_AWAY)));

        String NEXT_GAME = context.getString(R.string.match_info);
        String START_TIME = context.getString(R.string.future_start_time);
        String HOME_TEAM = context.getString(R.string.home);
        String AWAY_TEAM = context.getString(R.string.away);
        String SCORE = context.getString(R.string.score);

        String score_text_voice = Utilities.getScoresVoice(home_score, away_score);

        // override the default description because this view's Layout Direction never changes from LTR
        // this means that Talkback will say things in the wrong order when the direction is set to RTL
        String description = NEXT_GAME + ". " + HOME_TEAM + ". " + home_name + ". " + SCORE + ". " +
                score_text_voice + ". " + START_TIME + ". " + gameTime + "." + AWAY_TEAM + ". " + away_name;

        view.setContentDescription(description);

        //Log.v(FetchScoreTask.LOG_TAG,mHolder.home_name.getText() + " Vs. " + mHolder.away_name.getText() +" id " + String.valueOf(mHolder.match_id));
        //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(detail_match_id));
        LayoutInflater vi = (LayoutInflater) context.getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.detail_fragment, null);
        ViewGroup container = (ViewGroup) view.findViewById(R.id.details_fragment_container);
        if(mHolder.match_id == MainActivity.getSelected_match_id())
        {
            //Log.v(LOG_TAG, "will insert extraView for: " + String.valueOf(mHolder.match_id));

            container.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.MATCH_PARENT));
            TextView match_day = (TextView) v.findViewById(R.id.matchday_textview);
            match_day.setText(Utilities.getMatchDay(context, cursor.getInt(COL_MATCHDAY),
                    cursor.getInt(COL_LEAGUE)));
            TextView league = (TextView) v.findViewById(R.id.league_textview);
            league.setText(Utilities.getLeague(context, cursor.getInt(COL_LEAGUE)));
            Button share_button = (Button) v.findViewById(R.id.share_button);
            share_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    //add Share Action
                    context.startActivity(createShareForecastIntent(mHolder.home_name.getText()+" "
                    +mHolder.score.getText()+" "+mHolder.away_name.getText() + " "));
                }
            });
        }
        else
        {
            //Log.v(LOG_TAG, "will remove extraView for: " + String.valueOf(mHolder.match_id));
            container.removeAllViews();
        }

    }
    public Intent createShareForecastIntent(String ShareText) {
        final String intentType = "text/plain";
        Intent shareIntent = new Intent(Intent.ACTION_SEND);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        } else {
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        }

        shareIntent.setType(intentType);
        shareIntent.putExtra(Intent.EXTRA_TEXT, ShareText + FOOTBALL_SCORES_HASHTAG);
        return shareIntent;
    }

}
