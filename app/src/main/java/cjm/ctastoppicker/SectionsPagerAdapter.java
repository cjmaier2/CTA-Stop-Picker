package cjm.ctastoppicker;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/*
 * content from StackOverflow
 * http://stackoverflow.com/questions/13664155/dynamically-add-and-remove-view-to-viewpager
 * Q&A by Peri Hartman - http://stackoverflow.com/users/1022836/peri-hartman
 */

class SectionsPagerAdapter extends FragmentPagerAdapter
{
    // This holds all the currently displayable views, in order from left to right.
    private ArrayList<View> views = new ArrayList<>();
    private ArrayList<PredictionGroup> pgList = new ArrayList<>();

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if(position < pgList.size()) {
            return pgList.get(position);
        }
        PredictionGroup f = PredictionGroup.newInstance(position);
        pgList.add(f);
        return f;
    }

    //-----------------------------------------------------------------------------
    // Used by ViewPager; can be used by app as well.
    // Returns the total number of pages that the ViewPage can display.  This must
    // never be 0.
    @Override
    public int getCount ()
    {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "SECTION " + String.valueOf(position+1);
    }
}