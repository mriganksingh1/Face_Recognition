/*package com.example.facerecognitionmulltiplefaces;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_face;
import org.bytedeco.javacpp.opencv_objdetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import static com.example.facerecognitionmulltiplefaces.Training.EIGEN_FACES_CLASSIFIER;
import static com.example.facerecognitionmulltiplefaces.Training.names;
import static org.bytedeco.javacpp.opencv_core.FONT_HERSHEY_PLAIN;
import static org.bytedeco.javacpp.opencv_core.LINE_8;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.putText;
import static org.bytedeco.javacpp.opencv_imgproc.rectangle;
import static org.bytedeco.javacpp.opencv_imgproc.resize;

public class RecognizeActivity extends AppCompatActivity implements CvCameraPreview.CvCameraViewListener {

    private static final String TAG = "Recognize Activity";
    CvCameraPreview cameraPreview;
    opencv_objdetect.CascadeClassifier faceDetector;
    int absoluteFaceSize = 0;
    public static final double ACCEPT_LEVEL = 3000.0D;
    opencv_face.FaceRecognizer faceRecognizer = opencv_face.EigenFaceRecognizer.create();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize);
        cameraPreview = findViewById(R.id.camera_view);
        cameraPreview.setCvCameraViewListener(this);
        faceDetector = loadClassifierCascade(RecognizeActivity.this,R.raw.frontalface);
        File folder = new File(Environment.getExternalStorageDirectory()+"/facerecog/");
        File f = new File(folder,EIGEN_FACES_CLASSIFIER);
        faceRecognizer.read(f.getAbsolutePath());
        if(faceRecognizer == null)
        {
            Toast.makeText(getApplicationContext(),"Face Recognition is not trained",Toast.LENGTH_SHORT).show();
            finish();
        }
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
            recognize(faces.get(0),grayMat,mat);
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

    private void recognize(opencv_core.Rect dadosFace, opencv_core.Mat grayMat, opencv_core.Mat rgbaMat)
    {
        opencv_core.Mat detectedFace = new opencv_core.Mat(grayMat, dadosFace);
        resize(detectedFace, detectedFace, new opencv_core.Size(Training.IMG_SIZE,Training.IMG_SIZE));

        IntPointer label = new IntPointer(1);
        DoublePointer reliability = new DoublePointer(1);
        faceRecognizer.predict(detectedFace, label, reliability);
        int prediction = label.get(0);
        double acceptanceLevel = reliability.get(0);
        String name;
        if (prediction == -1 || acceptanceLevel >= ACCEPT_LEVEL) {
            name = getString(R.string.unknown);
        } else {
            name = names.get(prediction) + " - " + acceptanceLevel;
        }
        int x = Math.max(dadosFace.tl().x() - 10, 0);
        int y = Math.max(dadosFace.tl().y() - 10, 0);
        putText(rgbaMat, name, new opencv_core.Point(x, y), FONT_HERSHEY_PLAIN, 1.4, new opencv_core.Scalar(0,255,0,0));
    }
}*/
