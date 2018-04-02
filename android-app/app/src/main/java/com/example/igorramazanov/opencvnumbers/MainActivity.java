package com.example.igorramazanov.opencvnumbers;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.androidcv.R;
import com.themirrortruth.mnist.MnistServerGrpc;
import com.themirrortruth.mnist.MnistServerOuterClass;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private CameraBridgeViewBase mOpenCvCameraView;
    private static String TAG = "opencv";
    private static byte[] buffer = new byte[784];

    private SeekBar seekBar;
    private TextView answer;
    private MnistServerGrpc.MnistServerBlockingStub stub;
    private String hostname = "themirrortruth.local";
    private int port = 50051;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mOpenCvCameraView = findViewById(R.id.HelloOpenCvView);
        seekBar = findViewById(R.id.seekBar);
        answer = findViewById(R.id.answer);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(hostname, port)
                .usePlaintext(true)
                .build();
        stub = MnistServerGrpc.newBlockingStub(channel);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat rgba = inputFrame.rgba();
        Mat gray = inputFrame.gray();
        Mat gaussed = gaussBlur(gray);
        Mat thresholded = threshold(gaussed);
        ArrayList<MatOfPoint> contours = detectContours(thresholded);

        if (contours.size() == 1) {
            MatOfPoint mapOfPoint = contours.get(0);
            Rect roi = increaseBorderRect(rgba, mapOfPoint);
            drawRect(rgba, roi);
            MnistServerOuterClass.Features features = buildRequest(thresholded, roi);
            final int response = stub.classify(features).getDigit();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    answer.setText("Predicted: " + response);
                }
            });
        }

        return rgba;
    }

    @NonNull
    private Mat gaussBlur(Mat gray) {
        Mat gaussed = new Mat();
        Imgproc.GaussianBlur(gray, gaussed, new Size(5, 5), 0);
        return gaussed;
    }

    @NonNull
    private Mat threshold(Mat gaussed) {
        Mat thresholded = new Mat();
        Imgproc.threshold(gaussed, thresholded, seekBar.getProgress(), 255, Imgproc.THRESH_BINARY_INV);
        return thresholded;
    }

    @NonNull
    private ArrayList<MatOfPoint> detectContours(Mat thresholded) {
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(thresholded, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        return contours;
    }

    @NonNull
    private Rect increaseBorderRect(Mat rgba, MatOfPoint mapOfPoint) {
        Rect rect = Imgproc.boundingRect(mapOfPoint);

        double x1 = rect.tl().x;
        double y1 = rect.tl().y;

        double x2 = rect.br().x;
        double y2 = rect.br().y;

        double dx = x2 - x1;
        double dy = y2 - y1;

        double newX1 = Math.max(0, x1 - dx / 4);
        double newY1 = Math.max(0, y1 - dy / 4);

        double newX2 = Math.min(rgba.cols(), x2 + dx / 4);
        double newY2 = Math.min(rgba.rows(), y2 + dy / 4);
        Point tl = new Point(newX1, newY1);
        Point br = new Point(newX2, newY2);
        return new Rect(tl, br);
    }

    private void drawRect(Mat rgba, Rect roi) {
        Imgproc.rectangle(rgba, roi.tl(), roi.br(), new Scalar(0, 255, 0), 5);
    }

    private MnistServerOuterClass.Features buildRequest(Mat thresholded, Rect roi) {
        Mat cropped = new Mat(roi.height, roi.width, thresholded.type());
        thresholded.submat(roi).copyTo(cropped);
        Imgproc.resize(cropped, cropped, new Size(28, 28), 0, 0, Imgproc.INTER_AREA);
        cropped.get(0, 0, buffer);
        ArrayList<Integer> pixels = new ArrayList<>();
        for (byte b : buffer) {
            pixels.add((int) b);
        }
        return MnistServerOuterClass.Features.newBuilder().addAllPixels(pixels).build();
    }
}