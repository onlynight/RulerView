package com.github.onlynight.surfaceviewdemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lion on 2016/10/25.
 */

public class FreeMeasureSurfaceView extends SurfaceView implements
        SurfaceHolder.Callback, Runnable {

    public static final int LINE_TYPE_DASH_LINE = 1;
    public static final int LINE_TYPE_LINE = 2;
    private static final int INVALID_POINTER_ID = -1;

    private final static int MEASURE_BTN_START_X = 50;//dp
    private final static int MEASURE_BTN_START_Y = 80;//dp
    private final static int MEASURE_BTN_BOTTOM_START_Y = 200;//dp
    private final static int DASH_LINE_BLANK_DEFAULT = 3;//dp
    private final static int MEASURE_TEXT_SIZE_DEFAULT = 18;//sp
    private final static int RULER_WIDTH = 30;
    private final static int RULER_SCALE_WIDTH = 3;
    private final static int MEASURE_ICON_SIZE = 30;

    private SurfaceHolder surfaceHolder;
    private Thread drawThread;
    private boolean drawFlag = true;
    private boolean drawing = false;

    private RulerCalculator rulerCalculator;

    private Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint rulerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int lineType = LINE_TYPE_DASH_LINE;
    private int lineColor = Color.WHITE;
    private int rulerColor = Color.WHITE;
    private float lineHeight = 0f;
    private float rulerLineHeight = 0f;
    private float dashLineBlank = 0;
    private float measureTextSizef = 0;
    private int measureTextColor = Color.WHITE;

    private float rulerWidth = 0;
    private float rulerScaleWidth = 0;
    private float measureIconSize = 0;

    private Point measureBtn1Pos = new Point();
    private Point measureBtn2Pos = new Point();
    private Rect measureTextSize = new Rect();

    private boolean isMeasureBtn1CanMove = false;
    private boolean isMeasureBtn2CanMove = false;

    private TouchPoint[] tpoints = new TouchPoint[2];
    private boolean showBorder = true;
    private boolean lastBorderVisible = true;
    private boolean applyCameraScale = false;

    private float finalMeasureLength = 0;
    private double detectScale = 0;
    private float realBitmapScale = 1;

    private OnMeasureBtnListener onMeasureBtnListener;

    public void setOnMeasureBtnListener(OnMeasureBtnListener onMeasureBtnListener) {
        this.onMeasureBtnListener = onMeasureBtnListener;
    }

    public FreeMeasureSurfaceView(Context context) {
        super(context);
        init(null);
    }

    public FreeMeasureSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs,
                R.styleable.FreeMeasureView);
        init(typedArray);
    }

    public FreeMeasureSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs,
                R.styleable.FreeMeasureView, defStyleAttr, 0);
        init(typedArray);
    }

    private void init(TypedArray typedArray) {
        setZOrderOnTop(true);
        surfaceHolder = getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        surfaceHolder.addCallback(this);
        drawThread = new Thread(this);

        lineHeight = typedArray.getDimension(R.styleable.FreeMeasureView_line_height,
                Tools.dp2px(getContext(), 1));
        rulerLineHeight = typedArray.getDimension(R.styleable.FreeMeasureView_ruler_line_height,
                Tools.dp2px(getContext(), 2));
        lineType = typedArray.getInt(R.styleable.FreeMeasureView_line_type, LINE_TYPE_DASH_LINE);
        lineColor = typedArray.getColor(R.styleable.FreeMeasureView_line_color, Color.WHITE);
        rulerColor = typedArray.getColor(R.styleable.FreeMeasureView_ruler_color, Color.WHITE);
        dashLineBlank = typedArray.getDimension(R.styleable.FreeMeasureView_dash_line_blank,
                Tools.dp2px(getContext(), DASH_LINE_BLANK_DEFAULT));
        rulerWidth = typedArray.getDimension(
                R.styleable.FreeMeasureView_measure_ruler_width,
                Tools.dp2px(getContext(), RULER_WIDTH));
        rulerScaleWidth = typedArray.getDimension(
                R.styleable.FreeMeasureView_measure_ruler_scale_width,
                Tools.dp2px(getContext(), RULER_SCALE_WIDTH));
        measureIconSize = typedArray.getDimension(
                R.styleable.FreeMeasureView_measure_icon_size,
                Tools.dp2px(getContext(), MEASURE_ICON_SIZE));

        initMeasureBtnRect();
    }

    private void initMeasureBtnRect() {
        int width = getWidth();
        if (width > 0) {
            float left = width - Tools.dp2px(getContext(), MEASURE_BTN_START_X);
            float top = Tools.dp2px(getContext(), MEASURE_BTN_START_Y);
            measureBtn1Pos.x = (int) left;
            measureBtn1Pos.y = (int) top;

            measureBtn2Pos.x = (int) left;
            measureBtn2Pos.y = getHeight() - MEASURE_BTN_BOTTOM_START_Y * 2;

            invalidate();
            rulerCalculator = new RulerCalculator(getWidth(), getHeight(),
                    rulerWidth, rulerScaleWidth, measureIconSize);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initMeasureBtnRect();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setDrawFlag(true);
        drawThread = new Thread(this);
        drawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        setDrawFlag(false);
        drawThread = null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pointerCount = MotionEventCompat.getPointerCount(event);
        final int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                if (pointerCount > 2) {
                    break;
                }
                int pointerIndex = MotionEventCompat.getActionIndex(event);
                int activePointerId = MotionEventCompat.getPointerId(event, pointerIndex);
                TouchPoint tpoint = getTouchPoint(activePointerId);
                if (tpoint != null) {
                    float x = MotionEventCompat.getX(event, activePointerId);
                    float y = MotionEventCompat.getY(event, activePointerId);
                    tpoint.x = x;
                    tpoint.y = y;
                    tpoint.pointerId = activePointerId;

                    showBorder = checkBtnCanMove(tpoints);
                    if (onMeasureBtnListener != null) {
                        if (!showBorder) {
                            lastBorderVisible = onMeasureBtnListener.onShowBorder(showBorder);
                        }
                    }
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (pointerCount > 2) {
                    break;
                }
                moveMeasureBtn(tpoints, event);
                invalidate();
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (onMeasureBtnListener != null) {
                    if (lastBorderVisible && showBorder) {
                        showBorder = false;
                    }
                    lastBorderVisible = onMeasureBtnListener.onShowBorder(showBorder);
                }

                isMeasureBtn1CanMove = false;
                isMeasureBtn2CanMove = false;

                for (TouchPoint tp : tpoints) {
                    if (tp != null) {
                        tp.pointerId = INVALID_POINTER_ID;
                        tp.inArea = null;
                    }
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                if (pointerCount > 2) {
                    break;
                }
                int pointerIndex = MotionEventCompat.getActionIndex(event);
                int activePointerId = MotionEventCompat.getPointerId(event, pointerIndex);
                TouchPoint tpoint = getTouchPoint(activePointerId);
                if (tpoint != null) {
                    float x = MotionEventCompat.getX(event, activePointerId);
                    float y = MotionEventCompat.getY(event, activePointerId);
                    tpoint.x = x;
                    tpoint.y = y;
                    tpoint.pointerId = activePointerId;

                    showBorder = checkBtnCanMove(tpoints);
                    if (onMeasureBtnListener != null) {
                        if (!showBorder) {
                            lastBorderVisible = onMeasureBtnListener.onShowBorder(showBorder);
                        }
                    }
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                if (onMeasureBtnListener != null) {
                    if (lastBorderVisible && showBorder) {
                        showBorder = false;
                    }
                    lastBorderVisible = onMeasureBtnListener.onShowBorder(showBorder);
                }

                int pointerIndex = MotionEventCompat.getActionIndex(event);
                for (TouchPoint tp : tpoints) {
                    if (tp != null && tp.pointerId == pointerIndex) {
                        tp.pointerId = INVALID_POINTER_ID;
                    }
                }
                break;
            }
        }
        return true;
    }

    public void setDrawFlag(boolean drawFlag) {
        this.drawFlag = drawFlag;
    }

    @Override
    public void run() {
        while (drawFlag) {
            drawing = true;
            Canvas canvas = null;
            synchronized (surfaceHolder) {
                try {
                    canvas = surfaceHolder.lockCanvas();
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    innerDraw(canvas);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (null != canvas) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }

    private TouchPoint getTouchPoint(int pointerId) {
        for (TouchPoint tp : tpoints) {
            if (tp != null && tp.pointerId == pointerId) {
                return tp;
            }
        }

        for (TouchPoint tp : tpoints) {
            if (tp != null && tp.pointerId == INVALID_POINTER_ID) {
                tp.pointerId = pointerId;
                return tp;
            }
        }

        for (int i = 0; i < tpoints.length; i++) {
            if (tpoints[i] == null) {
                tpoints[i] = new TouchPoint();
                tpoints[i].pointerId = pointerId;
                return tpoints[i];
            }
        }

        return null;
    }

    private void moveMeasureBtn(TouchPoint[] tpts, MotionEvent event) {
        List<TouchPoint> movePts = new ArrayList<>();
        for (TouchPoint tpt1 : tpts) {
            if (tpt1 != null && tpt1.pointerId != INVALID_POINTER_ID) {
                int pointerIndex = MotionEventCompat.findPointerIndex(event, tpt1.pointerId);
                float x = MotionEventCompat.getX(event, pointerIndex);
                float y = MotionEventCompat.getY(event, pointerIndex);
                TouchPoint movePt = new TouchPoint();
                movePt.x = x;
                movePt.y = y;
                movePt.pointerId = pointerIndex;
                movePts.add(movePt);
            }
        }
        for (TouchPoint tpt : tpts) {
            for (TouchPoint movePt : movePts) {
                if (tpt != null && tpt.pointerId != INVALID_POINTER_ID
                        && movePt != null && movePt.pointerId == tpt.pointerId) {
                    float y = movePt.y;
                    float x = movePt.x;
                    if (tpt.inArea == measureBtn1Pos) {
                        int top = (int) (y - measureIconSize);
                        int left = (int) (x - measureIconSize);
                        int width = (int) measureIconSize;
                        int deltaX = tpt.deltaPoint.x;
                        int deltaY = tpt.deltaPoint.y;

                        if (top <= 0) {
                            top = 0;
                        }

                        if (top >= getHeight() - measureIconSize -
                                Tools.getNavigationBarHeight(getContext())) {
                            top = (int) (getHeight() - measureIconSize -
                                    Tools.getNavigationBarHeight(getContext()));
                        }

                        if (left < 0) {
                            left = 0;
                        }

                        if (left + width > getWidth()) {
                            left = getWidth() - width;
                        }

                        measureBtn1Pos.y = (int) (top + measureIconSize - deltaY);
                        measureBtn1Pos.x = (int) (left + measureIconSize - deltaX);

                        if (onMeasureBtnListener != null) {
                            onMeasureBtnListener.onMove();
                        }
                    }

                    if (tpt.inArea == measureBtn2Pos) {
                        int top = (int) (y - measureIconSize);
                        int left = (int) (x - measureIconSize);
                        int width = (int) measureIconSize;
                        int deltaX = tpt.deltaPoint.x;
                        int deltaY = tpt.deltaPoint.y;

                        if (top <= 0) {
                            top = 0;
                        }

                        if (top >= getHeight() - measureIconSize -
                                Tools.getNavigationBarHeight(getContext())) {
                            top = (int) (getHeight() - measureIconSize -
                                    Tools.getNavigationBarHeight(getContext()));
                        }

                        if (left < 0) {
                            left = 0;
                        }

                        if (left + width > getWidth()) {
                            left = getWidth() - width;
                        }

                        measureBtn2Pos.y = (int) (top + measureIconSize - deltaY);
                        measureBtn2Pos.x = (int) (left + measureIconSize - deltaX);

                        if (onMeasureBtnListener != null) {
                            onMeasureBtnListener.onMove();
                        }
                    }
                }
            }
        }
    }

    private boolean checkBtnCanMove(TouchPoint[] tpts) {
//        int touchArea = Tools.dp2px(getContext(), 20);
        int checkArea = (int) (measureIconSize * 2);
        for (TouchPoint tpt : tpts) {
            if (tpt != null && tpt.pointerId != INVALID_POINTER_ID) {
                if (tpt.x > measureBtn1Pos.x - checkArea &&
                        tpt.x < measureBtn1Pos.x + checkArea &&
                        tpt.y > measureBtn1Pos.y - checkArea &&
                        tpt.y < measureBtn1Pos.y + checkArea) {
                    isMeasureBtn1CanMove = true;
                    if (tpt.inArea == null) {
                        tpt.deltaPoint.x = (int) (tpt.x - measureBtn1Pos.x);
                        tpt.deltaPoint.y = (int) (tpt.y - measureBtn1Pos.y);
                    }
                    tpt.inArea = measureBtn1Pos;
                }

                if (tpt.x > measureBtn2Pos.x - checkArea &&
                        tpt.x < measureBtn2Pos.x + checkArea &&
                        tpt.y > measureBtn2Pos.y - checkArea &&
                        tpt.y < measureBtn2Pos.y + checkArea) {
                    isMeasureBtn2CanMove = true;
                    if (tpt.inArea == null) {
                        tpt.deltaPoint.x = (int) (tpt.x - measureBtn2Pos.x);
                        tpt.deltaPoint.y = (int) (tpt.y - measureBtn2Pos.y);
                    }
                    tpt.inArea = measureBtn2Pos;
                }
            }
        }

        return !(isMeasureBtn1CanMove | isMeasureBtn2CanMove);
    }

    private void initPaint() {
        linePaint.setColor(lineColor);
        linePaint.setStrokeWidth(lineHeight);
        linePaint.setStyle(Paint.Style.STROKE);
        if (lineType == LINE_TYPE_DASH_LINE) {
            PathEffect effects = new DashPathEffect(new float[]{dashLineBlank, dashLineBlank,
                    dashLineBlank, dashLineBlank}, 1);
            linePaint.setPathEffect(effects);
        }

        rulerPaint.setColor(rulerColor);
        rulerPaint.setStrokeWidth(rulerLineHeight);
        rulerPaint.setStyle(Paint.Style.STROKE);
    }

    private void innerDraw(Canvas canvas) {
        initPaint();

        drawRuler(canvas);
//        float length = (float) rulerLength();
//        drawMeasureText(canvas, length, (float) detectScale);
    }

    private void drawRuler(Canvas canvas) {
        if (rulerCalculator != null) {
            LogUtils.d("calculate ruler");
            LogUtils.d(new Date().getTime());
            rulerCalculator.calculate(Tools.point2PointO(measureBtn1Pos), Tools.point2PointO(measureBtn2Pos));
            LogUtils.d(new Date().getTime());

            LogUtils.d("draw ruler");
            LogUtils.d(new Date().getTime());
            //画标尺
            List<Point> points = Tools.pointfs2Points(rulerCalculator.getRulerLinePoints());
            if (points != null && points.size() == 4) {
                Path pathRuler1 = new Path();
                pathRuler1.moveTo(points.get(0).x, points.get(0).y);
                pathRuler1.lineTo(points.get(1).x, points.get(1).y);
                canvas.drawPath(pathRuler1, linePaint);

                Path pathRuler2 = new Path();
                pathRuler2.moveTo(points.get(2).x, points.get(2).y);
                pathRuler2.lineTo(points.get(3).x, points.get(3).y);
                canvas.drawPath(pathRuler2, linePaint);
            }

            //画尺子边框
            List<Point> rulerPoints = Tools.pointfs2Points(rulerCalculator.getRulerPoints());
            if (rulerPoints != null && rulerPoints.size() == 4) {
                Path pathRuler1 = new Path();
                pathRuler1.moveTo(rulerPoints.get(0).x, rulerPoints.get(0).y);
                pathRuler1.lineTo(rulerPoints.get(2).x, rulerPoints.get(2).y);
                canvas.drawPath(pathRuler1, rulerPaint);

                Path pathRuler2 = new Path();
                pathRuler2.moveTo(rulerPoints.get(1).x, rulerPoints.get(1).y);
                pathRuler2.lineTo(rulerPoints.get(3).x, rulerPoints.get(3).y);
                canvas.drawPath(pathRuler2, rulerPaint);

                Path pathRuler3 = new Path();
                pathRuler3.moveTo(rulerPoints.get(0).x, rulerPoints.get(0).y);
                pathRuler3.lineTo(rulerPoints.get(1).x, rulerPoints.get(1).y);
                canvas.drawPath(pathRuler3, rulerPaint);

                Path pathRuler4 = new Path();
                pathRuler4.moveTo(rulerPoints.get(2).x, rulerPoints.get(2).y);
                pathRuler4.lineTo(rulerPoints.get(3).x, rulerPoints.get(3).y);
                canvas.drawPath(pathRuler4, rulerPaint);
            }

            //画尺子刻度
            List<Point> bottomScalePoints = Tools.pointfs2Points(rulerCalculator.getRulerBottomScalePoints());
            List<Point> top1ScalePoints = Tools.pointfs2Points(rulerCalculator.getRulerTop1ScalePoints());
            List<Point> top2ScalePoints = Tools.pointfs2Points(rulerCalculator.getRulerTop2ScalePoints());
            List<Point> operatorPoints = Tools.pointfs2Points(rulerCalculator.getOperatorPoints());
            //画尺子小刻度
            if (bottomScalePoints != null && bottomScalePoints.size() > 0
                    && top1ScalePoints != null && top1ScalePoints.size() > 0) {
                for (int i = 0; i < bottomScalePoints.size(); i++) {
                    try {
                        Path path = new Path();
                        path.moveTo(top1ScalePoints.get(i).x, top1ScalePoints.get(i).y);
                        path.lineTo(bottomScalePoints.get(i).x, bottomScalePoints.get(i).y);
                        canvas.drawPath(path, rulerPaint);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            //画尺子大刻度
            if (bottomScalePoints != null && bottomScalePoints.size() > 0
                    && top2ScalePoints != null && top2ScalePoints.size() > 0) {
                for (int i = 0; i < top2ScalePoints.size(); i++) {
                    int index = (i + 1) * 5 - 1;
                    if (bottomScalePoints.size() > index) {
                        Path path = new Path();
                        path.moveTo(top2ScalePoints.get(i).x, top2ScalePoints.get(i).y);
                        path.lineTo(bottomScalePoints.get((i + 1) * 5 - 1).x, bottomScalePoints.get((i + 1) * 5 - 1).y);
                        canvas.drawPath(path, rulerPaint);
                    }
                }
            }

            //画操作按钮
            if (operatorPoints != null && operatorPoints.size() == 4) {
                Path path = new Path();
                path.moveTo(measureBtn1Pos.x, measureBtn1Pos.y);
                path.lineTo(operatorPoints.get(0).x, operatorPoints.get(0).y);
                canvas.drawPath(path, rulerPaint);

                path.moveTo(operatorPoints.get(0).x, operatorPoints.get(0).y);
                path.lineTo(operatorPoints.get(1).x, operatorPoints.get(1).y);
                canvas.drawPath(path, rulerPaint);

                path.moveTo(operatorPoints.get(1).x, operatorPoints.get(1).y);
                path.lineTo(measureBtn1Pos.x, measureBtn1Pos.y);
                canvas.drawPath(path, rulerPaint);

                path.moveTo(measureBtn2Pos.x, measureBtn2Pos.y);
                path.lineTo(operatorPoints.get(2).x, operatorPoints.get(2).y);
                canvas.drawPath(path, rulerPaint);

                path.moveTo(operatorPoints.get(2).x, operatorPoints.get(2).y);
                path.lineTo(operatorPoints.get(3).x, operatorPoints.get(3).y);
                canvas.drawPath(path, rulerPaint);

                path.moveTo(operatorPoints.get(3).x, operatorPoints.get(3).y);
                path.lineTo(measureBtn2Pos.x, measureBtn2Pos.y);
                canvas.drawPath(path, rulerPaint);
            }

            LogUtils.d(new Date().getTime());
        }
    }

    public double rulerLength() {
        return distance(measureBtn1Pos, measureBtn2Pos);
    }

    private double distance(Point btn1, Point btn2) {
        return Math.sqrt(Math.pow(btn1.x - btn2.x, 2) + Math.pow(btn1.y - btn2.y, 2));
    }

    public interface OnMeasureBtnListener {
        boolean onShowBorder(boolean visible);

        void onMove();
    }

    private static class TouchPoint {
        public float x;
        public float y;
        public Point inArea;
        public Point deltaPoint = new Point(0, 0);
        public int pointerId = INVALID_POINTER_ID;
    }
}
