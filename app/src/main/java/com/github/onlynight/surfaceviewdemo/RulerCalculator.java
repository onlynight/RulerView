package com.github.onlynight.surfaceviewdemo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lion on 2016/6/21 0021.
 */
public class RulerCalculator {

    private int width; //控件宽度
    private int height; //控件高度
    private float rulerWidth; // 尺子宽度
    private float rulerScaleWidth; //刻度宽度
    private float measureIconSize; //测量按钮尺寸

    private float kTouchPoint = Float.MAX_VALUE;
    private float kRulerLine = Float.MAX_VALUE;
    private float bTouchPoint = Float.MAX_VALUE;
    private float bRulerLine1 = Float.MAX_VALUE;
    private float bRulerLine2 = Float.MAX_VALUE;
    private float bRuler1 = Float.MAX_VALUE;
    private float bRuler2 = Float.MAX_VALUE;
    private float bRuler3 = Float.MAX_VALUE; // 1/2

    private List<PointO> rulerLinePoints = new ArrayList<>();
    private List<PointO> rulerPoints = new ArrayList<>();
    private List<PointO> rulerBottomScalePoints = new ArrayList<>();
    private List<PointO> rulerTop1ScalePoints = new ArrayList<>();
    private List<PointO> rulerTop2ScalePoints = new ArrayList<>();
    private List<PointO> operatorPoints = new ArrayList<>();

    public RulerCalculator(int width, int height, float rulerWidth,
                           float rulerScaleWidth, float measureIconSize) {
        this.width = width;
        this.height = height;
        this.rulerWidth = rulerWidth;
        this.rulerScaleWidth = rulerScaleWidth;
        this.measureIconSize = measureIconSize;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float getRulerWidth() {
        return rulerWidth;
    }

    public float getRulerScaleWidth() {
        return rulerScaleWidth;
    }

    public List<PointO> getRulerLinePoints() {
        return rulerLinePoints;
    }

    public List<PointO> getRulerPoints() {
        return rulerPoints;
    }

    public List<PointO> getRulerTop1ScalePoints() {
        return rulerTop1ScalePoints;
    }

    public List<PointO> getRulerTop2ScalePoints() {
        return rulerTop2ScalePoints;
    }

    public List<PointO> getRulerBottomScalePoints() {
        return rulerBottomScalePoints;
    }

    public List<PointO> getOperatorPoints() {
        return operatorPoints;
    }

    public void calculate(PointO btn1, PointO btn2) {
        calculateRulerLineInfo(btn1, btn2);
        rulerLinePoints = calculateRulerLine(btn1, btn2, kRulerLine, bRulerLine1, bRulerLine2);
        rulerPoints = calculateRulerBasePoint(btn1, btn2, kRulerLine, bRulerLine1, bRulerLine2);
        calculateRulerInfo(rulerPoints, btn1, kTouchPoint);
        if (rulerPoints != null && rulerPoints.size() == 4) {
            rulerBottomScalePoints = calculateBottomRulerScalePoints(btn1, btn2, rulerPoints.get(1), kTouchPoint, bRuler2);
            rulerTop1ScalePoints = calculateTop1RulerScalePoints(btn1, btn2, kTouchPoint, bRuler3);
            rulerTop2ScalePoints = calculateTop2RulerScalePoints(btn1, btn2, rulerPoints.get(0), kTouchPoint, bRuler1);
        }
        operatorPoints = calculateOperator(btn1, btn2);
    }

    private void calculateRulerLineInfo(PointO btn1, PointO btn2) {
        kTouchPoint = (float) (btn1.y - btn2.y) / (float) (btn1.x - btn2.x);
        kRulerLine = -(1 / kTouchPoint);
        bRulerLine1 = btn1.y - kRulerLine * btn1.x;
        bRulerLine2 = btn2.y - kRulerLine * btn2.x;
        bTouchPoint = btn1.y - kTouchPoint * btn1.x;
    }

    private void calculateRulerInfo(List<PointO> rulerPoints, PointO btn1, float k) {
        if (rulerPoints != null && rulerPoints.size() == 4) {
            bRuler1 = rulerPoints.get(0).y - k * rulerPoints.get(0).x;
            bRuler2 = rulerPoints.get(1).y - k * rulerPoints.get(1).x;
            bRuler3 = btn1.y - k * btn1.x;
        } else {
            bRuler1 = Float.MAX_VALUE;
            bRuler2 = Float.MAX_VALUE;
            bRuler3 = Float.MAX_VALUE;
        }
    }

    private List<PointO> calculateRulerLine(PointO btn1, PointO btn2, float k2, float b1, float b2) {
        List<PointO> points = new ArrayList<>();
        if (btn1.y - btn2.y == 0) {
            points.add(new PointO(btn1.x, 0));
            points.add(new PointO(btn1.x, height));
            points.add(new PointO(btn2.x, 0));
            points.add(new PointO(btn2.x, height));
        } else if (btn1.x - btn2.x == 0) {
            points.add(new PointO(0, btn1.y));
            points.add(new PointO(width, btn1.y));
            points.add(new PointO(0, btn2.y));
            points.add(new PointO(width, btn2.y));
        } else {
            try {
                points.add(recalculateRuler(getPointByX(k2, b1, 0), k2, b1));
                points.add(recalculateRuler(getPointByX(k2, b1, width), k2, b1));
                points.add(recalculateRuler(getPointByX(k2, b2, 0), k2, b2));
                points.add(recalculateRuler(getPointByX(k2, b2, width), k2, b2));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return points;
    }

    private List<PointO> calculateRulerBasePoint(PointO btn1, PointO btn2, float k, float b1, float b2) {
        List<PointO> points = new ArrayList<>();
        if (btn1.y - btn2.y == 0) {
            points.add(new PointO(btn1.x, (int) (btn1.y - rulerWidth / 2)));
            points.add(new PointO(btn1.x, (int) (btn1.y + rulerWidth / 2)));
            points.add(new PointO(btn2.x, (int) (btn2.y - rulerWidth / 2)));
            points.add(new PointO(btn2.x, (int) (btn2.y + rulerWidth / 2)));
        } else if (btn1.x - btn2.x == 0) {
            points.add(new PointO((int) (btn1.x - rulerWidth / 2), btn1.y));
            points.add(new PointO((int) (btn1.x + rulerWidth / 2), btn1.y));
            points.add(new PointO((int) (btn2.x - rulerWidth / 2), btn2.y));
            points.add(new PointO((int) (btn2.x + rulerWidth / 2), btn2.y));
        } else {
            double angle = Math.atan(k) * 180 / Math.PI;
            if (-1 < -1 / k && -1 / k < 1) {
                try {
                    float linear_a = (float) (Math.pow(1 / k, 2) + 1);
                    float linear_b = (float) -(2 * btn1.x / k + 2 * b1 / Math.pow(k, 2) + 2 * btn1.y);
                    float linear_c = (float) (Math.pow(btn1.x, 2) + Math.pow(btn1.y, 2) +
                            Math.pow(b1 / k, 2) + 2 * b1 * btn1.x / k - Math.pow(rulerWidth / 2f, 2));

                    int y1 = (int) ((-linear_b - Math.sqrt((Math.pow(linear_b, 2) -
                            4 * linear_a * linear_c))) / (2 * linear_a));
                    int y2 = (int) ((-linear_b + Math.sqrt((Math.pow(linear_b, 2) -
                            4 * linear_a * linear_c))) / (2 * linear_a));

                    PointO pt1 = new PointO();
                    PointO pt2 = new PointO();

                    if (btn1.x < btn2.x &&
                            (-90 < angle && angle < -45 || 45 < angle && angle < 90)) {
                        pt1.y = y2;
                        pt2.y = y1;
                    } else {
                        pt1.y = y1;
                        pt2.y = y2;
                    }

                    pt1.x = (int) ((pt1.y - b1) / k);
                    pt2.x = (int) ((pt2.y - b1) / k);

                    linear_a = (float) (Math.pow(1 / k, 2) + 1);
                    linear_b = (float) -(2 * btn2.x / k + 2 * b2 / Math.pow(k, 2) + 2 * btn2.y);
                    linear_c = (float) (Math.pow(btn2.x, 2) + Math.pow(btn2.y, 2) +
                            Math.pow(b2 / k, 2) + 2 * b2 * btn2.x / k - Math.pow(rulerWidth / 2f, 2));

                    y1 = (int) ((-linear_b - Math.sqrt((Math.pow(linear_b, 2) -
                            4 * linear_a * linear_c))) / (2 * linear_a));
                    y2 = (int) ((-linear_b + Math.sqrt((Math.pow(linear_b, 2) -
                            4 * linear_a * linear_c))) / (2 * linear_a));

                    PointO pt3 = new PointO();
                    PointO pt4 = new PointO();

                    if (btn1.x < btn2.x &&
                            (-90 < angle && angle < -45 || 45 < angle && angle < 90)) {
                        pt3.y = y2;
                        pt4.y = y1;
                    } else {
                        pt3.y = y1;
                        pt4.y = y2;
                    }

                    pt3.x = (int) ((pt3.y - b2) / k);
                    pt4.x = (int) ((pt4.y - b2) / k);

                    points.add(pt1);
                    points.add(pt2);
                    points.add(pt3);
                    points.add(pt4);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    float linear_a = (float) (Math.pow(k, 2) + 1);
                    float linear_b = 2 * k * b1 - 2 * btn1.x - 2 * k * btn1.y;
                    float linear_c = (float) (Math.pow(btn1.x, 2) + Math.pow(b1, 2) - 2 * b1 * btn1.y +
                            Math.pow(btn1.y, 2) - Math.pow(rulerWidth / 2f, 2));

                    int x1 = (int) ((-linear_b - Math.sqrt((Math.pow(linear_b, 2) -
                            4 * linear_a * linear_c))) / (2 * linear_a));
                    int x2 = (int) ((-linear_b + Math.sqrt((Math.pow(linear_b, 2) -
                            4 * linear_a * linear_c))) / (2 * linear_a));
                    PointO pt1 = new PointO();
                    PointO pt2 = new PointO();
                    if (btn1.x > btn2.x && -45 < angle && angle < 0 ||
                            btn1.x < btn2.x && 0 < angle && angle < 45) {
                        pt1.x = x2;
                        pt2.x = x1;
                    } else {
                        pt1.x = x1;
                        pt2.x = x2;
                    }
                    pt1.y = (int) (k * pt1.x + b1);
                    pt2.y = (int) (k * pt2.x + b1);

                    linear_a = (float) (Math.pow(k, 2) + 1);
                    linear_b = 2 * k * b2 - 2 * btn2.x - 2 * k * btn2.y;
                    linear_c = (float) (Math.pow(btn2.x, 2) + Math.pow(b2, 2) - 2 * b2 * btn2.y +
                            Math.pow(btn2.y, 2) - Math.pow(rulerWidth / 2f, 2));

                    x1 = (int) ((-linear_b - Math.sqrt((Math.pow(linear_b, 2) -
                            4 * linear_a * linear_c))) / (2 * linear_a));
                    x2 = (int) ((-linear_b + Math.sqrt((Math.pow(linear_b, 2) -
                            4 * linear_a * linear_c))) / (2 * linear_a));
                    PointO pt3 = new PointO();
                    PointO pt4 = new PointO();
                    if (btn1.x > btn2.x && -45 < angle && angle < 0 ||
                            btn1.x < btn2.x && 0 < angle && angle < 45) {
                        pt3.x = x2;
                        pt4.x = x1;
                    } else {
                        pt3.x = x1;
                        pt4.x = x2;
                    }
                    pt3.y = (int) (k * pt3.x + b2);
                    pt4.y = (int) (k * pt4.x + b2);

                    points.add(pt1);
                    points.add(pt2);
                    points.add(pt3);
                    points.add(pt4);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return points;
    }

    private PointO calculateRulerScalePoint(PointO btn1, PointO btn2, PointO basePt, float k, float b, float distance) {
        return calculateRulerScalePoint(btn1, btn2, basePt, k, b, distance, false);
    }

    private PointO calculateRulerScalePoint(PointO btn1, PointO btn2, PointO basePt, float k, float b, float distance, boolean reverse) {
        PointO finalPoint = new PointO();
        if (btn1.y - btn2.y == 0) {
            if (btn1.x > btn2.x) {
                if (reverse) {
                    finalPoint.x = (int) (basePt.x + distance);
                } else {
                    finalPoint.x = (int) (basePt.x - distance);
                }
            } else {
                if (reverse) {
                    finalPoint.x = (int) (basePt.x - distance);
                } else {
                    finalPoint.x = (int) (basePt.x + distance);
                }
            }
            finalPoint.y = basePt.y;
        } else if (btn1.x - btn2.x == 0) {
            finalPoint.x = basePt.x;
            if (btn1.y > btn2.y) {
                if (reverse) {
                    finalPoint.y = (int) (basePt.y + distance);
                } else {
                    finalPoint.y = (int) (basePt.y - distance);
                }
            } else {
                if (reverse) {
                    finalPoint.y = (int) (basePt.y - distance);
                } else {
                    finalPoint.y = (int) (basePt.y + distance);
                }
            }
        } else {
            double angle = Math.atan(k) * 180 / Math.PI;
            if (-1 < k && k < 1) {
                try {
                    float linear_a = (float) (Math.pow(k, 2) + 1);
                    float linear_b = 2 * k * b - 2 * basePt.x - 2 * k * basePt.y;
                    float linear_c = (float) (Math.pow(basePt.x, 2) + Math.pow(b, 2) - 2 * b * basePt.y +
                            Math.pow(basePt.y, 2) - Math.pow(distance, 2));

                    int x1 = (int) ((-linear_b - Math.sqrt((Math.pow(linear_b, 2) -
                            4 * linear_a * linear_c))) / (2 * linear_a));
                    int x2 = (int) ((-linear_b + Math.sqrt((Math.pow(linear_b, 2) -
                            4 * linear_a * linear_c))) / (2 * linear_a));
                    if (btn1.x < btn2.x && -45 < angle && angle < 45) {
                        if (reverse) {
                            finalPoint.x = x1;
                        } else {
                            finalPoint.x = x2;
                        }
                    } else {
                        if (reverse) {
                            finalPoint.x = x2;
                        } else {
                            finalPoint.x = x1;
                        }
                    }
                    finalPoint.y = (int) (k * finalPoint.x + b);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    float linear_a = (float) (Math.pow(1 / k, 2) + 1);
                    float linear_b = (float) -(2 * basePt.x / k + 2 * b / Math.pow(k, 2) + 2 * basePt.y);
                    float linear_c = (float) (Math.pow(basePt.x, 2) + Math.pow(basePt.y, 2) +
                            Math.pow(b / k, 2) + 2 * b * basePt.x / k - Math.pow(distance, 2));

                    int y1 = (int) ((-linear_b - Math.sqrt((Math.pow(linear_b, 2)
                            - 4 * linear_a * linear_c))) / (2 * linear_a));
                    int y2 = (int) ((-linear_b + Math.sqrt((Math.pow(linear_b, 2)
                            - 4 * linear_a * linear_c))) / (2 * linear_a));
                    if (btn1.x > btn2.x && -90 < angle && angle <= -45 ||
                            btn1.x < btn2.x && 45 <= angle && angle < 90) {
                        if (reverse) {
                            finalPoint.y = y1;
                        } else {
                            finalPoint.y = y2;
                        }
                    } else {
                        if (reverse) {
                            finalPoint.y = y2;
                        } else {
                            finalPoint.y = y1;
                        }
                    }
                    finalPoint.x = (int) ((finalPoint.y - b) / k);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return finalPoint;
    }

    private List<PointO> calculateRulerScalePoints(PointO btn1, PointO btn2, PointO basePt, float k, float b, float distance) {
        List<PointO> pointOs = new ArrayList<>();
        if (btn1.y - btn2.y == 0) {
            PointO point1 = new PointO();
            point1.x = basePt.x;
            point1.y = (int) (basePt.y + distance);
            pointOs.add(point1);
            PointO point2 = new PointO();
            point2.x = basePt.x;
            point2.y = (int) (basePt.y - distance);
            pointOs.add(point2);
        } else if (btn1.x - btn2.x == 0) {
            PointO point1 = new PointO();
            point1.x = (int) (basePt.x + distance);
            point1.y = basePt.y;
            pointOs.add(point1);
            PointO point2 = new PointO();
            point2.x = (int) (basePt.x - distance);
            point2.y = basePt.y;
            pointOs.add(point2);
        } else {
            if (-1 < k && k < 1) {
                try {
                    float linear_a = (float) (Math.pow(k, 2) + 1);
                    float linear_b = 2 * k * b - 2 * basePt.x - 2 * k * basePt.y;
                    float linear_c = (float) (Math.pow(basePt.x, 2) + Math.pow(b, 2) - 2 * b * basePt.y +
                            Math.pow(basePt.y, 2) - Math.pow(distance, 2));

                    int x1 = (int) ((-linear_b - Math.sqrt((Math.pow(linear_b, 2) -
                            4 * linear_a * linear_c))) / (2 * linear_a));
                    int x2 = (int) ((-linear_b + Math.sqrt((Math.pow(linear_b, 2) -
                            4 * linear_a * linear_c))) / (2 * linear_a));
                    PointO point1 = new PointO();
                    point1.x = x1;
                    point1.y = (int) (k * point1.x + b);
                    pointOs.add(point1);

                    PointO point2 = new PointO();
                    point2.x = x2;
                    point2.y = (int) (k * point2.x + b);
                    pointOs.add(point2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    float linear_a = (float) (Math.pow(1 / k, 2) + 1);
                    float linear_b = (float) -(2 * basePt.x / k + 2 * b / Math.pow(k, 2) + 2 * basePt.y);
                    float linear_c = (float) (Math.pow(basePt.x, 2) + Math.pow(basePt.y, 2) +
                            Math.pow(b / k, 2) + 2 * b * basePt.x / k - Math.pow(distance, 2));

                    int y1 = (int) ((-linear_b - Math.sqrt((Math.pow(linear_b, 2)
                            - 4 * linear_a * linear_c))) / (2 * linear_a));
                    int y2 = (int) ((-linear_b + Math.sqrt((Math.pow(linear_b, 2)
                            - 4 * linear_a * linear_c))) / (2 * linear_a));
                    PointO point1 = new PointO();
                    point1.y = y1;
                    point1.x = (int) ((point1.y - b) / k);
                    pointOs.add(point1);
                    PointO point2 = new PointO();
                    point2.y = y2;
                    point2.x = (int) ((point2.y - b) / k);
                    pointOs.add(point2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return pointOs;
    }

    private List<PointO> calculateBottomRulerScalePoints(PointO btn1, PointO btn2, PointO basePt, float k, float b) {
        double distance = distance(btn1, btn2);
        List<PointO> points = new ArrayList<>();
        for (int i = 0; i < ((int) distance / (int) rulerScaleWidth); i++) {
            PointO point = calculateRulerScalePoint(btn1, btn2, basePt, k, b, (i + 1) * rulerScaleWidth);
            points.add(point);
        }
        return points;
    }

    private List<PointO> calculateTop1RulerScalePoints(PointO btn1, PointO btn2, float k, float b) {
        double distance = distance(btn1, btn2);
        List<PointO> points = new ArrayList<>();
        for (int i = 0; i < ((int) distance / (int) rulerScaleWidth); i++) {
            PointO point = calculateRulerScalePoint(btn1, btn2, btn1, k, b, (i + 1) * rulerScaleWidth);
            points.add(point);
        }
        return points;
    }

    private List<PointO> calculateTop2RulerScalePoints(PointO btn1, PointO btn2, PointO basePt, float k, float b) {
        double distance = distance(btn1, btn2);
        List<PointO> points = new ArrayList<>();
        float blank = rulerScaleWidth * 5;
        for (int i = 0; i < ((int) distance / (int) blank); i++) {
            PointO point = calculateRulerScalePoint(btn1, btn2, basePt, k, b, (i + 1) * blank);
            points.add(point);
        }
        return points;
    }

    private List<PointO> calculateOperator(PointO btn1, PointO btn2) {
        List<PointO> pointOs = new ArrayList<>();
        float k = kTouchPoint;
        float b = btn1.y - k * btn1.x;
        float distance = (float) Math.sqrt(Math.pow(measureIconSize, 2) - Math.pow(measureIconSize / 2, 2));
        PointO point1 = calculateRulerScalePoint(btn1, btn2, btn1, k, b, distance, true);
        float b1 = point1.y - kRulerLine * point1.x;
        List<PointO> points1 = calculateRulerScalePoints(btn1, btn2, point1, kRulerLine, b1,
                measureIconSize / 2);
        if (points1 != null) {
            pointOs.addAll(points1);
        }

        PointO point2 = calculateRulerScalePoint(btn1, btn2, btn2, k, b, distance);
        float b2 = point2.y - kRulerLine * point2.x;
        List<PointO> points2 = calculateRulerScalePoints(btn1, btn2, point2, kRulerLine, b2,
                measureIconSize / 2);
        if (points2 != null) {
            pointOs.addAll(points2);
        }

        return pointOs;
    }

    private PointO recalculateRuler(PointO pt, float k, float b) {
        if (pt.x == 0 && pt.y < 0) {
            pt = getPointByY(k, b, 0);
        } else if (pt.x == getWidth() && pt.y > getHeight()) {
            pt = getPointByY(k, b, getHeight());
        } else if (pt.x == getWidth() && pt.y < 0) {
            pt = getPointByY(k, b, 0);
        } else if (pt.x == 0 && pt.y > getHeight()) {
            pt = getPointByY(k, b, getHeight());
        }

        return pt;
    }

    private PointO getPointByX(float k, float b, float x) {
        return new PointO((int) x, (int) (k * x + b));
    }

    private PointO getPointByY(float k, float b, float y) {
        return new PointO((int) ((y - b) / k), (int) y);
    }

    private double distance(PointO pt1, PointO pt2) {
        return Math.sqrt(Math.pow(pt1.x - pt2.x, 2) + Math.pow(pt1.y - pt2.y, 2));
    }

    public static class PointO {
        public int x;
        public int y;

        public PointO(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public PointO() {
        }
    }
}
