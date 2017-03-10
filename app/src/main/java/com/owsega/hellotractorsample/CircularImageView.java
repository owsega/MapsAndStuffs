package com.owsega.hellotractorsample;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;


/**
 * Original Written by: Pkmmte Xeleon. Check out https://github.com/Pkmmte/CircularImageView
 * Modified to include red dot overlay by Seyi Owoeye
 * <p/>
 * Custom ImageView for circular images in Android while maintaining the
 * best draw performance and supporting custom borders & selectors.
 */
public class CircularImageView extends AppCompatImageView {
    // For logging purposes
    private static final String TAG = CircularImageView.class.getSimpleName();

    // Default property values
    private static final boolean SHADOW_ENABLED = true;
    private static final float SHADOW_RADIUS = 4f;
    private static final float SHADOW_DX = 0f;
    private static final float SHADOW_DY = 2f;
    private static final int SHADOW_COLOR = Color.BLACK;
    RectF rekt;
    // Border & Selector configuration variables
    private boolean hasBorder;
    private boolean hasSelector;
    private boolean isSelected;
    private int borderWidth;
    private int canvasSize;
    private int selectorStrokeWidth;
    //Red dot variables
    private boolean showRedDot;
    private int insetX, insetY, redDotRadius;
    // Shadow properties
    private boolean shadowEnabled;
    private float shadowRadius;
    private float shadowDx;
    private float shadowDy;
    private int shadowColor;
    // Objects used for the actual drawing
    private BitmapShader shader;
    private Bitmap image;
    private Paint paint;
    private Paint paintBorder;
    private Paint redDotPainter;
    private Paint paintSelectorBorder;
    private ColorFilter selectorFilter;

    public CircularImageView(Context context) {
        this(context, null, R.styleable.CircularImageViewStyle_circularImageViewDefault);
    }

    public CircularImageView(Context context, AttributeSet attrs) {
        this(context, attrs, R.styleable.CircularImageViewStyle_circularImageViewDefault);
    }

    public CircularImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    /**
     * Initializes paint objects and sets desired attributes.
     *
     * @param context  Context
     * @param attrs    Attributes
     * @param defStyle Default Style
     */
    private void init(Context context, AttributeSet attrs, int defStyle) {
        // Initialize paint objects
        paint = new Paint();
        paint.setAntiAlias(true);
        paintBorder = new Paint();
        paintBorder.setAntiAlias(true);
        paintBorder.setStyle(Paint.Style.STROKE);
        paintSelectorBorder = new Paint();
        paintSelectorBorder.setAntiAlias(true);

        // Enable software rendering on HoneyComb and up. (needed for shadow)
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        // Load the styled attributes and set their properties
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.CircularImageView, defStyle, 0);

        // Check for extra features being enabled
        hasBorder = attributes.getBoolean(R.styleable.CircularImageView_civ_border, false);
        hasSelector = attributes.getBoolean(R.styleable.CircularImageView_civ_selector, false);
        shadowEnabled = attributes.getBoolean(R.styleable.CircularImageView_civ_shadow, SHADOW_ENABLED);

        // Set border properties, if enabled
        if (hasBorder) {
            int defaultBorderSize = (int) (2 * context.getResources().getDisplayMetrics().density + 0.5f);
            setBorderWidth(attributes.getDimensionPixelOffset(R.styleable.CircularImageView_civ_borderWidth, defaultBorderSize));
            setBorderColor(attributes.getColor(R.styleable.CircularImageView_civ_borderColor, Color.WHITE));
        }

        // Set selector properties, if enabled
        if (hasSelector) {
            int defaultSelectorSize = (int) (2 * context.getResources().getDisplayMetrics().density + 0.5f);
            setSelectorColor(attributes.getColor(R.styleable.CircularImageView_civ_selectorColor, Color.TRANSPARENT));
            setSelectorStrokeWidth(attributes.getDimensionPixelOffset(R.styleable.CircularImageView_civ_selectorStrokeWidth, defaultSelectorSize));
            setSelectorStrokeColor(attributes.getColor(R.styleable.CircularImageView_civ_selectorStrokeColor, Color.BLUE));
        }

        // Set shadow properties, if enabled
        if (shadowEnabled) {
            shadowRadius = attributes.getFloat(R.styleable.CircularImageView_civ_shadowRadius, SHADOW_RADIUS);
            shadowDx = attributes.getFloat(R.styleable.CircularImageView_civ_shadowDx, SHADOW_DX);
            shadowDy = attributes.getFloat(R.styleable.CircularImageView_civ_shadowDy, SHADOW_DY);
            shadowColor = attributes.getColor(R.styleable.CircularImageView_civ_shadowColor, SHADOW_COLOR);
            setShadowEnabled(true);
        }

        //Red Dots default properties
        showRedDot = false;
        redDotPainter = new Paint();
        redDotPainter.setColor(Color.RED);
        insetX = 9;
        insetY = 9;
        redDotRadius = 7;

        // We no longer need our attributes TypedArray, give it back to cache
        attributes.recycle();

        rekt = new RectF();
    }

    /**
     * Sets the CircularImageView's border width in pixels.
     *
     * @param borderWidth Width in pixels for the border.
     */
    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
        if (paintBorder != null)
            paintBorder.setStrokeWidth(borderWidth);
        requestLayout();
        invalidate();
    }

    /**
     * Sets the CircularImageView's basic border color.
     *
     * @param borderColor The new color (including alpha) to set the border.
     */
    public void setBorderColor(int borderColor) {
        if (paintBorder != null)
            paintBorder.setColor(borderColor);
        this.invalidate();
    }

    /**
     * Sets the color of the selector to be draw over the
     * CircularImageView. Be sure to provide some opacity.
     *
     * @param selectorColor The color (including alpha) to set for the selector overlay.
     */
    public void setSelectorColor(int selectorColor) {
        this.selectorFilter = new PorterDuffColorFilter(selectorColor, PorterDuff.Mode.SRC_ATOP);
        this.invalidate();
    }

    /**
     * Sets the stroke width to be drawn around the CircularImageView
     * during click events when the selector is enabled.
     *
     * @param selectorStrokeWidth Width in pixels for the selector stroke.
     */
    public void setSelectorStrokeWidth(int selectorStrokeWidth) {
        this.selectorStrokeWidth = selectorStrokeWidth;
        this.requestLayout();
        this.invalidate();
    }

    /**
     * Sets the stroke color to be drawn around the CircularImageView
     * during click events when the selector is enabled.
     *
     * @param selectorStrokeColor The color (including alpha) to set for the selector stroke.
     */
    public void setSelectorStrokeColor(int selectorStrokeColor) {
        if (paintSelectorBorder != null)
            paintSelectorBorder.setColor(selectorStrokeColor);
        this.invalidate();
    }

    /**
     * Enables a dark shadow for this CircularImageView.
     *
     * @param enabled Set to true to draw a shadow or false to disable it.
     */
    public void setShadowEnabled(boolean enabled) {
        shadowEnabled = enabled;
        updateShadow();
    }

    /**
     * Enables a dark shadow for this CircularImageView.
     * If the radius is set to 0, the shadow is removed.
     *
     * @param radius Radius for the shadow to extend to.
     * @param dx     Horizontal shadow offset.
     * @param dy     Vertical shadow offset.
     * @param color  The color of the shadow to apply.
     */
    public void setShadow(float radius, float dx, float dy, int color) {
        shadowRadius = radius;
        shadowDx = dx;
        shadowDy = dy;
        shadowColor = color;
        updateShadow();
    }

    @Override
    public void onDraw(Canvas canvas) {
        // Don't draw anything without an image
        if (image == null)
            return;

        // Nothing to draw (Empty bounds)
        if (image.getHeight() == 0 || image.getWidth() == 0)
            return;

        // Update shader if canvas size has changed
        int oldCanvasSize = canvasSize;
        canvasSize = getWidth() < getHeight() ? getWidth() : getHeight();
        if (oldCanvasSize != canvasSize)
            updateBitmapShader();

        // Apply shader to paint
        paint.setShader(shader);

        // Keep track of selectorStroke/border width
        int outerWidth = 0;

        // Get the exact X/Y axis of the view
        int center = canvasSize / 2;


        if (hasSelector && isSelected) { // Draw the selector stroke & apply the selector filter, if applicable
            outerWidth = selectorStrokeWidth;
            center = (canvasSize - (outerWidth * 2)) / 2;

            paint.setColorFilter(selectorFilter);
            canvas.drawCircle(center + outerWidth, center + outerWidth, ((canvasSize - (outerWidth * 2)) / 2) + outerWidth - 4.0f, paintSelectorBorder);
        } else if (hasBorder) { // If no selector was drawn, draw a border and clear the filter instead... if enabled
            outerWidth = borderWidth;
            center = (canvasSize - (outerWidth * 2)) / 2;

            paint.setColorFilter(null);
            rekt.set(outerWidth / 2, outerWidth / 2, canvasSize - outerWidth / 2, canvasSize - outerWidth / 2);
            canvas.drawArc(rekt, 360, 360, false, paintBorder);
            //canvas.drawCircle(center + outerWidth, center + outerWidth, ((canvasSize - (outerWidth * 2)) / 2) + outerWidth - 4.0f, paintBorder);
        } else // Clear the color filter if no selector nor border were drawn
            paint.setColorFilter(null);

        // Draw the circular image itself
        canvas.drawCircle(center + outerWidth, center + outerWidth, ((canvasSize - (outerWidth * 2)) / 2), paint);

        /*  */
        if (showRedDot) {
            canvas.drawCircle(2 * (center - insetX), insetY, redDotRadius, redDotPainter);
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // Check for clickable state and do nothing if disabled
        if (!this.isClickable()) {
            this.isSelected = false;
            return super.onTouchEvent(event);
        }

        // Set selected state based on Motion Event
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.isSelected = true;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_SCROLL:
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_CANCEL:
                this.isSelected = false;
                break;
        }

        // Redraw image and return super type
        this.invalidate();
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);

        // Extract a Bitmap out of the drawable & set it as the main shader
        image = drawableToBitmap(getDrawable());
        if (canvasSize > 0)
            updateBitmapShader();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);

        // Extract a Bitmap out of the drawable & set it as the main shader
        image = drawableToBitmap(getDrawable());
        if (canvasSize > 0)
            updateBitmapShader();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);

        // Extract a Bitmap out of the drawable & set it as the main shader
        image = drawableToBitmap(getDrawable());
        if (canvasSize > 0)
            updateBitmapShader();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);

        // Extract a Bitmap out of the drawable & set it as the main shader
        image = bm;
        if (canvasSize > 0)
            updateBitmapShader();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measureWidth(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private int measureWidth(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // The parent has determined an exact size for the child.
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            // The child can be as large as it wants up to the specified size.
            result = specSize;
        } else {
            // The parent has not imposed any constraint on the child.
            result = canvasSize;
        }

        return result;
    }

    private int measureHeight(int measureSpecHeight) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpecHeight);
        int specSize = MeasureSpec.getSize(measureSpecHeight);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            // The child can be as large as it wants up to the specified size.
            result = specSize;
        } else {
            // Measure the text (beware: ascent is a negative number)
            result = canvasSize;
        }

        return (result + 2);
    }

    private void updateShadow() {
        float radius = shadowEnabled ? shadowRadius : 0;
        //paint.setShadowLayer(radius, shadowDx, shadowDy, shadowColor);
        paintBorder.setShadowLayer(radius, shadowDx, shadowDy, shadowColor);
        paintSelectorBorder.setShadowLayer(radius, shadowDx, shadowDy, shadowColor);
    }

    /**
     * Convert a drawable object into a Bitmap.
     *
     * @param drawable Drawable to extract a Bitmap from.
     * @return A Bitmap created from the drawable parameter.
     */
    public Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null)   // Don't do anything without a proper drawable
            return null;
        else if (drawable instanceof BitmapDrawable) {  // Use the getBitmap() method instead if BitmapDrawable
            Log.i(TAG, "Bitmap drawable!");
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();

        if (!(intrinsicWidth > 0 && intrinsicHeight > 0))
            return null;

        try {
            // Create Bitmap object out of the drawable
            Bitmap bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            // Simply return null of failed bitmap creations
            Log.e(TAG, "Encountered OutOfMemoryError while generating bitmap!");
            return null;
        }
    }

    /**
     * Re-initializes the shader texture used to fill in
     * the Circle upon drawing.
     */
    public void updateBitmapShader() {
        if (image == null)
            return;

        shader = new BitmapShader(image, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        if (canvasSize != image.getWidth() || canvasSize != image.getHeight()) {
            Matrix matrix = new Matrix();
            float scale = (float) canvasSize / (float) image.getWidth();
            matrix.setScale(scale, scale);
            shader.setLocalMatrix(matrix);
        }
    }

    /**
     * @return Whether or not this view is currently
     * in its selected state.
     */
    public boolean isSelected() {
        return this.isSelected;
    }

    /**
     * When passed true, the image view shows a red dot overlayed on top of it
     *
     * @param show boolean stating whether the red dot should be shown or not
     *             for this view
     */
    public void showRedDot(boolean show) {
        showRedDot = show;
        this.invalidate();
    }

    /**
     * Sets the properties for the red dot overlay.
     * <p/>
     * Pass zero as any of the parameters in order to use the default value
     *
     * @param x      the amount of inset from the right border where the dot is drawn
     * @param y      the amount of inset from the top border where the dot is drawn
     * @param radius the radius of the inset
     * @param color  the color of the dot. The int passed as color here should be
     *               a legal color - or else the behavior can not be predicted.
     */
    public void setRedDotProperties(int x, int y, int radius, int color) {
        if (x > 0)
            insetX = x;
        if (y > 0)
            insetY = y;
        if (radius > 0)
            redDotRadius = radius;
        if (color > 0)
            redDotPainter.setColor(color);
    }

}
