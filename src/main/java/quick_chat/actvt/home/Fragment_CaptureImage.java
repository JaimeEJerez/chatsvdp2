package quick_chat.actvt.home;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static quick_chat.actvt.ImageProcessingActvt.newIntentImageProcessingActvt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Size;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class Fragment_CaptureImage extends Fragment
{
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions         = new ArrayList<>();

    private final static int ALL_PERMISSIONS_RESULT   = 107;
    private final static int IMAGE_TACKING_REQUEST    = 200;
    private static final int IMAGE_PROCESSING_REQUEST = 7002;

    private Uri outputFileUri = null;

    int imagehHSize;
    int imageVSize;

    ActivityResultLauncher<Intent> retriveImageActivityResultLauncher;
    ActivityResultLauncher<Intent> processImageActivityResultLauncher;


    public static abstract class TakePictureHandler
    {
        public abstract void result(byte[] buff, String error);
    }

    public static enum IMAGE_FORMAT
    {
        JPG, PNG
    }

    ;

    private IMAGE_FORMAT imageFormat = IMAGE_FORMAT.JPG;
    private boolean      roundBitMap = false;
    private Size         imageSize   = null;

    private Intent requestImageIntent = null;

    private boolean checkPermitions()
    {
        permissions.add(CAMERA);
        permissions.add(WRITE_EXTERNAL_STORAGE);
        permissions.add(READ_EXTERNAL_STORAGE);
        permissionsToRequest = findUnAskedPermissions(permissions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (permissionsToRequest.size() > 0)
            {
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);

                return false;
            }
            else
            {
                return true;
            }
        }

        return true;
    }

    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted)
    {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted)
        {
            if (!hasPermission(perm))
            {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission)
    {
        if (canMakeSmores())
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                return (this.getActivity().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canMakeSmores()
    {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        checkPermitions();

        processImageActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>()
                {
                    @Override
                    public void onActivityResult(ActivityResult result)
                    {
                        if (result.getResultCode() == Activity.RESULT_OK)
                        {
                            // There are no request codes
                            Intent data = result.getData();

                            Uri processedImageUri = data.getData();

                            receiveImage(processedImageUri, null, 2);
                        }
                    }
                });


        retriveImageActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>()
                {
                    @Override
                    public void onActivityResult(ActivityResult result)
                    {
                        if (result.getResultCode() == Activity.RESULT_OK)
                        {
                            // There are no request codes
                            Intent data = result.getData();

                            boolean isCamera = data == null || data.getData() == null;

                            Uri imegeUri = null;

                            if (isCamera)
                            {
                                imegeUri = outputFileUri;
                            }
                            else
                            {
                                imegeUri = data.getData();
                            }

                            if (receiveImage(outputFileUri, null, 1))
                            {
                                int orientation = isCamera ? getResources().getConfiguration().orientation : Configuration.ORIENTATION_UNDEFINED;

                                Intent intent = newIntentImageProcessingActvt( Fragment_CaptureImage.this.getActivity(), imegeUri, false, imagehHSize, imageVSize, imageFormat, orientation);

                                processImageActivityResultLauncher.launch( intent );
                            }

                        }
                    }
                });

    }

    public void takeImage(boolean roundBitMap, int hSize, int vSize, IMAGE_FORMAT imageFormat)
    {
        imagehHSize = hSize;
        imageVSize  = vSize;

        if (imageFormat == null)
        {
            this.imageFormat = IMAGE_FORMAT.JPG;
        }
        else
        {
            this.imageFormat = imageFormat;
        }

        setRoundBitMap(roundBitMap);

        if (hSize != 0 && vSize != 0)
        {
            setImageSize(new Size(hSize, vSize));
        }

        String destination = Environment.getExternalStorageDirectory().getPath() + "/temp_image.jpg";

        outputFileUri = Uri.fromFile(new File(destination));

        List<Intent>      allIntents     = new ArrayList<>();
        PackageManager    packageManager = this.getActivity().getPackageManager();
        Intent            captureIntent  = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam        = packageManager.queryIntentActivities(captureIntent, 0);

        for (ResolveInfo res : listCam)
        {
            Intent intent = new Intent(captureIntent);

            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

            intent.setPackage(res.activityInfo.packageName);

            if (outputFileUri != null)
            {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }

            allIntents.add(intent);
        }

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);

        galleryIntent.setType("image/*");

        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);

        for (ResolveInfo res : listGallery)
        {
            Intent intent = new Intent(galleryIntent);

            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

            intent.setPackage(res.activityInfo.packageName);

            allIntents.add(intent);
        }

        Intent mainIntent = allIntents.get(allIntents.size() - 1);

        for (Intent intent : allIntents)
        {
            if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity"))
            {
                mainIntent = intent;
                break;
            }
        }

        allIntents.remove(mainIntent);

        if (checkPermitions())
        {
            requestImageIntent = Intent.createChooser(mainIntent, "Seleccione...");

            requestImageIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

            retriveImageActivityResultLauncher.launch(requestImageIntent);
        }

    }

    private String getPathFromURI(Uri contentUri)
    {
        String[] proj         = {MediaStore.Audio.Media.DATA};
        Cursor   cursor       = this.getActivity().getContentResolver().query(contentUri, proj, null, null, null);
        int      column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public boolean receiveImage(Uri imageUri, String error, int step)
    {
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode)
        {
            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest)
                {
                    if (!hasPermission(perms))
                    {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0)
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0)))
                        {
                            showMessageOKCancel("Estos permisos son necesarios para continuar.", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                    {
                                        requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                    }
                                }
                            });
                            return;
                        }
                    }
                }
                else
                {
                    if (requestImageIntent != null)
                    {
                        startActivityForResult(requestImageIntent, IMAGE_TACKING_REQUEST);
                    }
                }

                break;
        }
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener)
    {
        new AlertDialog.Builder(this.getActivity()).setMessage(message).setPositiveButton("OK", okListener).setNegativeButton("Cancel", null).create().show();
    }

    /**
     * @return the roundBitMap
     */
    public boolean isRoundBitMap()
    {
        return roundBitMap;
    }

    /**
     * @param roundBitMap the radious to set
     */
    public void setRoundBitMap(boolean roundBitMap)
    {
        this.roundBitMap = roundBitMap;
    }

    /**
     * @return the imageSize
     */
    public Size getImageSize()
    {
        return imageSize;
    }

    /**
     * @param imageSize the imageSize to set
     */
    public void setImageSize(Size imageSize)
    {
        this.imageSize = imageSize;
    }

    public IMAGE_FORMAT getImageFormat()
    {
        return imageFormat;
    }

    public void setImageFormat(IMAGE_FORMAT imageFormat)
    {
        this.imageFormat = imageFormat;
    }

    public boolean onBackPressed()
    {
        return false;
    }
}

