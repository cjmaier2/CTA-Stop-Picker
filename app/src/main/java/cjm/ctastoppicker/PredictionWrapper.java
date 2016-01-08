package cjm.ctastoppicker;

import android.content.Context;
import android.widget.TextView;

import com.android.volley.RequestQueue;
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
import java.util.UUID;

public class PredictionWrapper {
    public UUID id;
    private String stopId;
    private String routeNum;
    public ArrayList<Prediction> predictions;
    private static Context curContext;

    public static void setContext(Context context) {
        curContext = context;
    }

    public PredictionWrapper(String stopId, String routeNum) {
        id = UUID.randomUUID();
        this.stopId = stopId;
        this.routeNum = routeNum;
        predictions = new ArrayList<Prediction>();
    }

    //create from saved json
    public PredictionWrapper(UUID id, String stopId, String routeNum) {
        this.id = id;
        this.stopId = stopId;
        this.routeNum = routeNum;
        predictions = new ArrayList<Prediction>();
    }

    public void initiatePredictionRequest(final PredictionGroup pg) {
        String url = HttpRequestHandler.apiURL+"getpredictions?"+HttpRequestHandler.key+"&rt="+routeNum+"&stpid="+stopId;
        StringRequest sr = HttpRequestHandler.getHttpResponse(url, curContext, new HttpRequestHandler.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    predictions = PredictionWrapper.populatePredictions(result, id);
                    pg.setPredictionView();
                } catch (XmlPullParserException e) {
                    System.out.println("XmlPullParserException");
                } catch (IOException e) {
                    System.out.println("IOException");
                }
            }
        });
        HttpRequestHandler.queue.add(sr);
    }

    public static ArrayList<Prediction> populatePredictions(String resp, UUID predictionWrapperId) throws XmlPullParserException, IOException
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
        String message = "";

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
                String tmp = xpp.getName();
                if(tmp.equals("prd")) {
                    returnPredictions.add(new Prediction(requestTime, predictionType, stopName, stopID,
                            vehicleID, distanceToStop, routeNumber, direction, destination, predictionTime,
                            predictionWrapperId));
                }
                else if(tmp.equals("error")) {
                    returnPredictions.add(new Prediction(message));
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
                    case "msg": message = tmp; break;
                    default: break;
                }
            }
            eventType = xpp.next();
        }
        return returnPredictions;
    }

    //getters
    public String getStopId() {
        return stopId;
    }

    public String getRouteNum() {
        return routeNum;
    }

    public UUID getId() {
        return id;
    }
}
