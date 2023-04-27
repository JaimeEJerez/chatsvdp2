package quick_chat.io;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;

import quick_chat.Utils;
import quick_chat.globals.Constants;

public class RetiveImagesFromRepository
{
    public static abstract class EventHandler
    {
        public abstract void finish( Bitmap bitmap, String error);
    }

    private String imageUUID;
    private String imgOwnerID;

    Hashtable<String, Vector<EventHandler>> eventHandlerTableVect = new Hashtable<String, Vector<EventHandler>>();

    public RetiveImagesFromRepository( String imgOwnerID, String imageUUID )
    {
        this.imgOwnerID     = imgOwnerID;
        this.imageUUID      = imageUUID;
    }

    synchronized public void execute( Context context, final EventHandler eventHandler)
    {
        Bitmap bitmap = Utils.bitmap_from_DIRECTORY_PICTURES( context, imageUUID);

        if ( bitmap != null )
        {
            eventHandler.finish( bitmap, null );

            return;
        }

        Vector<EventHandler> ehv = eventHandlerTableVect.get( imageUUID );

        if ( ehv != null )
        {
            ehv.add( eventHandler );
        }
        else
        {
            ehv = new Vector<EventHandler>();

            ehv.add( eventHandler );

            eventHandlerTableVect.put( imageUUID, ehv );

            Thread thread = new Thread()
            {
                public void run()
                {
                    Bitmap            bitmap        = null;
                    byte[]            buff          = null;
                    String            error         = null;
                    HttpURLConnection urlConnection = null;

                    try
                    {
                        String urlStr = Constants.kProtocol + Constants.kAPIEntryPoint + "/ImagesRepository?imageUUID=" + imageUUID;

                        URL url = new URL(urlStr);

                        urlConnection = (HttpURLConnection) url.openConnection();

                        DataInputStream dis = new DataInputStream(urlConnection.getInputStream());

                        int size = dis.readInt();

                        buff = new byte[size];

                        dis.readFully(buff);

                        Utils.bytes_to_DIRECTORY_PICTURES( context, buff, imageUUID );

                        bitmap = Utils.byteArr2BitMap(buff);
                    }
                    catch (IOException e)
                    {
                        error = e.getMessage();
                    }
                    finally
                    {
                        if ( bitmap != null )
                        {
                            final Bitmap finalBitMap = bitmap;

                            urlConnection.disconnect();

                            Vector<EventHandler> ehv = eventHandlerTableVect.get(imageUUID);

                            new Handler(Looper.getMainLooper()).post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    for (EventHandler eh : ehv)
                                    {
                                        eh.finish(finalBitMap, null);
                                    }
                                }
                            });

                            eventHandlerTableVect.remove(imageUUID);
                        }
                    }
                }
            };

            thread.start();
        }
    }


}

