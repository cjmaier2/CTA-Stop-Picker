package cjm.ctastoppicker;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

/**
 * Created by Chris on 12/11/2015.
 */
public class HttpRequestHandler {

    public static StringRequest getHttpResponse(String url, Context curContext, final VolleyCallback callback) {
        // Source: https://developer.android.com/training/volley/simple.html#manifest
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
        return stringRequest;
    }

    public interface VolleyCallback
    {
        void onSuccess(String result);
    }

}
