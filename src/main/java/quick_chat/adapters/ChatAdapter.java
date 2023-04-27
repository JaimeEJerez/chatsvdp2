package quick_chat.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.pojo.chatContent.ChatMsg;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;

import quick_chat.adapters.chat.ChatMessage;
import quick_chat.chat_utils.MSGBoxBox;

public class ChatAdapter extends BaseAdapter
{
    private     MSGBoxBox                       hystoricMSGBoxBox   = null;
    public      Context                         context             = null;
    //protected   TreeMap<String, ChatMsg>        chatMessages        = null;
    protected   ArrayList<ChatMsg>              messagesVect        = null;
    protected   Object[]                        keyArray            = null;
    private     String                          targetName          = null;
    private     String                          targetID            = null;
    private     Hashtable<String,Integer>       typeViewMap         = new Hashtable<String,Integer>();
    private     int                             typeViewCnt         = 0;

    public ChatAdapter( final Context               context,
                        final String                targetID,
                        final String                targetName,
                        final MSGBoxBox             hystoricMSGBoxBox,
                        final ArrayList<ChatMsg>    messagesVect)
    {
        sort();

        this.messagesVect       = messagesVect;
        this.context            = context;
        this.targetName         = targetName;
        this.targetID           = targetID;
        this.hystoricMSGBoxBox 	= hystoricMSGBoxBox;
    }

    @Override
    public int getCount()
    {
        int count = messagesVect == null ? 0 : messagesVect.size();

        return count;
        /*
        if (chatMessages != null)
        {
            if ( keyArray == null || chatMessages.size() != keyArray.length )
            {
                keyArray = chatMessages.keySet().toArray();

                return keyArray.length;
            }
            else
            {
                return chatMessages.size();
            }
        }
        else
        {
            return 0;
        }*/
    }

    @Override
    public ChatMsg getItem(int position)
    {
        return messagesVect == null ? null : messagesVect.get( position );

        /*
        if ( chatMessages != null )
        {
            if ( position <= keyArray.length )
            {
                Object key = keyArray[position];

                return chatMessages.get( key );
            }
        }*/
    }


    @Override
    public long getItemId(int position)
    {
        return position;
    }


    @Override
    public View getView(final int position, View convertView,  final ViewGroup parent)
    {
        final ChatMsg chatMsg = getItem( position );

        if (chatMsg == null )
        {
            return null;
        }

        if ( chatMsg.isToRead() )
        {
            ChatMessage.setFirstUnreadMessageTime( chatMsg.getTime() );

            chatMsg.deleteFile();

            chatMsg.setToRead( false );

            hystoricMSGBoxBox.pushMessage( chatMsg );
        }

        String chatItemClass = chatMsg.getChatContentClass();

        try
        {
            Class<?> theClass = Class.forName(chatItemClass);

            Constructor<?> theConstructor = theClass.getConstructor();

            Object theInstance = theConstructor.newInstance();

            convertView = ((ChatMessage)theInstance).populateView(context, position, convertView, chatMsg);
        }
        catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e)
        {
            e.printStackTrace();
        }

        return convertView;
    }

    public void sort()
    {
        if ( messagesVect == null || messagesVect.size() < 2 )
        {
            return;
        }

        Collections.sort( messagesVect, new Comparator<ChatMsg>()
        {
            @Override
            public int compare(ChatMsg o1, ChatMsg o2)
            {
                if ( o1==null || o2==null )
                {
                    return 0;
                }

                if ( o2.getTime() == o1.getTime() )
                {
                    return 0;
                }

                return o2.getTime() > o1.getTime()  ? -1 : 1;
            }
        });
    }

    public void add( String name, ChatMsg message)
    {
        messagesVect.add( message );

        sort();
    }

    @Override
    public void notifyDataSetChanged()
    {
        super.notifyDataSetChanged();
    }

    public void updateMessageFromTimeName(File f )
    {
        ChatMsg chatMsg2 = MSGBoxBox.loadMessage( f );

        String fName1 = chatMsg2.calcMSGFileNameFromTime();

        if ( messagesVect != null )
        {
            for (int i=0; i<messagesVect.size(); i++ )// (ChatMsg cm : messagesVect)
            {
                ChatMsg chatMsg1 = messagesVect.get( i );

                String fName2 = chatMsg1.getFileName();

                if ( fName1.equalsIgnoreCase( fName2 ) )
                {
                    messagesVect.set( i, chatMsg2 );

                    break;
                }
            }
        }
    }

    public void updateMessageFromTimeName(ChatMsg chatMsg1 )
    {
        String fName1 = chatMsg1.getFileName();

        System.out.println( "****" + fName1 + " " + chatMsg1.getMsgTxt() );

        if ( messagesVect != null )
        {
            for (int i=0; i<messagesVect.size(); i++ )// (ChatMsg cm : messagesVect)
            {
                ChatMsg chatMsg2 = messagesVect.get( i );

                String fName2 = chatMsg2.getFileName();

                System.out.println( fName2 + " " + chatMsg2.getMsgTxt() );

                if ( fName1.equalsIgnoreCase( fName2 ) )
                {
                    messagesVect.set( i, chatMsg1 );

                    break;
                }
            }
        }
    }


    @Override
    public int getViewTypeCount()
    {
        return 256;
    }

    @Override
    public int getItemViewType(int position)
    {
        ChatMsg chatMsg = getItem(position);

        String ccc = chatMsg.getChatViewType();

        Integer viewType = typeViewMap.get( ccc  );

        if ( viewType == null )
        {
            typeViewCnt++;

            typeViewMap.put( ccc, typeViewCnt );

            viewType = typeViewCnt;
        }

        return viewType;
    }

    /**
     * @return the targetName
     */
    public String getTargetName()
    {
        return targetName;
    }

    public void setParameters(String destinationID, String destinationName, ArrayList<ChatMsg> messagesVect)
    {
        this.targetID       = destinationID;
        this.targetName     = destinationName;
        this.messagesVect   = messagesVect;

        sort();

        notifyDataSetChanged();
    }
}
