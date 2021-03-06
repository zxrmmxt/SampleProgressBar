package com.xt.sampleprogressbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

import com.blankj.utilcode.util.ScreenUtils;
import com.xt.progressbar.rangeseekbar.RangeSliderBar;

public class MainActivity extends AppCompatActivity {
    private RangeSliderBar mRangeSliderBar;

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

        {
            mRangeSliderBar = (RangeSliderBar) findViewById(R.id.RangeSliderBar);
            mRangeSliderBar.setRangeCount(5); //设置分段的个数
            mRangeSliderBar.setContext(this);
            mRangeSliderBar.setTexts(new String[]{"关闭", "低", "中", "标准", "高"}); //设置分段的数据
        }
        {
            SeekBar seekBar = findViewById(R.id.seekBar5);
            int intrinsicWidth = seekBar.getThumb().getIntrinsicWidth();
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) seekBar.getLayoutParams();
            layoutParams.width = (int) (ScreenUtils.getScreenWidth() * 0.7f)+intrinsicWidth;
            seekBar.setLayoutParams(layoutParams);
            seekBar.setPadding(intrinsicWidth/2, 0, intrinsicWidth/2, 0);
            seekBar.setThumbOffset(intrinsicWidth/2);
        }
    }
}
