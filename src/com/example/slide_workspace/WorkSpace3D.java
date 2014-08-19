package com.example.slide_workspace;

import android.content.Context;

import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * 可以进行左右翻滚的效果
 * @ClassName: WorkSpace3D
 * @Description: 根据手势进行左右滚动
 */
public class WorkSpace3D extends WorkSpace2D{
    private static final String TAG = "WorkSpace3D";
   
    /**
     * 当前的角度,根据移动的距离进行计算
     */
    private int mCurrentDegree;
    /**
     * 每个页面的角度
     */
    private int mPreFaceDegree = 90;
    /**
     * 每移动一个像素角度的变化大小
     * mPreFaceDegree*1.0f/pageWidth
     */
    private float mDegreeOffset;
   
    private Camera mCamera;
    private Matrix mMatrix;
   
    public WorkSpace3D(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCamera = new Camera();
        mMatrix = new Matrix();
    }
   
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int pageWidth = mPageWidth;
        mDegreeOffset = mPreFaceDegree*1.0f/pageWidth;
    }
   
    /**
     * 根据手指滚动时会不停调用这个方法,所以在这个方法中计算当前角度
     * <p>Title: scrollTo</p>
     * <p>Description: </p>
     * @param x
     * @param y
     * @see android.view.View#scrollTo(int, int)
     */
    @Override
    public void scrollTo(int x, int y) {
        if (getScrollX() != x || getScrollY() != y) {
            mCurrentDegree = (int)(x * mDegreeOffset);
            super.scrollTo(x, y);
        }
    }
   
    /**
     * 当draw当前view时,同时draw当前页面的前一页和后一页
     * <p>Title: dispatchDraw</p>
     * <p>Description: </p>
     * @param canvas
     * @see android.view.ViewGroup#dispatchDraw(android.graphics.Canvas)
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        final int screen = mCurrentScreen;
        final int width = getWidth();
        final int scrollX = this.getScrollX();
        //消除锯齿
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));
        drawScreen(canvas, screen, System.currentTimeMillis());
        if(scrollX > screen * width) {
            drawScreen(canvas, screen+1, System.currentTimeMillis());
        } else {
            drawScreen(canvas, screen-1, System.currentTimeMillis());
        }
    }

    private void drawScreen(Canvas canvas, int screen, long drawingTime) {
        if(screen < 0 || screen >= mTotalScreens) {
            return;
        }
        final int width = getWidth();
        final int scrollWidth = screen * width;
        final int scrollX = this.getScrollX();  
        if(scrollWidth > scrollX + width || scrollWidth + width < scrollX) {
            return;
        }
        final View child = getChildAt(screen);
        final int faceIndex = screen;
        final int currentDegree = mCurrentDegree;
        final int preFaceDegree = mPreFaceDegree;
        final float faceDegree = currentDegree - faceIndex * preFaceDegree;
        Log.d(TAG, "currentDegree : " + currentDegree + ", faceDegree : " + faceDegree);
        if(faceDegree > 90 || faceDegree < -90) {
            return;
        }
        final float centerX = (scrollWidth < scrollX)?scrollWidth + width:scrollWidth;
        final float centerY = getHeight()/2;
        final Camera camera = mCamera;
        final Matrix matrix = mMatrix;
        canvas.save();
        camera.save();
        camera.rotateY(-faceDegree);
        camera.getMatrix(matrix);
        camera.restore();
        //将中心点设置为本页的边缘
        matrix.preTranslate(-(centerX), -centerY);
        matrix.postTranslate((centerX), centerY);
        canvas.concat(matrix);
        drawChild(canvas, child, drawingTime);
        canvas.restore();
    }
}

