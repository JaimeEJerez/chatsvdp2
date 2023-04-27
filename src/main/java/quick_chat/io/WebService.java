package quick_chat.io;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import quick_chat.JSONResponse;

public class WebService
{
    static final String COOKIES_HEADER = "Set-Cookie";
    static final String COOKIE = "Cookie";

    CookieManager msCookieManager = null;

    private static int responseCode;

    private String                          urlStr          = null;
    private LinkedTreeMap<String, String>   payload         =  new LinkedTreeMap<String, String>();

    public WebService( String urlStr )
    {
        this.urlStr     = urlStr;

        msCookieManager = new CookieManager();
    }

    public WebService( String urlStr, String cookieName, String cookieValue )
    {
        this.urlStr     = urlStr;

        msCookieManager = new CookieManager();

        if ( cookieName != null && cookieValue != null )
        {
            addCookie(cookieName, cookieValue);
        }
    }

    public void addCookie( String cookieName, String cookieValue )
    {
        URI uri = URI.create("https://quick_chat.com");

        Map<String, List<String>> header = new HashMap<>();

        List<String> value = new ArrayList<>();

        value.add(cookieName + "=" + cookieValue + "; domain=.quick_chat.com; path=/;");

        header.put("Set-Cookie", value);

        try
        {
            msCookieManager.put(uri, header);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public JSONResponse sendPost()
    {
        JSONResponse response = null;

        try
        {
            URL url = new URL(urlStr);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");

            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            if (msCookieManager != null && msCookieManager.getCookieStore().getCookies().size() > 0)
            {
                //While joining the Cookies, use ',' or ';' as needed. Most of the server are using ';'
                conn.setRequestProperty(COOKIE , TextUtils.join(";", msCookieManager.getCookieStore().getCookies()));
            }

            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(os, "UTF-8"));

            if ( payload != null && payload.size() > 0 )
            {
                Gson gson =   new GsonBuilder().create();

                String jsonTxt = gson.toJson( payload ).toString();

                writer.write( jsonTxt );
            }

            writer.flush();
            writer.close();

            int responseCode = conn.getResponseCode();

            setResponseCode( responseCode );

            os.close();

            String jsonTxt = "";

            if ( responseCode == HttpsURLConnection.HTTP_OK)
            {
                String         line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                while ((line = br.readLine()) != null)
                {
                    jsonTxt += line;
                }

                Gson gson =   new GsonBuilder().create();

                response = gson.fromJson( jsonTxt, JSONResponse.class );
            }
            else
                {
                    response = JSONResponse.not_success( 71, "WebService response code:"+ getResponseCode() );
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();

            response = JSONResponse.not_success( 71, "WebService error:"+ e.getMessage() );
        }

        return response;
    }


    // HTTP GET request
    public JSONResponse sendGet()
    {
        JSONResponse response = null;
        Gson gson =   new GsonBuilder().create();

        try
        {
            URL obj = new URL(urlStr);

            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            //add request header
            con.setRequestProperty( "User-Agent", "Mozilla" );
            /*
             * https://stackoverflow.com/questions/16150089/how-to-handle-cookies-in-httpurlconnection-using-cookiemanager
             * Get Cookies form cookieManager and load them to connection:
             */

            if ( msCookieManager.getCookieStore().getCookies().size() > 0 )
            {
                //While joining the Cookies, use ',' or ';' as needed. Most of the server are using ';'
                con.setRequestProperty( COOKIE , TextUtils.join(";", msCookieManager.getCookieStore().getCookies() ) );
            }

            /*
             * https://stackoverflow.com/questions/16150089/how-to-handle-cookies-in-httpurlconnection-using-cookiemanager
             * Get Cookies form response header and load them to cookieManager:
             */
            Map<String, List<String>> headerFields  = con.getHeaderFields();

            List<String>              cookiesHeader = headerFields.get(COOKIES_HEADER);

            if (cookiesHeader != null)
            {
                for (String cookie : cookiesHeader)
                {
                    msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                }
            }

            setResponseCode(con.getResponseCode());

            String jsonTxt = "";

            if (getResponseCode() == HttpsURLConnection.HTTP_OK)
            {
                String         line;
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                while ((line = br.readLine()) != null)
                {
                    jsonTxt += line;
                }

                response = gson.fromJson( jsonTxt, JSONResponse.class );
            }
            else
            {
                response = JSONResponse.not_success( 72, "WebService error:"+ getResponseCode() );
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();

            response = JSONResponse.not_success( 71, "WebService error:"+ e.getMessage() );
        }



        return response;
    }

    public static void setResponseCode(int responseCode)
    {
        WebService.responseCode = responseCode;
        //Log.i("Milad", "responseCode" + responseCode);
    }

    public void put( String key, String value )
    {
        payload.put( key, value );
    }
    public static int getResponseCode()
    {
        return responseCode;
    }
}