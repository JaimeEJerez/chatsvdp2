package quick_chat.services;

import android.app.Service;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Messenger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import quick_chat.start.R;


public class IOSocked extends Thread
{
	protected String 				serverIP			= null;
	protected int 					port				= 0;
	protected ArrayList<Messenger>	mClients			= null;

	protected Socket				clientSocket		= null;
	protected DataInputStream		dis					= null;
	protected DataOutputStream		dos					= null;

	private	static MediaPlayer 		pingSound 			= null;
	private	static MediaPlayer 		failSound 			= null;
	private	static MediaPlayer 		cashRegisterSound 	= null;

	private Service service = null;

	public IOSocked(String serverIP, int port, Service service, ArrayList<Messenger> mClients)
	{
		this.service 	= service;
		this.serverIP 	= serverIP;
		this.service 	= service;
		this.mClients 	= mClients;
		this.port	 	= port;
	}

	public Service getService()
	{
		return service;
	}

	/*
	public void playCashRegisterSound( boolean looping )
	{
		if ( cashRegisterSound == null )
		{
			cashRegisterSound = MediaPlayer.create( service.getApplicationContext(), R.raw.cashregister );
			cashRegisterSound.setLooping(looping);
		}
		cashRegisterSound.start();
	}

	public void stopCashRegisterSound()
	{
		if ( cashRegisterSound != null )
		{
			cashRegisterSound.stop();
			cashRegisterSound = null;
		}
	}
*/

	public void playPingSound()
	{
		if ( pingSound == null )
		{
			pingSound = MediaPlayer.create( service.getApplicationContext(), R.raw.ping );
			pingSound.setLooping(false);
		}
		pingSound.start();
	}

	/*
	public void playFailSound()
	{
		if ( failSound == null )
		{
			failSound = MediaPlayer.create( service.getApplicationContext(), R.raw.fail );
			failSound.setLooping(false);
		}
		failSound.start();
	}
*/

	public void playNotificationSound()
	{
		Uri alert = RingtoneManager.getDefaultUri( RingtoneManager.TYPE_NOTIFICATION );

		MediaPlayer mMediaPlayer = new MediaPlayer();

		try
		{
			mMediaPlayer.setDataSource( service, alert );

			final AudioManager audioManager = (AudioManager)service.getSystemService( Context.AUDIO_SERVICE );

			if ( audioManager.getStreamVolume( AudioManager.STREAM_NOTIFICATION ) != 0 )
			{
				mMediaPlayer.setAudioAttributes(new AudioAttributes
						.Builder()
						.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
						.build());

				mMediaPlayer.setLooping(false);
				mMediaPlayer.prepare();
				mMediaPlayer.start();
			}
		}
		catch (IllegalArgumentException e)
		{
		}
		catch (SecurityException e)
		{
		}
		catch (IllegalStateException e)
		{
		}
		catch (IOException e)
		{
		}
	}

	public boolean  openSocket()
	{
		try
		{
			clientSocket = new Socket( serverIP, port );
			clientSocket.setKeepAlive(true);
			clientSocket.setSoTimeout( 60*1000 );

			dis = new DataInputStream( clientSocket.getInputStream()  );
			dos = new DataOutputStream( clientSocket.getOutputStream() );

			return true;
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return false;
	}

	public void closeSocket()
	{
		if ( clientSocket != null )
		{
			try
			{
				clientSocket.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				clientSocket = null;
				dos = null;
				dis = null;
			}
		}
	}

}
