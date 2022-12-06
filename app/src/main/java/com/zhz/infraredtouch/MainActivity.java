package com.zhz.infraredtouch;

import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IZhsdService;
import android.os.RemoteException;
import android.os.ServiceManager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.lgh.uvccamera.UVCCameraProxy;
import com.lgh.uvccamera.bean.PicturePath;
import com.lgh.uvccamera.callback.ConnectCallback;
import com.lgh.uvccamera.callback.PhotographCallback;
import com.lgh.uvccamera.callback.PictureCallback;
import com.lgh.uvccamera.callback.PreviewCallback;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.UVCCamera;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.SimpleBlobDetector;
import org.opencv.features2d.SimpleBlobDetector_Params;
import org.opencv.imgproc.Imgproc;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TextureView mTextureView;
    //    private SurfaceView mSurfaceView;
    private ImageView mImageView1;
    private Spinner mSpinner;
    private UVCCameraProxy mUVCCamera;
    private boolean isFirst = true;
    private String path1;
    private Button btn5;
    private boolean bIrcut = false;
    private UVCCamera mUVCCamerano;
    private PreviewCallback mPreviewCallback;
    private Bitmap bitmap = null;
    private static final int PREVIEW_WIDTH = 640; // 640

    private static final int PREVIEW_HEIGHT = 480; //480
    private boolean isScaling = false;
    private boolean isInCapturing = false;
    private SimpleBlobDetector mSimpleBlobDetector;
   // private SimpleBlobDetector_Params mSimpleBlobDetectorParams;

    private boolean isTakei=false;
    private double m_data[]=new double[9];
    private final Bitmap srcBitmap = Bitmap.createBitmap(PREVIEW_WIDTH, PREVIEW_HEIGHT, Bitmap.Config.RGB_565);
    private Mat m_matrix;
//private Mat mat=new Mat();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uvc_camera);
       // initSimpleBlobParams();
        initView();
        initUVCCamera();
        initSimpleBlobParams();

        Log.d(TAG,"hello world");

      //  MatOfPoint2f  src_pt = new MatOfPoint2f( new Point(123,231),new Point(568,181),new Point(497,313),new Point(168,312)) ;
          MatOfPoint2f  src_pt = new MatOfPoint2f( new Point(90,122),new Point(627,121),new Point(566,345),new Point(143,355)) ;

        MatOfPoint2f  dst_pt = new MatOfPoint2f( new Point(0,0),new Point(1920,0),new Point(1920,1080),new Point(0,1080)) ;

        m_matrix=Imgproc.getPerspectiveTransform(src_pt,dst_pt);

        m_matrix.get(0,0,m_data);
    }

    private void initView() {
        mTextureView = findViewById(R.id.textureView);
//        mSurfaceView = findViewById(R.id.surfaceView);
//        mSurfaceView.setZOrderOnTop(true);
        mImageView1 = findViewById(R.id.imag1);
        mSpinner = findViewById(R.id.spinner);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                if (isFirst) {
                    isFirst = false;
                    return;
                }
                Log.i(TAG, "position-->" + position);
                mUVCCamera.stopPreview();
                List<Size> list = mUVCCamera.getSupportedPreviewSizes();
                if (!list.isEmpty()) {
                    mUVCCamera.setPreviewSize(list.get(position).width, list.get(position).height);
                    mUVCCamera.startPreview();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initUVCCamera() {
        mUVCCamera = new UVCCameraProxy(this);
        // 已有默认配置，不需要可以不设置
        mUVCCamera.getConfig()
                .isDebug(true)
                .setPicturePath(PicturePath.APPCACHE)
                .setDirName("uvccamera")
                .setProductId(0)
                .setVendorId(0);
        mUVCCamera.setPreviewTexture(mTextureView);
//        mUVCCamera.setPreviewSurface(mSurfaceView);
//        mUVCCamera.registerReceiver();

        mUVCCamera.setConnectCallback(new ConnectCallback() {
            @Override
            public void onAttached(UsbDevice usbDevice) {
                mUVCCamera.requestPermission(usbDevice);
            }

            @Override
            public void onGranted(UsbDevice usbDevice, boolean granted) {
                if (granted) {
                    mUVCCamera.connectDevice(usbDevice);
                }
            }

            @Override
            public void onConnected(UsbDevice usbDevice) {
                mUVCCamera.openCamera();
            }

            @Override
            public void onCameraOpened() {
                showAllPreviewSizes();
                mUVCCamera.setPreviewSize(640, 480);
                mUVCCamera.startPreview();
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
                path1 = path;
                mImageView1.setImageURI(null);
                mImageView1.setImageURI(Uri.parse(path));
            }
        });

     //   mUVCCamera.getUVCCamera().setFrameCallback(mIFrameCallback,UVCCamera.PIXEL_FORMAT_RGB565);
    }
    private final IFrameCallback mIFrameCallback = new IFrameCallback() {
        @Override
        public void onFrame(final ByteBuffer frame) {

            frame.clear();

            if (bitmap == null) {
            }
          //  synchronized (bitmap) {

                srcBitmap.copyPixelsFromBuffer(frame);
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
                    Mat rgbMat = new Mat();

                    Utils.bitmapToMat(srcBitmap, rgbMat);

                    MatOfKeyPoint mKeyPoint=new MatOfKeyPoint();

                   try {
                       mSimpleBlobDetector.detect(rgbMat, mKeyPoint);
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

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn1:
              //  mUVCCamera.startPreview();
              //  isTakei=true;
              // String path = String.format("%s/%s.jpg", getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "2357");
              //  Toast.makeText(this,path,Toast.LENGTH_SHORT).show();

                mUVCCamera.getUVCCamera().setFrameCallback(mIFrameCallback,UVCCamera.PIXEL_FORMAT_RGB565);
               //    mUVCCamera.getUVCCamera().setFrameCallback(mIFrameCallback,UVCCamera.PIXEL_FORMAT_YUV420SP);

                Log.d(TAG,"wocao");


                break;

            case R.id.btn2:
              //  mUVCCamera.stopPreview();
                mUVCCamera.takePicture();

               // mUVCCamera.savePicture();
                break;

            case R.id.btn3:
                mUVCCamera.clearCache();
                break;

            case R.id.btn4:
                mUVCCamera.setPreviewRotation(180);
                break;
            case R.id.btn5:
                Log.e(TAG, "bIrcut=" + bIrcut);
                openCloseIrcut(bIrcut);
                bIrcut = !bIrcut;
                break;

            case R.id.take_picture:
//                mUVCCamera.takePicture();
                mUVCCamera.takePicture("test.jpg");
                break;

            case R.id.imag1:
                Log.i(TAG, "path1-->" + path1);
                jump2ImageActivity(path1);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mUVCCamera.unregisterReceiver();
    }

    private void jump2ImageActivity(String path) {
        Intent intent = new Intent(this, ImageActivity.class);
        intent.putExtra("path", path);
        startActivity(intent);
    }

    private void showAllPreviewSizes() {
        isFirst = true;
        List<Size> previewList = mUVCCamera.getSupportedPreviewSizes();
        List<String> previewStrs = new ArrayList<>();
        for (Size size : previewList) {
            previewStrs.add(size.width + " * " + size.height);
        }
        ArrayAdapter adapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_item, previewStrs);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
    }
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            // TODO Auto-generated method stub
            switch (status){
                case BaseLoaderCallback.SUCCESS:
                   // Log.i(TAG, "成功加载opencv");

                    break;
                default:
                    super.onManagerConnected(status);
                    Log.i(TAG, "加载失败");
                    break;
            }

        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            Toast.makeText(this,"opencv",Toast.LENGTH_SHORT).show();
        }
    }
    //控制6771板卡GPIO口函数
    public final static int GPIO_NUM = 13;//GPIO13 IRCUT
    public final static int HIGHVAL = 0xA1;//GPIO13 IRCUT
    public final static int LOWVAL = 0xA0;//GPIO13 IRCUT

    private boolean setIrcutVal(int iVal) {

        Log.e(TAG, "iVal=" + iVal);
        IZhsdService powerService = IZhsdService.Stub.asInterface(ServiceManager.getService("ZhsdService"));
        ;
        boolean bRet = true;
        try {
            powerService.setValue(iVal, GPIO_NUM);

        } catch (RemoteException e) {
            e.printStackTrace();
            bRet = false;
        }
        return bRet;


    }

    public void openCloseIrcut(boolean isOpen) {

        int iVal = isOpen ? HIGHVAL : LOWVAL;
        setIrcutVal(iVal);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
    //控制结束



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


    static {
        System.loadLibrary("opencv_java4");
    }

}