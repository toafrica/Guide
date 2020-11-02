package com.toafrica.guidedemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.toafrica.guideview.ClickMode;
import com.toafrica.guideview.Guide;
import com.toafrica.guideview.Location;
import com.toafrica.guideview.Orientation;

public class MainActivity extends Activity {

    private View target_1;
    private View target_2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        target_1 = findViewById(R.id.target_1);
        target_2 = findViewById(R.id.target_2);
        new Guide.Builder(Orientation.PORTRAIT)
            .setTarget(target_1)
            .setCorner(5, 5)
            .setBackgroundColor(0xee000000)
            .location(Location.BOTTOM)
            .setHighLightPadding(10, 0, 8, 10)
            .setOffset(20, 20)
            .setGuide(R.layout.guide_1)
            .setRatio(0.58f)
            .setClickMode(ClickMode.GUIDE)
            .next()
            .setTarget(target_2)
            .location(Location.BOTTOM)
            .setClickMode(ClickMode.TARGET)
            .setOffset(20, 0)
            .setGuide(R.layout.guide_2)
            .setRatio(0.58f)
            .show();
    }
}