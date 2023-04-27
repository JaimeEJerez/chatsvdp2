package quick_chat.adapters;

import android.annotation.SuppressLint;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import quick_chat.start.R;

public class BaseNotificationAdapter extends RecyclerView.Adapter<BaseNotificationAdapter.ViewHolder>
{

    public static abstract class OnClickListener
    {
        public abstract void onClick( NotificationItem notifVect, View view, int posistion );
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private View        rootView;
        private ImageView   imagView;
        private TextView    sndrTView;
        private TextView    mssgTView;
        private TextView    textNumber;
        private TextView    timeTView;

        public ViewHolder(View view)
        {
            super(view);

            rootView    = view;
            imagView    = view.findViewById(R.id.imageView1);
            sndrTView   = view.findViewById(R.id.notif_senderTextView);
            textNumber  = view.findViewById(R.id.notif_numberTextView );
            timeTView   = view.findViewById(R.id.notif_timeTextView );
            mssgTView   = view.findViewById(R.id.notif_messageTextView );
        }

        public View getRootView()
        {
            return rootView;
        }

        public ImageView getImagView() {
            return imagView;
        }

        public TextView getSndrTView() {
            return sndrTView;
        }

        public TextView getMssgTView() {
            return mssgTView;
        }

        public TextView getTextNumber() {
            return textNumber;
        }

        public TextView getTimeTView() {
            return timeTView;
        }
    }

    private ArrayList<NotificationItem> notifVect = null;
    private OnClickListener             onClickListener = null;

    public BaseNotificationAdapter( ArrayList<NotificationItem> notifVect, OnClickListener onClickListener  )
    {
        this.notifVect             = notifVect;
        this.onClickListener        = onClickListener;
    }

    public void setNotifVect(ArrayList<NotificationItem> notifVect)
    {
        this.notifVect = notifVect;

        this.notifyDataSetChanged();

        int size = notifVect.size();

        System.out.println( "size:" + size );
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder( ViewGroup viewGroup, int viewType )
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.notification_item, viewGroup, false);

        viewGroup.getContext();

        return new ViewHolder(view);
    }

    private String  calcElapsedTime( long milis )
    {
        if ( milis == 0 )
        {
            return "";
        }

        long elapsed = (System.currentTimeMillis()-milis)/1000;

        if (elapsed < 60)
        {
            return "Ahora";
        }

        elapsed = elapsed / 60;
        if (elapsed < 60)
        {
            return elapsed + " min";
        }

        elapsed = elapsed / 60;
        if (elapsed < 24)
        {
            return elapsed + " hor";
        }

        elapsed = elapsed / 24;

        return elapsed + " dia(s)";
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, @SuppressLint("RecyclerView") int position)
    {
        NotificationItem notif = notifVect.get( position );

        String elapsed  = calcElapsedTime(  notif.time );

        Spanned msgHtmlTst = Html.fromHtml(notif.message == null ? "" : notif.message);

        viewHolder.getSndrTView().setText( notif.name );
        viewHolder.getTextNumber().setText( String.valueOf(notif.nHisMsgs) + "/" + String.valueOf(notif.nNewMsgs) );
        viewHolder.getMssgTView().setText( msgHtmlTst );
        viewHolder.getTimeTView().setText( elapsed );

        View rootView = viewHolder.getRootView();

        if ( (notif.nHisMsgs == 0 && notif.nNewMsgs > 1) ||
             (notif.nHisMsgs >  0 && notif.nNewMsgs > 0)  )
        {
            rootView.setBackgroundColor(rootView.getResources().getColor( R.color.light_light_blue) );
        }
        else
        {
            rootView.setBackgroundColor( rootView.getResources().getColor( R.color.light_light_gray ) );
        }

        //ImageView imageView = viewHolder.getImagView();

        /*
        Bitmap bitmap = Utils.bitmap_from_DIRECTORY_PICTURES( viewHolder.rootView.getContext(), notif.id );

        if ( bitmap == null )
        {
            RetiveImagesFromRepository dir = new RetiveImagesFromRepository( notif. );

            dir.execute( viewHolder.rootView.getContext(), new RetiveImagesFromRepository.EventHandler()
            {
                @Override
                public void finish( Bitmap bitmap, String error)
                {
                    if ( error == null && bitmap != null )
                    {
                        imageView.setImageBitmap( bitmap );
                    }
                }
            });
        }
        else
        {
            imageView.setImageBitmap(bitmap);
        }
        */

        viewHolder.getRootView().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                NotificationItem ntf = notifVect.get( position );

                onClickListener.onClick( ntf, v, position );

                viewHolder.getTextNumber().setText( String.valueOf( ntf.nHisMsgs + ntf.nNewMsgs ) + "/0"  );

                ntf.nHisMsgs += ntf.nNewMsgs;

                ntf.nNewMsgs = 0;

                rootView.setBackgroundColor( rootView.getResources().getColor( R.color.light_light_gray ) );            }
        });

    }


    @Override
    public int getItemCount()
    {
        return notifVect.size();
    }
}
