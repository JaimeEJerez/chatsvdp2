package quick_chat.start;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.StrictMode;

import java.io.File;

import quick_chat.globals.Constants;
import quick_chat.services.TCPService;
import quick_chat.start.R;

public class StartApp extends Application
{
    protected static final String 		kAppName 		= "QuickChat";
    protected static final String 		kMessage 		= "Servicio en ejecución...";


    private static final String   CHANNEL_ID  = "QuickChat_001";
    private static Context  appContext;
    private static StartApp instance    = null;
    private static String   packsgeName = null;
    private static       File     appFilesDir = null;
    private static       File     extFilesDir = null;

    protected  static   ServiceConnection   mConnection = null;
    protected  static   Messenger           mService    = null;

    protected final Messenger mMessenger = new Messenger(new IncomingHandler());

    public  class IncomingHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
        }
    }

    public StartApp()
    {
    }

    public static StartApp getInstance()
    {
        return instance;
    }

    public static void logOutServices()
    {
        if ( instance != null )
        {
            Message msg = Message.obtain(null, TCPService.MSG_LOGOUT );

            try
            {
                mService.send(msg);
            }
            catch (RemoteException e)
            {
                e.printStackTrace();
            }
        }
    }
    public static File getAppFilesDir()
    {
        return appFilesDir;
    }

    public static File getExtFilesDir()
    {
        return extFilesDir;
    }

    public static boolean doStartService(final Context context, final Messenger msgr)
    {
        if ( mService != null )
        {
            return true;
        }

        mConnection = new ServiceConnection()
        {
            public void onServiceConnected(ComponentName className, IBinder service)
            {
                mService = new Messenger(service);

                try
                {
                    Message msg = Message.obtain( null, TCPService.MSG_START_SERVICE);

                    msg.replyTo = msgr;

                    Bundle b = new Bundle();

                    b.putString( "SERVER_IP", Constants.kAPIEntryPoint);
                    b.putInt( "OUTBOX_PORT", Constants.kOutboxPort );
                    b.putInt( "INBOX_PORT", Constants.kInboxPort );
                    b.putString( "APP_NAME", kAppName );
                    b.putString( "MESSAGE", kMessage );

                    msg.setData(b);

                    mService.send(msg);
                }
                catch (RemoteException e)
                {}
            }

            public void onServiceDisconnected(ComponentName className)
            {
            }
        };

        return context.bindService( new Intent( context, TCPService.class ), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        instance = this;

        packsgeName = getApplicationContext().getPackageName();
        extFilesDir = new File( getFilesDir(), "DATA_V01" );
        appFilesDir = getFilesDir();

        if (!extFilesDir.exists())
        {
            extFilesDir.mkdir();
        }

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();

        StrictMode.setVmPolicy(builder.build());

        appContext = getApplicationContext();

        createNotificationChannel();

        doStartService( this.getApplicationContext(), mMessenger );
    }

    public static Context getAppContext()
    {
        return appContext;
    }

    public static String getStringFromMetaData( String name )
    {
        try
        {
            ApplicationInfo ai = instance.getPackageManager().getApplicationInfo(packsgeName, PackageManager.GET_META_DATA);

            Bundle aBundle =ai.metaData;

            String aValue = aBundle.getString( name );

            return aValue;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            return null;
        }
    }

    private void createNotificationChannel()
    {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            CharSequence        name        = getString(R.string.channel_name);
            String              description = getString(R.string.channel_description);
            NotificationChannel channel     = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static  Messenger getMassager()
    {
        return mService;
    }

    public static String getCHANNEL_ID()
    {
        return CHANNEL_ID;
    }
}
