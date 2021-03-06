package cjm.ctastoppicker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class PredictionAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<Prediction> predictions;

    public PredictionAdapter(Context c, ArrayList<Prediction> predictions) {
        mContext = c;
        this.predictions = predictions;
    }

    public int getCount() {
        if(predictions != null)
            return predictions.size();
        else return 0;
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
            stopText.setText(curPrediction.stopName);
            dirText.setText(curPrediction.direction);
            if(curPrediction.errorMessage == null) {
                minutesText.setText(curPrediction.timeRemaining.toString() + "m");
            }
            else {
                minutesText.setText(curPrediction.errorMessage);
            }
        } else {
            predictionView = convertView;
        }
        return predictionView;
    }
}
