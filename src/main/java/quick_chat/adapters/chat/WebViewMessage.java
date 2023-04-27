package quick_chat.adapters.chat;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.pojo.chatContent.ChatMsg;
import com.pojo.chatContent.HTMLMsg;

import quick_chat.chat_utils.GeolocationUtilities;
import quick_chat.start.R;

public class WebViewMessage extends ChatMessage
{
    int[] evids = { R.id.emonView01, R.id.emonView02,R.id.emonView03,R.id.emonView04,R.id.emonView05,R.id.emonView06};
    int[] tvids = { R.id.imageViewText01, R.id.imageViewText02,R.id.imageViewText03,R.id.imageViewText04,R.id.imageViewText05,R.id.imageViewText06};


    @Override
    public View populateView( Context context, int position, View view, ChatMsg message )
    {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if ( view == null )
        {
            int resourceID = message.isIncoming() ? R.layout.chat_item_webview_in : R.layout.chat_item_webview_ou;

            view = inflater.inflate(resourceID, null);
        }

        HTMLMsg htmlMsg = (HTMLMsg)message.getContent();

        TextView txtInfo    = view.findViewById(R.id.txtInfo);
        String   msgTxt     = htmlMsg.getHtmlContent();

        {
            WebView web_view = view.findViewById(R.id.web_view);

            web_view.requestFocus();
            web_view.getSettings().setLightTouchEnabled(true);
            web_view.getSettings().setJavaScriptEnabled(true);
            web_view.getSettings().setGeolocationEnabled(true);
            web_view.setSoundEffectsEnabled(true);
            web_view.loadData( msgTxt, "text/html", "UTF-8" );

            //"<html><body>Hello, world!</body></html>"
        }

        String topText = GeolocationUtilities.dateFormat( context, message.getTime());

        setTopText(txtInfo, topText, message);

        if ( message.isIncoming() )
        {
            TextView txtMessage0 = view.findViewById(R.id.txtMessage0);

            txtMessage0.setText(message.getSenderName());
        }

        int[]   rections    = message.getMsgCore().getReactions();
        boolean visible     = false;

        for ( int i=0; i<evids.length; i++ )
        {
            View        emonView = view.findViewById( evids[i] );
            TextView    textView = view.findViewById( tvids[i] );

            if ( rections[i] == 0 )
            {
                emonView.setVisibility( View.GONE );
            }
            else
            {
                visible = true;
                emonView.setVisibility( View.VISIBLE );
                textView.setText( String.valueOf( rections[i] ) );
            }
        }

        System.out.println( msgTxt + " " + visible );

        view.findViewById( R.id.emonjis_layout ).setVisibility( visible ? View.VISIBLE : View.GONE);

        return view;
    }
}
