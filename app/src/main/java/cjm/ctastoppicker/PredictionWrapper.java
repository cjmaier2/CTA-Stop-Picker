package cjm.ctastoppicker;

import android.content.Context;
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

public class PredictionWrapper {
    //sample call: http://www.ctabustracker.com/bustime/api/v1/getpredictions?key=Kb2wG89RmRWPA5Knst6gtmw8H&rt=60&stpid=15993
    private static final String apiURL = "http://www.ctabustracker.com/bustime/api/v1/";
    private static final String key = "key=Kb2wG89RmRWPA5Knst6gtmw8H";

    private String stopId;
    private String routeNum;
    public ArrayList<Prediction> predictions;

    public PredictionWrapper(String stopId, String routeNum) {
        this.stopId = stopId;
        this.routeNum = routeNum;
        predictions = new ArrayList<Prediction>();
    }

    public void initiatePredictionRequest(Context curContext, final MainActivity mainActivity) {
            getHttpResponse(curContext, new PredictionWrapper.VolleyCallback() {
                @Override
                public void onSuccess(String result) {
                    try {
                        predictions = PredictionWrapper.populatePredictions(result);
                        mainActivity.setPredictionView();
                    } catch (XmlPullParserException e) {
                        System.out.println("XmlPullParserException");
                    } catch (IOException e) {
                        System.out.println("IOException");
                    }
                }
            });
    }

    public static void initiateRouteRequest(Context curContext) {
        getHttpResponseRoute(curContext, new PredictionWrapper.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    PredictionWrapper.populateDbWithRoutes(result);
                } catch (XmlPullParserException e) {
                    System.out.println("XmlPullParserException");
                } catch (IOException e) {
                    System.out.println("IOException");
                }
            }
        });
    }

    public void getHttpResponse(Context curContext, final VolleyCallback callback) {
        // Source: https://developer.android.com/training/volley/simple.html#manifest
        RequestQueue queue = Volley.newRequestQueue(curContext);
        String url = "";
            url = apiURL+"getpredictions?"+key+"&rt="+routeNum+"&stpid="+stopId;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callback.onSuccess(response);
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

    public static void getHttpResponseRoute(Context curContext, final VolleyCallback callback) {
        // Source: https://developer.android.com/training/volley/simple.html#manifest
        RequestQueue queue = Volley.newRequestQueue(curContext);
        String url = "";
        url = apiURL+"getroutes?"+key;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callback.onSuccess(response);
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

    public static ArrayList<Prediction> populatePredictions(String resp) throws XmlPullParserException, IOException
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

    public static void populateDbWithRoutes(String resp) throws XmlPullParserException, IOException
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
                    long rowID = MainActivity.stopsTable.mDatabaseOpenHelper.addWord(routeNumber, routeColor);
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
        Cursor queryResults = MainActivity.stopsTable.mDatabaseOpenHelper.getWordMatches(
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

    //getters
    public String getStopId() {
        return stopId;
    }

    public String getRouteNum() {
        return routeNum;
    }
}
