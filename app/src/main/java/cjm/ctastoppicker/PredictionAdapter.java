package cjm.ctastoppicker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
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
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return null; //TODO
    }

    public long getItemId(int position) {
        return 0; //TODO
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        View predictionView;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            Prediction curPrediction = predictions.get(position);
            predictionView = inflater.inflate(R.layout.prediction_layout, null);
            TextView routeView = (TextView) predictionView.findViewById(R.id.route);
            TextView minutesText = (TextView) predictionView.findViewById(R.id.minutes);
            routeView.setText(curPrediction.routeNumber);
            minutesText.setText(curPrediction.predictionTime.toString());
        } else {
            predictionView = convertView;
        }
        return predictionView;
    }

    // references to our images
    private Integer[] mThumbIds = {
            R.drawable.sample_2, R.drawable.sample_3,
            R.drawable.sample_4, R.drawable.sample_5,
            R.drawable.sample_6, R.drawable.sample_7,
            R.drawable.sample_0, R.drawable.sample_1,
            R.drawable.sample_2, R.drawable.sample_3,
            R.drawable.sample_4, R.drawable.sample_5,
            R.drawable.sample_6, R.drawable.sample_7,
            R.drawable.sample_0, R.drawable.sample_1,
            R.drawable.sample_2, R.drawable.sample_3,
            R.drawable.sample_4, R.drawable.sample_5,
            R.drawable.sample_6, R.drawable.sample_7
    };
}