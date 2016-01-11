package cjm.ctastoppicker;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

public class PredictionGroup extends Fragment {
    public static Context mainContext;
    public static Context fragContext;
    public static MenuInflater menuInflater;
    public static LayoutInflater layoutInflater;
    private static final String ARG_SECTION_NUMBER = "section_number";

    public String groupName = "";
    public ArrayList<Prediction> allPredictionsInGroup;
    public ArrayList<PredictionWrapper> predictionWrappers;

    private int tabNumber;
    private SwipeRefreshLayout srl;
    private GridView gridview;
    public PredictionAdapter adapter;
    private Handler mHandler;
    private static final int mInterval = 30000; //ms
    FileHandler fileHandler;

    //Source: http://stackoverflow.com/questions/6242268/repeat-a-task-with-a-time-delay
    private Runnable mRequester = new Runnable() {
        @Override
        public void run() { //TODO: reduce # of times this is called
            stopTimer(); //"pause" timer
            initiatePredictionRequest();
        }
    };

    public PredictionGroup() {
    }

    public static PredictionGroup newInstance(int sectionNumber) {
        PredictionGroup fragment = new PredictionGroup();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public void initPredictionGroup(PredictionGroupStub pg) {
        groupName = pg.groupName;
        predictionWrappers = pg.predictionWrappers;
    }

    public String getGroupName() {
        return groupName;
    }

    public void addPredictionWrapper(PredictionWrapper pw) {
        if (srl != null) srl.setRefreshing(true);
        predictionWrappers.add(pw);
        mRequester.run();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        tabNumber = getArguments().getInt(ARG_SECTION_NUMBER);
        groupName = "Section "+tabNumber;

        View displayView = inflater.inflate(R.layout.prediction_group, container, false);
        srl = (SwipeRefreshLayout) displayView.findViewById(R.id.swiperefresh);
        srl.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if (predictionWrappers.size() == 0) {
                            srl.setRefreshing(false);
                            Toast.makeText(mainContext, "No stops have been added to this section yet", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        mRequester.run();
                    }
                }
        );

        fragContext = displayView.getContext();

        mHandler = new Handler();

        // Source: http://developer.android.com/guide/topics/ui/layout/gridview.html
        gridview = (GridView) displayView.findViewById(R.id.gridview);
        allPredictionsInGroup = new ArrayList<>();
        adapter = new PredictionAdapter(fragContext, allPredictionsInGroup);
//        adapter = new PredictionAdapter(mainContext, allPredictionsInGroup);
//        gridview.setAdapter(adapter);
//        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View v,
//                                    int position, long id) {
//                Toast.makeText(mainContext, allPredictionsInGroup.get(position).timeRemaining + " minutes remaining",
//                        Toast.LENGTH_SHORT).show();
//            }
//        });

        PredictionGroup.this.registerForContextMenu(gridview);

        if (predictionWrappers == null) {
            predictionWrappers = new ArrayList<PredictionWrapper>();
        }

        //TODO: remove this part
//        if (predictionWrappers.size() == 0 && tabNumber == 0)
//            predictionWrappers.add(new PredictionWrapper("15993", "60"));
//        if (predictionWrappers.size() == 0 && tabNumber == 1)
//            predictionWrappers.add(new PredictionWrapper("6344", "60"));
//        fileHandler.saveJson(mainContext, predictionWrappers);

        mRequester.run();
        return displayView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mRequester.run();
    }

    void startTimer() {
        if (srl != null) srl.setRefreshing(false);
        mHandler.postDelayed(mRequester, mInterval);
    }

    void stopTimer() {
        mHandler.removeCallbacks(mRequester);
    }

    private void initiatePredictionRequest() {
        if (predictionWrappers != null) {
            for (PredictionWrapper predictionWrapper : predictionWrappers) {
                predictionWrapper.initiatePredictionRequest(this);
            }
        }
    }

    //called when any httprequest finishes
    public void setPredictionView() { //TODO: set timer s.t. don't call this within x seconds
        allPredictionsInGroup.clear();
        if (predictionWrappers != null) {
            for (PredictionWrapper predictionWrapper : predictionWrappers) {
                allPredictionsInGroup.addAll(predictionWrapper.predictions);
            }
        }
        Collections.sort(allPredictionsInGroup);
//        allPredictionsInGroup.add(new Prediction("error"));
//        returnPredictions.add(new Prediction(requestTime, predictionType, stopName, stopID,
//                vehicleID, distanceToStop, routeNumber, direction, destination, predictionTime,
//                predictionWrapperId));
        refreshGrid();

        adapter.notifyDataSetChanged();
        startTimer(); //"continue" timer after pausing it
    }

    private void refreshGrid() {
        //TODO: more efficient way to do this?
        adapter = new PredictionAdapter(fragContext, allPredictionsInGroup);
        gridview.setAdapter(adapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(mainContext, allPredictionsInGroup.get(position).timeRemaining + " minutes remaining",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menuInflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete:
                PredictionGroup curPG = SectionsPagerAdapter.predictionGroups.get(MainActivity.getCurrentTabIndex());
                UUID idToRemove = curPG.allPredictionsInGroup.get(info.position).predictionWrapperId;
                //UUID idToRemove = UUID.randomUUID();
                removePredictionWrapper(curPG, idToRemove);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void removePredictionWrapper(PredictionGroup pg, UUID wrapperId) {
        for (int i = 0; i < pg.predictionWrappers.size(); i++) {
            if (pg.predictionWrappers.get(i).id.compareTo(wrapperId) == 0) {
                pg.predictionWrappers.remove(i);
                SectionsPagerAdapter.savePredictionGroups();
                //remove existing predictions with that wrapperId
                for (int j = 0; i < pg.allPredictionsInGroup.size(); i++) {
                    if(pg.allPredictionsInGroup.get(i).predictionWrapperId.compareTo(wrapperId) == 0) {
                        pg.allPredictionsInGroup.remove(i--);
                    }
                }
                pg.refreshGrid();
//                if (pg.predictionWrappers.size() == 0) {
//                    pg.allPredictionsInGroup.clear();
//                    pg.refreshGrid();
//                }
//                else mRequester.run();
                return;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.refresh) {
            mRequester.run();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
