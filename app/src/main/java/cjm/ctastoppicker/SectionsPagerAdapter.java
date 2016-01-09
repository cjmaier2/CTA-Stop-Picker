package cjm.ctastoppicker;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;

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

    public static void addPredictionWrapper(int curTabIdx, PredictionWrapper predWrap) {
        predictionGroups.get(curTabIdx).addPredictionWrapper(predWrap);
    }

    public static void savePredictionGroups() {
        fileHandler.saveJson(mainContext, predictionGroups);
    }

    public static void loadPredictionGroups() {
//        predictionGroups = new ArrayList<>();
//        ArrayList<PredictionWrapper> predictionWrappers = new ArrayList<PredictionWrapper>();
//        predictionWrappers.add(new PredictionWrapper("6344", "60"));
//        PredictionGroup pg = PredictionGroup.newInstance(0);
//        pg.predictionWrappers = predictionWrappers;
//        predictionGroups.add(pg);
//        predictionGroups.add(PredictionGroup.newInstance(1));
//        predictionGroups.add(PredictionGroup.newInstance(2));

        try {
            ArrayList<PredictionGroupStub> predictionGroupsFromFile = fileHandler.readJson(mainContext);
//            for (int i = 0; i < predictionGroupsFromFile.size(); i++) {
            if (predictionGroupsFromFile != null) {
                for (int i = 0; i < 3; i++) {
                    PredictionGroup pg = PredictionGroup.newInstance(i);
                    pg.initPredictionGroup(predictionGroupsFromFile.get(i));
                    predictionGroups.add(pg);
                }
            } else { //no saved predictionwrappers
                predictionGroups.add(PredictionGroup.newInstance(0));
                predictionGroups.add(PredictionGroup.newInstance(1));
                predictionGroups.add(PredictionGroup.newInstance(2));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}