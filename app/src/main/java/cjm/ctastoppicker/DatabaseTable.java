package cjm.ctastoppicker;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class DatabaseTable {
    //Source: http://developer.android.com/training/search/search.html#populate
    private static final String TAG = "StopDatabase";

    //The columns we'll include in the stop table
//    public static final String COL_STOPID = "ID";
//    public static final String COL_STOPNAME = "NAME";
    public static final String COL_ROUTES = "ROUTES";
    public static final String COL_ROUTECOLOR = "ROUTE_COLOR";
//    public static final String COL_DIRECTIONS = "DIRECTIONS";


    private static final String DATABASE_NAME = "STOPS";
    private static final String FTS_VIRTUAL_TABLE = "FTS";
    private static final int DATABASE_VERSION = 1;

    public final DatabaseOpenHelper mDatabaseOpenHelper;

    public DatabaseTable(Context context) {
        mDatabaseOpenHelper = new DatabaseOpenHelper(context);
    }

    public static class DatabaseOpenHelper extends SQLiteOpenHelper {

        private final Context mHelperContext;
        private SQLiteDatabase mDatabase;

        private static final String FTS_TABLE_CREATE =
                "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE +
                        " USING fts3 (" +
                        COL_ROUTES + ", " +
                        COL_ROUTECOLOR + ")";

        DatabaseOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mHelperContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            mDatabase = db;
            mDatabase.execSQL(FTS_TABLE_CREATE);
            loadDictionary();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
            onCreate(db);
        }

        private void loadDictionary() {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        loadWords();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }

        private void loadWords() throws IOException {
//            final Resources resources = mHelperContext.getResources();
//            InputStream inputStream = resources.openRawResource(R.raw.definitions);
//            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//
//            try {
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    String[] strings = TextUtils.split(line, "-");
//                    if (strings.length < 2) continue;
//                    long id = addWord(strings[0].trim(), strings[1].trim());
//                    if (id < 0) {
//                        Log.e(TAG, "unable to add word: " + strings[0].trim());
//                    }
//                }
//            } finally {
//                reader.close();
//            }
        }

        public long addWord(String word, String definition) {
            ContentValues initialValues = new ContentValues();
            initialValues.put(COL_ROUTES, word);
            initialValues.put(COL_ROUTECOLOR, definition);

            return mDatabase.insert(FTS_VIRTUAL_TABLE, null, initialValues);
        }
    }
}