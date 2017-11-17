package com.dummystudent.xd720p.diplomus;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.openalpr.OpenALPR;
import org.openalpr.model.Results;
import org.openalpr.model.ResultsError;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.Manifest.permission_group.STORAGE;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.take_photo_btn)
    Button takePhotoBtn;
    @BindView(R.id.photo)
    ImageView photoView;
    @BindView(R.id.number)
    TextView numberView;

    static final int PICK_PHOTO_REQUEST = 100;
    private String ANDROID_DATA_DIR;
    private static final int STORAGE = 1;
    private static File destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        ANDROID_DATA_DIR = this.getApplicationInfo().dataDir;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (destination != null) {// Picasso does not seem to have an issue with a null value, but to be safe
            Picasso.with(MainActivity.this).load(destination).fit().centerCrop().into(photoView);
        }
    }

    public void takePicture() {
        // Use a folder to store all results
        File folder = new File(Environment.getExternalStorageDirectory() + "/OpenALPR/");
        if (!folder.exists()) {
            folder.mkdir();
        }

        // Generate the path for the next photo
        String name = dateToString(new Date(), "yyyy-MM-dd-hh-mm-ss");
        destination = new File(folder, name + ".jpg");

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,  FileProvider.getUriForFile(MainActivity.this, getApplicationContext().getPackageName() + ".my.package.name.provider", destination));
        startActivityForResult(intent, PICK_PHOTO_REQUEST);
    }

    public String dateToString(Date date, String format) {
        SimpleDateFormat df = new SimpleDateFormat(format, Locale.getDefault());

        return df.format(date);
    }

    public void startRecognition(@NonNull Bitmap photo) {
        Context context = getApplicationContext();

        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
//TODO lowstorage recognition

        Frame imageFrame = new Frame.Builder()
                .setBitmap(photo)
                .build();
        SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < textBlocks.size(); i++) {
            TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));
            result.append(textBlock.getValue()).append("    ");
            Log.d(MainActivity.class.getSimpleName(), textBlock.getValue());
        }
        numberView.setText(result.toString());
    }

    @OnClick(R.id.take_photo_btn)
    void onTakePhotoClick() {
        checkPermission();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_PHOTO_REQUEST && resultCode == RESULT_OK) {
//            final ProgressDialog progress = ProgressDialog.show(this, "Loading", "Parsing result...", true);
//            final String openAlprConfFile = ANDROID_DATA_DIR + File.separatorChar + "runtime_data" + File.separatorChar + "openalpr.conf";

            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inSampleSize = 10;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            final Bitmap bitmap = BitmapFactory.decodeFile(destination.getAbsolutePath(), options);
            Picasso.with(MainActivity.this).load(destination).fit().centerCrop().into(photoView);
            startRecognition(bitmap);
//            startRecognition(((BitmapDrawable) photoView.getDrawable()).getBitmap());

//            AsyncTask.execute(new Runnable() {
//                @Override
//                public void run() {
//                    String result = OpenALPR.Factory.create(MainActivity.this, ANDROID_DATA_DIR).recognizeWithCountryRegionNConfig("us", "", destination.getAbsolutePath(), openAlprConfFile, 10);
//
//                    Log.d("OPEN ALPR", result);
//
//                    try {
//                        final Results results = new Gson().fromJson(result, Results.class);
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (results == null || results.getResults() == null || results.getResults().size() == 0) {
//                                    Toast.makeText(MainActivity.this, "It was not possible to detect the licence plate.", Toast.LENGTH_LONG).show();
//                                    numberView.setText("It was not possible to detect the licence plate.");
//                                } else {
//                                    numberView.setText("Plate: " + results.getResults().get(0).getPlate()
//                                            // Trim confidence to two decimal places
//                                            + " Confidence: " + String.format("%.2f", results.getResults().get(0).getConfidence()) + "%"
//                                            // Convert processing time to seconds and trim to two decimal places
//                                            + " Processing time: " + String.format("%.2f", ((results.getProcessingTimeMs() / 1000.0) % 60)) + " seconds");
//                                }
//                            }
//                        });
//
//                    } catch (JsonSyntaxException exception) {
//                        final ResultsError resultsError = new Gson().fromJson(result, ResultsError.class);
//
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                numberView.setText(resultsError.getMsg());
//                            }
//                        });
//                    }
//
//                    progress.dismiss();
//                }
//            });
        }
    }

    private void checkPermission() {
        List<String> permissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissions.isEmpty()) {
            Toast.makeText(this, "Storage access needed to manage the picture.", Toast.LENGTH_LONG).show();
            String[] params = permissions.toArray(new String[permissions.size()]);
            ActivityCompat.requestPermissions(this, params, STORAGE);
        } else { // We already have permissions, so handle as normal
            takePicture();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case STORAGE:{
                Map<String, Integer> perms = new HashMap<>();
                // Initial
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for WRITE_EXTERNAL_STORAGE
                Boolean storage = perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                if (storage) {
                    // permission was granted, yay!
                    takePicture();
                } else {
                    // Permission Denied
                    Toast.makeText(this, "Storage permission is needed to analyse the picture.", Toast.LENGTH_LONG).show();
                }
            }
            default:
                break;
        }
    }



}
