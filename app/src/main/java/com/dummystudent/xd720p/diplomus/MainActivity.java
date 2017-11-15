package com.dummystudent.xd720p.diplomus;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.take_photo_btn)
    Button takePhotoBtn;
    @BindView(R.id.photo)
    ImageView photoView;
    @BindView(R.id.number)
    TextView numberView;

    static Uri capturedImageUri = null;
    static final int PICK_PHOTO_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.take_photo_btn)
    void onTakePhotoClick() {
        capturedImageUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),"fname_" +
                String.valueOf(System.currentTimeMillis()) + ".jpg"));
        setProfilePic();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_PHOTO_REQUEST && resultCode == RESULT_OK) {
            Bitmap photo = null;
            try {
                photo = MediaStore.Images.Media.getBitmap(getContentResolver(), data != null ? data.getData() : capturedImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            photoView.setImageBitmap(photo);
        }
    }

    private void setProfilePic() {
        List<Intent> pickPicIntentList = new ArrayList<>();
        Intent gallIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        List<ResolveInfo> listGall = getPackageManager().queryIntentActivities(gallIntent, 0);
        addIntents(listGall, gallIntent, pickPicIntentList);

        final Intent chooserIntent = Intent.createChooser(new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(android.provider.MediaStore.EXTRA_OUTPUT, capturedImageUri), getString(R.string.pick_photo));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, pickPicIntentList.toArray(new Parcelable[pickPicIntentList.size()]));
        startActivityForResult(chooserIntent, PICK_PHOTO_REQUEST);

    }

    private void addIntents(List<ResolveInfo> resolveInfoList, Intent intent, List<Intent> intentList) {
        for (ResolveInfo resIntItem : resolveInfoList) {
            Intent pickedIntent = new Intent(intent);
            pickedIntent.setComponent(new ComponentName(resIntItem.activityInfo.packageName, resIntItem.activityInfo.name));
            intentList.add(pickedIntent);
        }
    }

}
