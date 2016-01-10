package barqsoft.footballscores;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import barqsoft.footballscores.service.myFetchService;

/**
 * Created by yehya khaled on 2/27/2015.
 */
public class PagerFragment extends Fragment
{
    private static final long MILLI_DAY = 86400000;
    private static final int PAST_DAY_OFFSET = 2;
    public static final int NUM_PAGES = 5;
    public ViewPager mPagerHandler;
    private myPageAdapter mPagerAdapter;
    private MainScreenFragment[] mViewFragments = new MainScreenFragment[NUM_PAGES];  // replace 5 with constant NUM_PAGES

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.pager_fragment, container, false);
        mPagerHandler = (ViewPager) rootView.findViewById(R.id.pager);
        mPagerAdapter = new myPageAdapter(getChildFragmentManager());
        mPagerHandler.setAdapter(mPagerAdapter);
        mPagerHandler.setCurrentItem(MainActivity.current_fragment);
        for (int i = 0; i < NUM_PAGES; i++)
        {
            mViewFragments[i] = new MainScreenFragment();
            mViewFragments[i].setFragmentDate(getActivity(), System.currentTimeMillis() + ((i - PAST_DAY_OFFSET) * MILLI_DAY));
        }

        // make the tab titles big enough to read easily
        PagerTabStrip pts = (PagerTabStrip)rootView.findViewById(R.id.pager_header);
        pts.setTextSize(TypedValue.COMPLEX_UNIT_PT, 12);

        // trigger the service to fetch the scores
        update_scores();

        return rootView;
    }

    private void update_scores()
    {
        Intent service_start = new Intent(getActivity(), myFetchService.class);
        getActivity().startService(service_start);
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
