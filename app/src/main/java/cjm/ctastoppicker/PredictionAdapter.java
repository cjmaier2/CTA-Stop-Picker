package cjm.ctastoppicker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;

import java.util.ArrayList;

public class PredictionAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<Prediction> predictions;

    public PredictionAdapter(Context c, ArrayList<Prediction> predictions) {
        mContext = c;
        this.predictions = predictions;
    }

    public int getCount() {
        return predictions.size();
    }

    public Object getItem(int position) {
        return null; //TODO
    }

    public long getItemId(int position) {
        return 0; //TODO
    }

    // create a new View for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        View predictionView;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            Prediction curPrediction = predictions.get(position);
            predictionView = inflater.inflate(R.layout.prediction_layout, null);
            TextView routeView = (TextView) predictionView.findViewById(R.id.route);
            TextView minutesText = (TextView) predictionView.findViewById(R.id.minutes);
            TextView stopText = (TextView) predictionView.findViewById(R.id.stop);
            TextView dirText = (TextView) predictionView.findViewById(R.id.direction);
            routeView.setText(curPrediction.routeNumber);
            minutesText.setText(curPrediction.timeRemaining.toString()+"m");
            stopText.setText(curPrediction.stopName);
            dirText.setText(curPrediction.direction);
        } else {
            predictionView = convertView;
        }
        return predictionView;
    }
}
