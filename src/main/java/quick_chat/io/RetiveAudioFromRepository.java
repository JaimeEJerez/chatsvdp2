package quick_chat.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import quick_chat.globals.Constants;

public class RetiveAudioFromRepository
{
    public static abstract class EventHandler
    {
        public abstract void finish( byte[] audioData, String error);
    }

    private String auOwnerID;
    private String audioUUID;

public RetiveAudioFromRepository( String auOwnerID, String audioUUID )
    {
        this.auOwnerID = auOwnerID;
        this.audioUUID = audioUUID;
    }

    public void execute( final EventHandler eventHandler)
    {
        Thread thread = new Thread()
        {
            public void run()
            {
                byte[]              buff            = null;
                String              error           = null;
                HttpURLConnection   urlConnection   = null;

                try
                {
                    String urlStr = Constants.kProtocol + Constants.kAPIEntryPoint + "AudioRepository?userID=" + auOwnerID + "&audioUUID=" + audioUUID;

                    URL url = new URL(urlStr);

                    urlConnection = (HttpURLConnection) url.openConnection();

                    DataInputStream dis = new DataInputStream(urlConnection.getInputStream());

                    int size = dis.readInt();

                    buff = new byte[size];

                    dis.readFully( buff );
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
                        eventHandler.finish( buff, error );
                    }
                }
            }
        };

        thread.start();
    }


}

