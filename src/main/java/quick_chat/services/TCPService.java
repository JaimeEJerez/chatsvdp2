package quick_chat.services;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;

import quick_chat.actvt.NotificationsActvt;
import quick_chat.globals.Constants;
import quick_chat.start.R;
import quick_chat.start.StartApp;

public class TCPService extends Service
{
    public static final int ONGOING_NOTIFICATION_ID		= 1777;

    public static final int MSG_START_SERVICE 			= 0;
    public static final int MSG_REGISTER_CLIENT 		= 1;
    public static final int MSG_UNREGISTER_CLIENT 		= 2;
    public static final int MSG_SET_VALUE 				= 3;
    public static final int MSG_INBOX_EVENT 			= 4;
    public static final int MSG_HISTBOX_EVENT 			= 5;
    public static final int MSG_OUTBOX_EVENT 			= 6;
    public static final int MSG_SENDING_EVENT 			= 7;
    public static final int MSG_CONECTION_EVENT 		= 8;
    public static final int MSG_LOGOUT 			        = 9;
    public static final int MSG_INBOX_SITUATION 		= 10;

    final       Messenger                   mMessenger = new Messenger(new IncomingHandler());
    private     ArrayList<Messenger>        mClients   = new ArrayList<Messenger>();

    private static InboxThread  inboxThread  = null;
    private static OutboxThread outboxThread = null;

    @Override
    public void onDestroy()
    {
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return mMessenger.getBinder();
    }

    class IncomingHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_LOGOUT:
                {
                    inboxThread.logOut();
                    outboxThread.logOut();
                    break;
                }
                case MSG_START_SERVICE:
                {
                    Bundle b = msg.getData();

                    if (b != null)
                    {
                        String appName      = b.getString("APP_NAME");
                        String message      = b.getString("MESSAGE");

                        inboxThread = new InboxThread( TCPService.this, Constants.kTCPIPAddress, Constants.kInboxPort , mClients);
                        inboxThread.start();

                        outboxThread = new OutboxThread(TCPService.this, Constants.kTCPIPAddress, Constants.kOutboxPort , mClients);
                        outboxThread.start();

                        //Start Foreground
                        {
                            Intent notificationIntent = new Intent( getBaseContext(), NotificationsActvt.class );

                            Notification notification = createNotification( "Servicio activo", TCPService.this, false, NotificationsActvt.class);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                            {
                                startForeground(1525, notification, FOREGROUND_SERVICE_TYPE_LOCATION);
                            }
                            else
                            {
                                startForeground(1525, notification);
                            }
                        }

                    }

                    synchronized (mClients)
                    {
                        mClients.clear();

                        mClients.add(msg.replyTo);
                    }

                    break;
                }
                case MSG_REGISTER_CLIENT:
                    synchronized (mClients)
                    {
                        mClients.add( msg.replyTo );
                    }
                    break;
                case MSG_UNREGISTER_CLIENT:
                    synchronized (mClients)
                    {
                        mClients.remove( msg.replyTo );
                    }
                    break;
                case MSG_SET_VALUE:
                {
                    int mValue = msg.arg1;
                    synchronized (mClients)
                    {
                        for (Messenger mc : mClients)
                        {
                            if (mc != null)
                            {
                                try
                                {
                                    mc.send(Message.obtain(null, MSG_SET_VALUE, mValue, 0));
                                }
                                catch (RemoteException e)
                                {
                                    //mc.remove(i);
                                    Log.e(getClass().getName(), e.getMessage());
                                }
                            }
                        }
                    }
                    break;
                }
                default:
                    super.handleMessage(msg);
            }
        }
    }

    public  Notification createNotification( final  String message, final Service service, boolean stayInScreen, Class<?> clickCallClass  )
    {
        Notification notification = null;

        ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);
        boolean isInForeground = appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE;

        String app_name = service.getString(R.string.app_name);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            if ( stayInScreen && ! isInForeground )
            {
                Intent notificationIntent = new Intent(getBaseContext(), clickCallClass );

                PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, notificationIntent, FLAG_IMMUTABLE);

                notification = new Notification.Builder(getBaseContext(), StartApp.getCHANNEL_ID())
                        .setOngoing(true)
                        .setContentTitle("quick_chat")
                        .setContentText(message)
                        .setTimeoutAfter( 60000 )
                        .setSmallIcon(R.drawable.chat_icon)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setFullScreenIntent(pendingIntent, true)
                        .setVisibility(Notification.VISIBILITY_PUBLIC).build();
            }
            else
            {
                Intent notificationIntent = new Intent(getBaseContext(), clickCallClass );

                PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, notificationIntent, FLAG_IMMUTABLE);

                notification = new Notification.Builder(getBaseContext(), StartApp.getCHANNEL_ID())
                                                                                .setContentTitle( app_name )
                                                                                .setContentText(message)
                                                                                .setContentIntent( pendingIntent )
                                                                                .setSmallIcon(R.drawable.chat_icon).build();
            }
        }
        else
        {
            NotificationCompat.Builder builder = new NotificationCompat.Builder( service );

            notification = builder.setContentTitle( app_name ).setContentText( message ).setSmallIcon(R.drawable.chat_icon).build();

            notification.flags |= Notification.FLAG_NO_CLEAR;
        }

        notification.when = System.currentTimeMillis();

        return notification;
    }

}
