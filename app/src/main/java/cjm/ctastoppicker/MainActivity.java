package cjm.ctastoppicker;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class MainActivity extends AppCompatActivity
        implements AddStopDialogFragment.AddDialogListener,
        FindStopDialogFragment.FindDialogListener {
    //tabs
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddStopDialog();
            }
        });

        initContextsAndInflaters();
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void initContextsAndInflaters() {
        SectionsPagerAdapter.mainContext = this;
        PredictionGroup.mainContext = this;
        PredictionGroup.menuInflater = getMenuInflater();
        PredictionGroup.layoutInflater = getLayoutInflater();
        PredictionWrapper.setContext(this);
    }


    public void openAddStopDialog() {
        DialogFragment newFragment = new AddStopDialogFragment();
        newFragment.show(getSupportFragmentManager(), "addStop");
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    public void onDialogPositiveClick(AddStopDialogFragment dialog) {
//        predictionWrappers.add(new PredictionWrapper(dialog.stopId, dialog.routeNum));
//        fileHandler.saveJson(mainContext, predictionWr
        SectionsPagerAdapter.addPredictionWrapper(mViewPager.getCurrentItem(), new PredictionWrapper(dialog.stopId, dialog.routeNum));
        SectionsPagerAdapter.savePredictionGroups();
//        mRequester.run();
    }

    @Override
    public void onDialogFindClick(AddStopDialogFragment dialog) {
        dialog.getDialog().cancel();
        DialogFragment newFragment = new FindStopDialogFragment();
        newFragment.show(getSupportFragmentManager(), "findStop");
    }

    @Override
    public void onDialogPositiveClick(FindStopDialogFragment dialog) {

    }

    @Override
    public void onBackPressed() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
        }
    }
}
