package com.example.eric.photopicker.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.example.eric.photopicker.R;


/**
 * Created by eric on 2016/9/27.
 */
public class ShadowImageView extends ImageView{
    public ShadowImageView(Context context) {
        super(context);
        init();
    }

    public ShadowImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ShadowImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        this.setPadding(0,0,20,20);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint=new Paint();
        paint.setColor(getResources().getColor(R.color.text_grep));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        Path path = new Path();
        path.moveTo(this.getMeasuredWidth()-3, 20);
        path.lineTo(this.getMeasuredWidth()-3, this.getMeasuredHeight()-3);
        path.lineTo(20, this.getMeasuredHeight()-3);
        canvas.drawPath(path, paint);
        paint.setColor(getResources().getColor(R.color.text_black_light));
        path = new Path();
        path.moveTo(this.getMeasuredWidth()-12, 10);
        path.lineTo(this.getMeasuredWidth()-12, this.getMeasuredHeight()-12);
        path.lineTo(10, this.getMeasuredHeight()-12);
        canvas.drawPath(path, paint);
    }

}
