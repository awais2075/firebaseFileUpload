package firebase.file.upload;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

import firebase.file.upload.constant.Constants;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnSuccessListener<UploadTask.TaskSnapshot>, OnFailureListener {


    private EditText editText_imageName;
    private Button button_upload;
    private ImageView imageView_uploadedImage;
    private Uri fileUri;

    private final int IMAGE_PICK_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        init();
    }

    private void init() {
        editText_imageName = findViewById(R.id.editText_imageName);
        button_upload = findViewById(R.id.button_upload);
        imageView_uploadedImage = findViewById(R.id.imageView_uploadedImage);

        button_upload.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_upload:
                chooseFile();
                break;
        }
    }

    private void chooseFile() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_PICK_REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
            case IMAGE_PICK_REQUEST_CODE:
                switch (resultCode) {
                    case RESULT_OK:
                        if (intent != null && intent.getData() != null) {
                            Bitmap bitmap = null;
                            try {
                                fileUri = intent.getData();
                                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fileUri);
                                uploadFileToFirebase();
                            } catch (IOException e) {
                                //e.printStackTrace();
                                Toast.makeText(this, e.getMessage() + "", Toast.LENGTH_SHORT).show();
                            }
                            //imageView_uploadedImage.setImageBitmap(bitmap);
                        }
                        break;
                    case RESULT_CANCELED:
                        break;
                }
                break;
        }
    }

    private void uploadFileToFirebase() {
        if (fileUri != null) {

            StorageReference storageReference = FirebaseStorage.getInstance().getReference();

            StorageReference reference = storageReference.child(Constants.STORAGE_PATH_UPLOADS + System.currentTimeMillis() + "." + getFileExtension(fileUri));

            reference.putFile(fileUri).addOnSuccessListener(this).addOnFailureListener(this);
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    @Override
    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
        Toast.makeText(this, "File Uploaded", Toast.LENGTH_SHORT).show();
        String url = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
        Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();

        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(MainActivity.this).load(uri).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                }).into(imageView_uploadedImage);
            }
        });

    }

    @Override
    public void onFailure(@NonNull Exception e) {
        Toast.makeText(this, "File Failed to Upload", Toast.LENGTH_SHORT).show();
    }
}