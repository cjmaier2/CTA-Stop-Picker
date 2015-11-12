package cjm.ctastoppicker;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import android.database.Cursor;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.EventListener;
import android.os.Handler;
import android.os.AsyncTask;

public class MainActivity extends AppCompatActivity implements EventListener {
    //sample call: http://www.ctabustracker.com/bustime/api/v1/getpredictions?key=Kb2wG89RmRWPA5Knst6gtmw8H&rt=60&stpid=15993
    public static final String apiURL = "http://www.ctabustracker.com/bustime/api/v1/";
    public static final String key = "key=Kb2wG89RmRWPA5Knst6gtmw8H";

    public static ArrayList<Prediction> predictions;
    public static DatabaseTable stopsTable;

    SwipeRefreshLayout srl;
    private static Handler mHandler;
    private static final int mInterval = 30000; //ms

    //Source: http://stackoverflow.com/questions/6242268/repeat-a-task-with-a-time-delay
    Runnable mRequester = new Runnable() {
        @Override
        public void run() {
            initiatePredictionRequest();
            //mHandler.postDelayed(mRequester, mInterval); //TODO: uncomment once done with db testing
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();
        startTimer();

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
                initiatePredictionRequest();
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });

        mRequester.run();

        stopsTable = new DatabaseTable(this);

        new DatabaseTask().execute("", "", "");
    }

    void startTimer() {
        mRequester.run();
    }

    void stopTimer() {
        mHandler.removeCallbacks(mRequester);
    }

    private void initiatePredictionRequest() {
        HttpRequestHandler.getHttpResponse(0, this, new HttpRequestHandler.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    predictions = HttpRequestHandler.populatePredictions(result);
                    setPredictionView(predictions);
                } catch (XmlPullParserException e) {
                    System.out.println("XmlPullParserException");
                } catch (IOException e) {
                    System.out.println("IOException");
                }
            }
        });
        if(srl != null) srl.setRefreshing(false);
    }

    private void initiateRouteRequest() {
        HttpRequestHandler.getHttpResponse(1, this, new HttpRequestHandler.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    HttpRequestHandler.populateDbWithRoutes(result);
                } catch (XmlPullParserException e) {
                    System.out.println("XmlPullParserException");
                } catch (IOException e) {
                    System.out.println("IOException");
                }
            }
        });
    }

    public void setPredictionView(ArrayList<Prediction> predictions) {
        // Source: http://developer.android.com/guide/topics/ui/layout/gridview.html
        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new PredictionAdapter(this, predictions));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(MainActivity.this, MainActivity.predictions.get(position).timeRemaining+" minutes remaining",
                        Toast.LENGTH_SHORT).show();
            }
        });
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
            initiateRouteRequest();
            return "";
        }

        protected void onProgressUpdate(String... test2) {
        }

        protected void onPostExecute(String test3) {
        }
    }
}
