package barqsoft.footballscores;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import barqsoft.footballscores.service.ScoreAlarmReceiver;

public class MainActivity extends AppCompatActivity //ActionBarActivity
{
    private static String LOG_TAG = MainActivity.class.getSimpleName();
    private static int mSelectedMatchID;

    private static int mCurrentFragment = 2;
    private final String save_tag = LOG_TAG + " Save Test ";
    private PagerFragment mPager_fragment;

    public static int getCurrentFragment() { return mCurrentFragment; }
    public static int getSelected_match_id() {
        return mSelectedMatchID;
    }

    public static void setSelected_match_id(int selected_match_id) {
        MainActivity.mSelectedMatchID = selected_match_id;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Log.d(LOG_TAG, "Reached MainActivity onCreate");
        if (savedInstanceState == null) {
            mPager_fragment = new PagerFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mPager_fragment)
                    .commit();
        }

        scheduleAlarm();
    }

/*
    https://github.com/codepath/android_guides/wiki/Starting-Background-Services

    Setup a recurring alarm every hour that causes the scores database to be updated
*/
    public void scheduleAlarm() {

        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), ScoreAlarmReceiver.class);

        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, ScoreAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                AlarmManager.INTERVAL_HOUR, pIntent);

        //Log.d(LOG_TAG, "Alarm Manager Configured to trigger updates via ScoreAlarmReceiver every 30 minutes");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about)
        {
            Intent start_about = new Intent(this,AboutActivity.class);
            startActivity(start_about);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
//        Log.v(save_tag,"will save");
//        Log.v(save_tag,"fragment: " + String.valueOf(mPager_fragment.getCurrentPage())); // mPagerHandler.getCurrentItem()));
//        Log.v(save_tag, "selected id: " + getSelected_match_id());
        outState.putInt(getString(R.string.key_current_pager), mPager_fragment.getCurrentPage()); // mPagerHandler.getCurrentItem());
        outState.putInt(getString(R.string.key_selected_match), getSelected_match_id());
        getSupportFragmentManager().putFragment(outState,getString(R.string.key_pager_fragment), mPager_fragment);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
//        Log.v(save_tag,"will retrive");
//        Log.v(save_tag,"fragment: "+String.valueOf(savedInstanceState.getInt("Pager_Current")));
//        Log.v(save_tag,"selected id: "+savedInstanceState.getInt("Selected_match"));
        mCurrentFragment = savedInstanceState.getInt(getString(R.string.key_current_pager));
        setSelected_match_id(savedInstanceState.getInt(getString(R.string.key_selected_match)));
        mPager_fragment = (PagerFragment) getSupportFragmentManager().getFragment(savedInstanceState,getString(R.string.key_pager_fragment));
        super.onRestoreInstanceState(savedInstanceState);
    }
}
