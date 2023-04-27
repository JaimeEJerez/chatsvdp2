package quick_chat.handlers;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pojo.ChatMessageCore;
import com.pojo.chatContent.ChatMsg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

import quick_chat.adapters.BaseNotificationAdapter;
import quick_chat.adapters.NotificationItem;
import quick_chat.chat_utils.MSGBoxBox;
import quick_chat.globals.Constants;
import quick_chat.services.TCPService;


public class NotificationsHandler
{
    private BaseNotificationAdapter notificationAdapter = null;

    private RecyclerView                                        messagesListView    = null;
    private int                                                 newMsgsCount        = 0;
    private boolean                                             storedMessagesMode  = false;
    private BaseNotificationAdapter.OnClickListener             onClickListener     = null;

    public NotificationsHandler( RecyclerView messagesListView,  boolean storedMessagesMode, BaseNotificationAdapter.OnClickListener onClickListener )
    {
        this.messagesListView       = messagesListView;
        this.storedMessagesMode     = storedMessagesMode;
        this.onClickListener        = onClickListener;
    }

    public static ArrayList<NotificationItem> calNotificationVector( )
    {
        ArrayList<NotificationItem> notifVect = new ArrayList<NotificationItem>();

        MSGBoxBox incomingMSGBoxBox = new MSGBoxBox(Constants.getInbDirFile());
        MSGBoxBox historicMSGBoxBox = new MSGBoxBox(Constants.getHysDirFile());

        Hashtable<String, MSGBoxBox.NotificationFilesInfo> histoMap = historicMSGBoxBox.getMsgsFile4Notification();
        Hashtable<String, MSGBoxBox.NotificationFilesInfo> incomMap = incomingMSGBoxBox.getMsgsFile4Notification();

        Hashtable<String, MSGBoxBox.NotificationFilesInfo> finalNotifMap = new Hashtable<String, MSGBoxBox.NotificationFilesInfo>();

        Enumeration<String>  hisKeyEnum = histoMap.keys();
        while ( hisKeyEnum.hasMoreElements() )
        {
            String key = hisKeyEnum.nextElement();
            MSGBoxBox.NotificationFilesInfo value = histoMap.get( key );
            finalNotifMap.put( key, value );
        }

        Enumeration<String>  incKeyEnum = incomMap.keys();
        while ( incKeyEnum.hasMoreElements() )
        {
            String key = incKeyEnum.nextElement();
            MSGBoxBox.NotificationFilesInfo value = incomMap.get( key );
            finalNotifMap.put( key, value );
        }

        Enumeration<String> finalNotKeys = finalNotifMap.keys();

        while ( finalNotKeys.hasMoreElements() )
        {
            String key = finalNotKeys.nextElement();

            MSGBoxBox.NotificationFilesInfo histoInfo = histoMap.get(key);
            MSGBoxBox.NotificationFilesInfo inboxInfo = incomMap.get(key);

            int historicMessagesCount = histoInfo == null ? 0 : histoInfo.filesCount;
            int incomminMessagesCount = inboxInfo == null ? 0 : inboxInfo.filesCount;

            {
                {
                    ChatMsg         cm          = inboxInfo != null ? inboxInfo.lastMessage : histoInfo.lastMessage;
                    boolean         incoming    = cm.isIncoming();
                    String  		name		= cm.getChatDiscriminationName();
                    String			userID		= incoming ? cm.getSenderID() : cm.getReceiverID();
                    long            msgID       = cm.getMsgCore().getId();
                    long  		    orderNbr	= cm.getOderID();
                    long            time        = cm.getTime();
                    String          message     = cm.getMsgTxt();

                    char senderType = cm.getMsgCore().getSenderType();
                    char receivType = cm.getMsgCore().getReceiverType();

                    NotificationItem.NotifKind notifKind = null;

                    NotificationItem notificationItem  = null;

                    if ( receivType == ChatMessageCore.MessageType.kGroupUser )
                    {
                        notifKind = NotificationItem.NotifKind.GROUP;

                        notificationItem = new NotificationItem(notifKind,
                                                                name,
                                                                orderNbr,
                                                                cm.getReceiverID(),
                                                                msgID,
                                                                time, message,
                                                                historicMessagesCount,
                                                                incomminMessagesCount,
                                                                false );
                    }
                    else
                    {
                        if (senderType == ChatMessageCore.MessageType.kSingleUser)
                        {
                            notifKind = NotificationItem.NotifKind.USER;
                        }

                        if (senderType == ChatMessageCore.MessageType.kGroupUser)
                        {
                            notifKind = NotificationItem.NotifKind.GROUP;
                        }

                        notificationItem = new NotificationItem(notifKind,
                                                                name,
                                                                orderNbr,
                                                                userID,
                                                                msgID,
                                                                time, message,
                                                                historicMessagesCount,
                                                                incomminMessagesCount,
                                                                false );

                    }

                    notifVect.add(notificationItem);
                }
            }
        }

        Collections.sort( notifVect, new Comparator<NotificationItem>()
        {
            @Override
            public int compare(NotificationItem lhs, NotificationItem rhs)
            {
                if ( lhs.time == rhs.time )
                {
                    return lhs.name.compareTo( rhs.name );
                }
                else
                {
                    return lhs.time < rhs.time ? 1 : -1;
                }
            }
        });

        return notifVect;
    }

    public void onCreate( Context context)
    {
        ArrayList<NotificationItem> notifVect = calNotificationVector( );

        notificationAdapter = new BaseNotificationAdapter( notifVect, onClickListener );

        messagesListView.setAdapter( notificationAdapter );

        messagesListView.setLayoutManager(new LinearLayoutManager(context));
    }

    public void onResume()
    {
    }

    NotificationEventHandler notificationEventHandler = null;

    public static abstract class  NotificationEventHandler
    {
        public abstract void notify( Message message );
    }

    public void addNotificationEventHandler( NotificationEventHandler notificationEventHandler )
    {
        this.notificationEventHandler = notificationEventHandler;
    }

    public void clearNotificationEventHandler()
    {
        notificationEventHandler = null;
    }

    public void handleServiceMessage( final Message msg )
    {
        switch (msg.what)
        {
            case TCPService.MSG_OUTBOX_EVENT:
            break;

            case TCPService.MSG_INBOX_EVENT:
            {
                if ( notificationEventHandler != null )
                {
                    notificationEventHandler.notify( msg );
                }

                ArrayList<NotificationItem> notifVect = calNotificationVector(  );

                notificationAdapter.setNotifVect( notifVect );

                new Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        messagesListView.invalidate();
                   }
                });

                break;
            }
        }
    }


}
