package com.example.facerecognitionmulltiplefaces;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_objdetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import static org.bytedeco.javacpp.opencv_core.LINE_8;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.rectangle;

public class DetectionActivity extends AppCompatActivity implements CvCameraPreview.CvCameraViewListener {
    CvCameraPreview cameraview;
    public static String TAG = "Detection Activity";
    opencv_objdetect.CascadeClassifier faceDetector;
    int absoluteFaceSize = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection);
        cameraview = (CvCameraPreview) findViewById(R.id.camera_view);
        cameraview.setCvCameraViewListener(this);
        faceDetector = loadClassifierCascade(DetectionActivity.this,R.raw.frontalface);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        absoluteFaceSize = (int) (width * 0.32f);
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public opencv_core.Mat onCameraFrame(opencv_core.Mat mat) {
        opencv_core.Mat grayMat = new opencv_core.Mat(mat.rows(),mat.cols());
        cvtColor(mat,grayMat,CV_BGR2GRAY);
        opencv_core.RectVector faces = new opencv_core.RectVector();
        faceDetector.detectMultiScale(grayMat,faces,1.25f,3,1,new opencv_core.Size(absoluteFaceSize,absoluteFaceSize),new opencv_core.Size(4*absoluteFaceSize,4*absoluteFaceSize));
        if(faces.size()==1)
        {
            showDetectedFace(faces,mat);
        }
        return mat;
    }

    public static opencv_objdetect.CascadeClassifier loadClassifierCascade(Context context, int resId) {
        FileOutputStream fos = null;
        InputStream inputStream;

        inputStream = context.getResources().openRawResource(resId);                                //Getting front faces detection file from raw folder inside res
        File xmlDir = context.getDir("xml", Context.MODE_PRIVATE);
        File cascadeFile = new File(xmlDir, "temp.xml");
        try {
            fos = new FileOutputStream(cascadeFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);                                                //Reading the frontal_face.xml file byte by byte
            }
        } catch (IOException e) {
            Log.d(TAG, "Can\'t load the cascade file");
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        opencv_objdetect.CascadeClassifier detector = new opencv_objdetect.CascadeClassifier(cascadeFile.getAbsolutePath());  //Setting the cascade classifier to haarcascades
        if (detector.isNull()) {
            Log.e(TAG, "Failed to load cascade classifier");
            detector = null;
        } else {
            Log.i(TAG, "Loaded cascade classifier from " + cascadeFile.getAbsolutePath());
        }
        // delete the temporary directory
        cascadeFile.delete();

        return detector;                                                                            //Returning the cascade classifier
    }
    void showDetectedFace(opencv_core.RectVector faces, opencv_core.Mat rgbaMat) {
        int x = faces.get(0).x();
        int y = faces.get(0).y();
        int w = faces.get(0).width();
        int h = faces.get(0).height();

        rectangle(rgbaMat, new opencv_core.Point(x, y), new opencv_core.Point(x + w, y + h), opencv_core.Scalar.GREEN, 2, LINE_8, 0);
    }
}
