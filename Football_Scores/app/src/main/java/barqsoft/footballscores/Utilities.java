package barqsoft.footballscores;

import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.view.ViewCompat;
import android.text.format.Time;

import java.text.SimpleDateFormat;

/**
 * Created by yehya khaled on 3/3/2015.
 */
public class Utilities
{
    // FIXED - updated the league numbers
    public static final int BUNDESLIGA1 = 394;
    public static final int BUNDESLIGA2 = 395;
    public static final int LIGUE1 = 396;
    public static final int LIGUE2 = 397;
    public static final int PREMIER_LEAGUE = 398;
    public static final int PRIMERA_DIVISION = 399;
    public static final int SEGUNDA_DIVISION = 400;
    public static final int SERIE_A = 401;
    public static final int PRIMERA_LIGA = 402;
    public static final int BUNDESLIGA3 = 403;
    public static final int EREDIVISIE = 404;
    public static final int CHAMPIONS_LEAGUE = 362;

    public static String getLeague(Context context, int league_num)
    {
        switch (league_num)
        {
            case CHAMPIONS_LEAGUE:
                return context.getString(R.string.champions_league);
            case SERIE_A:
                return context.getString(R.string.seriaa);
            case PREMIER_LEAGUE:
                return context.getString(R.string.premierleague);
            case PRIMERA_DIVISION:
                return context.getString(R.string.primeradivison);
            case BUNDESLIGA1:
                return context.getString(R.string.bundesliga1);
            case BUNDESLIGA2:
                return context.getString(R.string.bundesliga2);
            case BUNDESLIGA3:
                return context.getString(R.string.bundesliga3);
            case LIGUE1:
                return context.getString(R.string.ligue1);
            case LIGUE2:
                return context.getString(R.string.ligue2);
            case SEGUNDA_DIVISION:
                return context.getString(R.string.segunda_division);
            case PRIMERA_LIGA:
                return context.getString(R.string.primera_liga);
            case EREDIVISIE:
                return context.getString(R.string.eredivise);
            default:
                return context.getString(R.string.unknown_league_error_msg);
        }
    }
    public static String getMatchDay(Context context, int match_day,int league_num)
    {
        final int GROUP_STAGES = 6;
        final int FIRST_ROUND_KNOCKOUT_1 = 7;
        final int FIRST_ROUND_KNOCKOUT_2 = 8;
        final int QUARTER_FINAL_1 = 9;
        final int QUARTER_FINAL_2 = 10;
        final int SEMI_FINAL_1 = 11;
        final int SEMI_FINAL_2 = 12;

        if (league_num == CHAMPIONS_LEAGUE)
        {
            if (match_day <= GROUP_STAGES)
            {
                return context.getString(R.string.group_stages_6) + String.valueOf(match_day);
            }
            else if (match_day == FIRST_ROUND_KNOCKOUT_1 || match_day == FIRST_ROUND_KNOCKOUT_2)
            {
                return context.getString(R.string.first_knockout_round);
            }
            else if (match_day == QUARTER_FINAL_1 || match_day == QUARTER_FINAL_2)
            {
                return context.getString(R.string.quarter_final);
            }
            else if (match_day == SEMI_FINAL_1 || match_day == SEMI_FINAL_2)
            {
                return context.getString(R.string.semi_final);
            }
            else
            {
                return context.getString(R.string.final_text);
            }
        }
        else
        {
            return context.getString(R.string.matchday) + String.valueOf(match_day);
        }
    }

    public static String getScoresDisplay(Context context, int home_goals, int awaygoals)
    {
        if(home_goals < 0 || awaygoals < 0)
        {
            return " - ";
        }
        else
        {
            Configuration config = context.getResources().getConfiguration();

            if (config.getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL){
                return String.valueOf(awaygoals) + " - " + String.valueOf(home_goals);
            } else {
                return String.valueOf(home_goals) + " - " + String.valueOf(awaygoals);
            }
        }
    }

    public static String getScoresVoice(int home_goals, int awaygoals)
    {
        if(home_goals < 0 || awaygoals < 0)
        {
            return " - ";
        }
        else
        {
            return String.valueOf(home_goals) + " - " + String.valueOf(awaygoals);
        }
    }

    public static int getTeamCrestByTeamName (String teamname)
    {
        // it is ok to leave these strings here because they are only used once and they do not change by locale or language
        final String ARSENAL_LONDON_FC = "Arsenal London FC";
        final String MANCHESTER_UNITED_FC = "Manchester United FC";
        final String SWANSEA_CITY = "Swansea City";
        final String LEICESTER_CITY = "Leicester City";
        final String EVERTON_FC = "Everton FC";
        final String WEST_HAM_UNITED_FC = "West Ham United FC";
        final String TOTTENHAM_HOTSPUR_FC = "Tottenham Hotspur FC";
        final String WEST_BROMWICH_ALBION = "West Bromwich Albion";
        final String SUNDERLAND_AFC = "Sunderland AFC";
        final String STOKE_CITY_FC = "Stoke City FC";
        final String UDINESE_CALCIO = "Udinese Calcio";
        final String ATALANTA_BC = "Atalanta BC";
        final String AS_ROMA = "AS Roma";
        final String FC_BARCELONA = "FC Barcelona";

        // showing the No Icon image is a tip that there was no team name returned from the server
        if (null == teamname) { return R.drawable.no_icon; }

        switch (teamname)
        {   //This is the set of icons that are currently in the app. Feel free to find and add more as you go.

            case ARSENAL_LONDON_FC : return R.drawable.arsenal;
            case MANCHESTER_UNITED_FC : return R.drawable.manchester_united;
            case SWANSEA_CITY : return R.drawable.swansea_city_afc;
            case LEICESTER_CITY : return R.drawable.leicester_city_fc_hd_logo;
            case EVERTON_FC : return R.drawable.everton_fc_logo1;
            case WEST_HAM_UNITED_FC : return R.drawable.west_ham;
            case TOTTENHAM_HOTSPUR_FC : return R.drawable.tottenham_hotspur;
            case WEST_BROMWICH_ALBION : return R.drawable.west_bromwich_albion_hd_logo;
            case SUNDERLAND_AFC : return R.drawable.sunderland;
            case STOKE_CITY_FC : return R.drawable.stoke_city;
            case UDINESE_CALCIO : return R.drawable.udinese_calcio;
            case ATALANTA_BC : return R.drawable.atalanta;
            case AS_ROMA : return R.drawable.as_roma;
            case FC_BARCELONA : return R.drawable.barcelona_fc;

            // TODO Add more Team Crests to the resource folders or find a service to provide them on demand
            // Atalanta BC
            // AC Chievo Verona
            // Juventus Turin
            // Hellas Verona FC
            // SS Lazio
            // Carpi FC
            // AC Milan
            // Bologna FC

            //default: return R.drawable.no_icon;
            // changed to make the display more friendly
            default: return R.drawable.soccerball;
        }
    }

    // converts a date (in milliseconds) into a user friendly string like 'Yesterday', 'Tuesday', etc.
    public static String getDayName(Context context, long dateInMillis) {
        // If the date is today, yesterday or tomorrow then
        // return the localized version of "Today" (etc). instead of the actual day name.

        Time t = new Time();
        t.setToNow();
        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
        if (julianDay == currentJulianDay) {
            return context.getString(R.string.today);
        } else if ( julianDay == currentJulianDay +1 ) {
            return context.getString(R.string.tomorrow);
        }
        else if ( julianDay == currentJulianDay -1)
        {
            return context.getString(R.string.yesterday);
        }
        else
        {
            Time time = new Time();
            time.setToNow();
            // Otherwise, the format is just the day of the week (e.g "Wednesday".
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
            return dayFormat.format(dateInMillis);
        }
    }

    public static long getDateFromOffset(int dayOffset){
        final long MILLI_DAY = 86400000;

        return System.currentTimeMillis() + dayOffset * MILLI_DAY;
    }

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
