package quick_chat.adapters.chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.pojo.chatContent.AudioMsg;
import com.pojo.chatContent.ChatMsg;

import java.io.File;
import java.io.IOException;

import quick_chat.Utils;
import quick_chat.chat_utils.GeolocationUtilities;
import quick_chat.io.RetiveAudioFromRepository;
import quick_chat.start.R;

public class AudioMessage extends ChatMessage
{
    RetiveAudioFromRepository dir = null;

    static final int    stateMP_Downloading     = 0;
    static final int    stateMP_Stopped         = 1;
    static final int    stateMP_Playing         = 2;
    static final int    stateMP_Paused          = 3;

    private int stateMediaPlayer = stateMP_Stopped;

    private MediaPlayer mPlayer     = null;

    private ImageView   audioImage  = null;
    private ImageButton playButton  = null;
    private SeekBar     seekBar     = null;
    private TextView    txtInfo     = null;
    private TextView    duraTextV   = null;
    private Handler     handler     = null;
    private Context     context     = null;

    String   audioUUID    = null;
    byte[]   wave         = null;
    int      durat        = 0;

    public AudioMessage()
    {
    }

    private String int2Time( int value )
    {
        if ( value > 99 )
        {
            value = 99;
        }

        if ( value < 0 )
        {
            value = 0;
        }

        String txt = String.valueOf( value );

        if ( value < 9 )
        {
            txt = "0" + txt;
        }

        return txt;
    }

    @Override
    public View populateView( Context context, int position, View view, ChatMsg message )
    {
        this.context = context;

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if ( view == null )
        {
            int resourceID = message.isIncoming() ? R.layout.chat_item_audio_in : R.layout.chat_item_audio_ou;

            view = inflater.inflate(resourceID, null);
        }

        audioImage  = view.findViewById( R.id.imageView48   );
        playButton  = view.findViewById( R.id.imageButton10 );
        seekBar     = view.findViewById( R.id.seekBar2      );
        txtInfo     = view.findViewById( R.id.txtInfo       );
        duraTextV   = view.findViewById( R.id.textView93    );

        AudioMsg audioMsg = (AudioMsg)message.getMsgCore().getContent();

        audioUUID    = audioMsg.getAudioName();
        wave         = audioMsg.getAudioWave();
        durat        = audioMsg.getDuration();

        if ( wave != null )
        {
            Bitmap bm = Utils.byteArr2BitMap( wave );
            audioImage.setImageBitmap( bm );
        }

        int secunds     = durat;
        int minuts      = secunds/60;
        secunds        -= ( minuts * 60 );

        duraTextV.setText( int2Time(minuts ) + ":" + int2Time(secunds) );

        if ( audioUUID != null )
        {
            try
            {
                File file = Utils.file_from_DIRECTORY_AUDIO( context, audioUUID );

                if ( file == null || !file.exists() )
                {
                    stateMediaPlayer = stateMP_Downloading;

                    playButton.setImageResource( R.drawable.downloading );

                    dir = new RetiveAudioFromRepository( message.getSenderID(), audioUUID );

                    dir.execute(new RetiveAudioFromRepository.EventHandler()
                    {
                        @Override
                        public void finish(byte[] buff, String error)
                        {
                            if ( error == null && buff != null )
                            {
                                try
                                {
                                   File audioFile =  Utils.bytes_to_DIRECTORY_AUDIO( context,  buff, audioUUID );

                                    final AppCompatActivity  activity = (AppCompatActivity)context;

                                    stateMediaPlayer = stateMP_Stopped;

                                    //Utils.FixAudioMessageResult far = Utils.fixAudioMessage( audioFile, null, context );

                                    activity.runOnUiThread(new Runnable()
                                    {
                                        public void run()
                                        {
                                            playButton.setImageResource( R.drawable.play );
                                        }
                                    });
                                }
                                catch (IOException e)
                                {
                                    e.printStackTrace();
                                }
                                finally
                                {
                                    dir = null;
                                }
                            }
                        }
                    });
                }
            }
            catch (IOException e)
            {
            }
        }

        String topText = GeolocationUtilities.dateFormat( context, message.getTime());

        setTopText(txtInfo, topText, message);

        final AppCompatActivity  activity = (AppCompatActivity)context;

        playButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if ( stateMediaPlayer == stateMP_Downloading )
                {
                    return;
                }

                switch ( stateMediaPlayer )
                {
                    case stateMP_Stopped:
                    case stateMP_Paused:

                        initMediaPlayer();

                        mPlayer.start();

                        playButton.setImageResource( R.drawable.pause  );

                        stateMediaPlayer = stateMP_Playing;

                        int mediaMax = mPlayer.getDuration();

                        seekBar.setMax(mediaMax);

                        handler = new Handler();

                        handler.postDelayed(moveSeekBarThread, 100);

                        break;
                    case stateMP_Playing:

                        initMediaPlayer();

                        mPlayer.pause();

                        stateMediaPlayer = stateMP_Paused;

                        playButton.setImageResource( R.drawable.play );

                        break;
                }
            }
        });


        if ( message.isIncoming() )
        {
            TextView txtMessage0 = view.findViewById(R.id.txtMessage0);
            txtMessage0.setText(message.getSenderName());
        }

        return view;
    }

    private boolean initMediaPlayer()
    {
        try
        {
            if ( mPlayer == null )
            {
                final File file = Utils.file_from_DIRECTORY_AUDIO(context, audioUUID);

                if ( !file.exists() )
                {
                    return false;
                }

                mPlayer = new MediaPlayer();

                mPlayer.setDataSource(file.getAbsolutePath());

                mPlayer.prepare();

                seekBar.setOnSeekBarChangeListener(seekBarOnSeekChangeListener);

                int mediaMax = mPlayer.getDuration();

                seekBar.setMax(mediaMax);

                handler = new Handler();

                handler.removeCallbacks(moveSeekBarThread);

                mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
                {
                    @Override
                    public void onCompletion(MediaPlayer mp)
                    {
                        stateMediaPlayer = stateMP_Stopped;

                        playButton.setImageResource( R.drawable.play );

                        seekBar.setProgress(0);
                    }
                });
            }

            return true;
        }
        catch (IOException e)
        {
        }

        return false;
    }
    private Runnable moveSeekBarThread = new Runnable()
    {

        public void run()
        {
            if (mPlayer.isPlaying())
            {
                if ( initMediaPlayer() ) {

                    int mediaPos_new = mPlayer.getCurrentPosition();
                    int mediaMax_new = mPlayer.getDuration();
                    seekBar.setMax(mediaMax_new);
                    seekBar.setProgress(mediaPos_new);

                    handler.postDelayed(this, 100); // Looping the thread after 0.1
                }
            }
        }
    };

    SeekBar.OnSeekBarChangeListener seekBarOnSeekChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar)
        {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar)
        {
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser)
        {
            if (fromUser)
            {
                initMediaPlayer();

                mPlayer.seekTo(progress);

                seekBar.setProgress(progress);
            }
        }
    };
}
