package cjm.ctastoppicker;

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
    DatabaseTable stopsTable;

    public static enum RequestType {
        Prediction,
        Route
    }

    SwipeRefreshLayout srl;
    private static Handler mHandler;
    private static final int mInterval = 30000; //ms

    //Source: http://stackoverflow.com/questions/6242268/repeat-a-task-with-a-time-delay
    Runnable mRequester = new Runnable() {
        @Override
        public void run() {
            initiatePredictionRequest();
            //mHandler.postDelayed(mRequester, mInterval); //commented out for testing DB stuff
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
//        initiateRouteRequest();
    }

    void startTimer() {
        mRequester.run();
    }

    void stopTimer() {
        mHandler.removeCallbacks(mRequester);
    }

    public void initiatePredictionRequest() {
        getHttpResponse(0, new VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    predictions = populatePredictions(result);
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

    public void initiateRouteRequest() {
        getHttpResponse(1, new VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    populateDbWithRoutes(result);
                } catch (XmlPullParserException e) {
                    System.out.println("XmlPullParserException");
                } catch (IOException e) {
                    System.out.println("IOException");
                }
            }
        });
        if(srl != null) srl.setRefreshing(false);
    }

    public void getHttpResponse(int type, final VolleyCallback callback) {
        // Source: https://developer.android.com/training/volley/simple.html#manifest
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "";
        if(type == 0)
            url = apiURL+"getpredictions?"+key+"&rt=60&stpid=15993";
        else if(type == 1)
            url = apiURL+"getroutes?"+key;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String result = response;
                        callback.onSuccess(result);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("Http request error");
                    }
                }
        );
        queue.add(stringRequest);
    }

    public interface VolleyCallback
    {
        void onSuccess(String result);
    }

    public void setPredictionView(ArrayList<Prediction> predictions) {
        // Source: http://developer.android.com/guide/topics/ui/layout/gridview.html
        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new PredictionAdapter(this, predictions));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(MainActivity.this, "" + position,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private ArrayList<Prediction> populatePredictions(String resp) throws XmlPullParserException, IOException
    {
        boolean openedTag = false;
        ArrayList<Prediction> returnPredictions = new ArrayList<>();
        String requestTime = "";
        String predictionType = "";
        String stopName = "";
        String stopID = "";
        String vehicleID = "";
        String distanceToStop = "";
        String routeNumber = "";
        String direction = "";
        String destination = "";
        String predictionTime = "";

        // Source: http://developer.android.com/reference/org/xmlpull/v1/XmlPullParser.html
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();

        xpp.setInput(new StringReader(resp));
        String tag = "";
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_DOCUMENT) {
            } else if (eventType == XmlPullParser.START_TAG) {
                tag = xpp.getName();
                openedTag = true;
            } else if (eventType == XmlPullParser.END_TAG) {
                if(xpp.getName().equals("prd")) {
                    returnPredictions.add(new Prediction(requestTime, predictionType, stopName, stopID,
                            vehicleID, distanceToStop, routeNumber, direction, destination, predictionTime));
                }
                openedTag = false;
            } else if (eventType == XmlPullParser.TEXT && openedTag) {
                String tmp = xpp.getText();
                DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                switch(tag) {
                    case "tmstmp": requestTime = tmp; break;
                    case "typ": predictionType = tmp; break;
                    case "stpnm": stopName = tmp; break;
                    case "stpid": stopID = tmp; break;
                    case "vid": vehicleID = tmp; break;
                    case "dstp": distanceToStop = tmp; break;
                    case "rt": routeNumber = tmp; break;
                    case "rtdir": direction = tmp; break;
                    case "des": destination = tmp; break;
                    case "prdtm": predictionTime = tmp; break;
                    default: break;
                }
            }
            eventType = xpp.next();
        }
        return returnPredictions;
    }

    private void populateDbWithRoutes(String resp) throws XmlPullParserException, IOException
    {
        boolean openedTag = false;
        String routeNumber = "";
        String routeColor = "";

        // Source: http://developer.android.com/reference/org/xmlpull/v1/XmlPullParser.html
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();

        xpp.setInput(new StringReader(resp));
        String tag = "";
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_DOCUMENT) {
            } else if (eventType == XmlPullParser.START_TAG) {
                tag = xpp.getName();
                openedTag = true;
            } else if (eventType == XmlPullParser.END_TAG) {
                if(xpp.getName().equals("route")) {
                    long rowID = stopsTable.mDatabaseOpenHelper.addWord(routeNumber, routeColor);
                }
                openedTag = false;
            } else if (eventType == XmlPullParser.TEXT && openedTag) {
                String tmp = xpp.getText();
                DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                switch(tag) {
                    case "rt": routeNumber = tmp; break;
                    case "rtclr": routeColor = tmp; break;
                    default: break;
                }
            }
            eventType = xpp.next();
        }
        String[] tmpArr = new String[]{"ROUTES"};
        Cursor queryResults = stopsTable.mDatabaseOpenHelper.getWordMatches(
                "#c8c8c8",
                tmpArr
        );
        //Source: http://stackoverflow.com/questions/2810615/how-to-retrieve-data-from-cursor-class
        if(queryResults != null) {
            if (queryResults.moveToFirst()) {
                do {
                    String data = queryResults.getString(queryResults.getColumnIndex("ROUTES"));
                    System.out.println("Query results: " + data);
                } while (queryResults.moveToNext());
            }
            queryResults.close();
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
