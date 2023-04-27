package extras;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import open.SVDPCSDK;


public class PrivatePreferencesManager {

    //Si es true, el entorno es en desarrollo, si es false es en producción
    private static final String PREFS_DEVELOP_ENVIROMENT = "PREFS_DEVELOP_ENVIROMENT_NEW";

    private static final String PREFS_RATE_APP = "PREFS_RATE_APP";

    private static final String PREFS_NEWS = "PREFS_NEWS";

    private static final String PREFS_WALLET_SORT = "PREFS_WALLET_SORT";

    private static final String PREFS_USER_EMAIL = "PREFS_USER_EMAIL";
    private static final String PREFS_USER_ACCESS_TOKEN = "PREFS_USER_ACCESS_TOKEN";
    private static final String PREFS_USER_PASS = "PREFS_USER_PASS";
    private static final String PREFS_ITERATOR_ACCESS_TOKEN = "PREFS_ITERATOR_ACCESS_TOKEN";
    private static final String PREFS_USER_NICK = "PREFS_USER_NICK";
    private static final String PREFS_USER_NAME = "PREFS_USER_NAME";
    private static final String PREFS_USER_SURNAME = "PREFS_USER_SURNAME";
    private static final String PREFS_USER_ID = "PREFS_USER_ID";
    private static final String PREFS_USER_NO_LOGED_ID = "PREFS_USERNO_LOGED_ID";
    private static final String PREFS_USER_ITERATOR_ID = "PREFS_USER_ITERATOR_ID";
    private static final String PREFS_USER_AVATAR_URL = "PREFS_USER_AVATAR_URL";

    private static final String PREFS_ANON_USER_ID = "PREFS_ANON_USER_ID";

    private static final String PREFS_APP_HOST_CLIENT_ID = "PREFS_APP_HOST_CLIENT_ID";
    private static final String PREFS_APP_HOST_PASSKEY = "PREFS_APP_HOST_PASSKEY";
    private static final String PREFS_APP_HOST_API_KEY = "PREFS_APP_HOST_API_KEY";

    private static final String PREFS_NOTIFICATION_TOKEN = "PREFS_NOTIFICATION_TOKEN";

    private static final String PREFS_TRACKUSER = "PREFS_TRACKUSER";

    private static final String PREFS_ENABLE_WALLET = "PREFS_ENABLE_WALLET";
    private static final String PREFS_DN_USER = "PREFS_DN_USER";

    private static final String PREFS_USER_MIGRATED = "PREFS_USER_MIGRATED";

    //Preferencias por activar la trazabilidad por primera vez
    private static final String PREFS_POINTS_ACCEPT_TRAZABILITY = "PREFS_POINTS_ACCEPT_TRAZABILITY";

    private static final String PREFS_USER_DN = "PREFS_USER_DN";

    private static final String PREFS_USER_PHONE = "PREFS_USER_PHONE";

    private static final String PREFS_USER_PROFILE_ID = "PREFS_USER_PROFILE_ID";
    private static final String PREFS_USER_SDK_VERSION = "PREFS_USER_SDK_VERSION";

    private static final String PREFS_USER_DATA = "PREFS_USER_DATA";

    private static SharedPreferences getSharedPreferences(Context context) {
        try {
            if (context != null && context.getPackageName() != null && !context.getPackageName().equals("")) {
                //Log.e("InfoEMTingSDK", "getSharedPreferences - good ");
                //return context.getSharedPreferences(context.getPackageName(), context.MODE_PRIVATE);
                return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
            } else {
                //return EMTingSDK.getInstance().getContext().getSharedPreferences(EMTingSDK.getInstance().getContext().getPackageName(), 0);
                return SVDPCSDK.getInstance().getContext().getSharedPreferences("quick_chat.start", 0);
            }
        }catch (Exception e){
            e.printStackTrace();
            return SVDPCSDK.getInstance().getContext().getSharedPreferences("quick_chat.start", 0);
        }

    }

    public static void saveDataUser(UserData userData,Context context){

        SharedPreferences.Editor editor=getSharedPreferences(context).edit();
        Gson gson = new Gson();
        String json = gson.toJson(userData);
        editor.putString(PREFS_USER_DATA,json);
        Log.i("InfoEMTingSDK", "SAVE USERDATA GOOD ");
        editor.apply();

    }

    public static UserData getSavedUserData(Context context){

        Gson gson = new Gson();
        String json=getSharedPreferences(context).getString(PREFS_USER_DATA,"");
        Type type = new TypeToken<UserData>() {}.getType();
        UserData userData=gson.fromJson(json,UserData.class);
        //List<MyCourseInfo> myCourseInfoList=gson.fromJson(jsonMyCourseInfo,type);

        return userData;
    }

    //Al hacer logout o cuando caduca el accessToken de usuario
    public static void restorePreferencesOnUserLogout(Context context) {
        saveDataUser(null,context);
        Log.i("InfoEMTingSDK", "Restore restorePreferencesOnUserLogout ");

    }


}
