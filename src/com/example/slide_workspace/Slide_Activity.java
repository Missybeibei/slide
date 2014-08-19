package com.example.slide_workspace;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class Slide_Activity extends Activity {

	com.example.slide_workspace.SlideWorkspace slide;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_);
        //slide = (com.example.slide_workspace.SlideWorkspace)findViewById(R.id.slide);
        //slide.initScreens();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_slide_, menu);
        return true;
    }
}
