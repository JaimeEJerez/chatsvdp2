package quick_chat.actvt.home;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.pojo.ChatContent;
import com.pojo.ChatMessageCore;
import com.pojo.chatContent.AudioMsg;
import com.pojo.chatContent.ChatMsg;
import com.pojo.chatContent.StaticImageMsg;
import com.pojo.chatContent.TextMsg;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import quick_chat.Utils;
import quick_chat.actvt.ServiceFragment;
import quick_chat.adapters.ChatAdapter;
import quick_chat.adapters.chat.ChatMessage;
import quick_chat.chat_utils.MSGBoxBox;
import quick_chat.db.User;
import quick_chat.globals.Constants;
import quick_chat.io.RetiveImagesFromRepository;
import quick_chat.services.TCPService;
import quick_chat.start.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment_Chat#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Chat extends ServiceFragment
{
    private static final int REQUEST_AUDIO_PERMISSION_CODE = 7376272;

    public String destinationName                          = "";
    public String                       destinationID   = null;
    public long                         issueNumber     = 0;
    public ArrayList<ChatMsg>           messagesVect    = new ArrayList<ChatMsg>();
    public char                         destinationType = 0;

    protected TextView      messageEdit      = null;
    protected TextView      recordongTime = null;
    protected ProgressBar   progressBar   = null;
    protected View          recordGroup    = null;
    protected ImageButton   soundRecButton = null;
    protected ImageButton   chatSendButton = null;
    protected TextView      recipientLabel = null;
    protected ListView      messagesListView = null;
    private   View          sendGroup        = null;

    protected ChatAdapter adapter = null;

    protected MSGBoxBox outgoingMSGBoxBox = null;
    protected MSGBoxBox incomingMSGBoxBox = null;
    protected MSGBoxBox hystoricMSGBoxBox = null;
    protected MSGBoxBox archivedMSGBoxBox = null;

    private User            user                = null;
    private View            imageButton8        = null;
    private MediaRecorder   recorder            = null;
    private boolean         isRecording         = false;
    private File            audioFile           = null;
    private long            startTime           = 0;
    private int             oldDelta            = 0;
    private boolean         institutuional      = false;

    /*
    private Bitmap  bwBitmap    = null;
    private Canvas  canvas      = null;
    private Paint   paint1      = new Paint();
    */

    //private static Fragment_Chat self = null;

    public Fragment_Chat()
    {
       //bwBitmap = Bitmap.createBitmap(612, 128, Bitmap.Config.ARGB_8888 );
       //canvas = new Canvas(bwBitmap);
       //paint1.setStyle(Paint.Style.FILL);
       //paint1.setColor(Color.GRAY);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param destinationName Parameter 1.
     * @param destinationID Parameter 2.
     * @param issueNumber Parameter 3.
     * @param destinationType Parameter 4.
     * @return A new instance of fragment FragmentView.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_Chat newInstance(String                          destinationName,
                                            String                          destinationID,
                                            long                            issueNumber,
                                            char                            destinationType,
                                            boolean                         institutuional )
    {

            Fragment_Chat fragment = new Fragment_Chat();
            Bundle        args     = new Bundle();

            args.putString("destinationName", destinationName);
            args.putString("destinationID", destinationID);
            args.putLong("issueNumber", issueNumber);
            args.putChar("destinationType", destinationType );
            args.putSerializable("institutuional", institutuional);

            fragment.setArguments(args);

            return fragment;
    }

    private void setParameters( String                      destinationName,
                                String                       destinationID,
                                long                         issueNumber,
                                boolean                      institutuional,
                                char                        destinationType  )
    {
        this.destinationName = destinationName;
        this.destinationID = destinationID;
        this.issueNumber = issueNumber;
        this.destinationType = destinationType;
        this.institutuional = institutuional;

        user = Constants.getUser( this.getContext() );
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
        {
            String                      destinationName = getArguments().getString("destinationName");
            String                      destinationID   = getArguments().getString("destinationID");
            long                        issueNumber     = getArguments().getLong("orderNumber", 0);
            boolean                     institutuional  = getArguments().getBoolean("institutuional");
            char                        destinationType = getArguments().getChar("destinationType");

            this.setParameters( destinationName, destinationID, issueNumber, institutuional, destinationType );
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View rotView = inflater.inflate(R.layout.fragment_chat, container, false);

        messageEdit      = rotView.findViewById(R.id.chat_message_edit);
        chatSendButton   = rotView.findViewById(R.id.imageButton12);
        soundRecButton   = rotView.findViewById(R.id.imageButton11);
        recipientLabel   = rotView.findViewById(R.id.chat_dst_label);
        messagesListView = rotView.findViewById(R.id.chat_messages_list_view);
        sendGroup        = rotView.findViewById(R.id.send_group);
        imageButton8     = rotView.findViewById(R.id.imageButton8);
        recordongTime    = rotView.findViewById(R.id.textView94 );
        progressBar      = rotView.findViewById(R.id.progressBar2 );
        recordGroup      = rotView.findViewById(R.id.recordGroup );

        ImageView   imagView    = rotView.findViewById(R.id.imageView1);
        TextView    textView    = rotView.findViewById(R.id.notif_senderTextView);
        TextView    textNumber  = rotView.findViewById(R.id.notif_numberTextView );

        textView.setText( destinationName );

        if ( this.destinationID.startsWith("SY") )
        {
            sendGroup.setVisibility( View.GONE );
        }

        Bitmap bitmap = Utils.bitmap_from_DIRECTORY_PICTURES( rotView.getContext(), destinationID );

        if ( bitmap == null )
        {
            RetiveImagesFromRepository dir = new RetiveImagesFromRepository( "avatars", destinationID);

            dir.execute( rotView.getContext(), new RetiveImagesFromRepository.EventHandler()
            {
                @Override
                public void finish( Bitmap bitmap, String error)
                {
                    if ( error == null && bitmap != null )
                    {
                        imagView.setImageBitmap( bitmap );
                    }
                }
            });
        }
        else
        {
            imagView.setImageBitmap(bitmap);
        }

        progressBar.setMax( 32767/2 );

        final Vector<Integer> volumnVect = new Vector<Integer>();

        //recipientLabel.setText( destinationName );

        messageEdit.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                String text = messageEdit.getText().toString();

                if ( text.length() > 0 )
                {
                    soundRecButton.setVisibility( View.GONE );
                    chatSendButton.setVisibility( View.VISIBLE );
                }
                else
                {
                    soundRecButton.setVisibility( View.VISIBLE );
                    chatSendButton.setVisibility( View.GONE );
                }
            }

        });

        chatSendButton.setOnClickListener(new View.OnClickListener()
          {
              @Override
              public void onClick(View v)
              {
                  String text = messageEdit.getText().toString();

                  if ( text.length() > 0 )
                  {
                      String msgTxt = messageEdit.getText().toString();

                      msgTxt = msgTxt.replaceAll( "\n", "<br>" );

                      sendTextMessage( msgTxt );

                      messageEdit.setText( "" );
                  }
              }
          } );

        soundRecButton.setOnTouchListener(new View.OnTouchListener()
        {
            public boolean onTouch(View view, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    try
                    {
                        audioFile = Utils.createUUAudioFile(getContext(), ".mp4");

                        startRecording(audioFile, volumnVect);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                else if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    if ( recorder != null )
                    {
                        stopRecording();

                        try
                        {
                            sendAudioMessage(audioFile, volumnVect);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }

                return true;
            }
        });


        imageButton8.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                takeImage( false, 0, 0, IMAGE_FORMAT.JPG );
            }
        });

        outgoingMSGBoxBox = new MSGBoxBox(Constants.getOutDirFile());
        incomingMSGBoxBox = new MSGBoxBox(Constants.getInbDirFile());
        hystoricMSGBoxBox = new MSGBoxBox(Constants.getHysDirFile());
        archivedMSGBoxBox = new MSGBoxBox(Constants.getArcDirFile());

        adapter = new ChatAdapter(this.getContext(), destinationID, destinationName, hystoricMSGBoxBox, messagesVect);

        messagesListView.setAdapter(adapter);

        if (!CheckAudioPermissions())
        {
            // if audio recording permissions are
            // not granted by user below method will
            // ask for runtime permission for mic and storage.
            RequestAudioPermissions();
        }

        ImageButton backImageButton = rotView.findViewById( R.id.backImageButton );

        backImageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if ( onClickListener != null )
                {
                    onClickListener.onClick( v );
                }
            }
        });

        restoreMessagesFromMSGBox();

        textNumber.setText( String.valueOf( messagesVect.size()  ));

        return rotView;
    }

    View.OnClickListener onClickListener = null;

    public void setOnBackListener( View.OnClickListener onClickListener )
    {
        this.onClickListener = onClickListener;
    }

    protected void restoreMessagesFromMSGBox()
    {
        ArrayList<ChatMsg> messagesArrayList = MSGBoxBox.createMessagesArrayList(getChatDiscriminationCode(), false, false);

        for (ChatMsg message : messagesArrayList)
        {
            if (message != null)
            {
                adapter.add(message.getFilePath(), message);
            }
        }

        adapter.notifyDataSetChanged();

        scrollDown();
    }

    @Override
    public void handleServiceMessage(Message msg)
    {
        switch (msg.what)
        {
            case TCPService.MSG_HISTBOX_EVENT:
            {
                ChatMsg chatm = (ChatMsg) msg.obj;

                proccessModifyMessage(chatm);

                break;
            }
            case TCPService.MSG_INBOX_EVENT:
            {
                ChatMsg chatm = (ChatMsg) msg.obj;

                proccessInboxMessage(chatm);

                break;
            }
            case TCPService.MSG_OUTBOX_EVENT:
            {
                ChatMessage.setSendingProgress( null );

                final File file = (File) msg.obj;

                Fragment_Chat.this.getActivity().runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        adapter.updateMessageFromTimeName(file);

                        adapter.notifyDataSetChanged();

                        scrollDown();
                    }
                });

                break;
            }

            case TCPService.MSG_SENDING_EVENT:
            {
                final ChatMessage.SendingProgress progressMsg = (ChatMessage.SendingProgress) msg.obj;

                ChatMessage.setSendingProgress( progressMsg );

                Fragment_Chat.this.getActivity().runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        adapter.notifyDataSetChanged();
                    }
                });

                break;
            }

        }
    }


    public String getIssueString()
    {
        String orderString = "";

        if (issueNumber != 0)
        {
            orderString = "issue  " + issueNumber + " - ";
        }

        return orderString;
    }

    public String getChatDiscriminationName()
    {
        return getIssueString() + destinationName;
    }

    public String getChatDiscriminationCode()
    {
        return getIssueString() + destinationID;
    }

    public void sendChatContentObject(ChatContent chatContent)
    {
        String name     = user.getVisibleName();
        String uuID     = user.uuuid;

        ChatMsg messages = new ChatMsg( chatContent,
                                        ChatMessageCore.MessageType.kSingleUser,
                                        name, uuID,
                                        destinationType,
                                        destinationName,
                                        destinationID,
                                        System.currentTimeMillis(), false, true, false,
                                        institutuional );

        Fragment_Chat.this.getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                showMessagesInOutbox(messages, true);
            }
        });
    }

    public void scrollDown()
    {
        int count = messagesListView.getCount();

        messagesListView.setSelection( count- 1);

        messagesListView.invalidate();
    }

    public void showMessagesInOutbox(ChatMsg message, boolean redraw)
    {
        outgoingMSGBoxBox.pushMessage(message);

        adapter.add( message.getFileName(), message );

        if (redraw)
        {
            adapter.notifyDataSetChanged();

            scrollDown();
        }
    }

    public void sendTextMessage(String message)
    {
        if (message.length() > 0)
        {
            TextMsg textMsg = new TextMsg(issueNumber, message);

            sendChatContentObject( textMsg );
        }
    }

    public void sendAudioMessage( File audioFile, Vector<Integer> voulumen ) throws IOException
    {
        if ( audioFile.exists() && voulumen.size() > 10 )
        {
            /*
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();

            Uri uri = Uri.parse( audioFile.getAbsolutePath() );

            mmr.setDataSource( this.getContext(), uri );

            String  durationStr     = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            int     secunds         = Integer.parseInt(durationStr) / 1000;

            byte[] image;

            {
                float vectSize      = voulumen.size();
                float bMpWidth      = bwBitmap.getWidth();
                float bMappHei      = bwBitmap.getHeight();

                bwBitmap.eraseColor(Color.TRANSPARENT);

                for ( float i = 0; i < bMpWidth; i += 1.0f )
                {
                    int x = (int)Math.min( vectSize-1.0f, (i/bMpWidth) * vectSize);

                    int y = Math.round( voulumen.get( x ) * bMappHei / 32767.0f);

                    canvas.drawRect( i, 64 - y, i + 1, 64 + y, paint1 );
                }

                image = Utils.bitMap2PNG(bwBitmap);
            }
            */

            Utils.FixAudioMessageResult far = Utils.fixAudioMessage( audioFile, voulumen, this.getContext() );

            AudioMsg audioMsg = new AudioMsg( issueNumber, audioFile.getName(), far.secunds, far.image );

            audioMsg.setChatContentClass( "quick_chat.adapters.chat.AudioMessage" );

            sendChatContentObject( audioMsg );
        }
    }


    public void sendImageMessage( Uri imageUri ) throws IOException
    {
        Bitmap  bitmap              = Utils.readBitMapFromURI(this.getContext(), imageUri);
        String  imageFileName       = Utils.fileNameFromUri( this.getContext(), imageUri );
        int     pos                 = imageFileName.lastIndexOf("/");

        imageFileName = imageFileName.substring( pos + 1 );

        int h = bitmap.getWidth();
        int v = bitmap.getHeight();

        double hypo = Math.hypot( h , v );
        double fact = 128.0 / hypo;

        h = (int)Math.round( h*fact );
        v = (int)Math.round( v*fact );

        Size size = new Size( h, v );

        bitmap = Utils.resizeBitMap( bitmap, size );

        byte[] jpeg = Utils.bitMap2JPG( bitmap );

        StringBuilder sb = new StringBuilder( jpeg.length * 2 );

        for ( byte b : jpeg )
        {
            sb.append(  String.format("%02X", b) );
        }

        String smallHexImage = sb.toString();

        StaticImageMsg imgtMsg = new StaticImageMsg( 0, imageFileName, "", h, v, smallHexImage );

        imgtMsg.setChatContentClass( "quick_chat.adapters.chat.StaticImageMessage" );

        sendChatContentObject( imgtMsg );
    }

    @Override
    public boolean receiveImage(Uri imageUri, String error, int step )
    {
        if ( step == 1 )
        {
            return true;
        }
        else
        {
            if ( error == null && imageUri != null )
            {
                try
                {
                    sendImageMessage(imageUri);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    public void transferInboxtoHystory(String chatIDNameFilter)
    {
        ArrayList<File> incomingFiles = new ArrayList<File>();

        incomingMSGBoxBox.getMsgsFileList(destinationID, incomingFiles);

        if (incomingFiles != null && chatIDNameFilter != null)
        {
            for (File f : incomingFiles)
            {
                ChatMsg msg = MSGBoxBox.loadMessage(f);

                String chatIDname = msg.getChatDiscriminationCode();

                if (chatIDname.equalsIgnoreCase(chatIDNameFilter))
                {
                    f.delete();

                    msg.setToRead(false);

                    hystoricMSGBoxBox.pushMessage(msg);
                }
            }
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        transferInboxtoHystory( destinationID );

        scrollDown();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    public boolean proccessModifyMessage(final ChatMsg chatMsg)
    {
        File f = chatMsg.getFile();

        if ( f != null && f.exists() )
        {
            String senderIDName = f.getParentFile().getName();//msg.getSenderID();

            if (senderIDName != null && senderIDName.contains(destinationID))
            {
                if (chatMsg.getIssueNumber() == this.issueNumber)
                {
                    chatMsg.setToRead(false);

                    adapter.updateMessageFromTimeName( chatMsg );

                    Fragment_Chat.this.getActivity().runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            adapter.notifyDataSetChanged();

                            messagesListView.invalidateViews();
                        }
                    });
                }
            }
        }

        return true;
    }

    /*
    public static String getSenderIDFromFileName(String fileName)
    {
        String[] c = fileName.split(File.separator );

        return c[c.length-1];
    }*/

    public boolean proccessInboxMessage(final ChatMsg chatMsg)
    {
        File f = chatMsg.getFile();

        if ( f != null && f.exists() )
        {
            String senderIDName = f.getParentFile().getName();//msg.getSenderID();

            if (senderIDName != null && senderIDName.contains(destinationID))
            {
                ChatMsg msg = MSGBoxBox.loadMessage(f);

                if ( msg != null && msg.getIssueNumber() == this.issueNumber)
                {
                    msg.setToRead(false);

                    hystoricMSGBoxBox.pushMessage(msg);

                    f.delete();

                    adapter.add(chatMsg.getFileName(), chatMsg);

                    Fragment_Chat.this.getActivity().runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            adapter.notifyDataSetChanged();

                            messagesListView.invalidateViews();

                            scrollDown();
                        }
                    });
                }
            }
        }

        return true;
    }

    public boolean CheckAudioPermissions()
    {
        // this method is used to check permission
        int result = ContextCompat.checkSelfPermission( this.getContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(this.getContext(), RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void RequestAudioPermissions()
    {
        ActivityCompat.requestPermissions( this.getActivity(), new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        // this method is called when user will
        // grant the permission for audio recording.
        switch (requestCode)
        {
            case REQUEST_AUDIO_PERMISSION_CODE:
                if (grantResults.length > 0)
                {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToStore  = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (permissionToRecord && permissionToStore)
                    {
                        //Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                    }
                    else
                        {
                            toast("Permission Denied", Toast.LENGTH_LONG);
                    }
                }
                break;
        }
    }

    private String strSecunds( int secunds )
    {
        String s = String.valueOf( secunds  );

        if ( secunds < 10 )
        {
            s = "0" + s;
        }

        return s;
    }

    private void startRecording( File file, Vector<Integer> volumenVect )
    {
        if (recorder != null)
        {
            recorder.release();
        }

        recorder = new MediaRecorder();
        //recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        //recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
        //recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4 );

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setOutputFile(file.getAbsolutePath());

        try
        {
            recorder.prepare();

            recorder.start();

            final Handler handler = new Handler();

            // updates the visualizer every 50 milliseconds
            Runnable vulomenRecorder = new Runnable()
            {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void run()
                {
                    if (isRecording) // if we are already recording
                    {
                        recordGroup.setVisibility( View.VISIBLE );
                        messageEdit.setVisibility( View.INVISIBLE );

                        int x = recorder.getMaxAmplitude();

                        volumenVect.add( x );

                        handler.postDelayed(this, 50);

                        if ( startTime == 0 )
                        {
                            startTime = System.currentTimeMillis();
                        }

                        int deltaSecunds = (int)((System.currentTimeMillis() - startTime)/1000L);

                        if ( deltaSecunds != oldDelta )
                        {
                            int min = deltaSecunds/60;
                            int sec = deltaSecunds - (min*60);

                            recordongTime.setText( String.valueOf( min ) + ":" + strSecunds(sec) );

                            oldDelta = deltaSecunds;
                        }

                        progressBar.setProgress( x );
                    }
                }
            };

            volumenVect.clear();

            isRecording = true;

            handler.postDelayed(vulomenRecorder, 400);
        }
        catch (IOException e)
        {
            Log.e("giftlist", "io problems while preparing [" + file.getAbsolutePath() + "]: " + e.getMessage());
        }
    }

    private void stopRecording()
    {
        if (recorder != null)
        {
            isRecording = false;

            try
            {
                recorder.stop();
                recorder.release();
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
            finally
            {
                recorder = null;
                recordGroup.setVisibility(View.GONE);
                messageEdit.setVisibility(View.VISIBLE);
                startTime = 0;
            }
        }
    }

    public void playAudio(  File file  )
    {
        // for playing our recorded audio
        // we are using media player class.
        MediaPlayer mPlayer = new MediaPlayer();
        try
        {
            // below method is used to set the
            // data source which will be our file name
            mPlayer.setDataSource( file.getAbsolutePath() );

            // below method will prepare our media player
            mPlayer.prepare();

            // below method will start our media player.
            mPlayer.start();
        }
        catch (IOException e)
        {

        }
    }

    Toast  toast;

    public void toast( final String message, final int duration )
    {
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                LayoutInflater inflater = getLayoutInflater();

                ViewGroup vi = (ViewGroup)inflater.inflate(R.layout.toast, null);

                TextView text = vi.findViewById(R.id.txtvw);

                text.setText( message );

                toast = new Toast( Fragment_Chat.this.getContext());
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.setDuration(duration);
                toast.setView(vi);
                toast.show();
            }
        });

    }

}