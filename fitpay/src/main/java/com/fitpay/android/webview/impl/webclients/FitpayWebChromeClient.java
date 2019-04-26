package com.fitpay.android.webview.impl.webclients;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import com.fitpay.android.utils.Constants;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default Fitpay web chrome client.
 */
public class FitpayWebChromeClient extends WebChromeClient {
    public static final String CAPTURE_IMAGE_FILE_PROVIDER = "fitpay.pagare.fileprovider";

    private Activity mActivity;
    private Handler mHandler = new Handler();

    private Runnable deletePrivatePhotoRunnable;

    private boolean takingPhoto = false;
    private long mImageTakenTime = -1;

    /**
     * File upload callback for platform versions prior to Android 5.0
     */
    protected ValueCallback<Uri> mFileUploadCallbackFirst;
    /**
     * File upload callback for Android 5.0+
     */
    protected ValueCallback<Uri[]> mFileUploadCallbackSecond;

    public FitpayWebChromeClient(Activity activity) {
        mActivity = activity;
    }

    public boolean isTakingPhoto() {
        return takingPhoto;
    }

    public void updateTakingPhotoStatus() {
        if (mFileUploadCallbackFirst == null && mFileUploadCallbackSecond == null) {
            takingPhoto = false;
        }
    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        switch (requestCode) {
            case Constants.INTENT_TAKE_PHOTO_REQUEST: {
                if (resultCode == Activity.RESULT_OK) {

                    deletePublicPhoto();

                    if (mFileUploadCallbackFirst != null) {
                        mFileUploadCallbackFirst.onReceiveValue(getFileUri());
                        mFileUploadCallbackFirst = null;
                    } else if (mFileUploadCallbackSecond != null) {
                        mFileUploadCallbackSecond.onReceiveValue(new Uri[]{getFileUri()});
                        mFileUploadCallbackSecond = null;
                    }

                    deletePrivatePhoto();

                } else {
                    dismissPhotoAction(false);
                }
                break;
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == Constants.INTENT_TAKE_PHOTO_PERMISSION_REQUEST) {
            if (grantResults.length == 0) {
                dismissPhotoAction(true);
            } else {
                boolean canTakePhoto = true;
                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_DENIED) {
                        canTakePhoto = false;
                        break;
                    }
                }

                if (canTakePhoto) {
                    takePhoto();
                } else {
                    dismissPhotoAction(true);
                }
            }
        }
    }

    // file upload callback (Android 2.2 (API level 8) -- Android 2.3 (API level 10)) (hidden method)
    @SuppressWarnings("unused")
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        openFileChooser(uploadMsg, null);
    }

    // file upload callback (Android 3.0 (API level 11) -- Android 4.0 (API level 15)) (hidden method)
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        openFileChooser(uploadMsg, acceptType, null);
    }

    // file upload callback (Android 4.1 (API level 16) -- Android 4.3 (API level 18)) (hidden method)
    @SuppressWarnings("unused")
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        openFileInput(uploadMsg, null, false);
    }

    // file upload callback (Android 5.0 (API level 21) -- current) (public method)
    @SuppressWarnings("all")
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        if (Build.VERSION.SDK_INT >= 21) {
            final boolean allowMultiple = fileChooserParams.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE;

            openFileInput(null, filePathCallback, allowMultiple);

            return true;
        } else {
            return false;
        }
    }

    @SuppressLint("NewApi")
    protected void openFileInput(final ValueCallback<Uri> fileUploadCallbackFirst, final ValueCallback<Uri[]> fileUploadCallbackSecond, final boolean allowMultiple) {
        takingPhoto = true;

        if (mFileUploadCallbackFirst != null) {
            mFileUploadCallbackFirst.onReceiveValue(null);
        }
        mFileUploadCallbackFirst = fileUploadCallbackFirst;

        if (mFileUploadCallbackSecond != null) {
            mFileUploadCallbackSecond.onReceiveValue(null);
        }
        mFileUploadCallbackSecond = fileUploadCallbackSecond;

        takePhoto();
    }

    private File getFile() {
        File path = new File(mActivity.getFilesDir(), "/secured/");
        if (!path.exists()) path.mkdirs();
        return new File(path, "image.jpg");
    }

    private Uri getFileUri() {
        return FileProvider.getUriForFile(mActivity, CAPTURE_IMAGE_FILE_PROVIDER, getFile());
    }

    private void takePhoto() {
        boolean readPermissionGranted = ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean cameraPermissionGranted = ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;

        if (readPermissionGranted && cameraPermissionGranted) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(mActivity.getPackageManager()) != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, getFileUri());

                mImageTakenTime = System.currentTimeMillis();
                mHandler.removeCallbacks(deletePrivatePhotoRunnable);

                List<ResolveInfo> resInfoList = mActivity.getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    mActivity.grantUriPermission(packageName, getFileUri(), Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                mActivity.startActivityForResult(takePictureIntent, Constants.INTENT_TAKE_PHOTO_REQUEST);
            }
        } else {
            ActivityCompat.requestPermissions(mActivity,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                    Constants.INTENT_TAKE_PHOTO_PERMISSION_REQUEST);
        }
    }

    //hack. some devices creates duplicate of a photo in a public directory
    private void deletePublicPhoto() {
        if (mImageTakenTime == -1) {
            return;
        }

        String[] projection = {
                BaseColumns._ID,
                MediaStore.Images.ImageColumns.SIZE,
                MediaStore.Images.ImageColumns.DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.DATE_ADDED};
        final String imageOrderBy = MediaStore.Images.Media._ID + " DESC";
        final String selection = MediaStore.Images.Media.DATE_TAKEN + " > " + mImageTakenTime;
        //// intialize the Uri and the Cursor, and the current expected size.
        Uri u = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor c = mActivity.getContentResolver().query(u, projection, selection, null, imageOrderBy);
        if (null != c && c.moveToFirst()) {
            ContentResolver cr = mActivity.getContentResolver();
            try {
                cr.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, BaseColumns._ID + "=" + c.getString(3), null);
            } catch (Exception ex) {
            }

            try {
                int column_index_data = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                final String image_path = c.getString(column_index_data);

                final AtomicInteger repeat = new AtomicInteger(0);
                final Runnable deleteFileRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (repeat.get() == 5) {
                            return;
                        }

                        File nFile = new File(image_path);
                        if (nFile.exists()) {
                            nFile.delete();

                            MediaScannerConnection.scanFile(mActivity,
                                    new String[]{image_path}, new String[]{"image/*"},
                                    (path, uri) -> {
                                    });
                        }
                        repeat.set(repeat.get() + 1);

                        mHandler.postDelayed(this, 2000);
                    }
                };
                mHandler.postDelayed(deleteFileRunnable, 5000);

            } catch (Exception e) {
            }
        }

        mImageTakenTime = -1;
    }

    private void deletePrivatePhoto() {
        if (deletePrivatePhotoRunnable == null) {
            deletePrivatePhotoRunnable = () -> {
                File fdelete = getFile();
                if (fdelete.exists()) {
                    fdelete.delete();
                }
            };
        }

        mHandler.postDelayed(deletePrivatePhotoRunnable, 30000);
    }

    private void dismissPhotoAction(boolean showMsg) {
        if (mFileUploadCallbackFirst != null) {
            mFileUploadCallbackFirst.onReceiveValue(null);
            mFileUploadCallbackFirst = null;
        } else if (mFileUploadCallbackSecond != null) {
            mFileUploadCallbackSecond.onReceiveValue(null);
            mFileUploadCallbackSecond = null;
        }

        if (showMsg) {
            Toast.makeText(mActivity, "Photo can't be taken. Please check your application permissions", Toast.LENGTH_SHORT).show();
        }
    }
}