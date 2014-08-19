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
 * ���Խ������ҷ�����Ч��
 * @ClassName: WorkSpace2D
 * @Description: �������ƽ������ҹ���
 */
public class WorkSpace2D extends ViewGroup{
   
    private static final String TAG = "WorkSpace2D";
    private static final boolean DEBUG = false;
   
    /**
     * �Ƿ���ֹlayout��view,��Ҫ����layout��view��������Ϊfalse
     */
    private boolean mBlockLayouts = false;
    private Scroller mScroller;
    private WorkspaceOvershootInterpolator mScrollInterpolator;
    /**
     * ÿһ����VIEW�Ĵ�С
     */
    private float mLastMotionX;
    private float mLastMotionY;
    
    protected int mPageWidth;
    protected int mTotalScreens;
    protected int mCurrentScreen;
    /**
     * ���Ծ���
     */
    private final int mScrollingBounce=500;
    /**
     * �����ٶ�,snap�ٶ�
     */
    private final int mScrollingSpeed=400;
    /**
     * �ƶ������������������ƶ�
     */
    private int mTouchSlop;
    /**
     * �����ٶ��ٶ�
     */
    private int mMaximumVelocity;
    private VelocityTracker mVelocityTracker;
    /**
     * �����ٶ�,��������ٶȲŽ�����Ļ����
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
        //������view
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
       
        //��������Ŀ��
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
     * ʹ��view��������
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
     * �������,���������л�һֱ����
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
     * ���ش����¼�
     * <p>Title: onInterceptTouchEvent</p>
     * <p>Description: ֻ�����ص�down�¼�,�����view���¼�������true,�������ص�
     * �����Ƿ���true����false����ִ��onTouchEvent()</p>
     * @param ev
     * @return
     * @see android.view.ViewGroup#onInterceptTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(DEBUG) Log.d(TAG, "onInterceptTouchEvent(), action : " + ev.getAction() + ", mTouchState : " + mTouchState);
        final int action = ev.getAction();
        //���Ŀǰ�û������ƶ�,����״̬��Ϊrest(�ϴι�����û����),�����
        if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
            return true;
        }
       
        final float x = ev.getX();
        final float y = ev.getY();
       
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //��¼����λ��
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
                        //��������ƶ�,��״̬Ϊ����״̬
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
       
        //���״̬ΪTOUCH_STATE_RESTʱ��������´���,ִ��onTouchEvent()
        return mTouchState != TOUCH_STATE_REST;
    }
   
    /**
     * ���ܴ����¼�
     * <p>Title: onTouchEvent</p>
     * <p>Description:ֻ�з���true���ܼ������ܵ�move��up�¼�
     * (ԭ���Ǳ���ֻ����view���߱�view����true���ܽ��ܽ�������move��up�¼�)</p>
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
                       
                        //�����ƶ�
                        if (deltaX < 0) {
                            if (getScrollX() > -mScrollingBounce) {
                                scrollBy(Math.min(deltaX,mScrollingBounce), 0);
                            }
                        //�����ƶ�
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
                //�ڶ����ж��������ڹ���ʱ�ٰ������ɿ�ʱ�����Զ���������
                if (mTouchState == TOUCH_STATE_SCROLLING || mTouchState == TOUCH_STATE_DOWN) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int velocityX = (int) velocityTracker.getXVelocity();
                   
                    if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
                        //���󻬶�����һ����Ļ
                        snapToScreen(mCurrentScreen-1);
                    } else if (velocityX < -SNAP_VELOCITY && mCurrentScreen < (mTotalScreens - 1)) {
                        //���һ�������һ����Ļ
                        snapToScreen(mCurrentScreen+1);
                    } else {
                        snapToDestination();
                    }
                } else {
                    //�����¼�����view
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
     * ���ݻ����ľ������Ӧ�û������ĸ���Ļ,
     * �ĸ���Ļ��ʾ�������һ���򻬶����ĸ���Ļ
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
     * ������ĳ����view
     * @param whichScreen ��viewλ��
     */
    private void snapToScreen(int whichScreen) {
        if (!mScroller.isFinished()) return; //������ڹ�����ִ��
       
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


