package quick_chat.actvt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.internal.LinkedTreeMap;

import extras.PrivatePreferencesManager;
import extras.UserData;
import quick_chat.actvt.home.HomeActivity;
import quick_chat.db.User;
import quick_chat.globals.Constants;
import quick_chat.io.user.AutoLogIn;
import quick_chat.start.R;

public class SplashScreen extends AppCompatActivity
{
    public static void start(Context context)
    {
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                Intent intent = new Intent( context, SplashScreen.class);

                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                context.startActivity(intent);
            }
        });
    }

    private void showError( String error )
    {
        SplashScreen.this.runOnUiThread(new Runnable()
        {
            public void run()
            {
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash_screen);
    }

    /**
     * Called when the activity is about to start interacting with the user.
     */
    @Override
    protected void onResume()
    {
        super.onResume();
        Log.e("InfoEMTingSDK", "onResumen Slash ");
        /*
        userType:
            C = Client
            V = Volunteer
            M = ChapterMember
        */


        UserData userData= PrivatePreferencesManager.getSavedUserData(getApplicationContext());
        Log.e("InfoEMTingSDK", "onResume User Data "+userData.toString());

        //if ( type == 'M' )//ChapterMember
        //if ( type == 'V' )//Volunteer
        //if ( type == 'C' )//Client

        char    userType            = userData.getUserType();
        int     uid                 = userData.getUid();
        String  displayName         = userData.getDisplayName();
        String  eMail               = userData.getDisplayName();
        String  password            = userData.getPassword();

        try
        {
            HomeActivity.start( SplashScreen.this,
                                userType,
                                uid,
                                displayName,
                                eMail,
                                password );
            finish();
        }
        catch (Exception e)
        {
            showError( e.getMessage() );
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("InfoEMTingSDK", "onDestroy Splash ");
    }
}
