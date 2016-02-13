package barqsoft.footballscores;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import barqsoft.footballscores.service.FetchScoresService;

/**
 * Created by yehya khaled on 2/27/2015.
 */
public class PagerFragment extends Fragment
{
    private final String LOG_TAG = PagerFragment.class.getSimpleName();

    private static final long MILLI_DAY = 86400000;
    private static final int PAST_DAY_OFFSET = 2;
    private static final int NUM_PAGES = 5;
    private long mDateInMillis;

    private ViewPager mPagerHandler;
    private myPageAdapter mPagerAdapter;
    private MainScreenFragment[] mViewFragments = new MainScreenFragment[NUM_PAGES];  // replace 5 with constant NUM_PAGES

    public int getCurrentPage(){
        int page = PAST_DAY_OFFSET;
        if (mPagerHandler != null){
            page = mPagerHandler.getCurrentItem();
        }
        return page;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.pager_fragment, container, false);

        mPagerHandler = (ViewPager) rootView.findViewById(R.id.pager);
        mPagerAdapter = new myPageAdapter(getChildFragmentManager());
        mPagerHandler.setAdapter(mPagerAdapter);
        mPagerHandler.setCurrentItem(MainActivity.getCurrentFragment());
        int day_offset = 0;
        mDateInMillis = System.currentTimeMillis();
        boolean layoutRTL = false;

        Configuration config = getContext().getResources().getConfiguration();

        // something is causing this app to override the Layout Direction setting to always be LTR
        // therefore, manually force the Pager ordering based on the configuration setting
        if (config.getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL) {
            layoutRTL = true;
        }

        for (int i = 0; i < NUM_PAGES; i++)
        {
            if (layoutRTL){
                day_offset = PAST_DAY_OFFSET - i;
            } else {
                day_offset = i - PAST_DAY_OFFSET;
            }

            mViewFragments[i] = new MainScreenFragment();
            mViewFragments[i].setDayOffset(getContext(), day_offset);
        }

        // make the tab titles big enough to read easily
        PagerTabStrip pts = (PagerTabStrip)rootView.findViewById(R.id.pager_header);
        pts.setTextSize(TypedValue.COMPLEX_UNIT_PT, 10);
        pts.setTextColor(ContextCompat.getColor(getContext(), R.color.material_white));

        // trigger the service to fetch the scores
        update_scores();

        return rootView;
    }

    private void update_scores()
    {
        Intent service_start = new Intent(getActivity(), FetchScoresService.class);
        getActivity().startService(service_start);
    }

    @Override
    public void onStart(){
        super.onStart();
        // the value 'mDateInMillis' was saved when this Fragment was created or last updated below
        String currentDayName = Utilities.getDayName(getContext(), mDateInMillis);

        // if the day has changed then update this page
        if (!currentDayName.equals(getString(R.string.today))) {

            // save 'now' as the new Today date in Millis
            mDateInMillis = System.currentTimeMillis();

            // update the scores in the database from the server
            update_scores();
        }
    }


    private class myPageAdapter extends FragmentStatePagerAdapter
    {
        @Override
        public Fragment getItem(int i)
        {
            return mViewFragments[i];
        }

        @Override
        public int getCount()
        {
            return NUM_PAGES;
        }

        public myPageAdapter(FragmentManager fm)
        {
            super(fm);
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position)
        {
            // use the date that was assigned at start-up, not one based on the current time
            return mViewFragments[position].getDayName();
        }
    }
}
