package quick_chat.actvt.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import androidx.fragment.app.FragmentTransaction;

import quick_chat.actvt.ServiceActivity;
import quick_chat.actvt.ServiceFragment;
import quick_chat.actvt.SplashScreen;
import quick_chat.db.User;
import quick_chat.globals.Constants;
import quick_chat.start.R;

public class HomeActivity extends ServiceActivity
{
    private     static   HomeActivity   self                    = null;

    private     ServiceFragment         activeFragment          = null;

    /*
        user.uuuid              = payload.get( "UUID" );
        user.firstName          = payload.get( "DISPLAY_NAME" );
        user.email              = payload.get( "EMAIL" );
        user.password           = payload.get( "PASSWORD" );;
        user.kind               = payload.get( "TYPE" );
     */

    public static void start(Activity activity, char userType, int id, String displayName, String eMail, String password ) throws Exception
    {
        User user    = new User();

        user.uuuid              = "S" + userType + String.format("%08X", id);
        user.firstName          = displayName;
        user.email              = eMail;
        user.password           = password;
        user.kind               = String.valueOf(userType);

        Constants.clearUser( activity );
        Constants.setUser( activity, user);

        //PendingIntent.getActivity

        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                String welcome = activity.getString(R.string.welcome) + "\r\n" + displayName;

                Toast.makeText( activity, welcome, Toast.LENGTH_LONG).show();

                Intent intent = new Intent( activity, HomeActivity.class);

                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                activity.startActivity(intent);
            }//public void run() {
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        self = this;

        setContentView(R.layout.activity_home);
    }

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
                    activeFragment = openFragment(Fragment_Notification.newInstance());
                }
            }, 200);
        }
    }

    public void logOut()
    {
       Constants.clearUser( HomeActivity.this );
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
            //Constants.clearUser(this);
            finish();
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