package barqsoft.footballscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.data.DatabaseContract;
import barqsoft.footballscores.data.scoresAdapter;
import barqsoft.footballscores.service.FetchScoresService;


public class MainScreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    public final String LOG_TAG = MainScreenFragment.class.getSimpleName();

    private scoresAdapter mScoresAdapter;
    public static final int SCORES_LOADER = 0;
    long mDateInMilliseconds;
    int mDayOffset = 0;
    String mDayName;
    private String[] mFragmentdate = new String[1];
    //private int last_selected_item = -1;

    public MainScreenFragment()
    {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.action_refresh == item.getItemId()) {
            updateScores();
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    private void updateScores() {
        Intent intent = new Intent(getActivity(), FetchScoresService.class);
        getActivity().startService(intent);
    }

    public void setDayOffset(Context context, int dayOffset) {
        mDayOffset = dayOffset;
        setFragmentDate(context, Utilities.getDateFromOffset(mDayOffset));
    }

    public void setFragmentDate(Context context, long date){
        mDateInMilliseconds = date;
        Date fragmentdate = new Date(date);

        SimpleDateFormat mformat = new SimpleDateFormat(context.getString(R.string.date_format_string));

        mFragmentdate[0] = mformat.format(fragmentdate);

        mDayName = Utilities.getDayName(context, date);

        //Log.v(LOG_TAG, "setFragmentDate - mFragmentdate[0]: " + mFragmentdate[0] + " DayName: " + mDayName);
    }

    public long getFragmentDate() { return mDateInMilliseconds; }
    public String getDayName() { return mDayName; }

    @Override
    public void onStart(){
        super.onStart();
        //Log.v(LOG_TAG, "onStart");
        checkForDateChange(true);
    }

    private void checkForDateChange(boolean needsToReload){
        long currentDate = Utilities.getDateFromOffset(mDayOffset);
        String currentDayName = Utilities.getDayName(getContext(), currentDate);

        // if the day has changed then update this page
        if (null != mDayName && !currentDayName.equals(mDayName)) {

            //Log.v(LOG_TAG, "checkForDateChange - Day IS different - needsToReload: " + needsToReload);

            // save the new date & setup date related variables
            setFragmentDate(getContext(), currentDate);

            if (needsToReload) {
                // force the listView to reload
                getLoaderManager().restartLoader(SCORES_LOADER, null, this);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        final ListView score_list = (ListView) rootView.findViewById(R.id.tab_score_listview);

        TextView emptyView = (TextView) rootView.findViewById(R.id.tab_list_empty);
        score_list.setEmptyView(emptyView);

        mScoresAdapter = new scoresAdapter(getActivity(),null,0);
        score_list.setAdapter(mScoresAdapter);
        getLoaderManager().initLoader(SCORES_LOADER, null, this);

        score_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewHolder selected = (ViewHolder) view.getTag();

                // Toggle the existance of the 'detail_fragment' for this match
                // if the user clicks on a match that is showing League and Match Day (detail_fragment)
                // then reset the selected_match_id to 0 - that will hide the detail_fragment
                if (MainActivity.getSelected_match_id() == selected.match_id) {
                    MainActivity.setSelected_match_id(0);
                } else {
                    MainActivity.setSelected_match_id(selected.match_id);
                }

                //Log.v(LOG_TAG, "OnItemClickListener - selected Match ID: " + String.valueOf(MainActivity.getSelected_match_id()));

                mScoresAdapter.notifyDataSetChanged();
            }
        });

        setHasOptionsMenu(true);

        if (false == Utilities.isNetworkAvailable(getContext())) {
            Toast.makeText(getContext(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
        }

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        //Log.v(LOG_TAG, "onCreateLoader");

        checkForDateChange(false);

        return new CursorLoader(
                getActivity(),
                DatabaseContract.scores_table.buildScoreWithDate(),
                null,
                null,
                mFragmentdate,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
    {
        //Log.v(LOG_TAG, "loader finished");
        //cursor.moveToFirst();
        /*
        while (!cursor.isAfterLast())
        {
            Log.v(FetchScoreTask.LOG_TAG,cursor.getString(1));
            cursor.moveToNext();
        }

        int i = 0;
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            i++;
            cursor.moveToNext();
        }
        Log.v(LOG_TAG, "records in cursor: " + String.valueOf(i));
        */

        // changeCursor makes sure that the old Cursor is closed
        mScoresAdapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader)
    {
        mScoresAdapter.changeCursor(null);
    }
}
