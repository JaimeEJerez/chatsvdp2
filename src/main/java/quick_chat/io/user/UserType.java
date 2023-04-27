package quick_chat.io.user;

import android.content.Context;

import quick_chat.JSONResponse;
import quick_chat.globals.Constants;
import quick_chat.io.WebService;

public class UserType
{
    private Context					context;
    private String                  userUUID;
    private String                  userType;

    static public abstract class UserTypeEventHandler
    {
        public abstract void finish(String payload, String error);
    }

    public UserType(Context                 context,
                    String					userType,
                    String                  userUUID )
    {
        this.context	    = context;
        this.userUUID       = userUUID;
        this.userType       = userType;
    }

    public void execute( final UserTypeEventHandler logInEventHandler )
    {
        Thread thread = new Thread()
        {
            public void run()
            {
                String                          payload         = null;
                String                          error            = null;

                String urlStr = Constants.kProtocol + Constants.kAPIEntryPoint + "user/UserType";

                WebService ws = new WebService( urlStr);

                ws.put( "userUUID", userUUID );
                ws.put( "userType", userType );

                JSONResponse response  = ws.sendPost();

                if ( !response.success )
                {
                    error = response.error.message;
                }
                else
                {
                    payload  = (String)response.payload;
                }

                if ( logInEventHandler != null )
                {
                    logInEventHandler.finish( payload, error );
                }
            }
        };

        thread.start();
    }
}
