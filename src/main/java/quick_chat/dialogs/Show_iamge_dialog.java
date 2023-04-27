package quick_chat.dialogs;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import quick_chat.io.RetiveImagesFromRepository;
import quick_chat.start.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Show_iamge_dialog#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Show_iamge_dialog extends DialogFragment
{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String      imgOwnerID  = null;
    private String      imageName   = null;

    private ImageView   imageView   = null;

    public Show_iamge_dialog()
    {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static Show_iamge_dialog newInstance(String imgOwnerID, String imageName)
    {
        Show_iamge_dialog fragment = new Show_iamge_dialog();
        Bundle            args     = new Bundle();
        args.putString(ARG_PARAM1, imgOwnerID);
        args.putString(ARG_PARAM2, imageName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);

        if (getArguments() != null)
        {
            imgOwnerID  = getArguments().getString(ARG_PARAM1);
            imageName   = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rooyView   = inflater.inflate(R.layout.fragment_show_image_dialog, container, false);

        imageView       = rooyView.findViewById( R.id.myZoomageView);

        RetiveImagesFromRepository dir = new RetiveImagesFromRepository( imgOwnerID, imageName );

        dir.execute( getContext(), new RetiveImagesFromRepository.EventHandler()
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

        return rooyView;
    }
}