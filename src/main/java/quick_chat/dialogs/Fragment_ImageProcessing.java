package quick_chat.dialogs;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Size;
import android.view.MotionEvent;
import android.view.View;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import quick_chat.Utils;
import quick_chat.actvt.home.Fragment_CaptureImage;
import quick_chat.actvt.home.ToastActivity;
import quick_chat.custom_views.ImageProcessingView;
import quick_chat.start.R;

public class Fragment_ImageProcessing extends ToastActivity
{
    private Uri                                     imageUri                = null;
    private  boolean                                roundBitMap             = false;
    private  Size                                   imageSize               = null;
    private  Fragment_CaptureImage.IMAGE_FORMAT     imageFormat             = null;

    private ImageProcessingView                     imageProcessingView     = null;
    private String	                                imageType;
    private Bitmap                                  imageBitmap;


    public Fragment_ImageProcessing()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.image_processing_actvt);

        imageProcessingView = findViewById( R.id.imageView1 );

        if ( imageFormat == Fragment_CaptureImage.IMAGE_FORMAT.PNG  )
        {
            imageType = ".png";
        }
        else if ( imageFormat == Fragment_CaptureImage.IMAGE_FORMAT.JPG  )
        {
            imageType = ".jpg";
        }

        imageUri = getIntent().getData();
        {
            try
            {
                imageBitmap = Utils.readBitMapFromURI( this, imageUri);

                //Utils.deleteUri(imageUri);
            }
            catch (IOException e3)
            {
                e3.printStackTrace();
            }
        }

        imageProcessingView.init( imageBitmap, roundBitMap, imageSize );

        if ( imageProcessingView != null )
        {
            imageProcessingView.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View view, MotionEvent event)
                {
                    imageProcessingView.onTouch( event );

                    return true;
                }
            });
        }

        //return rootView;

        /*
        */
    }

    public void eraseBG(View v )
    {
        imageProcessingView.eraseBG();
    }

    public void rotLeft( View v )
    {
        imageProcessingView.rotate( -90 );
    }

    public void rotRight( View v )
    {
        imageProcessingView.rotate( 90 );
    }

    public void okAction( View v )
    {
        //imageProcessingView.clip();

        try
        {
           // Uri dstImageURI = Uri.fromFile( Utils.createImageFile( this, imageType));

            final OutputStream os =  this.getContentResolver().openOutputStream( imageUri );

            Bitmap bitMap1 = imageProcessingView.getBitMap();

            byte[] imageBytes 	= null;

            if ( roundBitMap )
            {
                imageBytes 	= Utils.bitMap2PNG( bitMap1 );
            }
            else
            {
                imageBytes 	= Utils.bitMap2JPG( bitMap1 );
            }

            try
            {
                os.write( imageBytes );

                os.close();
            }
            catch (FileNotFoundException e1)
            {
                e1.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            finally
            {
                try
                {
                    Intent intent = new Intent();

                    intent.setData( imageUri );

                    //setResult( RESULT_OK, intent );

                    imageProcessingView.recycle();

                    //finishAndRemoveTask();
                }
                catch (Throwable e)
                {
                }
            }
        }
        catch (FileNotFoundException e2)
        {
            e2.printStackTrace();
        }
    }


   // @Override
   // public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   // {
    //}
}