package quick_chat.chat_utils;


import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GeolocationUtilities
{
    final static SimpleDateFormat	dateTimeFormat		= new SimpleDateFormat("dd-MM-yyyy hh:mm aa");
    final static SimpleDateFormat	dateFormat			= new SimpleDateFormat("dd-MM-yyyy");
    final static SimpleDateFormat	timeFormat			= new SimpleDateFormat("hh:mm aa");
    final static SimpleDateFormat	hourFormat			= new SimpleDateFormat("hh:mm aa");
    final static SimpleDateFormat	minutFormat			= new SimpleDateFormat("mm");

    static public String dateTimeFormat( Date d )
    {
        return dateTimeFormat.format(d);
    }

    static public String timeFormat( Date d )
    {
        return timeFormat.format(d);
    }

    static public String dateFormat( Date d )
    {
        return dateFormat.format(d);
    }

    static public String deltaTime( long delta )
    {
        long secunds 	= delta/1000;
        long minutes 	= secunds/60;
        long hours 		= minutes / 60;

        minutes = minutes - ( hours * 60 );

        String h = String.valueOf( hours );
        String m = String.valueOf( minutes );

        if ( hours < 10 )
        {
            h = "0" + h;
        }

        if ( minutes < 10 )
        {
            m = "0" + m;
        }

        if ( hours == 0 )
        {
            return m + "min.";
        }
        else
        {
            return m + "min.";
        }
    }

    static public String currentDeltaTime(long d)
    {
        long deltaTime = d-System.currentTimeMillis();

        if ( deltaTime < 0)
        {
            deltaTime = 0;
        }

        return deltaTime( deltaTime );
    }

    static public String dateFormat(Context context, long d)
    {
        Date now = new Date();
        String todayTxt = dateFormat.format( now );
        String dateText = dateFormat.format( new Date(d) );

        if ( todayTxt.equals(dateText) )
        {
            long deltaTime = d-System.currentTimeMillis();

            return hourFormat.format( d );
        }
        else
        {
            return dateTimeFormat.format( d );
        }
    }

	/*
		public String getStatusMessage()
	{
		switch ( getStatus() )
		{
			case STORE_TAKEN:
				 return "Order taken - " 		+ GeolocationUtilities.dateFromNowFormat( getTakeOrderTime() );
			case STORE_REFUSED:
				return "Order refused - " 		+ GeolocationUtilities.dateFromNowFormat( getTakeOrderTime() );
			case DLIVR_ASIGNED:
				return "Delivery taken - " 		+ GeolocationUtilities.dateFromNowFormat( getTakeDeliveryTime() );
			case DLIVR_REFUSED:
				return "Delivery refused - " 	+ GeolocationUtilities.dateFromNowFormat( getTakeDeliveryTime() );
			case ON_TRANSIT:
				return "Order on transit - " 	+ GeolocationUtilities.dateFromNowFormat( getShippingTime() );
			case DELIVERED:
				return "Order delivered - " 	+ GeolocationUtilities.dateFromNowFormat( getShippingTime() );
			case NEW:
				return "NEW";
			default:
				return "NEW";
		}
	}

	 */
}
