package quick_chat.actvt;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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

public class ImageProcessingActvt extends ToastActivity
{
    ImageProcessingView                 imageProcessingView = null;
    boolean                             roundBitMap         = false;
    Size                                imageSize           = null;
    Fragment_CaptureImage.IMAGE_FORMAT  imageFormat         = null;

    private String	imageType;


    public static Intent newIntentImageProcessingActvt(Context context,
                                                       Uri                                    imageUri,
                                                       boolean                                roundBitMap,
                                                       int                                    imageSizeH,
                                                       int                                    imageSizeV,
                                                       Fragment_CaptureImage.IMAGE_FORMAT     imageFormat,
                                                       int                                    orientation )
    {
        Intent intent = new Intent( context, ImageProcessingActvt.class );

        intent.setData( imageUri );

        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        intent.putExtra("roundBitMap", roundBitMap );
        intent.putExtra("imageSizeH", imageSizeH );
        intent.putExtra("imageSizeV", imageSizeV );
        intent.putExtra("imageFormat", imageFormat.toString() );
        intent.putExtra("orientation", orientation );

        return intent;
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
       imageProcessingView.clip();

        try
        {
            Uri dstImageURI = Uri.fromFile( Utils.createUUImageFile( this.getBaseContext(), imageType) );

            final OutputStream os = getContentResolver().openOutputStream( dstImageURI );

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

                    intent.setData( dstImageURI );

                    setResult( RESULT_OK, intent );

                    imageProcessingView.recycle();

                    finishAndRemoveTask();
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
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView( R.layout.image_processing_actvt );

        imageProcessingView = this.findViewById( R.id.imageView1 );

        Intent intent = this.getIntent();

        Uri    	srcImageURI      	= intent.getData();
        Bundle 	bitMapDataBundle 	= intent.getBundleExtra("bitMapData");
        Bitmap	imageBitmap 		= bitMapDataBundle != null ? (Bitmap)bitMapDataBundle.get("data") : null;

        Utils.deleteUri( srcImageURI );

        if ( srcImageURI == null && imageBitmap == null)
        {
            Utils.showAlert(  this,"ALERTA", "Lamantablemente, no se pudo procesar la imagen por un error interno.");
            return;
        }

        roundBitMap 	= intent.getBooleanExtra( "roundBitMap", false );
        int iw 			= intent.getIntExtra( "imageWidth", 0 );
        int ih 			= intent.getIntExtra( "imageHeight", 0 );
        int orientation = intent.getIntExtra( "orientation", 0 );

        String 	imageFormatName = intent.getStringExtra( "imageFormat" );

        imageFormat = Fragment_CaptureImage.IMAGE_FORMAT.valueOf( imageFormatName );

        if ( imageFormat == Fragment_CaptureImage.IMAGE_FORMAT.PNG  )
        {
            imageType = ".png";
        }
        else if ( imageFormat == Fragment_CaptureImage.IMAGE_FORMAT.JPG  )
        {
            imageType = ".jpg";
        }

        if ( iw != 0 && ih != 0 )
        {
            imageSize = new Size( iw, ih );
        }

        if ( imageBitmap == null && srcImageURI != null )
        {
            try
            {
                imageBitmap = Utils.readBitMapFromURI(this, srcImageURI);

                Utils.deleteUri(srcImageURI);
            }
            catch (IOException e3)
            {
                e3.printStackTrace();
            }
        }

        if ( imageBitmap == null )
        {
            return;
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

        if (orientation == Configuration.ORIENTATION_PORTRAIT )
        {
            imageProcessingView.rotate( 90 );
        }
    }
}
