package quick_chat.actvt.home;


import android.app.Activity;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pojo.ChatMessageCore;

import quick_chat.start.R;

public class ToastActivity extends AppCompatActivity
{
    private	static	Toast 			toast 				= null;

    private static final int CHAT_CODE = 6745;


    private static Activity 					parentActivity;
    private static String	 					destinationName;
    private static String 						destinationID;
    private static char 	                    destinationType;

    public void toast( final String message, final int duration )
    {
        this.runOnUiThread( new Runnable()
        {

            @Override
            public void run()
            {
                LayoutInflater inflater = getLayoutInflater();

                ViewGroup vi = (ViewGroup)inflater.inflate(R.layout.toast, null);

                TextView text = vi.findViewById(R.id.txtvw);

                text.setText( message );

                toast = new Toast(getApplicationContext());
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.setDuration(duration);
                toast.setView(vi);
                toast.show();
            }
        });

    }

    public void hideToats( )
    {
        if ( toast != null )
        {
            this.runOnUiThread( new Runnable()
            {
                @Override
                public void run()
                {
                    if ( toast != null )
                    {
                        toast.cancel();
                    }
                    toast = null;
                }
            });
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
