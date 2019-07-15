package com.bytedance.clockapplication.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.LogRecord;

public class Clock extends View {

    private final static String TAG = Clock.class.getSimpleName();

    private static final int FULL_ANGLE = 360;

    private static final int CUSTOM_ALPHA = 140;
    private static final int FULL_ALPHA = 255;

    private static final int DEFAULT_PRIMARY_COLOR = Color.WHITE;
    private static final int DEFAULT_SECONDARY_COLOR = Color.LTGRAY;

    private static final float DEFAULT_DEGREE_STROKE_WIDTH = 0.010f;

    public final static int AM = 0;

    private static final int RIGHT_ANGLE = 90;

    private int mWidth, mCenterX, mCenterY, mRadius;

    /**
     * properties
     */
    private int centerInnerColor;
    private int centerOuterColor;

    private int secondsNeedleColor;
    private int hoursNeedleColor;
    private int minutesNeedleColor;

    private int degreesColor;

    private int hoursValuesColor;

    private int numbersColor;

    private boolean mShowAnalog = true;

    private TimerHandler myHandler;
    private int mHours,mMinutes,mSeconds;

    private static final class TimerHandler extends Handler{
        private WeakReference<Clock> ClockWeakReference;
        private TimerHandler(Clock view){
            ClockWeakReference = new WeakReference<>(view);
        }
        @Override
        public void handleMessage(Message msg){
            Clock view = ClockWeakReference.get();
            if(view != null){
                view.getTime();
                view.invalidate();
                sendEmptyMessageDelayed(1,1000);
            }
        }
    }

    public Clock(Context context) {
        super(context);
        init(context, null);
        myHandler = new TimerHandler(this);
        getTime();
        myHandler.sendEmptyMessageDelayed(1,1000);
    }

    public Clock(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
        myHandler = new TimerHandler(this);
        getTime();
        myHandler.sendEmptyMessageDelayed(1,1000);
    }

    public Clock(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
        myHandler = new TimerHandler(this);
        getTime();
        myHandler.sendEmptyMessageDelayed(1,1000);
    }

    public void getTime() {
        Calendar calendar = Calendar.getInstance();
        mHours = calendar.get(Calendar.HOUR);
        mMinutes = calendar.get(Calendar.MINUTE);
        mSeconds = calendar.get(Calendar.SECOND);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        int heightWithoutPadding = height - getPaddingTop() - getPaddingBottom();

        if (widthWithoutPadding > heightWithoutPadding) {
            size = heightWithoutPadding;
        } else {
            size = widthWithoutPadding;
        }

        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(), size + getPaddingTop() + getPaddingBottom());
    }

    private void init(Context context, AttributeSet attrs) {

        this.centerInnerColor = Color.LTGRAY;
        this.centerOuterColor = DEFAULT_PRIMARY_COLOR;

        this.secondsNeedleColor = DEFAULT_SECONDARY_COLOR;
        this.hoursNeedleColor = DEFAULT_PRIMARY_COLOR;
        this.minutesNeedleColor = DEFAULT_PRIMARY_COLOR;

        this.degreesColor = DEFAULT_PRIMARY_COLOR;

        this.hoursValuesColor = DEFAULT_PRIMARY_COLOR;

        numbersColor = Color.WHITE;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        mWidth = getHeight() > getWidth() ? getWidth() : getHeight();

        int halfWidth = mWidth / 2;
        mCenterX = halfWidth;
        mCenterY = halfWidth;
        mRadius = halfWidth;

        if (mShowAnalog) {
            drawDegrees(canvas);
            drawHoursValues(canvas);
            drawNeedles(canvas);
            drawCenter(canvas);
        } else {
            drawNumbers(canvas);
        }
    }

    private void drawDegrees(Canvas canvas) {

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(mWidth * DEFAULT_DEGREE_STROKE_WIDTH);
        paint.setColor(degreesColor);

        int rPadded = mCenterX - (int) (mWidth * 0.01f);
        int rEnd = mCenterX - (int) (mWidth * 0.05f);

        for (int i = 0; i < FULL_ANGLE; i += 6 /* Step */) {

            if ((i % RIGHT_ANGLE) != 0 && (i % 15) != 0)
                paint.setAlpha(CUSTOM_ALPHA);
            else {
                paint.setAlpha(FULL_ALPHA);
            }

            int startX = (int) (mCenterX + rPadded * Math.cos(Math.toRadians(i)));
            int startY = (int) (mCenterX - rPadded * Math.sin(Math.toRadians(i)));

            int stopX = (int) (mCenterX + rEnd * Math.cos(Math.toRadians(i)));
            int stopY = (int) (mCenterX - rEnd * Math.sin(Math.toRadians(i)));

            canvas.drawLine(startX, startY, stopX, stopY, paint);

        }
    }

    /**
     * @param canvas
     */
    private void drawNumbers(Canvas canvas) {

        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(mWidth * 0.2f);
        textPaint.setColor(numbersColor);
        textPaint.setColor(numbersColor);
        textPaint.setAntiAlias(true);

        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int amPm = calendar.get(Calendar.AM_PM);

        String time = String.format("%s:%s:%s%s",
                String.format(Locale.getDefault(), "%02d", hour),
                String.format(Locale.getDefault(), "%02d", minute),
                String.format(Locale.getDefault(), "%02d", second),
                amPm == AM ? "AM" : "PM");

        SpannableStringBuilder spannableString = new SpannableStringBuilder(time);
        spannableString.setSpan(new RelativeSizeSpan(0.3f), spannableString.toString().length() - 2, spannableString.toString().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // se superscript percent

        StaticLayout layout = new StaticLayout(spannableString, textPaint, canvas.getWidth(), Layout.Alignment.ALIGN_CENTER, 1, 1, true);
        canvas.translate(mCenterX - layout.getWidth() / 2f, mCenterY - layout.getHeight() / 2f);
        layout.draw(canvas);
        myHandler.sendEmptyMessage(1);
    }

    /**
     * Draw Hour Text Values, such as 1 2 3 ...
     *
     * @param canvas
     */
    private void drawHoursValues(Canvas canvas) {
        // Default Color:
        // - hoursValuesColo
        Paint textPaint = new Paint();
        textPaint.setColor(degreesColor);
        textPaint.setTextSize(75);
        textPaint.setAlpha(FULL_ALPHA);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float top = fontMetrics.top;
        float bottom = fontMetrics.bottom;
        float textSize = (bottom - top) / 4;
        float rPadded = mCenterX - (int) (mWidth * 0.12f);

        for(int i=0;i<FULL_ANGLE;i+=30){
            float Text_X = (int) (mCenterX + rPadded * Math.sin(Math.toRadians(i)));
            float Text_Y = (int) (mCenterX - rPadded * Math.cos(Math.toRadians(i)));
            if(i==0){
                canvas.drawText("12",Text_X,Text_Y+textSize,textPaint);
            }
            else if(i<300){
                canvas.drawText("0"+String.valueOf(i / 30), Text_X, Text_Y+textSize, textPaint);
            }
            else {
                canvas.drawText(String.valueOf(i / 30), Text_X, Text_Y+textSize, textPaint);
            }
        }
    }

    /**
     * Draw hours, minutes needles
     * Draw progress that indicates hours needle disposition.
     *
     * @param canvas
     */
    private void drawNeedles(final Canvas canvas) {
        // Default Color:
        // - secondsNeedleColor
        // - hoursNeedleColor
        // - minutesNeedleColor
        Calendar calendar = Calendar.getInstance();
        int hourAngle = calendar.get(Calendar.HOUR) * 30;
        int minuteAngle = calendar.get(Calendar.MINUTE) * 6;
        int secondAngle = calendar.get(Calendar.SECOND) * 6;

        Paint hoursNeedle = new Paint(Paint.ANTI_ALIAS_FLAG);
        hoursNeedle.setColor(degreesColor);
        hoursNeedle.setAlpha(FULL_ALPHA);
        hoursNeedle.setStrokeCap(Paint.Cap.ROUND);
        hoursNeedle.setStrokeWidth(10f);
        canvas.drawLine(mCenterX+20*(float)Math.sin(Math.toRadians(hourAngle)),mCenterY-20*(float)Math.cos(Math.toRadians(hourAngle)),mCenterX+220*(float)Math.sin(Math.toRadians(hourAngle)),mCenterY-220*(float)Math.cos(Math.toRadians(hourAngle)),hoursNeedle);

        Paint minutesNeedle = new Paint(Paint.ANTI_ALIAS_FLAG);
        minutesNeedle.setColor(degreesColor);
        minutesNeedle.setAlpha(FULL_ALPHA);
        minutesNeedle.setStrokeCap(Paint.Cap.ROUND);
        minutesNeedle.setStrokeWidth(10f);
        canvas.drawLine(mCenterX+20*(float)Math.sin(Math.toRadians(minuteAngle)),mCenterY-20*(float)Math.cos(Math.toRadians(minuteAngle)),mCenterX+280*(float)Math.sin(Math.toRadians(minuteAngle)),mCenterY-280*(float)Math.cos(Math.toRadians(minuteAngle)),minutesNeedle);

        Paint secondsNeedle = new Paint(Paint.ANTI_ALIAS_FLAG);
        secondsNeedle.setColor(degreesColor);
        secondsNeedle.setAlpha(CUSTOM_ALPHA);
        secondsNeedle.setStrokeCap(Paint.Cap.ROUND);
        secondsNeedle.setStrokeWidth(7f);
        canvas.drawLine(mCenterX+20*(float)Math.sin(Math.toRadians(secondAngle)),mCenterY-20*(float)Math.cos(Math.toRadians(secondAngle)),mCenterX+320*(float)Math.sin(Math.toRadians(secondAngle)),mCenterY-320*(float)Math.cos(Math.toRadians(secondAngle)),secondsNeedle);
    }

    /**
     * Draw Center Dot
     *
     * @param canvas
     */
    private void drawCenter(Canvas canvas) {
        // Default Color:
        // - centerInnerColor
        // - centerOuterColor
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(degreesColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8);
        paint.setAlpha(FULL_ALPHA);
        canvas.drawCircle(mCenterX,mCenterY,20,paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(CUSTOM_ALPHA);
        canvas.drawCircle(mCenterX,mCenterY,15,paint);
    }

    public void setShowAnalog(boolean showAnalog) {
        mShowAnalog = showAnalog;
        invalidate();
    }

    public boolean isShowAnalog() {
        return mShowAnalog;
    }

}