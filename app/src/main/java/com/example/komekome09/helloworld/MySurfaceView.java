package com.example.komekome09.helloworld;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

/**
 * SurfaceView for Boids
 */
public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable{
    private MainActivity ac;
    private SurfaceHolder mHolder;
    private Thread mLooper;

    private int mDisplayWidth;
    private int mDisplayHeight;
    private int mCircNum = getResources().getInteger(R.integer.init_num);
    private int mCircPrevNum = 0;
    private int mMaxCirc = getResources().getInteger(R.integer.max_num);
    private double mMaxVelocity = 10;
    private double mCircRad = 3.0;
    private boolean bFirst = true;
    private boolean bSurface = false;

    private ArrayList<CircleData> list = new ArrayList<>(mMaxCirc);
    private ArrayList<Double> dist = new ArrayList<>(mMaxCirc * mMaxCirc);

    public class CircleData{
        private double px;
        private double py;
        private double vx;
        private double vy;
        private double radius;
        private int color;

        public CircleData(double px, double py, double vx, double vy, double radius, int color){
            this.px = px;
            this.py = py;
            this.vx = vx;
            this.vy = vy;
            this.radius = radius;
            this.color = color;
        }

        void move(int width, int height) {
            if (px < 0 || width < px) {
                if(px < 0) px = 0;
                else if(width < px) px = width;
                vx = -vx;
            }
            if (py < 0 || height < py) {
                if(py < 0) py = 0;
                else if(height < py ) py = height;
                vy = -vy;
            }

            // limit speed
            if (vecLength(vx, vy) > mMaxVelocity) {
                double v = mMaxVelocity / vecLength(vx, vy);
                vx *= v;
                vy *= v;
            }

            px += vx;
            py += vy;
        }

    }

    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight){
        mDisplayWidth = width;
        mDisplayHeight = height;
    }

    public MySurfaceView(Context context){
        super(context);
        ac = (MainActivity)context;
        getHolder().addCallback(this);
        mDisplayWidth = getWidth();
        mDisplayHeight = getHeight();
    }

    // This method called if SurfaceView is Created.
    public void surfaceCreated(SurfaceHolder holder){
        mHolder = holder;
        bSurface = true;
        mLooper = new Thread(this);

    }

    // This method called if SurfaceView is updated.
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){
        if(mLooper != null){
            mLooper.start();
        }
    }

    // This method called if SurfaceView is Destroyed.
    public void surfaceDestroyed(SurfaceHolder holder){
        mLooper = null;
        bSurface = false;
    }

    public void run(){
        if(bFirst){
            makeCircle();
            for(int i = 0; i < mMaxCirc * mMaxCirc; i++){
                dist.add(null);
            }
            bFirst = false;
        }
        while(mLooper != null){
            isChanged();
            for (int i = 0; i < mCircNum; i++) {
                calcDistance();
                doAvoid(i);
                doApproachGroup(i);
                doAlignment(i);
                doDraw();
             }
            try {
                Thread.sleep(5);
            }catch(InterruptedException e){
                Log.d("MSG", "Interrupted Exception caught.");
            }
        }
    }

    void makeCircle(){
        Random rand = new Random();

        for(int i = 0; i < mMaxCirc; i++){
            list.add(new CircleData(rand.nextInt(mDisplayWidth),
                    rand.nextInt(mDisplayHeight),
                    rand.nextDouble() * mMaxVelocity,
                    rand.nextDouble() * mMaxVelocity,
                    mCircRad,
                    Color.RED));
        }
    }

    // Reflection setting if setting is changed.
    void isChanged(){
        if(ac.getCircNum() != -1 && ac.getCircNum() != mCircNum){
            mCircPrevNum = mCircNum;
            mCircNum = ac.getCircNum();
            final String str = getResources().getString(R.string.appbar_name, mCircNum);

            // To changed UI method in another thread, you must use handler for inter-thread communication.
            ac.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ac.setTitle(str);
                }
            });
        }
    }

    void doDraw(){
        // canvas must be locked and unlocked to use canvas.
        Canvas canvas = mHolder.lockCanvas();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Is surface alive? = Canvas is not null?
        if(bSurface) {
            canvas.drawColor(Color.WHITE);
            for (int i = 0; i < mCircNum; i++) {
                paint.setColor(list.get(i).color);
                list.get(i).move(mDisplayWidth, mDisplayHeight);
                canvas.drawCircle((float) list.get(i).px, (float) list.get(i).py, (float) list.get(i).radius, paint);
            }
            mHolder.unlockCanvasAndPost(canvas);
        }
    }

    // Boids rules 1: Separation
    void doAvoid(int i){
        for(int j = 0; j < mCircNum; j++){
            if(i != j) {
                double dx = list.get(j).px - list.get(i).px,
                       dy = list.get(j).py - list.get(i).py;

                if (dist.get(i * mCircNum + j) < 10.0) {
                    list.get(i).vx -= dx;
                    list.get(i).vy -= dy;
                }
            }
        }
    }

    // Boids rules 2: Cohesion
    void doApproachGroup(int i){
        CircleData cd = calcGroup(i);
        double dx = cd.px - list.get(i).px,
               dy = cd.py - list.get(i).py;

        list.get(i).vx += dx / 100;
        list.get(i).vy += dy / 100;
    }

    // Boids rules 3: Alignment
    void doAlignment(int i){
        CircleData cd = calcGroup(i);
        double dx = cd.vx - list.get(i).vx,
                dy = cd.vy - list.get(i).vy;

        list.get(i).vx += dx / 8;
        list.get(i).vy += dy / 8;
    }

    double vecLength(double x, double y){
        return Math.sqrt(x * x + y * y);
    }

    void calcDistance(){
        for(int i = 0; i < mCircNum; i++){
            for(int j = i + 1; j < mCircNum; j++){
                double dx = list.get(j).px - list.get(i).px,
                       dy = list.get(j).py - list.get(i).py,
                       d = vecLength(dx, dy);
                dist.set(i * mCircNum + j, d);
                dist.set(i + j * mCircNum, d);
             }
        }
    }

    CircleData calcGroup(int i){
        double aveX = 0, aveY = 0, aveVx = 0, aveVy = 0;
        int sum = mCircNum - 1;
        CircleData ret;
        for(int j = 0; j < mCircNum; j++){
            if(i != j){
                aveX += list.get(j).px;
                aveY += list.get(j).py;
                aveVx += list.get(j).vx;
                aveVy += list.get(j).vy;
            }
        }

        ret = new CircleData(aveX / sum, aveY / sum, aveVx / sum, aveVy / sum, list.get(i).radius, list.get(i).color);
        return ret;
    }
}
