package quick_chat.adapters.chat;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.pojo.chatContent.ChatMsg;


public abstract class ChatMessage
{
    public static class SendingProgress
    {
        public String  uuid        = null;
        public int     progress    = 0;

        public SendingProgress(  String  uuid, int    progress )
        {
            this.uuid       = uuid;
            this.progress   = progress;
        }
    }

    private static long             firstUnreadMessageTime  =   0;
    private static SendingProgress  sendingProgress         =   null;

    public ChatMessage()
    {
    }

    protected void setTopText(TextView txtInfo, String topText, ChatMsg chatMsg)
    {
        if ( firstUnreadMessageTime != 0 && !chatMsg.isToSend() && chatMsg.getTime() >= firstUnreadMessageTime  )
        {
            txtInfo.setTextColor( Color.RED);
        }
        else
        {
            txtInfo.setTextColor( Color.DKGRAY );
        }

        txtInfo.setText( topText );
    }

    public abstract View populateView( Context context, int position, View view, ChatMsg message );

    public static void setFirstUnreadMessageTime( long firstUnreadMessageTime )
    {
        ChatMessage.firstUnreadMessageTime = firstUnreadMessageTime;
    }

    public static void setSendingProgress( SendingProgress sp )
    {
        sendingProgress = sp;
    }

    public static SendingProgress getSendingProgress()
    {
        return sendingProgress;
    }
}
