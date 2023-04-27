package quick_chat.actvt;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import org.jetbrains.annotations.NotNull;

import quick_chat.actvt.home.Fragment_CaptureImage;
import quick_chat.handlers.NotificationsHandler;
import quick_chat.services.TCPService;
import quick_chat.start.R;
import quick_chat.start.StartApp;

public abstract class ServiceFragment extends Fragment_CaptureImage
{
    protected final Messenger           mReplyTo        = new Messenger( new IncomingHandler() );
    protected       boolean           	mIsBound        = false;

    NotificationsHandler notificationsHandler = null;

    public  class IncomingHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);

            handleServiceMessage( msg );
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        View retView = super.onCreateView( inflater, container, savedInstanceState );

        ListView notfListView = container.findViewById( R.id.notif_listView );

        //notificationsHandler = new NotificationsHandler( this.getContext(), notfListView, false );

        //notificationsHandler.onCreate(savedInstanceState);

        return retView;
    }


    public void onResume()
    {
        super.onResume();

        doBindService( StartApp.getMassager());
    }

    public void onDestroy()
    {
        super.onDestroy();

        doUnbindService( StartApp.getMassager() );
    }

    public boolean doBindService( @NotNull Messenger  mService)
    {
        if ( mIsBound )
        {
            return true;
        }

        Message msg = Message.obtain( null, TCPService.MSG_REGISTER_CLIENT );


        msg.replyTo = mReplyTo;
        try
        {
            mService.send( msg );

            mIsBound = true;
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
        }

        return true;
    }

    public void doUnbindService( Messenger mService )
    {
        if (mIsBound)
        {
            if ( mService != null )
            {
                Message msg = Message.obtain(null, TCPService.MSG_UNREGISTER_CLIENT );

                msg.replyTo = mReplyTo;
                try
                {
                    mService.send(msg);
                }
                catch (RemoteException e)
                {
                    e.printStackTrace();
                }
            }

            mIsBound = false;
        }
    }

    public FragmentTransaction openFragmentInParent(ServiceFragment fragment)
    {
       return  ((ServiceFragment)this.getParentFragment()).openFragment( fragment );
    }


    public FragmentTransaction openFragment(ServiceFragment fragment)
    {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        transaction.replace( R.id.container, fragment );

        transaction.addToBackStack(null);   // if you add fragments it will be added to the backStack.
                                            // If you replace the fragment it will add only the last fragment

        transaction.commitAllowingStateLoss();


        //transaction.commit();

        return transaction;
    }

    public abstract void handleServiceMessage(Message msg);

}
