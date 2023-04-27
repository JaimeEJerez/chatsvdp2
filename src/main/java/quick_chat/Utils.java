package quick_chat;

import static android.os.Environment.DIRECTORY_MUSIC;
import static android.os.Environment.DIRECTORY_PICTURES;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;

//import android.media.AudioTrack.Builder
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Size;
import android.widget.Button;

import androidx.core.graphics.drawable.DrawableCompat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.Vector;

import quick_chat.actvt.home.ToastActivity;
import quick_chat.start.R;

public class Utils
{
    public static String sql2SpanishDate( String sqldate )
    {
        String[] split = sqldate.split( "-" );

        return split[2] + "/" + split[1] + "/" + split[0];
    }

    public static void setEanbleButton( Button button, boolean enable )
    {
        Drawable drawable = button.getBackground();

        drawable = DrawableCompat.wrap(drawable);

        if ( !enable )
        {
            DrawableCompat.setTint( drawable, Color.WHITE );

            button.setBackground(drawable);

            button.setTextColor( Color.BLACK );

            button.setEnabled( false );
        }
        else
        {
            DrawableCompat.setTint(drawable, button.getResources().getColor(R.color.orange_button));

            button.setBackground(drawable);

            button.setTextColor(Color.WHITE);

            button.setEnabled( true );
        }
    }


    	private	 	static byte[]   			avatarImage 	= null;

	public static abstract class ActionHandler
	{
		public abstract void doAction( int resultCode );
	}

	public static abstract class DownloadImageResult
	{
		public abstract void doAction( byte[] resultBuff );
	}

	public static Bitmap roundBitMap(Bitmap srcBitMap)
	{
		Bitmap dstBitMap = Bitmap.createBitmap(srcBitMap.getWidth(), srcBitMap.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas    = new Canvas(dstBitMap);

		final Paint paint = new Paint();
		final Rect  rect  = new Rect(0, 0, srcBitMap.getWidth(), srcBitMap.getHeight());

		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);

		canvas.drawARGB(0, 0, 0, 0);

		// It doesn't matter which color!
		paint.setColor(Color.WHITE);
		canvas.drawRoundRect(new RectF(0, 0, srcBitMap.getWidth(), srcBitMap.getHeight()), srcBitMap.getWidth() / 2, srcBitMap.getHeight() / 2, paint);

		// The second drawing should only be visible of if overlapping with the
		// first
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(srcBitMap, rect, rect, paint);

		return dstBitMap;
	}

	public static Bitmap readBitMapFromBytes( Context context, byte[] bytes ) throws IOException
	{
		ByteArrayInputStream bis = new ByteArrayInputStream( bytes );

		Bitmap thumbnail = BitmapFactory.decodeStream( bis );

		bis.close();

		return thumbnail;
	}

	public static Bitmap readBitMapFromURI( Context context, Uri uri ) throws IOException
	{
		InputStream is = context.getContentResolver().openInputStream(uri);

		Bitmap thumbnail = BitmapFactory.decodeStream(is);

		is.close();

		return thumbnail;
	}

	public static boolean deleteUri( Uri uri )
	{
		boolean success = false;

		File file = new File(uri.getPath());

		try
		{
			file.delete();
			if(file.exists())
			{
				try
				{
					file.getCanonicalFile().delete();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		finally
		{
			success = !file.exists();
		}

		return success;
	}

	public static byte[] readBuffFromURI( Context context, Uri uri ) throws IOException
	{
		byte[] buff = null;

		InputStream is = context.getContentResolver().openInputStream(uri);

		int size = is.available();

		if ( size > 0 )
		{
			DataInputStream dis = new DataInputStream( is );

			buff = new byte[size];

			dis.readFully(buff);
		}

		is.close();

		return buff;
	}


	public static void showAlert(final ToastActivity context, final String title, final String message)
	{
		showAlert( context, title, message, null );
	}

	public static void showAlert(final ToastActivity context, final String title, final String message, final ActionHandler action)
	{
		context.hideToats();

		context.runOnUiThread( new Runnable()
		{
			@Override
			public void run()
			{
				AlertDialog alertDialog = new AlertDialog.Builder( context ).create();
				alertDialog.setTitle( title );
				alertDialog.setMessage( message );
				alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
						new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int which)
							{
								dialog.dismiss();
								if ( action != null )
								{
									action.doAction(AlertDialog.BUTTON_POSITIVE);
								}
							}
						});
				alertDialog.show();
			}

		});
	}

	public static void showAlertWCancel( final Activity context, final String title, final String message, final ActionHandler action )
	{
		context.runOnUiThread( new Runnable()
		{

			@Override
			public void run()
			{
				AlertDialog alertDialog = new AlertDialog.Builder( context ).create();
				alertDialog.setTitle( title );
				alertDialog.setMessage( message );
				alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
						new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int which)
							{
								if ( action != null )
								{
									action.doAction( AlertDialog.BUTTON_POSITIVE );
								}
								dialog.dismiss();
							}
						});

				alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancelar",
						new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int which)
							{
								if ( action != null )
								{
									action.doAction( AlertDialog.BUTTON_NEGATIVE );
								}
								dialog.dismiss();
							}
						});

				alertDialog.show();
			}

		});
	}

	public static byte[] bitMap2JPG( Bitmap bmp )
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		bmp.compress(Bitmap.CompressFormat.JPEG, 75, stream);

		byte[] byteArray = stream.toByteArray();

		return byteArray;
	}

	public static byte[] bitMap2PNG( Bitmap bmp )
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		bmp.compress(Bitmap.CompressFormat.PNG, 75, stream);

		byte[] byteArray = stream.toByteArray();

		return byteArray;
	}

	public static Bitmap byteArr2BitMap(byte[] buff)
	{
		Bitmap bMap = BitmapFactory.decodeByteArray( buff, 0, buff.length );

		return bMap;
	}

	public static String createDirectoryTree( String[] dirTree )
	{
		String dir =  File.separator;
		File f = null;

		for ( String d: dirTree )
		{
			dir = dir + d + File.separator;

			f = new File( dir );

			if ( !f.exists() )
			{
				boolean result = f.mkdir();

				if ( !result )
				{
					return null;
				}
			}
		}

		return dir ;
	}


	public static Bitmap resizeBitMap(Bitmap bitMap, Size tagetSmageSize )
	{
		int bmW = tagetSmageSize.getWidth();
		int bmH = tagetSmageSize.getHeight();

		Bitmap resized = Bitmap.createScaledBitmap(bitMap, bmW, bmH, true);

		return resized;
	}

	public static Bitmap resizeBitMap(Bitmap bitMap, double diagonal )
	{
		int bmW = bitMap.getWidth();
		int bmH = bitMap.getHeight();

		double bmX = Math.hypot( bmW, bmH );
		double bmK = diagonal;
		double fact = bmK / bmX;

		bmW = (int)(bmW*fact);
		bmH = (int)(bmH*fact);

		Bitmap resized = Bitmap.createScaledBitmap(bitMap, bmW, bmH, true);

		return resized;
	}

	/**
	 * Calculate distance between two points in latitude and longitude taking
	 * into account height difference. If you are not interested in height
	 * difference pass 0.0. Uses Haversine method as its base.
	 *
	 * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
	 * el2 End altitude in meters
	 * @returns Distance in Meters
	 */
	public static double distance(	double lat1, double lat2, double lon1,
									  double lon2, double el1, double el2)
	{
		final int R = 6371; // Radius of the earth

		double latDistance = Math.toRadians(lat2 - lat1);
		double lonDistance = Math.toRadians(lon2 - lon1);

		double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
				   + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
					 * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		double distance = R * c * 1000; // convert to meters

		double height = el1 - el2;

		distance = Math.pow(distance, 2) + Math.pow(height, 2);

		return Math.sqrt(distance);
	}

	public static File createUUImageFile( Context context, String imageType ) throws IOException
	{
		String 	imageFileName 	= UUID.randomUUID().toString() + imageType;
		File 	storageDir      = context.getExternalFilesDir(DIRECTORY_PICTURES);
		File 	image 			= new File( storageDir, imageFileName  );

		return image;
	}

	public static File createUUAudioFile( Context context, String audioType ) throws IOException
	{
		String 	imageFileName 	= UUID.randomUUID().toString() + audioType;
		File 	storageDir      = context.getExternalFilesDir(DIRECTORY_MUSIC);
		File 	image 			= new File( storageDir, imageFileName  );

		return image;
	}



	public static byte[] bytes_from_DIRECTORY_PICTURES( Context context,String imageName ) throws IOException
	{
		File 	storageDir      = context.getExternalFilesDir(DIRECTORY_PICTURES);
		File 	imageFile 		= new File( storageDir, imageName  );

		String p  = imageFile.getAbsolutePath();

		byte[] 	data			= null;

		if ( imageFile.exists() )
		{
			FileInputStream fis = new FileInputStream(imageFile);

			DataInputStream dis = new DataInputStream( context.getContentResolver().openInputStream(Uri.fromFile(imageFile)) );

			int size = dis.available();

			data = new byte[size];

			dis.readFully( data );

			dis.close();
			fis.close();
		}

		return data;
	}

	public static  void bytes_to_DIRECTORY_PICTURES( Context context, byte[] bytes, String imageName ) throws IOException
	{
		File 	storageDir      = context.getExternalFilesDir(DIRECTORY_PICTURES);

		File 	imageFile 		= new File( storageDir, imageName  );

		byte[] 	data			= null;

		if ( imageFile.exists() )
		{
			imageFile.delete();
		}

		{
			FileOutputStream fis = new FileOutputStream(imageFile);

			DataOutputStream dos = new DataOutputStream( context.getContentResolver().openOutputStream(Uri.fromFile(imageFile)) );

			dos.write( bytes );

			fis.close();
			dos.close();
		}
	}

	public static File bytes_to_DIRECTORY_AUDIO( Context context, byte[] bytes, String imageName ) throws IOException
	{
		File 	storageDir      = context.getExternalFilesDir(DIRECTORY_MUSIC);
		File 	audioFile 		= new File( storageDir, imageName  );

		byte[] 	data			= null;

		if ( !audioFile.exists() )
		{
			FileOutputStream fis = new FileOutputStream(audioFile);

			DataOutputStream dos = new DataOutputStream( context.getContentResolver().openOutputStream(Uri.fromFile(audioFile)) );

			dos.write( bytes );

			fis.close();
			dos.close();
		}

		return audioFile;
	}

	public static byte[] bytes_from_DIRECTORY_AUDIO( Context context,String audioName ) throws IOException
	{
		File 	storageDir      = context.getExternalFilesDir(DIRECTORY_MUSIC);
		File 	imageFile 		= new File( storageDir, audioName  );

		byte[] 	data			= null;

		if ( imageFile.exists() )
		{
			FileInputStream fis = new FileInputStream(imageFile);

			DataInputStream dis = new DataInputStream( context.getContentResolver().openInputStream(Uri.fromFile(imageFile)) );

			int size = dis.available();

			data = new byte[size];

			dis.readFully( data );

			dis.close();
			fis.close();
		}

		return data;
	}


	public static File file_from_DIRECTORY_AUDIO( Context context,String imageName ) throws IOException
	{
		File 	storageDir      = context.getExternalFilesDir(DIRECTORY_MUSIC);
		File 	audioFile 		= new File( storageDir, imageName  );

		return audioFile;
	}


	public static Bitmap bitmap_from_DIRECTORY_PICTURES( Context context,String imageName )
	{
		File 	storageDir      = context.getExternalFilesDir(DIRECTORY_PICTURES);
		File 	imageFile 		= new File( storageDir, imageName  );

		Bitmap 	bitMap			= null;

		if ( imageFile.exists() )
		{
			FileInputStream fis = null;
			try
			{
				fis = new FileInputStream(imageFile);

				InputStream is = context.getContentResolver().openInputStream(Uri.fromFile(imageFile));

				bitMap = BitmapFactory.decodeStream(is);

				is.close();
				fis.close();
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		return bitMap;
	}

	public static String fileNameFromUri( Context context, Uri uri )
	{
		String fileName = null;

		if (uri != null)
		{
			if (uri.toString().startsWith("file:"))
			{
				fileName = uri.getPath();
			}
			else
				{ // uri.startsWith("content:")

				Cursor c = context.getContentResolver().query(uri, null, null, null, null);

				if (c != null && c.moveToFirst())
				{

					int id = c.getColumnIndex(MediaStore.Images.Media.DATA);
					if (id != -1)
					{
						fileName = c.getString(id);
					}
				}
			}
		}

		return fileName;
	}

	private static Bitmap  bwBitmap    = null;
	private static Canvas  canvas      = null;
	private static Paint   paint1      = new Paint();

	static
	{
		bwBitmap = Bitmap.createBitmap(612, 128, Bitmap.Config.ARGB_8888 );

		canvas = new Canvas(bwBitmap);

		paint1.setStyle(Paint.Style.FILL);
		paint1.setColor(Color.GRAY);
	}

	public static class FixAudioMessageResult
	{
		public int     	secunds;
		public byte[] 	image;

		public FixAudioMessageResult( int secunds, byte[] image  )
		{
			this.secunds 	= secunds;
			this.image 		= image;
		}
	}

	static public synchronized FixAudioMessageResult fixAudioMessage( File audioFile, Vector<Integer> voulumen, Context context )
	{
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();

		Uri uri = Uri.parse( audioFile.getAbsolutePath() );

		mmr.setDataSource( context, uri );

		String  durationStr     = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
		int     secunds         = Integer.parseInt(durationStr) / 1000;

		byte[] image;

		{
			float vectSize      = voulumen.size();
			float bMpWidth      = bwBitmap.getWidth();
			float bMappHei      = bwBitmap.getHeight();

			bwBitmap.eraseColor(Color.TRANSPARENT);

			for ( float i = 0; i < bMpWidth; i += 1.0f )
			{
				int x = (int)Math.min( vectSize-1.0f, (i/bMpWidth) * vectSize);

				int y = Math.round( voulumen.get( x ) * bMappHei / 32767.0f);

				canvas.drawRect( i, 64 - y, i + 1, 64 + y, paint1 );
			}

			image = Utils.bitMap2PNG(bwBitmap);
		}

		return new FixAudioMessageResult( secunds, image );
	}
}
