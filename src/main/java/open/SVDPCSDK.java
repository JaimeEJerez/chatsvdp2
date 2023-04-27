package open;

import android.content.Context;
import android.content.Intent;

import extras.PrivatePreferencesManager;
import extras.UserData;
import quick_chat.actvt.SplashScreen;
import quick_chat.db.User;


public class SVDPCSDK {

// ### SINGLETON PATTERN

    private static SVDPCSDK instance = null;

    public SVDPCSDK() {

    }

    public static SVDPCSDK getInstance() {

        if (instance == null)
            instance = new SVDPCSDK();

        return instance;
    }
    // ### SINGLETON PATTERN

    // Context
    private Context context;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }



    /*
     * Test de prueba para abrir Actividad desde la app que lo aloja
     * Solo usar por desarrolladores SDk!!
     */
    public void openSVDPChat() {

        Intent open=new Intent(this.context, SplashScreen.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(open);
    }

    public void openSVDPChat(char type, int uid, String name, String email, String pass) {

        UserData userData=new UserData();
        userData.setUserType(type);
        userData.setUid(uid);
        userData.setDisplayName(name);
        userData.seteMail(email);
        userData.setPassword(pass);

        PrivatePreferencesManager.saveDataUser(userData,this.context);
        Intent open=new Intent(this.context, SplashScreen.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(open);

    }

    public void logOutSVDPChat() {

        PrivatePreferencesManager.restorePreferencesOnUserLogout(context);
    }

    public UserData getUserDataFromSVDPChat() {

        UserData userData=PrivatePreferencesManager.getSavedUserData(context);
        return userData;
    }
}
