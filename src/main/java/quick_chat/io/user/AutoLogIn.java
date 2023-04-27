package quick_chat.io.user;

import android.content.Context;

import com.google.gson.internal.LinkedTreeMap;

import quick_chat.JSONResponse;
import quick_chat.globals.Constants;
import quick_chat.io.WebService;

public class AutoLogIn
{
    private Context                 context;

    static public abstract class NewUserEventHandler
    {
        public abstract void finish(LinkedTreeMap<String, String> userID, String error );
    }

    public AutoLogIn( Context context )
    {
        this.context		= context;
    }

    public void execute( final NewUserEventHandler logInEventHandler)
    {
        Thread thread = new Thread()
        {
            public void run()
            {
                LinkedTreeMap<String, String> payload = null;
                String                        error   = null;

                String urlStr = Constants.kProtocol + Constants.kAPIEntryPoint + "AutoLogIn";

                WebService ws = new WebService(urlStr);

                JSONResponse response = ws.sendPost();

                if ( !response.success )
                {
                    error = response.error.message;
                }
                else
                {
                    payload = (LinkedTreeMap<String, String>) response.payload;

                    if (payload == null)
                    {
                        error = "Usuario o clave invalidos.";
                    }
                }

                if (logInEventHandler != null)
                {
                    logInEventHandler.finish(payload, error);
                }
            }

        };

        thread.start();
    }
}
