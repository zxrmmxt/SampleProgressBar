package com.xt.sampleprogressbar;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*{
            ShapeDrawable thumb = new ShapeDrawable(new OvalShape());
            thumb.getPaint().setColor(0xFF3FA8F4);
            thumb.setIntrinsicHeight(AppCommonUiUtils.dp2px(33));
            thumb.setIntrinsicWidth(AppCommonUiUtils.dp2px(33));
            mVerticalSeekBar.setThumb(thumb);

            mVerticalSeekBar.setMax(100);
        }*/
    }
}
