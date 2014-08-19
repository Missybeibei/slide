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
 * ���Խ������ҷ�����Ч��
 * @ClassName: WorkSpace3D
 * @Description: �������ƽ������ҹ���
 */
public class WorkSpace3D extends WorkSpace2D{
    private static final String TAG = "WorkSpace3D";
   
    /**
     * ��ǰ�ĽǶ�,�����ƶ��ľ�����м���
     */
    private int mCurrentDegree;
    /**
     * ÿ��ҳ��ĽǶ�
     */
    private int mPreFaceDegree = 90;
    /**
     * ÿ�ƶ�һ�����ؽǶȵı仯��С
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
     * ������ָ����ʱ�᲻ͣ�����������,��������������м��㵱ǰ�Ƕ�
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
     * ��draw��ǰviewʱ,ͬʱdraw��ǰҳ���ǰһҳ�ͺ�һҳ
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
        //�������
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
        //�����ĵ�����Ϊ��ҳ�ı�Ե
        matrix.preTranslate(-(centerX), -centerY);
        matrix.postTranslate((centerX), centerY);
        canvas.concat(matrix);
        drawChild(canvas, child, drawingTime);
        canvas.restore();
    }
}

