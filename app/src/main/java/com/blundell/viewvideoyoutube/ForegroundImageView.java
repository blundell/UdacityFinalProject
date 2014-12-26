package com.blundell.viewvideoyoutube;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * https://gist.github.com/JakeWharton/0a251d67649305d84e8a
 * <p/>
 * Then modified to not scale the foreground to the ImageView size and draw centered
 */
public class ForegroundImageView extends ImageView {
    private Drawable foreground;

    public ForegroundImageView(Context context) {
        this(context, null);
    }

    public ForegroundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ForegroundImageView);
        Drawable foreground = a.getDrawable(R.styleable.ForegroundImageView_android_foreground);
        if (foreground != null) {
            setForeground(foreground);
        }
        a.recycle();
    }

    /**
     * Supply a drawable resource that is to be rendered on top of all of the child
     * views in the frame layout.
     *
     * @param drawableResId The drawable resource to be drawn on top of the children.
     */
    public void setForegroundResource(int drawableResId) {
        setForeground(getContext().getResources().getDrawable(drawableResId));
    }

    /**
     * Supply a Drawable that is to be rendered on top of all of the child
     * views in the frame layout.
     *
     * @param drawable The Drawable to be drawn on top of the children.
     */
    public void setForeground(Drawable drawable) {
        if (foreground == drawable) {
            return;
        }
        if (foreground != null) {
            foreground.setCallback(null);
            unscheduleDrawable(foreground);
        }

        foreground = drawable;

        if (drawable != null) {
            drawable.setCallback(this);
            if (drawable.isStateful()) {
                drawable.setState(getDrawableState());
            }
        }
        requestLayout();
        invalidate();
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == foreground;
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (foreground != null) foreground.jumpToCurrentState();
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (foreground != null && foreground.isStateful()) {
            foreground.setState(getDrawableState());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (foreground != null) {

            foreground.setBounds(0, 0, foreground.getIntrinsicWidth(), foreground.getIntrinsicHeight());
            invalidate();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (foreground != null) {
            foreground.setBounds(0, 0, foreground.getIntrinsicWidth(), foreground.getIntrinsicHeight());
            invalidate();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (foreground != null) {
            int halfForegroundWidth = foreground.getIntrinsicWidth() / 2;
            int halfForegroundHeight = foreground.getIntrinsicHeight() / 2;
            int left = (getMeasuredWidth() / 2) - halfForegroundWidth;
            int top = (getMeasuredHeight() / 2) - halfForegroundHeight;
            canvas.save();
            canvas.translate(left, top);
            foreground.draw(canvas);
            canvas.restore();
        }
    }
}
