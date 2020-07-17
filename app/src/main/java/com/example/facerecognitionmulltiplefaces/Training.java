package com.example.facerecognitionmulltiplefaces;

import android.content.Context;
import android.content.Intent;
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
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_core.FONT_HERSHEY_PLAIN;
import static org.bytedeco.javacpp.opencv_core.LINE_8;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.putText;
import static org.bytedeco.javacpp.opencv_imgproc.rectangle;
import static org.bytedeco.javacpp.opencv_imgproc.resize;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;

public class Training extends AppCompatActivity implements CvCameraPreview.CvCameraViewListener {

    private static final String TAG = "Training Activity" ;
    opencv_core.Mat mrgba;
    opencv_core.Mat mgray;
    CvCameraPreview camerapreview;
    opencv_objdetect.CascadeClassifier faceDetector;
    int count =0;
    int absoluteFaceSize = 0;
    String mPath = "";
    String newname = "";
    static ArrayList<String> savednames = new ArrayList<String>();
    static ArrayList<String> names = new ArrayList<String>();
    public static final String FILE_NAME_PATTERN = "person.%d.%d.jpg";
    public static final int IMG_SIZE = 160;
    public static final String EIGEN_FACES_CLASSIFIER = "eigenFacesClassifier.yml";
    File folder;
    int reset;
    int recognize;
    opencv_face.FaceRecognizer eigenfaces= opencv_face.EigenFaceRecognizer.create();
    opencv_face.FaceRecognizer faceRecognizer = opencv_face.EigenFaceRecognizer.create();
    public static final double ACCEPT_LEVEL = 3000.0D;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);
        camerapreview = findViewById(R.id.tutorial3_activity_java_surface_view);
        camerapreview.setCvCameraViewListener(this);
        Intent intent = getIntent();
        newname = intent.getStringExtra("name");
        recognize = intent.getIntExtra("recognize",0);
        SharedPreferences sharedPreferences = this.getSharedPreferences("com.example.facerecognitionmulltiplefaces",Context.MODE_PRIVATE);
        reset = intent.getIntExtra("reset",0);
        try {
            savednames = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("friends",ObjectSerializer.serialize(new ArrayList<String>())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(recognize!=1)
        {
            savednames.add(newname);
        }
        if(recognize == 1)
        {
            File folder = new File(Environment.getExternalStorageDirectory()+"/facerecog/");
            File f = new File(folder,EIGEN_FACES_CLASSIFIER);
            faceRecognizer.read(f.getAbsolutePath());
            if(faceRecognizer == null)
            {
                Toast.makeText(getApplicationContext(),"Face Recognition is not trained",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        try {
            sharedPreferences.edit().putString("friends",ObjectSerializer.serialize(savednames)).apply();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            names = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("friends",ObjectSerializer.serialize(new ArrayList<String>())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mPath = Environment.getExternalStorageDirectory()+"/facerecog/";
        Log.e("Path",mPath);
        faceDetector = loadClassifierCascade(Training.this,R.raw.frontalface);
        folder = new File(mPath);
        if (folder.exists() && !folder.isDirectory())
        {
            folder.delete();
        }
        if(!folder.exists())
        {
            folder.mkdirs();
        }
        if(reset == 1)
        {
            sharedPreferences.edit().clear().apply();
            File directory = new File(Environment.getExternalStorageDirectory()+"/facerecog/");
            FilenameFilter imagefilter = new FilenameFilter(){
                    @Override
                    public boolean accept(File file, String name) {
                        return name.endsWith(".jpg") || name.endsWith(".gif") || name.endsWith(".png") || name.endsWith(".yml");
                    }
            };
            File[] files = directory.listFiles(imagefilter);
            for(File images:files)
            {
                images.delete();
            }
            Toast.makeText(getApplicationContext(),"Reset Complete",Toast.LENGTH_SHORT).show();
            finish();
        }
        Log.e("Path",mPath);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mrgba = new opencv_core.Mat();
        absoluteFaceSize = (int) (width * 0.32f);
    }

    @Override
    public void onCameraViewStopped() {
        mrgba.release();
        mgray.release();
    }

    @Override
    public opencv_core.Mat onCameraFrame(opencv_core.Mat mat) {
        mrgba = mat;
        mgray = new opencv_core.Mat(mat.rows(),mat.cols());
        cvtColor(mrgba,mgray,CV_BGR2GRAY);
        opencv_core.RectVector faces = new opencv_core.RectVector();
        faceDetector.detectMultiScale(mgray,faces,1.25f,3,1,new opencv_core.Size(absoluteFaceSize,absoluteFaceSize),new opencv_core.Size(4*absoluteFaceSize,4*absoluteFaceSize));
        if(faces.size()==1)
        {
            if(recognize!=1)
            {
                opencv_core.Rect rectFace = faces.get(0);
                opencv_core.Mat capturedFace = new opencv_core.Mat(mgray,rectFace);
                resize(capturedFace,capturedFace,new opencv_core.Size(IMG_SIZE,IMG_SIZE));
                if(count<10)
                {
                    File f = new File(folder,String.format(FILE_NAME_PATTERN,names.size()-1,count));
                    try {
                        f.createNewFile();
                        imwrite(f.getAbsolutePath(),capturedFace);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    count++;
                }
                if(count==10)
                {
                    train();
                    count++;
                }
            }
            showDetectedFace(faces,mat);
            if(recognize==1)
            {
                recognize(faces.get(0),mgray,mat);
            }
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

    public void train()
    {
        File directory = new File(Environment.getExternalStorageDirectory()+"/facerecog/");
        FilenameFilter imagefilter = new FilenameFilter(){
                @Override
                public boolean accept(File file, String name) {
                    return name.endsWith(".jpg") || name.endsWith(".gif") || name.endsWith(".png"); }
        };
        File[] files = directory.listFiles(imagefilter);
        opencv_core.MatVector photos = new opencv_core.MatVector(files.length);                     //Creating a MatVector of images
        opencv_core.Mat labels = new opencv_core.Mat(files.length, 1, CV_32SC1);
        IntBuffer rotulosBuffer = labels.createBuffer();
        int counter = 0;
        for(File image : files)
        {
            opencv_core.Mat photo = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
            int classe = Integer.parseInt(image.getName().split("\\.")[1]);
            Log.e("Class",String.valueOf(classe));
            resize(photo, photo, new opencv_core.Size(IMG_SIZE, IMG_SIZE));
            photos.put(counter, photo);
            rotulosBuffer.put(counter, classe);
            counter++;
        }
        //opencv_face.FaceRecognizer fisherfaces = opencv_face.FisherFaceRecognizer.create();
        //opencv_face.FaceRecognizer lbph = opencv_face.LBPHFaceRecognizer.create();
        eigenfaces.train(photos, labels);
        File f = new File(Environment.getExternalStorageDirectory()+"/facerecog/", EIGEN_FACES_CLASSIFIER);
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        eigenfaces.save(f.getAbsolutePath());
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
}
