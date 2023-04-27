package quick_chat.actvt;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import quick_chat.animators.AnimateProgressBar;
import quick_chat.start.R;

public class LoadingFeedBack
{
    private View                contView            = null;
    private ProgressBar         progessBar          = null;
    private AnimateProgressBar  animator            = null;

    public LoadingFeedBack(View rootView )
    {
        progessBar  = rootView.findViewById(R.id.progressBar);
        contView    = rootView.findViewById(R.id.contView );

        if ( contView != null)
        {
            contView.setAlpha(0.3f);
        }
    }

    public LoadingFeedBack(AppCompatActivity actvt )
    {
        progessBar  = actvt.findViewById(R.id.progressBar);
        contView    = actvt.findViewById(R.id.contView );

        if ( contView != null)
        {
            contView.setAlpha(0.3f);
        }
    }


    public void start()
    {
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                animator    = new AnimateProgressBar( progessBar );

                progessBar.setVisibility( View.VISIBLE );

                animator.begin();

                if ( contView != null)
                {
                    contView.setAlpha(0.3f);
                    contView.setEnabled(false);
                }
            }
        });
    }

    public void finish()
    {
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                progessBar.setVisibility( View.INVISIBLE );

                animator.end();

                if ( contView != null)
                {
                    contView.setAlpha(1.0f);
                    contView.setEnabled(true);
                }
            }
        });
    }
}
