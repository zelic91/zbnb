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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    FrameLayout fmMenu;
    FrameLayout fmContent;
    View interceptView;
    Button btnMenu;

    boolean isOpen = false;
    boolean willMove = true;

    double firstTouchX = 0;

    double currentX = 0;
    double currentScale = 1;
    double currentRotation = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fmMenu = (FrameLayout) findViewById(R.id.fm_menu);
        fmContent = (FrameLayout) findViewById(R.id.fm_content);
        btnMenu = (Button) findViewById(R.id.btn_menu);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fm_menu, MenuFragment.newInstance())
                .replace(R.id.fm_content, ContentFragment.newInstance())
                .commit();

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isOpen) {
                    performAnimation(1);
                }
            }
        });

        interceptView = new View(MainActivity.this);
        interceptView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
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
        fmContent.setX((float) currentX);
        fmContent.setScaleX((float) currentScale);
        fmContent.setScaleY((float) currentScale);
        fmContent.setRotationY((float) currentRotation);
    }

    private void performAnimation(double percent) {
        performAnimation(300, percent, 0);
    }

    private void performAnimation(long duration, final double percent, long delay) {
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

                if (percent == 1) {
                    fmContent.addView(interceptView);
                    isOpen = true;
                } else {
                    fmContent.removeView(interceptView);
                    isOpen = false;
                }
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

        interceptView.setOnTouchListener(new View.OnTouchListener() {
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
                            performAnimation(0);
                        } else {
                            performAnimation(1);
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float currentX = event.getRawX();

                        double percent = 1 - (limitWidth - (currentX - firstTouchX)) / limitWidth;
                        if (isOpen) {
                            percent = (limitWidth - (-currentX + firstTouchX)) / limitWidth;
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
