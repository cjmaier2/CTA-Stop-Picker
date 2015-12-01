package cjm.ctastoppicker;

import android.content.Context;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * Created by Chris on 11/30/2015.
 */
public class FileHandler
{
    public FileHandler() { }

    public ArrayList<PredictionWrapper> readJson(Context context) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = context.openFileInput("data.txt");
        }
        catch (FileNotFoundException e) {
            Log.e("read json", "Can't read file: " + e.toString());
        }
        if ( inputStream != null ) {
            JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            try {
                return readPredictionWrapperArray(reader);
            }
            finally{
                reader.close();
            }
        }
        return null;
    }

    private ArrayList<PredictionWrapper> readPredictionWrapperArray(JsonReader reader) throws IOException {
        ArrayList<PredictionWrapper> predWraps = new ArrayList();

        reader.beginArray();
        while (reader.hasNext()) {
            predWraps.add(readPredictionWrapper(reader));
        }
        reader.endArray();
        return predWraps;
    }

    private PredictionWrapper readPredictionWrapper(JsonReader reader) throws IOException {
        String stopId = "";
        String routeNum = "";

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("stopId")) {
                stopId = reader.nextString();
            } else if (name.equals("routeNum")) {
                routeNum = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new PredictionWrapper(stopId, routeNum);
    }

    public void saveJson(Context context, ArrayList<PredictionWrapper> predictionWrappers) {
        try {
            String filename = "data.txt";
            FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            writeJsonStream(outputStream, predictionWrappers);
            outputStream.close();
        } catch (Exception e) {
            Log.e("write json", "Can't write file: " + e.toString());
            e.printStackTrace();
        }
    }

    public void writeJsonStream(OutputStream out, ArrayList<PredictionWrapper> predWraps) throws IOException {
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
        writer.setIndent("  ");
        writePredictionWrapperArray(writer, predWraps);
        writer.close();
    }

    private void writePredictionWrapperArray(JsonWriter writer, ArrayList<PredictionWrapper> predWraps) throws IOException {
        writer.beginArray();
        for (PredictionWrapper predWrap : predWraps) {
            writePredictionWrapper(writer, predWrap);
        }
        writer.endArray();
    }

    private void writePredictionWrapper(JsonWriter writer, PredictionWrapper predWrap) throws IOException {
        writer.beginObject();
        writer.name("stopId").value(predWrap.getStopId());
        writer.name("routeNum").value(predWrap.getRouteNum());
        writer.endObject();
    }
}
