package quick_chat.services;

import android.app.Service;
import android.content.Context;
import android.os.Message;
import android.os.Messenger;

import androidx.room.Room;

import com.pojo.ChatMessageCore;
import com.pojo.chatContent.ChatMsg;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import quick_chat.Utils;
import quick_chat.adapters.chat.ChatMessage;
import quick_chat.chat_utils.MSGBoxBox;
import quick_chat.db.User;
import quick_chat.globals.Constants;

public class OutboxThread extends IOSocked
{
    private long   		        lastTime   		= System.currentTimeMillis();

    public OutboxThread( Service service, String serverIP, int port, ArrayList<Messenger> mClients )
    {
        super( serverIP, port, service, mClients );
    }

    private boolean sameUserRegistered( Context context, String uuuid )
    {
        final User      user    = Constants.getUser( context );

        return user != null && user.uuuid.equals(uuuid);
    }

    @Override
    public void run()
    {
        User user = null;

        for (;;)
        {
            try
            {
                while ( (user = Constants.getUser( getService() ) ) == null )
                {
                    sleep( 1000);
                }

                String 	registryUUID  = user.uuuid;
                String 	registryName  = user.getVisibleName();

                if ( openSocket() )
                {
                    String welcome = dis.readUTF();

                    if ( welcome.contains("WELLCOME"))
                    {
                        dos.writeUTF(registryUUID);
                        dos.writeUTF(registryName);

                        MSGBoxBox outgoingMSGBoxBox = new MSGBoxBox( Constants.getOutDirFile() );

                        dos.flush();

                        while ( sameUserRegistered( getService(), registryUUID )  )
                        {
                            ArrayList<File> inboxMsgFiles = new ArrayList<File>();

                            outgoingMSGBoxBox.getMsgsFileList(  null, inboxMsgFiles );

                            if ( inboxMsgFiles.size()  > 0 )
                            {
                                for ( File inboxMsgFile : inboxMsgFiles )
                                {
                                    ChatMsg inboxMsg = MSGBoxBox.loadMessage( inboxMsgFile );

                                    if ( inboxMsg != null )
                                    {
                                        ChatMessageCore inboxMsgCore = inboxMsg.getMsgCore();

                                        ChatMessageCore.BinaryPayload binariPayload = inboxMsgCore.getBinaryPayload();

                                        if ( binariPayload != null )
                                        {
                                            byte[] data = getBinariPayloadData( binariPayload );

                                            if ( data != null )
                                            {
                                                dos.writeUTF("BINARY");
                                                dos.writeUTF(binariPayload.name);
                                                dos.writeUTF(binariPayload.type);
                                                dos.writeInt( data.length );
                                                //dos.write( data );

                                                long    lastTime    = 0;
                                                int     counter     = 0;
                                                int     progress    = 0;

                                                for ( byte b : data )
                                                {
                                                    if ( System.currentTimeMillis() - lastTime > 100 )
                                                    {
                                                        lastTime =  System.currentTimeMillis();
                                                        progress = (counter * 100)/data.length;

                                                        ChatMessage.SendingProgress sendingProgress = new ChatMessage.SendingProgress( inboxMsgCore.getMSGuid(), progress );

                                                        synchronized (mClients)
                                                        {
                                                            for (Messenger mClient : mClients)
                                                            {
                                                                mClient.send( Message.obtain(null, TCPService.MSG_SENDING_EVENT, sendingProgress ) );
                                                            }
                                                        }
                                                    }

                                                    dos.write( b );

                                                    counter++;
                                                }

                                                synchronized (mClients)
                                                {
                                                    ChatMessage.SendingProgress sendingProgress = new ChatMessage.SendingProgress( inboxMsgCore.getMSGuid(), 100 );

                                                    for (Messenger mClient : mClients)
                                                    {
                                                        mClient.send( Message.obtain(null, TCPService.MSG_SENDING_EVENT, sendingProgress ) );
                                                    }
                                                }

                                                dos.flush();
                                            }
                                        }

                                        dos.writeUTF("MSG");

                                        inboxMsgCore.toJSON( dos );

                                        dos.flush();

                                        String received = dis.readUTF();

                                        if ( received.startsWith("AKN") )
                                        {
                                            inboxMsg.setToSend( false );

                                            long msgID = Long.valueOf( received.substring( 4 ) );

                                            inboxMsgCore.setId( msgID );

                                            MSGBoxBox hystoricMSGBoxBox	= new MSGBoxBox( Constants.getHysDirFile() );

                                            File file = hystoricMSGBoxBox.pushMessage( inboxMsg );

                                            inboxMsgFile.delete();

                                            synchronized ( mClients )
                                            {
                                                for ( Messenger mClient :mClients )
                                                {
                                                    mClient.send( Message.obtain( null, TCPService.MSG_OUTBOX_EVENT, file) );
                                                }
                                            }

                                            playNotificationSound();

                                            lastTime = System.currentTimeMillis();
                                        }
                                    }
                                }
                            }
                            else
                            {
                                if ( System.currentTimeMillis()-lastTime > 1000L )
                                {
                                    dos.writeUTF( "TIME" );
                                    dos.flush();

                                    String response = dis.readUTF();

                                    lastTime = System.currentTimeMillis();

                                    if ( !response.equalsIgnoreCase("AKN") )
                                    {
                                        throw new Exception("Bad aknowledge received in OutboxThread " + response );
                                    }
                                }

                                sleep(1000);
                            }
                        }

                        dos.writeUTF("QUIT");
                        dos.flush();
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
                this.closeSocket();
            }
        }
    }

    private byte[] getBinariPayloadData(ChatMessageCore.BinaryPayload binariPayload) throws IOException
    {
        if ( binariPayload.type.equalsIgnoreCase( "STATIC_IMAGE" ) )
        {
            return Utils.bytes_from_DIRECTORY_PICTURES( getService().getBaseContext(), binariPayload.name );
        }
        else if ( binariPayload.type.equalsIgnoreCase( "DINAMIC_IMAGE" ) )
        {

        }
        else if ( binariPayload.type.equalsIgnoreCase( "AUDIO" ) )
        {
            return Utils.bytes_from_DIRECTORY_AUDIO( getService().getBaseContext(), binariPayload.name );
        }

        return null;
    }

    public void logOut()
    {
        this.closeSocket();
    }
}
