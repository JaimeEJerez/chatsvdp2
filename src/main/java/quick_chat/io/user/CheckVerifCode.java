package quick_chat.io.user;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import quick_chat.JSONResponse;
import quick_chat.globals.Constants;

public class CheckVerifCode
{
    private String uuid;
    private String confCode;

    public CheckVerifCode( String uuid, String confCode)
    {
        this.uuid       = uuid;
        this.confCode   = confCode;
    }

    public void execute( final EventHandler eventHandler)
    {
        Thread thread = new Thread()
        {
            public void run()
            {
                String            uuuaid        = null;
                String            error         = null;
                HttpURLConnection urlConnection = null;

                try
                {
                    String urlStr = Constants.kProtocol + Constants.kAPIEntryPoint + "/user/CheckVerifCode?uuid=" + uuid + "&confCode=" + confCode;

                    URL url = new URL(urlStr);

                    urlConnection = (HttpURLConnection) url.openConnection();

                    InputStreamReader isReader = new InputStreamReader(urlConnection.getInputStream());
                    BufferedReader    reader   = new BufferedReader(isReader);
                    String            jsonTxt  = reader.readLine();

                    Gson gson =   new GsonBuilder().create();

                    JSONResponse response = gson.fromJson( jsonTxt, JSONResponse.class);

                    if ( !response.success )
                    {
                        error = response.error.message;
                    }
                    else
                    {
                        uuuaid = (String)response.payload;
                    }
                }
                catch (IOException e)
                {
                    error = e.getMessage();
                }
                finally
                {
                    urlConnection.disconnect();

                    if ( eventHandler != null )
                    {
                        eventHandler.finish( uuuaid, error );
                    }
                }
            }
        };

        thread.start();
    }

    public static abstract class EventHandler
    {
        public abstract void finish(String uuuaid, String error);
    }
}
