package cjm.ctastoppicker;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements AddStopDialogFragment.AddDialogListener {
    public static ArrayList<Prediction> predictions;
    public static ArrayList<PredictionWrapper> predictionWrappers;
    public static DatabaseTable stopsTable;

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
        mRequester.run(); //starts timer

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

        stopsTable = new DatabaseTable(this);

        new DatabaseTask().execute("", "", "");
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

    void stopTimer() { //TODO: is this necessary?
        mHandler.removeCallbacks(mRequester);
    }

    private void initiatePredictionRequest() {
        if(predictionWrappers != null) {
            for (PredictionWrapper predictionWrapper: predictionWrappers) {
                predictionWrapper.initiatePredictionRequest(this, MainActivity.this);
            }
        }
        if(srl != null) srl.setRefreshing(false);
    }

    //called when any httprequest finishes
    public void setPredictionView() { //TODO: set timer s.t. don't call this within x seconds
        predictions.clear();
        if (predictionWrappers != null) {
            for (PredictionWrapper predictionWrapper: predictionWrappers) {
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

    //asynctask because http://stackoverflow.com/questions/14678593/the-application-may-be-doing-too-much-work-on-its-main-thread
    private class DatabaseTask extends AsyncTask<String, String, String> {
        protected String doInBackground(String... test1) {
//            PredictionWrapper.initiateRouteRequest(this); //TODO: uncomment to test db stuff
            return "";
        }

        protected void onProgressUpdate(String... test2) {
        }

        protected void onPostExecute(String test3) {
        }
    }
}
