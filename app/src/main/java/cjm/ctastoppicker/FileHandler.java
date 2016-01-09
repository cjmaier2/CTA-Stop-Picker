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
import java.util.UUID;

/**
 * Created by Chris on 11/30/2015.
 */
public class FileHandler
{
    public FileHandler() { }

    public ArrayList<PredictionGroupStub> readJson(Context context) throws IOException {
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
                return readPredictionGroupArray(reader);
            }
            finally{
                reader.close();
            }
        }
        return null;
    }

    private ArrayList<PredictionGroupStub> readPredictionGroupArray(JsonReader reader) throws IOException {
        ArrayList<PredictionGroupStub> predGroups = new ArrayList();

        reader.beginArray();
        while (reader.hasNext()) {
            reader.beginObject();
            PredictionGroupStub pg = new PredictionGroupStub();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("groupName")) {
                    pg.groupName = reader.nextString();
                } else if (name.equals("predictionWrappers")) {
                    pg.predictionWrappers = readPredictionWrapperArray(reader);
                }
            }
            predGroups.add(pg);
            reader.endObject();
        }
        reader.endArray();
        return predGroups;
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
        String id = "";

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("id")) {
                id = reader.nextString();
            } else if (name.equals("stopId")) {
                stopId = reader.nextString();
            } else if (name.equals("routeNum")) {
                routeNum = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        UUID uuid = UUID.fromString(id);
        return new PredictionWrapper(uuid, stopId, routeNum);
    }

    public void saveJson(Context context, ArrayList<PredictionGroup> predictionGroups) {
        try {
            String filename = "data.txt";
            FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            writeJsonStream(outputStream, predictionGroups);
            outputStream.close();
        } catch (Exception e) {
            Log.e("write json", "Can't write file: " + e.toString());
            e.printStackTrace();
        }
    }

    private void writeJsonStream(OutputStream out, ArrayList<PredictionGroup> predGroups) throws IOException {
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
        writer.setIndent("  ");
        writePredictionGroupArray(writer, predGroups);
        writer.close();
    }

    private void writePredictionGroupArray(JsonWriter writer, ArrayList<PredictionGroup> predGroups) throws IOException {
        writer.beginArray();
        for (PredictionGroup predGroup : predGroups) { //need object within array to hold name and wraps?
            writer.beginObject();
            writer.name("groupName").value(predGroup.getGroupName());
            writer.name("predictionWrappers");
            writePredictionWrapperArray(writer, predGroup.predictionWrappers);
            writer.endObject();
        }
        writer.endArray();
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
        writer.name("id").value(predWrap.getId().toString());
        writer.name("stopId").value(predWrap.getStopId());
        writer.name("routeNum").value(predWrap.getRouteNum());
        writer.endObject();
    }
}
