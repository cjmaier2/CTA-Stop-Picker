package cjm.ctastoppicker;

import android.content.Context;

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

/**
 * Created by Chris on 1/7/2016.
 */
public class FindStopParameters {
    private static RequestQueue queue;
    private static Context curContext;

    public ArrayList<String> routes;

    public FindStopParameters (Context context) {
        curContext = context;
    }

    public void getRoutes() {
        String url = HttpRequestHandler.apiURL + "getroutes?" + HttpRequestHandler.key;
        StringRequest sr = HttpRequestHandler.getHttpResponse(url, curContext, new HttpRequestHandler.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    parseRoutes(result);
                } catch (XmlPullParserException e) {
                    System.out.println("XmlPullParserException");
                } catch (IOException e) {
                    System.out.println("IOException");
                }
            }
        });
        HttpRequestHandler.queue.add(sr);
    }


    private void parseRoutes(String resp) throws XmlPullParserException, IOException
    {
        boolean openedTag = false;
        routes = new ArrayList<>();
        String route = "";

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
                if(tmp.equals("route")) {
                    routes.add(route);
                }
                else if(tmp.equals("error")) {
                    //TODO: deal with this
                }
                openedTag = false;
            } else if (eventType == XmlPullParser.TEXT && openedTag) {
                String tmp = xpp.getText();
                switch(tag) {
                    case "rt": route = tmp; break;
                    default: break;
                }
            }
            eventType = xpp.next();
        }

        String[] routesArr = new String[routes.size()];
        FindStopDialogFragment.fillRoutes(routes.toArray(routesArr));
    }

}
