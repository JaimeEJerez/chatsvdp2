package quick_chat.actvt.home;

import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.fragment.app.FragmentTransaction;

import quick_chat.actvt.SplashScreen;
import quick_chat.db.User;
import quick_chat.globals.Constants;
import quick_chat.services.TCPService;
import quick_chat.start.StartApp;

import com.pojo.ChatMessageCore;
import com.pojo.chatContent.ChatMsg;

import quick_chat.actvt.ServiceFragment;
import quick_chat.adapters.BaseNotificationAdapter;
import quick_chat.adapters.NotificationItem;
import quick_chat.handlers.NotificationsHandler;
import quick_chat.start.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment_Notification#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Notification extends ServiceFragment
{
    private NotificationsHandler                            notificationsHandler    = null;
    private ServiceFragment                                 activeFragment          = null;
    private RecyclerView                                    notif_listView          = null;


    public Fragment_Notification()
    {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FragmentChat.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_Notification newInstance()
    {
        return new Fragment_Notification();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
        {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_v_notif_view, container, false);

        TextView titleTextView = view.findViewById( R.id.titleTextView );

        if ( titleTextView != null )
        {
            titleTextView.setText( "Tus mensages almacenados" );
        }

        User user = Constants.getUser( this.getContext() );

        TextView textView = view.findViewById( R.id.textView21);

        if ( textView != null )
        {
            textView.setText( user.getVisibleName() );
        }

        notif_listView = view.findViewById( R.id.notif_listView);

        notificationsHandler = new NotificationsHandler( notif_listView, false, new BaseNotificationAdapter.OnClickListener()
        {
            @Override
            public void onClick(NotificationItem notifItem, View view, int posistion)
            {
                notif_listView.setVisibility( View.GONE );

                final Fragment_Chat         chatFragment        =  Fragment_Chat.newInstance(   notifItem.name,
                                                                                                notifItem.userID,
                                                                                                notifItem.issueNumber,
                                                                                                notificationKind2MessageType(notifItem.kind),
                                                                                               // notifItem.messagesVect,
                                                                                                false);

                final  FragmentTransaction  fragmentTransaction =  openFragment( chatFragment );

                chatFragment.setOnBackListener( new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        getChildFragmentManager().beginTransaction().remove(chatFragment).commit();

                        notif_listView.setVisibility(View.VISIBLE);

                        chatFragment.doUnbindService( StartApp.getMassager() );
                    }
                });

                view.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        chatFragment.scrollDown();
                    }
                }, 500);
            }
        });

        notificationsHandler.onCreate( this.getContext() );

        return view;
    }

    public char notificationKind2MessageType( NotificationItem.NotifKind notifKind )
    {
        char mt = ChatMessageCore.MessageType.kSingleUser;

        if ( notifKind == NotificationItem.NotifKind.GROUP )
        {
            mt = ChatMessageCore.MessageType.kGroupUser;
        }
        if ( notifKind == NotificationItem.NotifKind.USER )
        {
            mt = ChatMessageCore.MessageType.kSingleUser;
        }

        return mt;
    }



    @Override
    public void onResume()
    {
        super.onResume();

        notificationsHandler.onResume();
    }

    @Override
    public void handleServiceMessage(Message msg)
    {
        if ( notificationsHandler != null )
        {
            notificationsHandler.handleServiceMessage(msg);
        }

        switch (msg.what)
        {
            case TCPService.MSG_HISTBOX_EVENT:
            case TCPService.MSG_OUTBOX_EVENT: {
                //ChatMsg chatm = (ChatMsg) msg.obj;

                break;
            }
            case TCPService.MSG_INBOX_EVENT:
            {
                ChatMsg chatm = (ChatMsg) msg.obj;

                break;
            }
            case TCPService.MSG_INBOX_SITUATION:
            {
                String message = (String) msg.obj;

                Toast.makeText( this.getContext(), "ESTE USUARIO ESTA EN LINEA.......", Toast.LENGTH_LONG).show();

                //SplashScreen.start( this.getContext() );
                break;
            }
        }
    }

    public boolean onBackPressed( Fragment_Chat chatFragment )
    {
        if ( notif_listView.getVisibility() != View.VISIBLE )
        {
            notif_listView.setVisibility(View.VISIBLE);

            return true;
        }
        else
        {
            return false;
        }
    }
}