package sda.oscail.edu.gigiddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.system.ErrnoException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


// ref: https://theartofdev.com/2015/02/15/android-cropping-image-from-camera-or-gallery/
// ref: https://gist.github.com/ArthurHub/8a8530dd688df409fb20
public class SetProfileImage extends AppCompatActivity {
    private static final String TAG = "SetProfileImage";

    private CropImageView mCropImageView;
    private Uri mCropImageUri;
    Button saveImage;

    private DatabaseReference dbRef;
    private StorageReference userProfileImageRef;
    private String currentUserID;
    private FirebaseAuth mAuth;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile_image);

        mCropImageView = findViewById(R.id.CropImageView);
        saveImage = findViewById(R.id.save_image);
        loadingBar = new ProgressDialog(this);

        // Firebase and db references
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference();
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        saveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loadingBar.setTitle("Set profile Image");
                loadingBar.setMessage("Please wait, your profile image is updating...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                //ref: https://firebase.google.com/docs/storage/android/upload-files
                Bitmap croppedImage = mCropImageView.getCroppedImage();
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                croppedImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                byte[] data = bytes.toByteArray();

                final StorageReference filePath = userProfileImageRef.child(currentUserID + ".jpg");
                final UploadTask uploadTask = filePath.putBytes(data);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        if(uploadTask.isSuccessful()) {
                            Toast.makeText(SetProfileImage.this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();

                            //ref: https://stackoverflow.com/questions/50158921/firebase-storage-retrieves-a-long-lived-download-url-using-getdownloadurl-no
                            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadUrl = uri.toString();
                                    Log.d(TAG, "/////////////////// ------ " + downloadUrl);

                                    // ref: https://www.youtube.com/watch?v=zV9PSBnCkJc&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=27
                                    dbRef.child("Users").child(currentUserID).child("image")
                                            .setValue(downloadUrl)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()) {
                                                        Toast.makeText(SetProfileImage.this, "Image save in database successfully...", Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();
                                                         Intent goBacktoSettings = new Intent(SetProfileImage.this, Settings.class);
                                                         goBacktoSettings.putExtra("from_activity", "crop_image");
                                                         startActivity(goBacktoSettings);
                                                    } else {
                                                        String message = task.getException().toString();
                                                        Toast.makeText(SetProfileImage.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();
                                                    }
                                                }
                                    });
                                }
                            });
                        } else {
                            String message = uploadTask.getException().toString();
                            Toast.makeText(SetProfileImage.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            }
        });
    }

    /**
     * On load image button click, start pick image chooser activity.
     */
    public void onLoadImageClick(View view) {
        startActivityForResult(getPickImageChooserIntent(), 200);

    }

    /**
     * Crop the image and set it back to the cropping view.
     */
    public void onCropImageClick(View view) {
        Bitmap cropped = mCropImageView.getCroppedImage(500, 500);
        if (cropped != null) {
            mCropImageView.setImageBitmap(cropped);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {

            Uri imageUri = getPickImageResultUri(data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage,
            // but we don't know if we need to for the URI so the simplest is to try open the stream and see if we get error.
            boolean requirePermissions = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                    isUriRequiresPermissions(imageUri)) {

                // request permissions and handle the result in onRequestPermissionsResult()
                requirePermissions = true;
                mCropImageUri = imageUri;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }

            if (!requirePermissions) {
                mCropImageView.setImageUriAsync(imageUri);
            }
        }
        saveImage.setVisibility(View.VISIBLE);
        saveImage.setClickable(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mCropImageView.setImageUriAsync(mCropImageUri);
        } else {
            Toast.makeText(this, "Required permissions are not granted", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Create a chooser intent to select the source to get image from.<br/>
     * The source can be camera's (ACTION_IMAGE_CAPTURE) or gallery's (ACTION_GET_CONTENT).<br/>
     * All possible sources are added to the intent chooser.
     */
    public Intent getPickImageChooserIntent() {

        // Determine Uri of camera image to save.
        Uri outputFileUri = getCaptureImageOutputUri();

        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = getPackageManager();

        // collect all camera intents
        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }

        // collect all gallery intents
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }

        // the main intent is the last in the list (fucking android) so pickup the useless one
        Intent mainIntent = allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);

        // Create a chooser from the main intent
        Intent chooserIntent = Intent.createChooser(mainIntent, "Select source");

        // Add all other intents
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    }

    /**
     * Get URI to image received from capture by camera.
     */
    private Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
        File getImage = getExternalCacheDir();
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "pickImageResult.jpeg"));
        }
        return outputFileUri;
    }

    /**
     * Get the URI of the selected image from {@link #getPickImageChooserIntent()}.<br/>
     * Will return the correct URI for camera and gallery image.
     *
     * @param data the returned data of the activity result
     */
    public Uri getPickImageResultUri(Intent data) {
        boolean isCamera = true;
        if (data != null && data.getData() != null) {
            String action = data.getAction();
            isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
        }
        return isCamera ? getCaptureImageOutputUri() : data.getData();
    }

    /**
     * Test if we can open the given Android URI to test if permission required error is thrown.<br>
     */
    public boolean isUriRequiresPermissions(Uri uri) {
        try {
            ContentResolver resolver = getContentResolver();
            InputStream stream = resolver.openInputStream(uri);
            stream.close();
            return false;
        } catch (FileNotFoundException e) {
            if (e.getCause() instanceof ErrnoException) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }
}
