package cjm.ctastoppicker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FindStopDialogFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FindStopDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FindStopDialogFragment extends DialogFragment {
    public String stopId = "";
    public String routeNum = "";
    private static View dialogView;
    private static Context context;
    private static Activity activity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        dialogView = inflater.inflate(R.layout.find_stop_dialog, null);
        context = getContext();
        activity = getActivity();

        builder.setView(dialogView)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText stopIdView = (EditText) dialogView.findViewById(R.id.stopid);
                        stopId = stopIdView.getText().toString();
                        EditText routeNumView = (EditText) dialogView.findViewById(R.id.routenum);
                        routeNum = routeNumView.getText().toString();
                        mListener.onDialogPositiveClick(FindStopDialogFragment.this);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FindStopDialogFragment.this.getDialog().cancel();
                    }
                });
        Dialog dialog = builder.create();

        //http://stackoverflow.com/questions/5469005/show-alertdialog-in-any-position-of-the-screen
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        wmlp.y = 50;   //y position
        return dialog;
    }

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface FindDialogListener {
        public void onDialogPositiveClick(FindStopDialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    FindDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (FindDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement FindDialogListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        FindStopParameters paramFinder = new FindStopParameters(context);
        paramFinder.getRoutes();
    }

    public static void fillRoutes (String[] route_options) {
        AutoCompleteTextView routesActv = (AutoCompleteTextView) dialogView.findViewById(R.id.routes_actv);
        //String[] route_options = {"1","2", "123", "234"};
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, route_options);
        routesActv.setAdapter(adapter);
    }
}
