package com.example.slide_workspace;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import java.util.List;

/**
 * 可以进行左右翻滚的效果
 * @ClassName: WorkSpace2D
 * @Description: 根据手势进行左右滚动
 */
public class WorkSpace2D extends ViewGroup{
   
    private static final String TAG = "WorkSpace2D";
    private static final boolean DEBUG = false;
   
    /**
     * 是否阻止layout子view,需要重新layout子view是先设置为false
     */
    private boolean mBlockLayouts = false;
    private Scroller mScroller;
    private WorkspaceOvershootInterpolator mScrollInterpolator;
    /**
     * 每一个子VIEW的大小
     */
    private float mLastMotionX;
    private float mLastMotionY;
    
    protected int mPageWidth;
    protected int mTotalScreens;
    protected int mCurrentScreen;
    /**
     * 弹性距离
     */
    private final int mScrollingBounce=500;
    /**
     * 滑动速度,snap速度
     */
    private final int mScrollingSpeed=400;
    /**
     * 移动距离大于这个数才算移动
     */
    private int mTouchSlop;
    /**
     * 滚动速度速度
     */
    private int mMaximumVelocity;
    private VelocityTracker mVelocityTracker;
    /**
     * 滚动速度,大于这个速度才进行屏幕滑动
     */
    private static final int SNAP_VELOCITY = 1000;
   
    private final static int TOUCH_STATE_REST = 0;
    private final static int TOUCH_STATE_SCROLLING = 1;
    private static final int TOUCH_STATE_DOWN = 3;
   
    private int mTouchState = TOUCH_STATE_REST;
   
    public WorkSpace2D(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
   
    public void setChildren(List<View> children) {
        //增加子view
        ViewGroup.LayoutParams p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT);
        for (int i = 0; i < children.size(); i++) {
            this.addView(children.get(i), i, p);
        }
        mTotalScreens = children.size();
    }
   
    private void init() {
        mScrollInterpolator = new WorkspaceOvershootInterpolator();
        mScroller = new Scroller(getContext(), mScrollInterpolator);
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }
   
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
       
        //保存测量的宽高
        setMeasuredDimension(widthSize, heightSize);
        mPageWidth=widthSize;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(!mBlockLayouts) {
            layoutScreens();
        }
        invalidate();
    }
   
    /**
     * 使子view并排排列
     */
    protected void layoutScreens() {
        int childLeft = 0;
        final int count = getChildCount();
        final int childWidth = mPageWidth;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                child.layout(childLeft, 0, childLeft + childWidth, getMeasuredHeight());
                childLeft += childWidth;
            }
        }
        mBlockLayouts=true;
    }
   
    /**
     * 计算滚动,滚动过程中会一直调用
     * <p>Title: computeScroll</p>
     * <p>Description: </p>
     * @see android.view.View#computeScroll()
     */
    @Override
    public void computeScroll() {
        if(DEBUG) Log.d(TAG, "computeScroll()");
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }
   
    /**
     * 拦截触摸事件
     * <p>Title: onInterceptTouchEvent</p>
     * <p>Description: 只能拦截到down事件,如果子view对事件处理返回true,则能拦截到
     * 无论是返回true或者false都会执行onTouchEvent()</p>
     * @param ev
     * @return
     * @see android.view.ViewGroup#onInterceptTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(DEBUG) Log.d(TAG, "onInterceptTouchEvent(), action : " + ev.getAction() + ", mTouchState : " + mTouchState);
        final int action = ev.getAction();
        //如果目前用户正在移动,并且状态不为rest(上次滚动还没结束),则忽略
        if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
            return true;
        }
       
        final float x = ev.getX();
        final float y = ev.getY();
       
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //记录按下位置
                mLastMotionX = x;
                mLastMotionY = y;
                
                mTouchState=mScroller.isFinished() ? TOUCH_STATE_DOWN:TOUCH_STATE_SCROLLING;
                break;
            case MotionEvent.ACTION_MOVE:      
                final int xDiff = (int)Math.abs(mLastMotionX-x);      
                if (xDiff>mTouchSlop) {      
                    mTouchState = TOUCH_STATE_SCROLLING;      
                          
                }      
                break; 
            /*case MotionEvent.ACTION_MOVE:
                final int xDiff = (int) Math.abs(x - mLastMotionX);
                final int yDiff = (int) Math.abs(y - mLastMotionY);

                final int touchSlop = mTouchSlop;
                boolean xMoved = xDiff > touchSlop;
                boolean yMoved = yDiff > touchSlop;

                if (xMoved || yMoved) {
                    if (xMoved) {
                        //如果横向移动,则状态为滚动状态
                        mTouchState = TOUCH_STATE_SCROLLING;
                    }
                }
                break;*/
            case MotionEvent.ACTION_UP:
                mTouchState = TOUCH_STATE_REST;
                break;
            default:
                break;
        }
       
        //如果状态为TOUCH_STATE_REST时则继续往下传递,执行onTouchEvent()
        return mTouchState != TOUCH_STATE_REST;
    }
   
    /**
     * 接受触摸事件
     * <p>Title: onTouchEvent</p>
     * <p>Description:只有返回true才能继续接受到move和up事件
     * (原理是必须只有子view或者本view返回true才能接受接下来的move和up事件)</p>
     * @param ev
     * @return
     * @see android.view.View#onTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(DEBUG) Log.d(TAG, "onTouchEvent(), action : " + ev.getAction());
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
       
        final int action = ev.getAction();
        final float x = ev.getX();
       
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mTouchState = TOUCH_STATE_DOWN;
               
                mLastMotionX = x;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTouchState == TOUCH_STATE_SCROLLING || mTouchState == TOUCH_STATE_DOWN) {
                    final int deltaX = (int) (mLastMotionX - x);
                    if(Math.abs(deltaX)>mTouchSlop || mTouchState == TOUCH_STATE_SCROLLING){
                        mTouchState = TOUCH_STATE_SCROLLING;
                        mLastMotionX = x;
                       
                        //向右移动
                        if (deltaX < 0) {
                            if (getScrollX() > -mScrollingBounce) {
                                scrollBy(Math.min(deltaX,mScrollingBounce), 0);
                            }
                        //向左移动
                        } else if (deltaX > 0) {
                            final int availableToScroll = ((mTotalScreens)*mPageWidth)-getScrollX()-mPageWidth+mScrollingBounce;
                            if (availableToScroll > 0) {
                                scrollBy(deltaX, 0);
                            }
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:      
                Log.e(TAG, "event : up");         
                // if (mTouchState == TOUCH_STATE_SCROLLING) {         
                final VelocityTracker velocityTracker = mVelocityTracker;         
                velocityTracker.computeCurrentVelocity(1000);         
                int velocityX = (int) velocityTracker.getXVelocity();         
                Log.e(TAG, "velocityX:"+velocityX);       
                      
                if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {         
                    // Fling enough to move left         
                    Log.e(TAG, "snap left");      
                    snapToScreen(mCurrentScreen - 1);         
                } else if (velocityX < -SNAP_VELOCITY         
                        && mCurrentScreen < getChildCount() - 1) {         
                    // Fling enough to move right         
                    Log.e(TAG, "snap right");      
                    snapToScreen(mCurrentScreen + 1);         
                } else {         
                    snapToDestination();         
                }         
                if (mVelocityTracker != null) {         
                    mVelocityTracker.recycle();         
                    mVelocityTracker = null;         
                }         
                // }         
                mTouchState = TOUCH_STATE_REST;         
                break;
            /*case MotionEvent.ACTION_UP:
                //第二个判断是在正在滚动时再按下再松开时进行自动滚动处理
                if (mTouchState == TOUCH_STATE_SCROLLING || mTouchState == TOUCH_STATE_DOWN) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int velocityX = (int) velocityTracker.getXVelocity();
                   
                    if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
                        //向左滑动到上一个屏幕
                        snapToScreen(mCurrentScreen-1);
                    } else if (velocityX < -SNAP_VELOCITY && mCurrentScreen < (mTotalScreens - 1)) {
                        //向右滑动到下一个屏幕
                        snapToScreen(mCurrentScreen+1);
                    } else {
                        snapToDestination();
                    }
                } else {
                    //传递事件到子view
                    return false;
                }
                mTouchState = TOUCH_STATE_REST;
                break;*/
            case MotionEvent.ACTION_CANCEL:
                mTouchState = TOUCH_STATE_REST;
                break;
        }
       
        return true;
    }
   
    /**
     * 根据滑动的距离计算应该滑动到哪个屏幕,
     * 哪个屏幕显示区域大于一半则滑动到哪个屏幕
     * @Title: snapToDestination
     * @Description:
     * @param
     * @return void
     * @throws
     */
    private void snapToDestination() {
        final int screenWidth = mPageWidth;
        final int whichScreen = (getScrollX() + (screenWidth / 2)) / screenWidth;
        snapToScreen(whichScreen);
    }
   
    /**
     * 滚动到某个子view
     * @param whichScreen 子view位置
     */
    private void snapToScreen(int whichScreen) {
        if (!mScroller.isFinished()) return; //如果正在滚动则不执行
       
        whichScreen = Math.max(0, Math.min(whichScreen, mTotalScreens - 1));
        boolean changingScreens = whichScreen != mCurrentScreen;

        final int screenDelta = Math.max(1, Math.abs(whichScreen - mCurrentScreen));
        mCurrentScreen = whichScreen;

        View focusedChild = getFocusedChild();
        if (focusedChild != null && changingScreens) {
            focusedChild.clearFocus();
        }

        final int newX = whichScreen * mPageWidth;
        final int delta = newX - getScrollX();
       
        int durationOffset = 1;
        if (screenDelta == 0) {
            durationOffset = 200;
        }
        final int duration = mScrollingSpeed + durationOffset;
       
        mScrollInterpolator.setDistance(screenDelta);
        mScroller.startScroll(getScrollX(), 0, delta, 0, duration);
        invalidate();
    }
   
    private static class WorkspaceOvershootInterpolator implements Interpolator {
        private static final float DEFAULT_TENSION = 1.3f;
        private float mTension;

        public WorkspaceOvershootInterpolator() {
            mTension = DEFAULT_TENSION;
        }
       
        public void setDistance(int distance) {
            mTension = distance > 0 ? DEFAULT_TENSION / distance : DEFAULT_TENSION;
        }

        @SuppressWarnings("unused")
        public void disableSettle() {
            mTension = 0.f;
        }

        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * ((mTension + 1) * t + mTension) + 1.0f;
        }
    }
}


