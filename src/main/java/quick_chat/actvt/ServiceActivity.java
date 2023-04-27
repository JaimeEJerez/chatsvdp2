package quick_chat.actvt;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import org.jetbrains.annotations.NotNull;

import quick_chat.actvt.home.ToastActivity;
import quick_chat.services.TCPService;
import quick_chat.start.StartApp;

public abstract class ServiceActivity extends ToastActivity
{
    protected final	Messenger mReplyTo = new Messenger( new IncomingHandler() );

    protected       boolean           	mIsBound        = false;

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
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        doBindService( StartApp.getMassager() );

    }
    @Override
    protected void onDestroy()
    {
        doUnbindService( StartApp.getMassager() );

        super.onDestroy();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        //doUnbindService( StartApp.getMassager() );
    }


    @Override
    protected void onStart()
    {
        super.onStart();

        //doBindService( StartApp.getMassager() );
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }


    public boolean doBindService( @NotNull  Messenger  mService)
    {
        if ( mIsBound )
        {
            return true;
        }

        Message msg = Message.obtain( null, TCPService.MSG_REGISTER_CLIENT );

        msg.replyTo = mReplyTo;
        try
        {
            if ( mService != null && msg != null )
            {
                mService.send(msg);
            }

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

    public abstract void handleServiceMessage(Message msg);
}
