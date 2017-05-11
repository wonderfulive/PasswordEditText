package com.phoenix.passwordedittext;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.EditText;

import java.lang.reflect.Field;

/**
 * Created by lcf on 2017/3/23.
 */

public class PassWordEditText extends EditText {
    private static final int DEFAULT_LENGTH = 6;
    private static final int DEFAULT_PADDING = 1;

    private Paint bgPaint;
    private Paint arcPaint;
    private Paint passPaint;

    private int mMaxLength;
    private int mRadius;

    private boolean isAdd;
    private boolean isDel;

    private float rate;

    private int currentLength;

    private PaintLastAnimation animation;

    public PassWordEditText(Context context) {
        this(context, null);
    }

    public PassWordEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PassWordEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
        initParams();
    }

    private void initPaint() {
        bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setColor(Color.WHITE);
        bgPaint.setStyle(Paint.Style.FILL);

        arcPaint = new Paint();
        arcPaint.setAntiAlias(true);
        arcPaint.setColor(Color.GRAY);
        arcPaint.setStyle(Paint.Style.STROKE);

        passPaint = new Paint();
        passPaint.setAntiAlias(true);
        passPaint.setColor(Color.BLACK);
        passPaint.setStyle(Paint.Style.FILL);
    }

    private void initParams() {
        mMaxLength = getMaxLength();
        mRadius = dp2px(5);
        animation = new PaintLastAnimation();
        animation.setDuration(200);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private int getMaxLength() {
        int length = DEFAULT_LENGTH;
        InputFilter[] filters = getFilters();
        for (InputFilter filter : filters) {
            Class<?> c = filter.getClass();
            if ("android.text.InputFilter$LengthFilter".equals(c.getName())) {
                Field[] fields = c.getDeclaredFields();
                for (Field field : fields) {
                    if ("mMax".equals(field.getName())) {
                        field.setAccessible(true);
                        try {
                            length = field.getInt(filter);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return length;
    }

    private int dp2px(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        RectF rect = new RectF(0, 0, getMeasuredWidth() - DEFAULT_PADDING, getMeasuredHeight() - DEFAULT_PADDING);

        //背景
        canvas.drawRoundRect(rect, mRadius, mRadius, bgPaint);

        arcPaint.setStrokeWidth(dp2px(4));//设置边框线宽
        //边框
        canvas.drawRoundRect(rect, mRadius, mRadius, arcPaint);

        arcPaint.setStrokeWidth(dp2px(2));//设置边框线宽

        float width = getMeasuredWidth() / (mMaxLength * 1.0f);
        //分割线
        for (int i = 1; i < mMaxLength; i++) {
            float x = width * i;
            canvas.drawLine(x, DEFAULT_PADDING, x, getMeasuredHeight()-DEFAULT_PADDING, arcPaint);
        }
        if(isAdd || isDel) {
            float circleRadius = getMeasuredHeight() / 5.0f;
            float half = getMeasuredWidth() / (mMaxLength * 2.0f);
            float y = getMeasuredHeight() / 2.0f;
            for (int i = 0; i < mMaxLength; i++) {
                float x = getMeasuredWidth() / mMaxLength * i + half;
                if (isAdd) {
                    if (i < currentLength - 1) {
                        canvas.drawCircle(x, y, circleRadius, passPaint);
                    } else if (i == currentLength - 1) {
                        canvas.drawCircle(x, y, circleRadius * rate, passPaint);
                    }
                }
                if (isDel) {
                    if (i < currentLength) {
                        canvas.drawCircle(x, y, circleRadius, passPaint);
                    } else if (i == currentLength) {
                        Log.d("PassWordEditText", "currentLength=" + currentLength);
                        float radius = circleRadius - circleRadius * rate;
                        canvas.drawCircle(x, y, radius, passPaint);
                    }
                }
            }
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text,start,lengthBefore,lengthAfter);
        if(text.toString().length()!=0 || this.currentLength!=0) {
            isAdd = text.toString().length() > this.currentLength;
            isDel = text.toString().length() < this.currentLength;

            currentLength = text.toString().length();

            if (currentLength <= getMaxLength()) {
                if (animation != null) {
                    clearAnimation();
                    startAnimation(animation);
                } else {
                    invalidate();
                }
            }
        }
    }

    private class PaintLastAnimation extends Animation{
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            PassWordEditText.this.rate = interpolatedTime;
            postInvalidate();
        }
    }
}
