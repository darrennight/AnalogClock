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
public class AnalogClock extends View
{
	private Context mContext;
	
	private Drawable mDial;//表盘
	private Drawable mHourHand;//时针
	private Drawable mMinHand;//分针
	private Drawable mSecHand;//秒针
	private Time  mCalendar;
	float mHour;
	float mMin;
	float mSec;
	boolean mChange=false;
	
	private int mDialWidth;
	private int mDialHeight;
	private boolean mAttached=false;
	private Paint mPaint;
	
	Message mSecMsg;
	private Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg)
		{
			// TODO: Implement this method
			super.handleMessage(msg);
			if(msg.what==0){
				onTimeChanged();
				invalidate();
			}
		}
		
	};
	public AnalogClock(Context context){
		this(context,null);
	}
	public AnalogClock(Context context,AttributeSet attrs){
		this(context,attrs,0);
	}	
	public AnalogClock(Context context,AttributeSet attrs,int defStyle){
		super(context,attrs,defStyle);
		mContext=context;
		Resources r=mContext.getResources();
		mDial=r.getDrawable(R.drawable.clock_analog_dial);
		mHourHand=r.getDrawable(R.drawable.clock_analog_hour);
		mMinHand=r.getDrawable(R.drawable.clock_analog_minute);
		mSecHand=r.getDrawable(R.drawable.clock_analog_second);
		
		TypedArray arrays=context.obtainStyledAttributes(attrs,R.styleable.AnalogClock);
		
		mCalendar=new Time();
		mDialWidth=mDial.getIntrinsicWidth();
		mDialHeight=mDial.getIntrinsicWidth();
		
	}

	public void onTimeChanged(){
		mCalendar.setToNow();
		int hour=mCalendar.hour;
		int min=mCalendar.minute;
		int sec=mCalendar.second;
		
		mSec=sec;
		mMin=min+sec/60;
		mHour=hour+min/60;
		
		mChange=true;
	
	}
	@Override
	protected void onAttachedToWindow()
	{
		// TODO: Implement this method
		super.onAttachedToWindow();
		if(mAttached==false){
			mAttached=true;
			IntentFilter intentFilter=new IntentFilter();
			intentFilter.addAction(Intent.ACTION_TIME_CHANGED);//外部修改系统时间后发送广播
			intentFilter.addAction(Intent.ACTION_TIME_TICK);//每分钟系统发送一次
			intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);//时区发生变化后
			mContext.registerReceiver(br,intentFilter);
		}
		mCalendar=new Time();
		onTimeChanged();
		initSecondThread();
	}
	
	/*通过启动一个新的线程来监听每秒的时间变化
	*而不是通过过去广播的方式
	*/
	public void initSecondThread(){
	//	mSecMsg=handler.obtainMessage();
		Thread secThread=new Thread(){
			public void run(){
				mSecMsg=handler.obtainMessage(0);
				handler.sendMessage(mSecMsg);
				try{
					sleep(1000);
				}catch(Exception e){
					
				}
			}
		};
		secThread.start();
	}

	@Override
	protected void onDetachedFromWindow()
	{
		// TODO: Implement this method
		super.onDetachedFromWindow();
		if(mAttached){
			mContext.unregisterReceiver(br);
		    mAttached=false;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		/*
		*MeasureSpec类封装了从父容器传递给子容器的布局要求,共有三种模式
		*1.UNSPECIFIED:父容器对于子容器没有任何限制,子容器想要多大就多大;the parent has not imposed any constraint on the child,it can be whatever size it wants.
		*2.EXACTLY:父容器已经为子容器设置了尺寸,子容器应当服从这些边界,不管他自己想要多大的空间;the parent has determined and exact sixe for the child,the child is going to be give those bounds regardless of how big it want to be.
		*3.AT_MOST:子容器可以是声明发现内的任意尺寸;the child can be as large as it wants up to the specified size.
		*
		**/
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int widthMode=MeasureSpec.getMode(widthMeasureSpec);
		int heightMode=MeasureSpec.getMode(heightMeasureSpec);
		int widthSize=MeasureSpec.getSize(widthMeasureSpec);
		int heightSize=MeasureSpec.getSize(heightMeasureSpec);
		
		float horiScale=1.0f;
		float verScale=1.0f;
		
		if(widthMode!=MeasureSpec.UNSPECIFIED&&widthSize<mDialWidth){
			horiScale=widthSize/mDialWidth;
		}
		if(heightMode!=MeasureSpec.UNSPECIFIED&&heightSize<mDialHeight){
			verScale=heightSize/mDialHeight;
		}
		float scale=Math.min(horiScale,verScale);
		
		setMeasuredDimension(resolveSizeAndState((int)(mDialWidth*scale),widthMeasureSpec,0),
		resolveSizeAndState((int)(mDialHeight*scale),heightMode,0));
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		mChange=true;
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		boolean change=mChange;
		if(change) mChange=false;
		int avaliableWidth=getWidth();
		int avaliableHeight=getHeight();
		
		int x=avaliableWidth/2;
		int y=avaliableHeight/2;
		
		Drawable dial=mDial;
		int w=dial.getIntrinsicWidth();
		int h=dial.getIntrinsicHeight();
	}
	
	
	
	
	BroadcastReceiver br=new BroadcastReceiver(){

		@Override
		public void onReceive(Context c, Intent intent)
		{
			//处理时区变化
		}

		
	};
}
