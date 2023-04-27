package quick_chat.animators;

import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

public class AnimateProgressBar extends Thread
{
    private ProgressBar progressBar         = null;
    private boolean     quit                = false;
    private Handler     progressBarHandler  = new Handler();
    private int         progressBarStatus   = 0;

    public AnimateProgressBar(ProgressBar progressBar)
    {
        this.progressBar    = progressBar;
    }

    public void begin()
    {
        start();
    }

    public void end()
    {
        quit = true;
    }

    public void run()
    {
        progressBarHandler.post(new Runnable()
        {
            public void run()
            {
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        while (!quit)
        {
            try
            {
                Thread.sleep(100);
            } catch (InterruptedException e) {}

            progressBarHandler.post(new Runnable()
            {
                public void run()
                {
                    progressBar.setProgress( progressBarStatus++ );

                    if ( progressBarStatus == 100 )
                    {
                        progressBarStatus = 0;
                    }
                }
            });
        }
        try
        {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}

        progressBarHandler.post(new Runnable()
        {
            public void run()
            {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

}
