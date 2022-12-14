package com.zhz.infraredtouch;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.TextureView;

import com.lgh.uvccamera.UVCCameraProxy;
import com.lgh.uvccamera.bean.PicturePath;
import com.lgh.uvccamera.callback.ConnectCallback;
import com.lgh.uvccamera.callback.PhotographCallback;
import com.lgh.uvccamera.callback.PictureCallback;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.UVCCamera;

import org.opencv.android.Utils;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.features2d.SimpleBlobDetector;
import org.opencv.features2d.SimpleBlobDetector_Params;
import org.opencv.imgproc.Imgproc;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;

public class iruvcService extends Service {
    private static final int PREVIEW_WIDTH = 640; // 640

    private static final int PREVIEW_HEIGHT = 480; //480
    private static final String TAG = "iruvcService";
    private SimpleBlobDetector mSimpleBlobDetector;
    private Bitmap bitmap = null;

    private UVCCameraProxy mUVCCamera;
    private double m_data[]=new double[9];
    private Mat m_matrix;
    private final Bitmap srcBitmap = Bitmap.createBitmap(PREVIEW_WIDTH, PREVIEW_HEIGHT, Bitmap.Config.RGB_565);
    private boolean isTakei=false;
    private TextureView mTextureView;
    public iruvcService() {
    }
    @Override
    public void onCreate(){
        super.onCreate();
        initUVCCamera();
        initSimpleBlobParams();
    }
    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
       // mTextureView = findViewById(R.id.textureView);
       // mImageView = findViewById(R.id.imag1);
        Log.d(TAG,"onstart");

        //initUVCCamera();
       // initSimpleBlobParams();
       // mUVCCamera.getUVCCamera().setFrameCallback(mIFrameCallback, UVCCamera.PIXEL_FORMAT_RGB565);
        MatOfPoint2f src_pt = new MatOfPoint2f( new Point(90,122),new Point(627,121),new Point(566,345),new Point(143,355)) ;

        MatOfPoint2f  dst_pt = new MatOfPoint2f( new Point(0,0),new Point(1920,0),new Point(1920,1080),new Point(0,1080)) ;

        m_matrix= Imgproc.getPerspectiveTransform(src_pt,dst_pt);

        m_matrix.get(0,0,m_data);


        return super.onStartCommand(intent,flags,startId);
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    private void initUVCCamera() {
        mUVCCamera = new UVCCameraProxy(this);
        // 已有默认配置，不需要可以不设置
        Log.d(TAG,"init1");

        mUVCCamera.getConfig()
                .isDebug(true)
                .setPicturePath(PicturePath.APPCACHE)
                .setDirName("uvccamera")
                .setProductId(0)
                .setVendorId(0);
       // mUVCCamera.setPreviewTexture(mTextureView);
//        mUVCCamera.setPreviewSurface(mSurfaceView);
//        mUVCCamera.registerReceiver();
        Log.d(TAG,"init2");

        mUVCCamera.setConnectCallback(new ConnectCallback() {
            @Override
            public void onAttached(UsbDevice usbDevice) {
                mUVCCamera.requestPermission(usbDevice);
                Log.d(TAG,"onAttached");
            }

            @Override
            public void onGranted(UsbDevice usbDevice, boolean granted) {
                if (granted) {
                    mUVCCamera.connectDevice(usbDevice);
                    Log.d(TAG,"uvcongranted");

                }
            }

            @Override
            public void onConnected(UsbDevice usbDevice) {
                mUVCCamera.openCamera();
                Log.d(TAG,"uvcconnected");

            }

            @Override
            public void onCameraOpened() {
                //showAllPreviewSizes();
                mUVCCamera.setPreviewSize(640, 480);
                mUVCCamera.startPreview();
                mUVCCamera.getUVCCamera().setFrameCallback(mIFrameCallback, UVCCamera.PIXEL_FORMAT_RGB565);
                Log.d(TAG,"uvcopened");
            }

            @Override
            public void onDetached(UsbDevice usbDevice) {
                mUVCCamera.closeCamera();
            }
        });

        mUVCCamera.setPhotographCallback(new PhotographCallback() {
            @Override
            public void onPhotographClick() {
                mUVCCamera.takePicture();
//                mUVCCamera.takePicture("test.jpg");
            }
        });



        mUVCCamera.setPictureTakenCallback(new PictureCallback() {
            @Override
            public void onPictureTaken(String path) {
              //  path1 = path;
             //   mImageView1.setImageURI(null);
            //    mImageView1.setImageURI(Uri.parse(path));
            }
        });

        //   mUVCCamera.getUVCCamera().setFrameCallback(mIFrameCallback,UVCCamera.PIXEL_FORMAT_RGB565);
       //  mUVCCamera.getUVCCamera().setFrameCallback(mIFrameCallback, UVCCamera.PIXEL_FORMAT_RGB565);

    }
    private final IFrameCallback mIFrameCallback = new IFrameCallback() {
        @Override
        public void onFrame(final ByteBuffer frame) {

            frame.clear();

            if (bitmap == null) {
            }
            //  synchronized (bitmap) {

            srcBitmap.copyPixelsFromBuffer(frame);
            Log.d(TAG,"nihao5");
            if(isTakei) {
                isTakei=false;
                String path = String.format("%s/%s.jpg", getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "2358");
                try (FileOutputStream fos = new FileOutputStream(path)) {
                    // 鎶婁綅鍥炬暟鎹帇缂╁埌鏂囦欢杈撳嚭娴佷腑
                    srcBitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                    //Toast.makeText(this,path,Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                //  Toast.makeText(this,"opencv",Toast.LENGTH_SHORT).show();

            }
            //frame.get(srcBitmap);
               /* if (bitmap.getWidth() != 640 || bitmap.getHeight() != 480) {
                    Log.d(TAG,"nihao3");
                    bitmap = Bitmap.createBitmap(640, 480, Bitmap.Config.RGB_565);
                    Log.d(TAG,"nihao3");
                      }*/

            // bitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
            Log.d(TAG,"nhao1");
            Mat rgbMat = new Mat();
            Log.d(TAG,"nihao7");
            Utils.bitmapToMat(srcBitmap, rgbMat);

            MatOfKeyPoint mKeyPoint=new MatOfKeyPoint();

            try {
                mSimpleBlobDetector.detect(rgbMat, mKeyPoint);
                Log.d(TAG,"nihao6");
            }catch (Exception e){
                Log.d(TAG,"nihao5");
            }
            for(KeyPoint p:mKeyPoint.toList()){
                //   KeyPoint q=new KeyPoint();
                Log.d(TAG,"x is "+p.pt.x+"y is "+p.pt.y);
                Point point=new Point();
                point.x=(m_data[0]* p.pt.x+m_data[1]*  p.pt.y+m_data[2] )/(m_data[6]*p.pt.x+m_data[7]*  p.pt.y+m_data[8] );
                point.y=(m_data[3]* p.pt.x+m_data[4]*  p.pt.y+m_data[5] )/(m_data[6]*p.pt.x+m_data[7]*  p.pt.y+m_data[8] );
                if(point.x>1920)
                    point.x=1920;
                if(point.y>1080)
                    point.y=1080;
                if(point.x<0)
                    point.x=0;
                if(point.y<0)
                    point.y=0;
                Log.d(TAG," dis x is "+point.x+"dis y is "+point.y);

                //    Instrumentation inst =new Instrumentation();
                //  inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),0,MotionEvent.ACTION_DOWN,(float)p.pt.x,(float)p.pt.y,0));
                //     inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),0,MotionEvent.ACTION_UP,(float)p.pt.x,(float)p.pt.y,0));

            }
            Utils.matToBitmap(rgbMat, bitmap);

            // Mat grb = new Mat();


        }
        //   }
        //}
    };
    private   void initSimpleBlobParams(){
        SimpleBlobDetector_Params mSimpleBlobDetectorParams=new SimpleBlobDetector_Params();

        mSimpleBlobDetectorParams.set_minThreshold(100);
        mSimpleBlobDetectorParams.set_maxThreshold(255);
        mSimpleBlobDetectorParams.set_thresholdStep(30);

        mSimpleBlobDetectorParams.set_filterByArea(true);
        mSimpleBlobDetectorParams.set_minArea(10);
        mSimpleBlobDetectorParams.set_maxArea(5000);

        mSimpleBlobDetectorParams.set_filterByInertia(true);
        mSimpleBlobDetectorParams.set_minInertiaRatio(0.01F);

        mSimpleBlobDetectorParams.set_filterByCircularity(true);
        mSimpleBlobDetectorParams.set_minCircularity(0.7F);

        mSimpleBlobDetectorParams.set_filterByConvexity(true);
        mSimpleBlobDetectorParams.set_minConvexity(0.5F);

        mSimpleBlobDetectorParams.set_filterByColor(false);
        //  mSimpleBlobDetectorParams.set_b

        mSimpleBlobDetector=SimpleBlobDetector.create(mSimpleBlobDetectorParams);
        Log.d(TAG,"nihao5");

        //mKeyPoint=new MatOfKeyPoint();
        // MatOfRect mFaces = new MatOfRect();
        // mKeyPoint=new MatOfKeyPoint();
        //   mSimpleBlobDetector.detect(mRgba,mKeyPoint);
    }
}