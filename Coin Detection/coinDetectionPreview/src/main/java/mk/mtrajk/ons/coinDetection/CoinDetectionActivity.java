package mk.mtrajk.ons.coinDetection;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

public class CoinDetectionActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "CoinDetection::Activity";
    private CameraBridgeViewBase   cameraView;
    private Mat                    rgbaFrame;
    private Mat                    detectedCoins;
    private Mat                    grayframe;
    private Mat                    gradientX;
    private Mat                    gradientY;
    private Mat                    absGradientX;
    private Mat                    absGradientY;
    private Mat                    edgeDetect;
    private Size                   kSize;
    private Size                   centralSize;
    private Point                  cointPoint;
    private Point                  digitPoint;
    private Point                  textPoint;
    private Scalar                 colorRed;
    private Scalar                 colorGreen;
    private Scalar                 colorBlue;
    private int                    circleThickness;
    private int                    coinMinRadius;
    private int                    coinMaxRadius;
    private int                    accumulator;
    private int                    cannyUpperThreshold;
    private int                    radius;
    private int                    frameHeight;
    private int                    frameWidth;
    private int                    scale;
    private int                    delta;
    private int                    ddepth;
    private int                    i;
    private double                 coin[];
    private double                 digitSize;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    cameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public CoinDetectionActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.coindetection_surface_view);

        cameraView = (CameraBridgeViewBase) findViewById(R.id.coindetection_activity_java_surface_view);

        cameraView.setVisibility(SurfaceView.VISIBLE);

        cameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (cameraView != null)
            cameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (cameraView != null)
            cameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        init(width, height);
    }

    // gi definiram pocetnite vrednosti na site promenlivi koi ponatamu ke se koristat
    private void init(int width, int height) {
        frameHeight = height;
        frameWidth = width;
        rgbaFrame = new Mat(height, width, CvType.CV_8UC4);
        grayframe = new Mat(height, width, CvType.CV_8UC1);
        detectedCoins = new Mat();
        edgeDetect = new Mat();
        gradientX = new Mat();
        gradientY = new Mat();
        absGradientX = new Mat();
        absGradientY = new Mat();
        colorRed = new Scalar(255,0,0);
        colorGreen = new Scalar(0,255,0);
        colorBlue = new Scalar(0,0,255);
        kSize = new Size(7,7);
        cointPoint = new Point();
        textPoint = new Point(frameWidth -220, 30);
        circleThickness = 5;
        scale = 1;
        delta = 0;
        ddepth = CvType.CV_16S;
        coinMinRadius = 30;    // go pravam 30 za da ne gi faka i najmalite tocki
        coinMaxRadius = 240;   // bidejki mene rezolucijata mi e 800x480 znaci najgolemata paricka bi imala 240 radius
        accumulator = 300;
        cannyUpperThreshold = 100;
    }

    public void onCameraViewStopped() {
        rgbaFrame.release();
        detectedCoins.release();
        grayframe.release();
        edgeDetect.release();
        gradientX.release();
        gradientY.release();
        absGradientX.release();
        absGradientY.release();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        // go zemam momentalniot frejm
        rgbaFrame = inputFrame.rgba();

        // ja pretvaram slikata vo siva (moze pravo da ja zemam kako siva so inputFrame.gray()
        // ama ja zemam normalna za na kraj izlezot da mi bide kako normalna slika so drugite dodatoci
        Imgproc.cvtColor(rgbaFrame, grayframe, Imgproc.COLOR_RGBA2GRAY);

        // na sivata slika i pravam gaussian blur za da se procisti noisy-ot (da se zamagli za da se ostranat onie mali precki)
        Imgproc.GaussianBlur(grayframe, grayframe, kSize, 2, 2);
        //Imgproc.medianBlur(grayframe, grayframe, 5);

        /// gradient X
        Imgproc.Sobel(grayframe, gradientX, ddepth, 1, 0, 3, scale, delta, Imgproc.BORDER_DEFAULT );
        /// gradient Y
        Imgproc.Sobel(grayframe, gradientY, ddepth, 0, 1, 3, scale, delta, Imgproc.BORDER_DEFAULT);

        // gi konvertiram vo abs
        Core.convertScaleAbs(gradientX, absGradientX);
        Core.convertScaleAbs(gradientY, absGradientY);

        // potoa gi kombiniram
        Core.addWeighted(absGradientX, 0.5, absGradientY, 0.5, 0, edgeDetect);

        // i na kraj so pomos na HoughCircles gi naogam parickite
        // (poslednite dva argumenti se za minimum i maksimum radius za detekcija, ako ne e poznato stavi 0)
        Imgproc.HoughCircles(edgeDetect, detectedCoins,
                Imgproc.CV_HOUGH_GRADIENT, 2,
                grayframe.rows() / 5, cannyUpperThreshold,
                accumulator, coinMinRadius, coinMaxRadius);

        // ovde gi crtam krugovite na pronajdenite paricki
        for (i = 0; i < detectedCoins.cols(); i++) {
            coin = detectedCoins.get(0, i);

            cointPoint.x = coin[0];
            cointPoint.y = coin[1];
            radius = (int) Math.round(coin[2]);

            // (ova od prilika spored moi merenja) za radius 30, goleminata na tekstot e 1, znaci spored radiusot opredeluvam koja e goleminata na tekstot
            digitSize = radius/30.0;

            // ja naogam goleminata na brojkite sto treba da se vpisat
            // ova mi treba za da znam kolku treba da odzemam od centralnite koordinati za da bidat vo sredina cifrite
            centralSize = Core.getTextSize(String.format("%d", i +1), Core.FONT_HERSHEY_COMPLEX, digitSize,3,null);

            // ja naogam tockata kade sto treba da stoi brojkata (presmetuvam da e brojkata vo sredina na krugot)
            digitPoint = new Point(cointPoint.x-(int)(centralSize.width/2), cointPoint.y+(int)(centralSize.height/2));

            // gi crtam brojkite
            Core.putText(rgbaFrame,String.format("%d", i +1),
                    digitPoint, Core.FONT_HERSHEY_COMPLEX,
                    digitSize, colorGreen, 3);

            // go crta krugot okolu pronajdenata paricka
            Core.circle(rgbaFrame, cointPoint, radius, colorRed, circleThickness);
        }

        // go pecatam siniot tekst vo agolot, koj kazuva kolku paricki se pronajdeni
        Core.putText(rgbaFrame,
                String.format((i ==0)?"Нема парички":("Има %d паричк"+((i ==1)?"а":"и")), i),
                textPoint, Core.FONT_HERSHEY_COMPLEX, 0.75, colorBlue, 2);

        // ja vrakam modificiranata slika (orginalnata slika+krugovite+brojkite+tekstot)
        return rgbaFrame;
    }
}
