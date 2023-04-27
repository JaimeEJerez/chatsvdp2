package quick_chat.actvt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.fragment.app.FragmentTransaction;

import quick_chat.actvt.home.Fragment_Notification;
import quick_chat.actvt.home.HomeActivity;
import quick_chat.globals.Constants;
import quick_chat.start.R;

public class NotificationsActvt extends ServiceActivity
{
    private     ServiceFragment         activeFragment          = null;

    public static void start(Context context)
    {
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                Intent intent = new Intent( context, HomeActivity.class);

                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                context.startActivity(intent);
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onResume()
    {
        super.onResume();

        if ( activeFragment == null )
        {
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    //activeFragment = openFragment(Fragment_Notification.newInstance());
                    String packageName= getPackageName();
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                    if (launchIntent != null) {
                        try {
                            String nameClass = launchIntent.getComponent().getClassName();
                            startActivity(new Intent(NotificationsActvt.this, Class.forName(nameClass)));
                            Log.e("InfoEMTingSDK","lanzadora   "+nameClass);
                        }catch (NullPointerException | ClassNotFoundException e){
                            e.getStackTrace();
                            Log.e("InfoEMTingSDK","lanzadora error   "+e.getMessage());
                        }
                    }
                }
            }, 20, 0);
        }
    }

    public ServiceFragment openFragment(ServiceFragment fragment)
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace( R.id.container, fragment );

        transaction.addToBackStack(null); //if you add fragments it will be added to the backStack. If you replace the fragment it will add only the last fragment

        transaction.commit();

        return fragment;
    }

    @Override
    public void onBackPressed()
    {
        if ( !activeFragment.onBackPressed() )
        {
            this.finish();
        }
    }

    @Override
    public void handleServiceMessage(Message msg)
    {
        if ( activeFragment != null && Constants.getUser( this.getApplicationContext() ) != null )
        {
            activeFragment.handleServiceMessage(msg);
            //MSGBoxBox.countIncommingMessages(null, false); Coño
        }
    }

}
