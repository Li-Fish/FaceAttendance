package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.hardware.Camera;
//import android.hardware.camera2.;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.this.getClass().getSimpleName();

    private Camera camera;
    private Lock cameraLock;
    private boolean isPreview = false;

    private TextView mTextView;

    private String rstRec;
    private String rstName;

    private final MyHandler mHandler = new MyHandler(this);

    static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);

        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                activity.mTextView.setText(activity.rstName);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        SurfaceView mSurfaceView = findViewById(R.id.surface_view);
        // 获得 SurfaceHolder 对象
        SurfaceHolder mSurfaceHolder = mSurfaceView.getHolder();

        mTextView = findViewById(R.id.name);

        // 设置 Surface 格式
        // 参数： PixelFormat中定义的 int 值 ,详细参见 PixelFormat.java
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);


        // 如果需要，保持屏幕常亮
        mSurfaceHolder.setKeepScreenOn(true);

        // 设置 Surface 的分辨率
        // mSurfaceHolder.setFixedSize(width,height);

        // 设置 Surface 类型
        // 参数：
        //        SURFACE_TYPE_NORMAL       : 用 RAM 缓存原生数据的普通 Surface
        //        SURFACE_TYPE_HARDWARE     : 适用于 DMA(Direct memory access )引擎和硬件加速的Surface
        //        SURFACE_TYPE_GPU          : 适用于 GPU 加速的 Surface
        //        SURFACE_TYPE_PUSH_BUFFERS ：表明该 Surface 不包含原生数据，Surface用到的数据由其他对象提供
        // 在 Camera 图像预览中就使用 SURFACE_TYPE_PUSH_BUFFERS 类型的 Surface，有 Camera 负责提供给预览 Surface 数据，这样图像预览会比较流
        //mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // 添加 Surface 的 callback 接口
        mSurfaceHolder.addCallback(mSurfaceCallback);


        SurfaceView mSurfaceViewFront = findViewById(R.id.surface_view_front);
        SurfaceHolder mSurfaceHolderFront = mSurfaceViewFront.getHolder();
        mSurfaceHolderFront.setFormat(PixelFormat.TRANSLUCENT);
        mSurfaceHolderFront.addCallback(mSurfaceCallbackFront);


        cameraLock = new ReentrantLock();


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(1000);
                        cameraLock.lock();
                        try {
                            if (isPreview) {
                                takePhoto();
                            }
                        } catch (java.lang.RuntimeException e) {
                            e.printStackTrace();
                        } finally {
                            cameraLock.unlock();
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    void takePhoto() {

        Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {
                camera.startPreview();
                Log.e(TAG, String.format("###1 %d", data.length));

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String[] rst = SocketUtil.sendData(data);

                        rstName = rst[1];
                        System.out.println("FISH !@#!@#" + rstName);
                        mHandler.sendEmptyMessage(0);
                    }
                }).start();
            }
        };

        camera.takePicture(null, null, pictureCallback);
    }

    private SurfaceHolder.Callback mSurfaceCallbackFront = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(final SurfaceHolder holder) {
            new Thread() {
                public void run() {
                    while (true) {

                        //1.这里就是核心了， 得到画布 ，然后在你的画布上画出要显示的内容
                        Canvas c = holder.lockCanvas();
                        c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                        //2.开画
                        Paint p = new Paint();
                        p.setColor(Color.RED);

                        String srcRect = rstRec;

                        System.out.println("####F + " + rstRec);

                        if (rstRec != null) {
                            String[] tmp = srcRect.split(",");
                            int x1 = Integer.parseInt(tmp[0]);
                            int y1 = Integer.parseInt(tmp[1]);
                            int x2 = Integer.parseInt(tmp[2]);
                            int y2 = Integer.parseInt(tmp[3]);

                            x1 = c.getWidth() - c.getWidth() * x1 / 1080;
                            y1 = c.getHeight() * y1 / 1920;
                            x2 = c.getWidth() - c.getWidth() * x2 / 1080;
                            y2 = c.getHeight() * y2 / 1920;


                            Rect aa = new Rect(x1, y1, x2, y2);

                            System.out.println(c.getHeight());
                            System.out.println(c.getWidth());

                            p.setStyle(Paint.Style.STROKE);
                            p.setStrokeWidth(10);
                            c.drawRect(aa, p);
                        }

                        //3. 解锁画布   更新提交屏幕显示内容
                        holder.unlockCanvasAndPost(c);
                        try {
                            Thread.sleep(1000);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                ;
            }.start();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    private SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {

        /**
         *  在 Surface 首次创建时被立即调用：活得叫焦点时。一般在这里开启画图的线程
         * @param surfaceHolder 持有当前 Surface 的 SurfaceHolder 对象
         */
        @Override
        public void surfaceCreated(final SurfaceHolder surfaceHolder) {
            try {
                // Camera,open() 默认返回的后置摄像头信息

                int numberOfCameras = Camera.getNumberOfCameras();// 获取摄像头个数
                int selectCameraID = -1;
                //遍历摄像头信息
                for (int cameraId = 0; cameraId < numberOfCameras; cameraId++) {
                    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                    Camera.getCameraInfo(cameraId, cameraInfo);
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {//前置摄像头
                        camera = Camera.open(cameraId);//打开摄像头
                        selectCameraID = cameraId;
                    }
                }

                Log.e(TAG, String.format("Select %d", selectCameraID));

                //此处也可以设置摄像头参数
                /**
                 WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);//得到窗口管理器
                 Display display  = wm.getDefaultDisplay();//得到当前屏幕
                 Camera.Parameters parameters = camera.getParameters();//得到摄像头的参数
                 parameters.setPictureFormat(PixelFormat.RGB_888);//设置照片的格式
                 parameters.setJpegQuality(85);//设置照片的质量
                 parameters.setPictureSize(display.getHeight(), display.getWidth());//设置照片的大小，默认是和     屏幕一样大
                 camera.setParameters(parameters);
                 **/

                Camera.Parameters parameters = camera.getParameters();//得到摄像头的参数
                WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);//得到窗口管理器
                Display display = wm.getDefaultDisplay();//得到当前屏幕

                parameters.setPictureSize(
                        Math.min(display.getHeight(), 1920),
                        Math.min(display.getWidth(), 1080));//设置照片的大小，默认是和屏幕一样大
                parameters.setJpegQuality(25);//设置照片的质量


//                parameters.setPreviewSize(
//                        Math.min(display.getHeight(), 1920),
//                        Math.min(display.getWidth(), 1080));

                camera.setParameters(parameters);

                //设置角度，此处 CameraId 我默认 为 0 （后置）
                // CameraId 也可以 通过 参考 Camera.open() 源码 方法获取
                setCameraDisplayOrientation(MainActivity.this, selectCameraID, camera);
                camera.setPreviewDisplay(surfaceHolder);//通过SurfaceView显示取景画面
                camera.startPreview();//开始预览
                isPreview = true;//设置是否预览参数为真
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }

        /**
         *  在 Surface 格式 和 大小发生变化时会立即调用，可以在这个方法中更新 Surface
         * @param surfaceHolder   持有当前 Surface 的 SurfaceHolder 对象
         * @param format          surface 的新格式
         * @param width           surface 的新宽度
         * @param height          surface 的新高度
         */
        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {

        }

        /**
         *  在 Surface 被销毁时立即调用：失去焦点时。一般在这里将画图的线程停止销毁
         * @param surfaceHolder 持有当前 Surface 的 SurfaceHolder 对象
         */
        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            Log.e(TAG, "FISH !!! Destroyed");
            if (camera != null) {
                if (isPreview) {//正在预览
                    cameraLock.lock();
                    try {
                        isPreview = false;
                        camera.stopPreview();
                        camera.release();
                    } finally {
                        cameraLock.unlock();
                    }
                }
            }
        }
    };


    /**
     * 设置 摄像头的角度
     *
     * @param activity 上下文
     * @param cameraId 摄像头ID（假如手机有N个摄像头，cameraId 的值 就是 0 ~ N-1）
     * @param camera   摄像头对象
     */
    public void setCameraDisplayOrientation(Activity activity,
                                            int cameraId, android.hardware.Camera camera) {

        Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();

        //获取摄像头信息
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();

        //获取摄像头当前的角度
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            //前置摄像头
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else {
            // back-facing  后置摄像头
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }


}
