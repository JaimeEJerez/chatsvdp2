package quick_chat.adapters.chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.pojo.chatContent.ChatMsg;
import com.pojo.chatContent.StaticImageMsg;

import java.io.ByteArrayInputStream;

import quick_chat.Utils;
import quick_chat.chat_utils.GeolocationUtilities;
import quick_chat.dialogs.Show_iamge_dialog;

import quick_chat.start.R;

public class StaticImageMessage extends ChatMessage
{
    public StaticImageMessage()
    {
    }

    private static byte charToByte(char c)
    {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static byte[] hexStringToBytes(String hexString)
    {
        if (hexString == null || hexString.equals(""))
        {
            return null;
        }

        hexString = hexString.toUpperCase();

        int length = hexString.length() / 2;

        char[] hexChars = hexString.toCharArray();

        byte[] d = new byte[length];

        for (int i = 0; i < length; i++)
        {
            int pos = i * 2;

            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }

        return d;
    }

    private static byte[] fromHexString(final String encoded)
    {
        if ((encoded.length() % 2) != 0) {
            throw new IllegalArgumentException("Input string must contain an even number of characters");
        }

        final byte result[] = new byte[encoded.length()/2];
        final char enc[] = encoded.toCharArray();
        for (int i = 0; i < enc.length; i += 2)
        {
            StringBuilder curr = new StringBuilder(2);
            curr.append(enc[i]).append(enc[i + 1]);
            result[i/2] = (byte) Integer.parseInt(curr.toString(), 16);
        }

        return result;
    }

    private static int dpToPx(int dp, Context context)
    {
        float density = context.getResources().getDisplayMetrics().density;

        return Math.round((float) dp * density);
    }

    @Override
    public View populateView( Context context, int position, View view, ChatMsg message )
    {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if ( view == null )
        {
            int resourceID = message.isIncoming() ? R.layout.chat_item_static_image_in : R.layout.chat_item_static_image_ou;

            view = inflater.inflate(resourceID, null);
        }

        TextView                    txtMessage  = view.findViewById(R.id.txtMessage);
        TextView                    txtInfo     = view.findViewById(R.id.txtInfo);
        ImageView                   imageView   = view.findViewById( R.id.imageView45 );
        CircularProgressIndicator   progressBar = view.findViewById( R.id.progressBar5);

        StaticImageMsg sim         = (StaticImageMsg)message.getMsgCore().getContent();

        final String   msgTxt     = sim.getMsgTxt();
        final String   imgName    = sim.getImageName();
        final int      height     = sim.getImageHeight();
        final int      wide       = sim.getImageWide();
        final String   jpegHex    = sim.getSmallImage();

        final float scale = context.getResources().getDisplayMetrics().density * 1.3f;

        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();

        layoutParams.width  = Math.round(wide * scale );
        layoutParams.height = Math.round(height * scale );
        imageView.setLayoutParams(layoutParams);

        if ( msgTxt == null || msgTxt.isEmpty() )
        {
            txtMessage.setText("");

            txtMessage.setVisibility( View.GONE );
        }
        else
        {
            txtMessage.setText(msgTxt);

            txtMessage.setVisibility( View.VISIBLE );
        }

        if ( imgName != null )
        {
            Bitmap bitmap = Utils.bitmap_from_DIRECTORY_PICTURES( context, imgName );

            if ( bitmap == null )
            {
                byte[] imageBuff = hexStringToBytes( jpegHex );

                bitmap = BitmapFactory.decodeByteArray(imageBuff, 0, imageBuff.length);

                /*
                RetiveImagesFromRepository dir = new RetiveImagesFromRepository( imgUUID );

                dir.execute( context, new RetiveImagesFromRepository.EventHandler()
                {
                    @Override
                    public void finish( Bitmap bitmap, String error)
                    {
                        if ( error == null && bitmap != null )
                        {
                            imageView.setImageBitmap( bitmap );
                        }
                    }
                });
                 */
            }

            if ( bitmap != null )
            {
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();

                double hypo = Math.hypot(w, h);
                double fact = 256.0 / hypo;

                w = (int) Math.round(w * fact);
                h = (int) Math.round(h * fact);

                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) imageView.getLayoutParams();
                params.width = dpToPx(w, context);
                params.height = dpToPx(h, context);
                imageView.setLayoutParams(params);
            }

            imageView.setImageBitmap( bitmap );

            {
                SendingProgress sendingProgress = ChatMessage.getSendingProgress();

                if (sendingProgress != null && sendingProgress.uuid.equalsIgnoreCase( message.getSenderID() ) )
                {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setMax(100);
                    progressBar.setProgress(sendingProgress.progress);
                }
                else
                {
                    if ( message.isToSend() )
                    {
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.setMax(100);
                        progressBar.setProgress(0);
                    }
                    else
                    {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }
        }

        String topText = GeolocationUtilities.dateFormat( context, message.getTime() );

        setTopText(txtInfo, topText, message);

        final AppCompatActivity  activity = (AppCompatActivity)context;
        
        imageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Show_iamge_dialog dialog = Show_iamge_dialog.newInstance( message.getSenderID(), imgName );

                dialog.show( activity.getSupportFragmentManager(), "Show_iamge_dialog" );
            }
        });

        if ( message.isIncoming() )
        {
            TextView txtMessage0 = view.findViewById(R.id.txtMessage0);
            txtMessage0.setText(message.getSenderName());
        }

        return view;
    }
}
