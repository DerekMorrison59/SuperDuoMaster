package barqsoft.footballscores;

import android.content.Context;
import android.content.res.Configuration;
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
        if(league_num == CHAMPIONS_LEAGUE)
        {
            if (match_day <= 6)
            {
                return context.getString(R.string.group_stages_6);
            }
            else if(match_day == 7 || match_day == 8)
            {
                return context.getString(R.string.first_knockout_round);
            }
            else if(match_day == 9 || match_day == 10)
            {
                return context.getString(R.string.quarter_final);
            }
            else if(match_day == 11 || match_day == 12)
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
        if (teamname==null){return R.drawable.no_icon;}
        switch (teamname)
        {   //This is the set of icons that are currently in the app. Feel free to find and add more as you go.

            // it is ok to leave these strings here because they are only used once and they do not change by locale or language
            case "Arsenal London FC" : return R.drawable.arsenal;
            case "Manchester United FC" : return R.drawable.manchester_united;
            case "Swansea City" : return R.drawable.swansea_city_afc;
            case "Leicester City" : return R.drawable.leicester_city_fc_hd_logo;
            case "Everton FC" : return R.drawable.everton_fc_logo1;
            case "West Ham United FC" : return R.drawable.west_ham;
            case "Tottenham Hotspur FC" : return R.drawable.tottenham_hotspur;
            case "West Bromwich Albion" : return R.drawable.west_bromwich_albion_hd_logo;
            case "Sunderland AFC" : return R.drawable.sunderland;
            case "Stoke City FC" : return R.drawable.stoke_city;
            case "Udinese Calcio" : return R.drawable.udinese_calcio;
            case "Atalanta BC" : return R.drawable.atalanta;
            case "AS Roma" : return R.drawable.as_roma;
            case "FC Barcelona" : return R.drawable.barcelona_fc;

            // TODO Add more Team Crests to the resource folders
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
}
