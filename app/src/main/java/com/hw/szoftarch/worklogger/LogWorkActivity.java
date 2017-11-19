package com.hw.szoftarch.worklogger;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class LogWorkActivity extends AppCompatActivity implements View.OnClickListener {
    private Boolean isFabOpen = false;
    private FloatingActionButton fab, fabManual, fabStopwatch;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_work);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        fabManual = findViewById(R.id.fab_manual);
        fabStopwatch = findViewById(R.id.fab_stopwatch);
        fab.setOnClickListener(this);
        fabManual.setOnClickListener(this);
        fabStopwatch.setOnClickListener(this);

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fab:
                animateFAB();
                break;
            case R.id.fab_manual:
                break;
            case R.id.fab_stopwatch:
                break;
        }
    }

    public void animateFAB() {
        if (isFabOpen) {
            fab.startAnimation(rotate_backward);
            fabManual.startAnimation(fab_close);
            fabStopwatch.startAnimation(fab_close);
            fabManual.setClickable(false);
            fabStopwatch.setClickable(false);
            isFabOpen = false;
        } else {
            fab.startAnimation(rotate_forward);
            fabManual.startAnimation(fab_open);
            fabStopwatch.startAnimation(fab_open);
            fabManual.setClickable(true);
            fabStopwatch.setClickable(true);
            isFabOpen = true;
        }
    }
}
