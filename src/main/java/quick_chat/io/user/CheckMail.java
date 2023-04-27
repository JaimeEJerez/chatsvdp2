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

public class CheckMail
{

    public static abstract class CheckMailEventHandler
    {
        public abstract void finish(String uuuid, String error);
    }

    private String      eMail;

    public  CheckMail( String eMail )
    {
        this.eMail = eMail;
    }

    public void execute( final CheckMailEventHandler checkMailEventHandler)
    {
        Thread thread = new Thread()
        {
            public void run()
            {
                String            uuuaid            = null;
                String            error         = null;
                HttpURLConnection urlConnection = null;

                try
                {
                    String urlStr = Constants.kProtocol + Constants.kAPIEntryPoint + "/user/CheckMail?email=" + eMail;

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

                    if ( checkMailEventHandler != null )
                    {
                        checkMailEventHandler.finish( uuuaid, error );
                    }
                }
            }
        };

        thread.start();
    }


}
