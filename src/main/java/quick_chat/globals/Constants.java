package quick_chat.globals;

import android.content.Context;

import androidx.room.Room;

import java.io.File;

import quick_chat.db.AppDatabase;
import quick_chat.db.User;
import quick_chat.db.UserDao;
import quick_chat.start.StartApp;

public class Constants
{
    private static final boolean kLocalServer = false;


    private static final    String          dataBaseName    = "quick_chat_DB_V01";
    private static          AppDatabase     db              = null;

    public  static final    String  kProtocol       = kLocalServer ? "http://" : "http://";
    public  static final 	String  kTCPIPAddress   = kLocalServer ? "10.201.1.66" : "3.140.43.19";//"3.135.35.233";//"46.137.19.61";
    public  static final 	String  kTomcatAddress  = kTCPIPAddress + ":8080";
    private static          User    user            = null;

    public Constants() {

        super();
    }

    public static final 	String  kAPIEntryPoint  = kTomcatAddress + "/QuickChat/";
    public static final 	int     kInboxPort      = 17015;
    public static final 	int     kOutboxPort	    = 17013;

    public static final		String 			oubox						= "oubox_V21_";
    public static final		String 			inbox						= "inbox_V21_";
    public static final		String 			hybox						= "hybox_V21_";
    public static final		String 			arbox						= "arbox_V21_";

    public static  			File 			dataDir     = null;
    public static  			File 			extFDir     = null;

    protected static    File file = null;

    static
    {
        dataDir = StartApp.getAppFilesDir();
        extFDir = StartApp.getExtFilesDir();

        file = new File( dataDir, "Globals.json" );
    }

    public static File getOutDirFile()
    {
        File fdir = new File( extFDir, oubox + File.separator + Constants.user.uuuid );

        if ( !fdir.exists() )
        {
            fdir.mkdirs();
        }

        return fdir;
    }

    static public boolean isFirstTime( String registryUUID )
    {
        File fdir = new File( extFDir, registryUUID );

        return !fdir.exists();
    }

    static public void setFirstTime( String registryUUID )
    {
        File fdir = new File( extFDir, registryUUID );

        fdir.mkdir();
    }

    static public void clearFirstTime( String registryUUID )
    {
        File fdir = new File( extFDir, registryUUID );

        fdir.delete();
    }

    public static File getInbDirFile()
    {
        File fdir = new File( extFDir, inbox+ File.separator + Constants.user.uuuid );

        if ( !fdir.exists() )
        {
            fdir.mkdirs();
        }

        return fdir;
    }

    public static File getHysDirFile()
    {
        File fdir = new File( extFDir, hybox + File.separator + Constants.user.uuuid );

        if ( !fdir.exists() )
        {
            fdir.mkdirs();
        }

        return fdir;
    }

    public static File getArcDirFile()
    {
        File fdir = new File( extFDir, arbox + File.separator + Constants.user.uuuid );

        if ( !fdir.exists() )
        {
            fdir.mkdirs();
        }

        return fdir;
    }

    public static synchronized void setUser( Context context, User user )
    {
        final Object semaphore = new Object();

        new Thread()
        {
            public void run()
            {
                synchronized(semaphore)
                {
                    if (db == null)
                    {
                        db = Room.databaseBuilder(context, AppDatabase.class, dataBaseName).build();
                    }

                    UserDao userDao = db.userDao();

                    userDao.deleteUser();

                    userDao.insertUser(user);

                    Constants.user = user;

                    semaphore.notify();
                }
            }
        }.start();

        try
        {
            synchronized(semaphore) {
                semaphore.wait();
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public static User getUser( Context context )
    {
        if ( user != null )
        {
            return user;
        }

        final Object semaphore = new Object();

        new Thread()
        {
            public void run()
            {
                synchronized(semaphore)
                {
                    try
                    {
                        if (db == null)
                        {
                            db = Room.databaseBuilder(context, AppDatabase.class, dataBaseName).fallbackToDestructiveMigration().build();
                        }

                        UserDao userDao = db.userDao();

                        Constants.user = userDao.getUser();
                    }
                    finally
                    {
                        semaphore.notify();
                    }
                }
            }
        }.start();

        try
        {
            synchronized(semaphore) {
                semaphore.wait();
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        return user;
    }

    public static synchronized void clearUser( Context context )
    {
        final Object semaphore = new Object();

        new Thread()
        {
            public void run()
            {
                synchronized(semaphore)
                {
                    if (db == null)
                    {
                        db = Room.databaseBuilder(context, AppDatabase.class, dataBaseName).build();
                    }

                    UserDao userDao = db.userDao();

                    userDao.deleteUser();

                    Constants.user = null;

                    semaphore.notify();
                }
            }
        }.start();

        try
        {
            synchronized(semaphore) {
                semaphore.wait();
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

}
