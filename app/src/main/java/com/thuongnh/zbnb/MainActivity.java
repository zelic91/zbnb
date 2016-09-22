package com.thuongnh.zbnb;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.FloatEvaluator;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {

    FrameLayout fmMenu;
    FrameLayout fmContent;

    boolean isOpen = false;
    boolean willMove = true;

    double firstTouchX = 0;

    int currentColor = Color.TRANSPARENT;
    double currentX = 0;
    double currentScale = 1;
    double currentRotation = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fmMenu = (FrameLayout) findViewById(R.id.fm_menu);
        fmContent = (FrameLayout) findViewById(R.id.fm_content);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fm_menu, MenuFragment.newInstance())
                .replace(R.id.fm_content, ContentFragment.newInstance())
                .commit();

        fmContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        addTouchGesture();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void moveView(double percent) {
        Point point = new Point();
        getWindow().getWindowManager().getDefaultDisplay().getSize(point);
        float width = point.x;
        currentX = (width * 0.6 - 100) * percent;
        currentScale = (1 - 0.4 * percent);
        currentRotation = 10 * percent;
        currentColor = (int)((Color.BLACK - Color.TRANSPARENT)* percent * 100) + Color.TRANSPARENT;
        fmContent.setX((float)currentX);
        fmContent.setScaleX((float)currentScale);
        fmContent.setScaleY((float)currentScale);
        fmContent.setRotationY((float)currentRotation);
        PorterDuffColorFilter greyFilter = new PorterDuffColorFilter(currentColor, PorterDuff.Mode.DST_ATOP);
        fmContent.getBackground().setColorFilter(greyFilter);
        Log.e("Color", "" + Color.BLACK);
    }

    private void performAnimation(double percent) {
        performAnimation(300, percent, 0);
    }

    private void performAnimation(long duration, double percent, long delay) {
        Point point = new Point();
        getWindow().getWindowManager().getDefaultDisplay().getSize(point);
        float width = point.x;
        final double x = (width * 0.6 - 100) * percent;
        final double scale = (1 - 0.4 * percent);
        final double rotation = 10 * percent;

        ObjectAnimator transitionX = ObjectAnimator.ofObject(fmContent, "x", new FloatEvaluator(), currentX, x);
        ObjectAnimator scaleX = ObjectAnimator.ofObject(fmContent, "scaleX", new FloatEvaluator(), currentScale, scale);
        ObjectAnimator scaleY = ObjectAnimator.ofObject(fmContent, "scaleY", new FloatEvaluator(), currentScale, scale);
        ObjectAnimator rotationX = ObjectAnimator.ofObject(fmContent, "rotationY", new FloatEvaluator(), currentRotation, rotation);
        AnimatorSet set = new AnimatorSet();
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                fmContent.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                fmContent.setLayerType(View.LAYER_TYPE_NONE, null);
                currentX = x;
                currentScale = scale;
                currentRotation = rotation;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                fmContent.setLayerType(View.LAYER_TYPE_NONE, null);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        set.playTogether(transitionX, scaleX, scaleY, rotationX);
        set.setDuration(duration);
        set.setStartDelay(delay);
        set.start();
    }

    private void addTouchGesture() {
        Point point = new Point();
        getWindow().getWindowManager().getDefaultDisplay().getSize(point);
        final double limitWidth = point.x * 0.6 - 100;

        fmContent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        firstTouchX = event.getRawX();
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!willMove) {
                            willMove = true;
                            return true;
                        }
                        if (isOpen) {
                            isOpen = false;
                            performAnimation(0);
                        } else {
                            isOpen = true;
                            performAnimation(1);
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float currentX = event.getRawX();

                        double percent = 1 - (limitWidth - (currentX - firstTouchX)) / limitWidth;
                        if (isOpen) {
                            percent = (limitWidth - ( - currentX + firstTouchX)) / limitWidth;
                        }

                        if (!isOpen && currentX < firstTouchX) {
                            willMove = false;
                        }
                        if (percent <= 1.2 && willMove) {
                            moveView(percent);
                        }
                        return true;
                }
                return false;
            }
        });
    }
}
