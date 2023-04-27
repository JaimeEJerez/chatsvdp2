package quick_chat.services;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import androidx.room.Room;

import com.pojo.ChatMessageCore;
import com.pojo.chatContent.ChatMsg;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import quick_chat.actvt.NotificationsActvt;
import quick_chat.adapters.BaseNotificationAdapter;
import quick_chat.adapters.NotificationItem;
import quick_chat.chat_utils.MSGBoxBox;
import quick_chat.db.AppDatabase;
import quick_chat.db.User;
import quick_chat.db.UserDao;
import quick_chat.globals.Constants;
import quick_chat.handlers.NotificationsHandler;
import quick_chat.start.R;


public class InboxThread extends IOSocked
{
//10.201.1.66
    public InboxThread(Service service, String serverIP, int port, ArrayList<Messenger>	mClients)
    {
        super( serverIP, port, service, mClients );
    }

    private long						lastPlayTime 		= 0;
    private boolean                     isConected          = false;

    private void showConnectionSatatus( boolean active )
    {
        if ( isConected != active )
        {
            isConected = active;

            if (mClients != null)
            {
                synchronized (mClients)
                {
                    for (Messenger mClient : mClients)
                    {
                        try
                        {
                            mClient.send(Message.obtain(null, TCPService.MSG_CONECTION_EVENT, active));
                        }
                        catch (RemoteException e)
                        {
                        }
                    }
                }
            }
        }
    }

    private boolean sameUserRegistered(Context context, String uuuid )
    {
        final User      user    = Constants.getUser( context );

        return user != null && user.uuuid.equals(uuuid);
    }

    private void notifyUI( ChatMsg chatMsg, boolean newMessage ) throws RemoteException
    {
        if ( mClients != null )
        {
            synchronized (mClients)
            {
                if ( newMessage )
                {
                    for (Messenger mClient : mClients)
                    {
                        mClient.send(Message.obtain(null, TCPService.MSG_INBOX_EVENT, chatMsg));
                    }
                }
                else
                {
                    for (Messenger mClient : mClients)
                    {
                        mClient.send(Message.obtain(null, TCPService.MSG_HISTBOX_EVENT, chatMsg ) );
                    }
                }
            }
        }
    }

    private void notifySituation( String message ) throws RemoteException
    {
        if ( mClients != null )
        {
            synchronized (mClients)
            {
                    for (Messenger mClient : mClients)
                    {
                        mClient.send(Message.obtain(null, TCPService.MSG_INBOX_SITUATION, message));
                    }
            }
        }
    }

    @Override
    public void run()
    {
        for (;;)
        {
            try
            {
                User user = null;

                while(  (user = Constants.getUser(getService())) == null )
                {
                    sleep( 1000);
                };

                String 	registryUUID  		= user.uuuid;
                String 	name  		        = user.getVisibleName();
                String  kind                = user.kind;

                MSGBoxBox  incomingMSGBoxBox = new MSGBoxBox( Constants.getInbDirFile() );
                MSGBoxBox  historyMSGBoxBox  = new MSGBoxBox( Constants.getHysDirFile() );

                if ( openSocket() )
                {
                    File histFile = Constants.getHysDirFile();

                    String wellcome = dis.readUTF();

                    if ( wellcome.contains( "WELLCOME" ))
                    {
                        dos.writeUTF( registryUUID );
                        dos.writeUTF( name );
                        dos.flush();

                        String firstCommand = dis.readUTF();

                        if (firstCommand.equalsIgnoreCase("THIS_USER_IS_ONLINE"))
                        {
                            //Constants.clearUser( getService() );

                            notifySituation( "THIS_USER_IS_ONLINE" );

                            continue;
                        }

                        if (firstCommand.equalsIgnoreCase("BEGIN_HISTORY"))
                        {
                            dos.writeUTF("HISTORY_STATUS_BEGIN" );

                            ArrayList<NotificationItem> notifItmsArr = NotificationsHandler.calNotificationVector();

                            for ( NotificationItem ni : notifItmsArr )
                            {
                                dos.writeUTF("ITEM" );
                                dos.writeUTF(ni.userID );
                                dos.writeLong(ni.msgUID );
                            }

                            dos.writeUTF("HISTORY_STATUS_END" );
                        }

                        if (firstCommand.equalsIgnoreCase("BEGIN_HISTORY"))
                        {
                            ChatMsg lastMessage = null;

                            String command = null;

                            while ((command = dis.readUTF()).equalsIgnoreCase("MSG"))
                            {
                                ChatMessageCore     msgCore     = (ChatMessageCore) ChatMessageCore.fromJSON(dis);
                                String              senderID    = msgCore.getSenderID();
                                boolean             isIncomming = !senderID.endsWith(registryUUID);
                                ChatMsg chatMsg = new ChatMsg(msgCore, isIncomming, false, true);

                                incomingMSGBoxBox.pushMessage(chatMsg);

                                if (lastMessage != null)
                                {
                                    String oldRec = lastMessage.getSenderID();
                                    String newRec = chatMsg.getSenderID();
                                    if (!oldRec.equalsIgnoreCase(newRec))
                                    {
                                        notifyUI(lastMessage, true);
                                    }
                                }
                                lastMessage = chatMsg;
                            }

                            playNotificationSound();
                        }

                        for(;;)
                        {
                            showConnectionSatatus( true );

                            String command = dis.readUTF();

                            if (  !sameUserRegistered( getService(), registryUUID ) )
                            {
                                dos.writeUTF("QUIT");
                                dos.flush();
                               // mClients.clear();
                                break;
                            }

                            if ( command.startsWith("ERROR") )
                            {
                                throw new IOException( command );
                            }
                            else
                            if ( command.equalsIgnoreCase("TIME") )
                            {
                                dos.writeUTF("AKN");
                                dos.flush();
                            }
                            else
                            if ( command.startsWith("MSG") )
                            {
                                boolean hightPriority = false;

                                ChatMessageCore msgCore       = (ChatMessageCore)ChatMessageCore.fromJSON( dis );
                                String          senderID      = msgCore.getSenderID();
                                boolean         isIncomming   = !senderID.endsWith( registryUUID );
                                ChatMsg         chatMsg       = new ChatMsg( msgCore, isIncomming, false, true );
                                String          uuid          = chatMsg.getMSGuid();
                                boolean         soundLoopping = false;

                                File inboxDirectory = new File( Constants.getInbDirFile(), chatMsg.calcMSGFileName() );
                                File hystoDirectory = new File( Constants.getHysDirFile(), chatMsg.calcMSGFileName() );

                                if ( inboxDirectory.exists() )
                                {
                                    System.out.println( "Exist in incomingMSGBoxBox" );

                                    incomingMSGBoxBox.pushMessage( chatMsg );

                                    notifyUI( chatMsg, false );

                                    playNotificationSound();

                                    dos.writeUTF("AKN");
                                    dos.flush();
                                }
                                else
                                if ( hystoDirectory.exists() )
                                {
                                    System.out.println( "Exist in historyMSGBoxBox" );

                                    historyMSGBoxBox.pushMessage(chatMsg);

                                    notifyUI( chatMsg, false );

                                    playPingSound();

                                    dos.writeUTF("AKN");
                                    dos.flush();
                                }
                                else
                                {
                                    System.out.println( "New message" );

                                    int 		unreadMessages 		= incomingMSGBoxBox.getSize();
                                    boolean 	stayInScreenNotif	= false;
                                    Class<?>	callingBack			= NotificationsActvt.class;
                                    String		messageTxt 			= getService().getString(R.string.new_message);

                                    if ( unreadMessages > 1 )
                                    {
                                        messageTxt 		= "Hay " + unreadMessages + " nuevos mensajes...";
                                    }

                                    if ( System.currentTimeMillis() - lastPlayTime > 3000 )
                                    {
                                        lastPlayTime = System.currentTimeMillis();

                                        playNotificationSound();
                                    }

                                    chatMsg.updateTime();

                                    incomingMSGBoxBox.pushMessage(chatMsg);

                                    notifyUI( chatMsg, true );

                                    Notification notification = ((TCPService)getService()).createNotification( messageTxt, this.getService(), stayInScreenNotif, callingBack);

                                    getService().startForeground( 1525, notification);
                                }

                                dos.writeUTF("AKN");
                                dos.flush();
                            }
                        }
                    }
                    else
                    {
                        throw new Exception( "NOT WELCOMME FOUND");
                    }
                }
                else
                {
                    try
                    {
                        sleep( 7000);
                    }
                    catch (InterruptedException interruptedException)
                    {}
                }
            }
            catch ( Exception e )
            {
                e.printStackTrace();

                try
                {
                    sleep( 7000);
                }
                catch (InterruptedException interruptedException)
                {}
            }

            finally
            {
                //mClients.clear();
                showConnectionSatatus( false );
                this.closeSocket();
            }
        }
    }


    public void logOut()
    {
        this.closeSocket();
    }

}
