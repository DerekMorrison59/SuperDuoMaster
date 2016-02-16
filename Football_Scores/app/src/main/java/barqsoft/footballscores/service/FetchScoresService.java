package barqsoft.footballscores.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

import barqsoft.footballscores.data.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities;

/**
 * Created by yehya khaled on 3/2/2015.
 */
public class FetchScoresService extends IntentService
{
    public static final String LOG_TAG = "FetchScoresService";

    public static final String ACTION_DATA_UPDATED =
            "barqsoft.footballscores.app.ACTION_DATA_UPDATED";

    final String FIXTURES = "fixtures";
    final static int MILLIS_IN_A_DAY = 86400000;

    public FetchScoresService()
    {
        super("FetchScoresService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        // no point in trying to call server if there is no internet connection
        if (false == Utilities.isNetworkAvailable(getApplicationContext())){
            return;
        }

        // changed the call to get current & future data from "n2" to "n3" to ensure
        // that we get matches for the past 2 days, today and 2 days into the future
        getData("n3");
        getData("p2");
    }

    private void getData (String timeFrame)
    {
        //Creating fetch URL
        final String BASE_URL = "http://api.football-data.org/alpha/fixtures"; //Base URL
        final String QUERY_TIME_FRAME = "timeFrame"; //Time Frame parameter to determine days
        //final String QUERY_MATCH_DAY = "matchday";

        Uri fetch_build = Uri.parse(BASE_URL).buildUpon().
                appendQueryParameter(QUERY_TIME_FRAME, timeFrame).build();
        //Log.v(LOG_TAG, "The url we are looking at is: "+fetch_build.toString()); //log spam
        HttpURLConnection m_connection = null;
        BufferedReader reader = null;
        String JSON_data = null;
        //Opening Connection
        try {
            URL fetch = new URL(fetch_build.toString());
            m_connection = (HttpURLConnection) fetch.openConnection();
            m_connection.setRequestMethod("GET");
            m_connection.addRequestProperty("X-Auth-Token",getString(R.string.api_key));
            m_connection.connect();

            // Read the input stream into a String
            InputStream inputStream = m_connection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            JSON_data = buffer.toString();
        }
        catch (Exception e)
        {
            Log.e(LOG_TAG,getString(R.string.error_during_fetch) + e.getMessage());
        }
        finally {
            if(m_connection != null)
            {
                m_connection.disconnect();
            }
            if (reader != null)
            {
                try {
                    reader.close();
                }
                catch (IOException e)
                {
                    Log.e(LOG_TAG,getString(R.string.error_closing_stream));
                }
            }
        }
        try {
            if (JSON_data != null) {
                //This bit is to check if the data contains any matches. If not, we call processJson on the dummy data
                JSONArray matches = new JSONObject(JSON_data).getJSONArray(FIXTURES);
                if (matches.length() == 0) {
                    //if there is no data, call the function on dummy data
                    //this is expected behavior during the off season.
                    processJSONdata(getString(R.string.dummy_data), getApplicationContext(), false);
                    return;
                }
                processJSONdata(JSON_data, getApplicationContext(), true);
            } else {
                //Could not Connect
                Log.d(LOG_TAG, getString(R.string.error_could_not_connect));
            }
        }
        catch(Exception e)
        {
            Log.e(LOG_TAG,e.getMessage());
        }
    }
    private void processJSONdata (String JSONdata,Context mContext, boolean isReal)
    {
        //JSON data
        // The set of league codes is defined in the DatabaseContract.java file

        final String SEASON_LINK = getString(R.string.json_season_link);
        final String MATCH_LINK = getString(R.string.json_match_link);
        final String LINKS = getString(R.string.json_links);
        final String SOCCER_SEASON = getString(R.string.json_soccerseason);
        final String SELF = getString(R.string.json_self);
        final String MATCH_DATE = getString(R.string.json_match_date);
        final String HOME_TEAM = getString(R.string.json_home_team);
        final String AWAY_TEAM = getString(R.string.json_away_team);
        final String RESULT = getString(R.string.json_result);
        final String HOME_GOALS = getString(R.string.json_goals_home);
        final String AWAY_GOALS = getString(R.string.json_goals_away);
        final String MATCH_DAY = getString(R.string.json_matchday);

        //Match data
        String League = null;
        String mDate = null;
        String mTime = null;
        String Home = null;
        String Away = null;
        String Home_goals = null;
        String Away_goals = null;
        String match_id = null;
        String match_day = null;

        try {
            JSONArray matches = new JSONObject(JSONdata).getJSONArray(FIXTURES);

            int unusedMatches = 0;

            //ContentValues to be inserted
            Vector<ContentValues> values = new Vector <ContentValues> (matches.length());
            for(int i = 0;i < matches.length();i++)
            {

                JSONObject match_data = matches.getJSONObject(i);
                League = match_data.getJSONObject(LINKS).getJSONObject(SOCCER_SEASON).
                        getString("href");
                League = League.replace(SEASON_LINK,"");
                //This if statement controls which leagues we're interested in the data from.
                //add leagues here in order to have them be added to the DB.
                // If you are finding no data in the app, check that this contains all the leagues.
                // If it doesn't, that can cause an empty DB, bypassing the dummy data routine.
                if(     League.equals(DatabaseContract.Leagues.BUNDESLIGA1)      ||
                        League.equals(DatabaseContract.Leagues.BUNDESLIGA2)      ||
                        League.equals(DatabaseContract.Leagues.BUNDESLIGA3)      ||
                        League.equals(DatabaseContract.Leagues.CHAMPIONS_LEAGUE) ||
                        League.equals(DatabaseContract.Leagues.EREDIVISIE) ||
                        League.equals(DatabaseContract.Leagues.LIGUE1) ||
                        League.equals(DatabaseContract.Leagues.LIGUE2) ||
                        League.equals(DatabaseContract.Leagues.PREMIER_LEAGUE) ||
                        League.equals(DatabaseContract.Leagues.PRIMERA_DIVISION) ||
                        League.equals(DatabaseContract.Leagues.PRIMERA_LIGA) ||
                        League.equals(DatabaseContract.Leagues.SEGUNDA_DIVISION) ||
                        League.equals(DatabaseContract.Leagues.SERIE_A)
                  )
                {
                    match_id = match_data.getJSONObject(LINKS).getJSONObject(SELF).
                            getString("href");
                    match_id = match_id.replace(MATCH_LINK, "");
                    if(!isReal){
                        //This if statement changes the match ID of the dummy data so that it all goes into the database
                        match_id=match_id+Integer.toString(i);
                    }

                    mDate = match_data.getString(MATCH_DATE);
                    mTime = mDate.substring(mDate.indexOf("T") + 1, mDate.indexOf("Z"));
                    mDate = mDate.substring(0,mDate.indexOf("T"));
                    SimpleDateFormat match_date = new SimpleDateFormat(getString(R.string.date_string_format_to_seconds));
                    match_date.setTimeZone(TimeZone.getTimeZone("UTC"));
                    try {
                        Date parseddate = match_date.parse(mDate+mTime);
                        SimpleDateFormat new_date = new SimpleDateFormat(getString(R.string.date_string_format_to_minutes));
                        new_date.setTimeZone(TimeZone.getDefault());
                        mDate = new_date.format(parseddate);
                        mTime = mDate.substring(mDate.indexOf(":") + 1);
                        mDate = mDate.substring(0,mDate.indexOf(":"));

                        if(!isReal){
                            //This if statement changes the dummy data's date to match our current date range.
                            Date fragmentdate = new Date(System.currentTimeMillis()+((i-2)*MILLIS_IN_A_DAY));
                            SimpleDateFormat mformat = new SimpleDateFormat(getString(R.string.date_format_string));
                            mDate=mformat.format(fragmentdate);
                        }
                    }
                    catch (Exception e)
                    {
                        Log.d(LOG_TAG, getString(R.string.error_parse_date));
                        Log.e(LOG_TAG,e.getMessage());
                    }
                    Home = match_data.getString(HOME_TEAM);
                    Away = match_data.getString(AWAY_TEAM);
                    Home_goals = match_data.getJSONObject(RESULT).getString(HOME_GOALS);
                    Away_goals = match_data.getJSONObject(RESULT).getString(AWAY_GOALS);
                    match_day = match_data.getString(MATCH_DAY);

                    ContentValues match_values = new ContentValues();
                    match_values.put(DatabaseContract.scores_table.MATCH_ID,match_id);
                    match_values.put(DatabaseContract.scores_table.DATE_COL,mDate);
                    match_values.put(DatabaseContract.scores_table.TIME_COL,mTime);
                    match_values.put(DatabaseContract.scores_table.HOME_COL,Home);
                    match_values.put(DatabaseContract.scores_table.AWAY_COL,Away);
                    match_values.put(DatabaseContract.scores_table.HOME_GOALS_COL,Home_goals);
                    match_values.put(DatabaseContract.scores_table.AWAY_GOALS_COL,Away_goals);
                    match_values.put(DatabaseContract.scores_table.LEAGUE_COL,League);
                    match_values.put(DatabaseContract.scores_table.MATCH_DAY,match_day);

                    //log spam
                    //Log.v(LOG_TAG,match_id);
                    //Log.v(LOG_TAG,mDate);
                    //Log.v(LOG_TAG,mTime);
                    //Log.v(LOG_TAG,Home);
                    //Log.v(LOG_TAG,Away);
                    //Log.v(LOG_TAG,Home_goals);
                    //Log.v(LOG_TAG,Away_goals);

                    values.add(match_values);
                }
                else
                {
                    unusedMatches++;
                }
            }

            // used for debugging via logging
            int inserted_rows = 0;

            ContentValues[] insert_data = new ContentValues[values.size()];
            values.toArray(insert_data);

            // BUG FIXED: the old code did not delete database entries that had expired
            // Any match that is older than 2 days ago is not displayed and should
            // therefore be deleted from the database

            // used for debugging via logging
            int deleted_rows;

            // create a date string for 2 days ago
            Date cutoffDate = new Date(System.currentTimeMillis()-(2*MILLIS_IN_A_DAY));
            SimpleDateFormat mformat = new SimpleDateFormat(getString(R.string.date_format_string));
            String cutDate = mformat.format(cutoffDate);

            // use Less Than because the app may not have been run for a while and there could be old entries
            String scoreColumns = DatabaseContract.scores_table.DATE_COL + "<?";
            String[] scoreSpecs = {cutDate};

            // delete all matches that are older than 'cutDate' in the database
            deleted_rows = mContext.getContentResolver().delete(
                    DatabaseContract.BASE_CONTENT_URI,
                    scoreColumns,
                    scoreSpecs
            );

            //Log.v(LOG_TAG,"Deleted Rows : " + String.valueOf(deleted_rows));

            // only call insert if there are new items to insert
            if (insert_data.length > 0) {
                inserted_rows = mContext.getContentResolver().bulkInsert(
                        DatabaseContract.BASE_CONTENT_URI, insert_data);
                //Log.v(LOG_TAG,"Succesfully Inserted : " + String.valueOf(inserted_rows));
            }
            //Log.v(LOG_TAG,"Unused Matches : " + String.valueOf(unusedMatches));

            updateWidgets(mContext);
        }
        catch (JSONException e)
        {
            Log.e(LOG_TAG,e.getMessage());
        }
    }

    private void updateWidgets(Context context) {

        // Setting the package ensures that only components in this app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());

        //Log.v(LOG_TAG, "updateWidgets - package name: " + context.getPackageName());

        context.sendBroadcast(dataUpdatedIntent);
    }

}

