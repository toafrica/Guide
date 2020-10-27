package com.toafrica.guideview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;

/**
 * 引导图+蒙层的实现
 */
public class Guide extends FrameLayout {
    private Path mTargetPath;
    private Paint mTargetPaint;
    private Rect bounds;
    private Configuration config;
    private RectF mRect;
    private int mScreenWidth;
    private int mScreenHeight;
    private RectF targetRectF;

    public Guide(Context context) {
        super(context);
        initAttrs(context, null);
    }

    public Guide(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
    }

    public Guide(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
    }

    protected void initAttrs(Context context, AttributeSet attrs) {
        setWillNotDraw(false);
        bounds = new Rect();
        mTargetPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mTargetPaint.setColor(Color.WHITE);
        mTargetPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        mTargetPath = new Path();
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (config != null && config.mode != ClickMode.PARENT) {
                    return;
                }
                next();
            }
        });
    }

    private void next() {
        if (config != null && config.hasNext()) {
            config = config.next;
            parse();
            invalidate();
        } else {
            ViewGroup parent = (ViewGroup) getParent();
            parent.removeView(this);
        }
    }

    private void init(Configuration config, Orientation orientation) {
        this.config = config;
        if (orientation == Orientation.LANDSCAPE) {
            mScreenWidth = getScreenHeight(getContext());
            mScreenHeight = getScreenWidth(getContext());
        } else if (orientation == Orientation.PORTRAIT) {
            mScreenWidth = getScreenWidth(getContext());
            mScreenHeight = getScreenHeight(getContext());
        }

        parse();
    }

    private void checkIsValid() {
        if (config == null) {

            throw new IllegalStateException("GuideView generate failed :config = null");
        }
        if (config.target == null) {

            throw new IllegalStateException("GuideView generate failed :targetView = null");
        }
        if (config.guideView == null && config.tipRes <= 0) {
            throw new IllegalStateException("GuideView generate failed :guideView not set");
        }
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (config.mode == ClickMode.TARGET
            && isInTarget(ev)
            && ev.getAction() == MotionEvent.ACTION_UP) {
            post(new Runnable() {
                @Override
                public void run() {
                    next();
                }
            });
            return true;
        }

        return super.dispatchTouchEvent(ev);
    }

    private boolean isInTarget(MotionEvent ev) {
        return ev.getX() > targetRectF.left

            && ev.getX() < targetRectF.right

            && ev.getY() > targetRectF.top

            && ev.getY() < targetRectF.bottom;
    }

    private void parse() {
        checkIsValid();
        removeAllViews();

        targetRectF = getRect(config.target);
        mTargetPath.reset();
        mTargetPath.moveTo(targetRectF.left, targetRectF.top);
        mTargetPath.addRoundRect(targetRectF, config.rx, config.ry, Path.Direction.CW);

        int width, height;
        if (config.guideView == null) {
            config.guideView = LayoutInflater.from(getContext()).inflate(config.tipRes, this, false);
        }

        config.guideView.measure(MeasureSpec.makeMeasureSpec(mScreenWidth, MeasureSpec.EXACTLY),
                                 MeasureSpec.makeMeasureSpec(mScreenHeight, MeasureSpec.EXACTLY));
        width = config.guideView.getMeasuredWidth();
        if (width <= 0) {
            throw new RuntimeException("width not correct");
        }
        height = config.guideView.getMeasuredHeight();
        if (config.ratio != 0) {
            float widthScaled = mScreenWidth * config.ratio;
            float scale = widthScaled / width;
            config.guideView.setPivotX(0);
            config.guideView.setPivotY(0);
            config.guideView.setScaleX(scale);
            config.guideView.setScaleY(scale);
        }

        switch (config.location) {
            case LEFT:
                bounds.right = ((int) (targetRectF.left + config.offsetX));
                bounds.top = ((int) (targetRectF.top + config.offsetY));
                bounds.left = bounds.right - width;
                bounds.bottom = bounds.top + height;
                break;
            case TOP:
                bounds.left = ((int) (targetRectF.left + config.offsetX));
                bounds.bottom = ((int) (targetRectF.top + config.offsetY));
                bounds.top = bounds.bottom - height;
                bounds.right = bounds.left + width;
                break;
            case RIGHT:
                bounds.left = ((int) (targetRectF.right + config.offsetX));
                bounds.top = ((int) (targetRectF.top + config.offsetY));
                bounds.right = bounds.left + width;
                bounds.bottom = bounds.top + height;
                break;
            case BOTTOM:
                bounds.left = ((int) (targetRectF.left + config.offsetX));
                bounds.top = ((int) (targetRectF.bottom + config.offsetY));
                bounds.right = bounds.left + width;
                bounds.bottom = bounds.top + height;
        }
        MarginLayoutParams params = (MarginLayoutParams) config.guideView.getLayoutParams();
        if (params == null) {
            params = generateDefaultLayoutParams();
        }
        params.leftMargin = bounds.left;
        params.topMargin = bounds.top;
        if (config.mode == ClickMode.GUIDE) {
            config.guideView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    next();
                }
            });
        } else if (config.mode == ClickMode.TARGET) {
            config.target.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    next();
                }
            });
        }
        addView(config.guideView);
    }

    private RectF getRect(View view) {
        int[] loc = new int[2];
        view.getLocationOnScreen(loc);
        RectF rect = new RectF();
        rect.set(loc[0] - config.targetPaddingLeft, loc[1] - config.targetPaddingTop,
                 loc[0] + view.getMeasuredWidth() + config.targetPaddingRight,
                 loc[1] + view.getMeasuredHeight() + config.targetPaddingBottom);
        return rect;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRect = new RectF(0, 0, w, h);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int saveContent = canvas.saveLayer(mRect, null, Canvas.ALL_SAVE_FLAG);
        canvas.drawColor(config.backgroundColor);
        canvas.drawPath(mTargetPath, mTargetPaint);
        canvas.restoreToCount(saveContent);
        super.dispatchDraw(canvas);
    }

    public static class Builder {
        Configuration config = new Configuration();
        Guide mGuide;
        Orientation orientation;

        public Builder(Orientation orientation) {
            this.orientation = orientation;
        }

        public Builder setTarget(View target) {
            config.target = target;
            return this;
        }

        public Builder setGuide(View tip) {
            config.guideView = tip;
            return this;
        }

        public Builder setGuide(int tipRes) {
            config.tipRes = tipRes;
            return this;
        }

        public Builder setTipOffset(int offsetX, int offsetY) {
            config.offsetX = offsetX;
            config.offsetY = offsetY;
            return this;
        }

        public void show() {
            final Configuration root = config.findRoot();
            if (root.target != null) {
                root.target.getViewTreeObserver()
                    .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            mGuide = new Guide(root.target.getContext());
                            mGuide.init(root, orientation);
                            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(-1, -1);
                            ViewGroup rootView = (ViewGroup) root.target.getRootView();
                            rootView.addView(mGuide, params);
                            root.target.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    });
            }
        }

        public void hide() {
            if (mGuide != null) {
                mGuide.setVisibility(GONE);
            }
        }

        public Builder setTargetPadding(int left, int top, int right, int bottom) {
            config.targetPaddingLeft = left;
            config.targetPaddingTop = top;
            config.targetPaddingRight = right;
            config.targetPaddingBottom = bottom;
            return this;
        }

        public Builder setCorner(float rx, float ry) {
            config.rx = rx;
            config.ry = ry;
            return this;
        }

        public Builder next() {
            config.next = new Configuration();
            config.next.before = config;
            config = config.next;
            return this;
        }

        /**
         * 屏占比
         *
         * @param ratio
         * @return
         */
        public Builder setRatio(float ratio) {
            config.ratio = ratio;
            return this;
        }

        public Builder location(Location location) {
            config.location = location;
            return this;
        }

        public Builder setBackgroundColor(int color) {
            config.backgroundColor = color;
            return this;
        }

        public Builder setClickMode(ClickMode mode) {
            config.mode = mode;
            return this;
        }
    }

    public static int getScreenHeight(Context context) {
        if (context != null) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (wm != null) {
                DisplayMetrics dm = new DisplayMetrics();
                wm.getDefaultDisplay().getRealMetrics(dm);
                return Math.max(dm.widthPixels, dm.heightPixels);
            }
        }
        return 0;
    }

    public static int getScreenWidth(Context context) {
        if (context != null) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (wm != null) {
                DisplayMetrics dm = new DisplayMetrics();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    wm.getDefaultDisplay().getRealMetrics(dm);
                } else {
                    wm.getDefaultDisplay().getMetrics(dm);
                }
                return Math.min(dm.widthPixels, dm.heightPixels);
            }
        }
        return 0;
    }
}
