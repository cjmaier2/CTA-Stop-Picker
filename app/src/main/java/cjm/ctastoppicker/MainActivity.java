package cjm.ctastoppicker;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements
        AddStopDialogFragment.AddDialogListener, FindStopDialogFragment.FindDialogListener {
    public static ArrayList<Prediction> predictions;
    public static ArrayList<PredictionWrapper> predictionWrappers;

    SwipeRefreshLayout srl;
    GridView gridview;
    public static PredictionAdapter adapter;
    private static Handler mHandler;
    private static final int mInterval = 30000; //ms
    FileHandler fileHandler;

    //Source: http://stackoverflow.com/questions/6242268/repeat-a-task-with-a-time-delay
    Runnable mRequester = new Runnable() {
        @Override
        public void run() {
            initiatePredictionRequest();
            mHandler.postDelayed(mRequester, mInterval);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        srl = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        srl.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        mRequester.run();
                    }
                }
        );

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddStopDialog();
            }
        });

        // Source: http://developer.android.com/guide/topics/ui/layout/gridview.html
        gridview = (GridView) findViewById(R.id.gridview);
        predictions = new ArrayList<Prediction>();
        adapter = new PredictionAdapter(this, predictions);
        gridview.setAdapter(adapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(MainActivity.this, predictions.get(position).timeRemaining + " minutes remaining",
                        Toast.LENGTH_SHORT).show();
            }
        });
        MainActivity.this.registerForContextMenu(gridview);

        PredictionWrapper.setContext(MainActivity.this);

        fileHandler = new FileHandler();
        try {
            predictionWrappers = fileHandler.readJson(MainActivity.this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (predictionWrappers == null) {
            predictionWrappers = new ArrayList<PredictionWrapper>();
        }

        mRequester.run();
    }

    @Override
    public void onResume() {
        super.onResume();
        mRequester.run();
    }

    public void openAddStopDialog() {
        DialogFragment newFragment = new AddStopDialogFragment();
        newFragment.show(getSupportFragmentManager(), "addStop");
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    public void onDialogPositiveClick(AddStopDialogFragment dialog) {
        predictionWrappers.add(new PredictionWrapper(dialog.stopId, dialog.routeNum));
        fileHandler.saveJson(MainActivity.this, predictionWrappers);
        mRequester.run();
    }

    @Override
    public void onDialogFindClick(AddStopDialogFragment dialog) {
        DialogFragment newFragment = new FindStopDialogFragment();
        newFragment.show(getSupportFragmentManager(), "findStop");
    }

    void stopTimer() { //TODO: is this necessary?
        mHandler.removeCallbacks(mRequester);
    }

    private void initiatePredictionRequest() {
        if (predictionWrappers != null) {
            for (PredictionWrapper predictionWrapper : predictionWrappers) {
                predictionWrapper.initiatePredictionRequest(this);
            }
        }
        if (srl != null) srl.setRefreshing(false);
    }

    //called when any httprequest finishes
    public void setPredictionView() { //TODO: set timer s.t. don't call this within x seconds
        predictions.clear();
        if (predictionWrappers != null) {
            for (PredictionWrapper predictionWrapper : predictionWrappers) {
                predictions.addAll(predictionWrapper.predictions);
            }
        }
        Collections.sort(predictions);
        //TODO: more efficient way to do this?
        adapter = new PredictionAdapter(this, predictions);
        gridview.setAdapter(adapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(MainActivity.this, predictions.get(position).timeRemaining + " minutes remaining",
                        Toast.LENGTH_SHORT).show();
            }
        });

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete:
                UUID idToRemove = predictions.get(info.position).predictionWrapperId;
                for (int i = 0; i < predictionWrappers.size(); i++) {
                    if (predictionWrappers.get(i).id.compareTo(idToRemove) == 0) {
                        predictionWrappers.remove(i);
                        fileHandler.saveJson(MainActivity.this, predictionWrappers);
                        mRequester.run();
                        return true;
                    }
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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

    @Override
    public void onDialogPositiveClick(FindStopDialogFragment dialog) {
        //TODO: write this
    }
}
