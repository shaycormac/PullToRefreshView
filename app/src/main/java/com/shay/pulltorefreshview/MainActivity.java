package com.shay.pulltorefreshview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //gradlew clean build bintrayUpload -PbintrayUser=shay -PbintrayKey=4caf631212db58b0de4c0dcf2abcc4d2316840bb -PdryRun=false
    }
}
