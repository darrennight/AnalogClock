package com.wxp.clock;

import android.view.View;
import android.content.*;
import android.util.*;
import android.content.res.*;
import android.graphics.drawable.*;
import android.graphics.*;
import java.util.*;

import android.text.format.*;
import android.os.*;

public class AnalogClock extends View {
	private Context mContext;

	private Drawable mDial;// 表盘
	private Drawable mHourHand;// 时针
	private Drawable mMinHand;// 分针
	private Drawable mSecHand;// 秒针
	private Time mCalendar;
    private String mTimeZoneId;
	float mHour;
	float mMin;
	float mSec;
	boolean mChange = false;

	private int mDialWidth;
	private int mDialHeight;
	private boolean mAttached = false;
	private Paint mPaint;
	private Handler myHandler=new Handler();
 
	boolean noSecHand=false;//不显示秒针
	

	public AnalogClock(Context context) {
		this(context, null);
	}

	public AnalogClock(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AnalogClock(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		Resources r = mContext.getResources();
		mDial = r.getDrawable(R.drawable.clock_analog_dial);
		mHourHand = r.getDrawable(R.drawable.clock_analog_hour);
		mMinHand = r.getDrawable(R.drawable.clock_analog_minute);
		mSecHand = r.getDrawable(R.drawable.clock_analog_second);

		TypedArray arrays = context.obtainStyledAttributes(attrs,
				R.styleable.AnalogClock);

		mCalendar = new Time();
		mDialWidth = mDial.getIntrinsicWidth();
		mDialHeight = mDial.getIntrinsicHeight();

	}


	@Override
	protected void onAttachedToWindow() {
	
		super.onAttachedToWindow();
		if (!mAttached) {
			mAttached = true;
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Intent.ACTION_TIME_CHANGED);// 外部修改系统时间后发送广播
			intentFilter.addAction(Intent.ACTION_TIME_TICK);// 每分钟系统发送一次
			intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);// 时区发生变化后
			mContext.registerReceiver(br, intentFilter);
		}
		mCalendar = new Time();
		onTimeChanged();
		post(mClockTick);
	}



	@Override
	protected void onDetachedFromWindow() {
		
		super.onDetachedFromWindow();
		if (mAttached) {
			mContext.unregisterReceiver(br);
			  removeCallbacks(mClockTick);
			mAttached = false;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		/*
		 * MeasureSpec类封装了从父容器传递给子容器的布局要求,共有三种模式
		 * 1.UNSPECIFIED:父容器对于子容器没有任何限制,子容器想要多大就多大;
		 * the parent has not imposed any constraint on the child,it can be whatever size it wants.
		 * 2.EXACTLY:父容器已经为子容器设置了尺寸,子容器应当服从这些边界,不管他自己想要多大的空间;
		 * the parent has determined and exact sixe for the child,the child is going to be give those bounds regardless of how big it want to be.
		 * 3.AT_MOST:子容器可以是声明发现内的任意尺寸;
		 * the child can be as large as it wants up to the specified size.
		 */
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		float horiScale = 1.0f;
		float verScale = 1.0f;

		if (widthMode != MeasureSpec.UNSPECIFIED && widthSize < mDialWidth) {
			horiScale = widthSize / mDialWidth;
		}
		if (heightMode != MeasureSpec.UNSPECIFIED && heightSize < mDialHeight) {
			verScale = heightSize / mDialHeight;
		}
		float scale = Math.min(horiScale, verScale);

		setMeasuredDimension(
				resolveSizeAndState((int) (mDialWidth * scale),
						widthMeasureSpec, 0),
				resolveSizeAndState((int) (mDialHeight * scale), widthMeasureSpec, 0));
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mChange = true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Log.e("wxp","onDraw()");
		boolean changed = mChange;
		if (changed){
			mChange = false;
		}
		int availableWidth = getWidth();
		int availableHeight = getHeight();

		int x = availableWidth / 2;
		int y = availableHeight / 2;

		Drawable dial = mDial;
		int w = dial.getIntrinsicWidth();
		int h = dial.getIntrinsicHeight();
		
        boolean scaled = false;

        if (availableWidth < w || availableHeight < h) {
            scaled = true;
            float scale = Math.min((float) availableWidth / (float) w,
                                   (float) availableHeight / (float) h);
            canvas.save();
            canvas.scale(scale, scale, x, y);
        }

        if (changed) {
            dial.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        dial.draw(canvas);

/*        if (mDotRadius > 0f && mDotPaint != null) {
            canvas.drawCircle(x, y - (h / 2) + mDotOffset, mDotRadius, mDotPaint);
        }*/

        drawHand(canvas, mHourHand, x, y, mHour / 12.0f * 360.0f, changed);
        drawHand(canvas, mMinHand, x, y, mMin / 60.0f * 360.0f, changed);
        if (!noSecHand) {
            drawHand(canvas, mSecHand, x, y, mSec/ 60.0f * 360.0f, changed);
        }

        if (scaled) {
            canvas.restore();
        }
	}

	public void drawHand(Canvas canvas, Drawable hand, int x, int y,
			float angle, boolean change) {
		Log.e("wxp","drawHand()");
		canvas.save();
		canvas.rotate(angle, x, y);
		if (change) {
			int w = hand.getIntrinsicWidth();
			int h = hand.getIntrinsicHeight();
			hand.setBounds(x - w / 2, y - h / 2, x + w / 2, y + h / 2);
		}
		hand.draw(canvas);
		canvas.restore();
	}

	public void onTimeChanged() {
		Log.e("wxp","onTimeChanged()");
		mCalendar.setToNow();
        if (mTimeZoneId != null) {
            mCalendar.switchTimezone(mTimeZoneId);
        }
		int hour = mCalendar.hour;
		int min = mCalendar.minute;
		int sec = mCalendar.second;

		mSec = sec;
		mMin = min + sec / 60;
		mHour = hour + min / 60;

		mChange = true;

	}
	BroadcastReceiver br = new BroadcastReceiver() {

		@Override
		public void onReceive(Context c, Intent intent) {
			// 处理时区变化
			if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                String tz = intent.getStringExtra("time-zone");
                mCalendar = new Time(TimeZone.getTimeZone(tz).getID());
            }
            onTimeChanged();
            invalidate();
		}
	};
	
	

	/*
	 * 对于每秒的时间变化是通过启动一个新的线程来监听每秒的时间变化而不是通过过去广播的方式监听的
	 */
    private final Runnable mClockTick = new Runnable () {

        @Override
        public void run() {
    		Log.e("wxp","mClockTick()");
            onTimeChanged();
            invalidate();
            AnalogClock.this.postDelayed(mClockTick, 1000);
        }
    };
}
