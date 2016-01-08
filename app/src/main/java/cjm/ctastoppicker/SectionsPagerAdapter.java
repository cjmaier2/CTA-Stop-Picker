package cjm.ctastoppicker;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;

/*
 * content from StackOverflow
 * http://stackoverflow.com/questions/13664155/dynamically-add-and-remove-view-to-viewpager
 * Q&A by Peri Hartman - http://stackoverflow.com/users/1022836/peri-hartman
 */

class SectionsPagerAdapter extends FragmentPagerAdapter
{
    // This holds all the currently displayable views, in order from left to right.
    private static ArrayList<PredictionGroup> predictionGroups = new ArrayList<>();
    private static FileHandler fileHandler;
    public static Context mainContext;

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
        fileHandler = new FileHandler();
        loadPredictionGroups();
    }

    @Override
    public Fragment getItem(int position) {
        if(position < predictionGroups.size()) {
            return predictionGroups.get(position);
        }
        PredictionGroup f = PredictionGroup.newInstance(position);
        predictionGroups.add(f);
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
        return predictionGroups.get(position).getGroupName();
    }

    public static void savePredictionGroups() {
        fileHandler.saveJson(mainContext, predictionGroups);
    }

    public static void loadPredictionGroups() {
        try {
            predictionGroups = fileHandler.readJson(mainContext);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}