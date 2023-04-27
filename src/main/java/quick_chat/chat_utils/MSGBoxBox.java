package quick_chat.chat_utils;


import com.pojo.chatContent.ChatMsg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import quick_chat.globals.Constants;


public class MSGBoxBox
{
    private File 	filesDir  	= null;

    public MSGBoxBox( File boxFilesDir )
    {
        this.filesDir = boxFilesDir;

        if ( !filesDir.exists() )
        {
            filesDir.mkdirs();
        }

        String path = filesDir.getAbsolutePath();

        System.out.println( path );
    }

    /*
    public static Hashtable<String,File> getUUIDTable( File boxFilesDir, Hashtable<String,File> inTable )
    {
        if ( inTable == null )
        {
            inTable = new Hashtable<String,File>();
        }

        String path = boxFilesDir.getAbsolutePath();

        if ( boxFilesDir.exists() && boxFilesDir.isDirectory() )
        {
            File[] files = boxFilesDir.listFiles();

            for ( File f : files )
            {
                if ( !f.exists() )
                {
                    System.err.println("!f.exists() en getUUIDTable() ");
                }
                else
                {
                    ChatMsg msg = loadMessage(f);

                    if (msg == null)
                    {
                        System.err.println("Null nessage en getUUIDTable() ");
                    }
                    else
                    {
                        String uuid = msg.getUuid();

                        inTable.put(uuid, f);

                        msg.clear();
                    }
                }
            }
        }

        return inTable;
    }*/


    public int  getMsgsFileList( final String filter, ArrayList<File> mFiles  )
    {
        int count = 0;

        File[] fDirs = filesDir.listFiles();

        int lengthI = fDirs.length;

        for ( int i=0; i<lengthI; i++ )
        {
            //if ( senderIDNameFilter != null && senderIDNameFilter.equalsIgnoreCase() )
            File dir = fDirs[i];

            String dirName = dir.getName();

            if ( filter != null && !filter.equals(dirName) )
            {
                continue;
            }

            File[] fFiles = dir.listFiles();

            for (File file : fFiles)
            {
                if (file.getName().endsWith(".msg"))
                {
                    count++;

                    mFiles.add(file);
                }
            }
        }

        return count;
    }

    public static class NotificationFilesInfo
    {
        public int      filesCount  = 0;
        public File     lastFile    = null;
        public ChatMsg  lastMessage = null;
    }

    public Hashtable<String,NotificationFilesInfo> getMsgsFile4Notification()
    {
        Hashtable<String,NotificationFilesInfo> mFiles = new Hashtable<String,NotificationFilesInfo>();

        File[] fDirs = filesDir.listFiles();

        int lengthI = fDirs.length;

        for ( int i=0; i<lengthI; i++ )
        {
            //if ( senderIDNameFilter != null && senderIDNameFilter.equalsIgnoreCase() )
            File dir = fDirs[i];

            File[] fFiles = dir.listFiles();

            NotificationFilesInfo nfi = new NotificationFilesInfo();

            {
                int lengthJ = fFiles.length;

                for ( int j=0; j<lengthJ; j++ )
                {
                    File file = fFiles[j];

                    if (file.getName().endsWith(".msg"))
                    {
                        nfi.filesCount++;

                        if ( nfi.lastFile == null )
                        {
                            nfi.lastFile = file;
                        }
                        else
                        {
                            if ( (nfi.lastFile.getName().compareTo( file.getName()) < 0 ) )
                            {
                                nfi.lastFile = file;
                            }
                        }
                    }
                }

                if ( nfi.lastFile != null )
                {
                    nfi.lastMessage = loadMessage( nfi.lastFile  );

                    if ( nfi.lastMessage != null )
                    {
                        String discriminCode = nfi.lastMessage.getChatDiscriminationCode();

                        mFiles.put(discriminCode, nfi);
                    }
                }
            }
        }

        return mFiles;
    }


    public int getMessages(final String filter, ArrayList<ChatMsg> messages)
    {
        final ArrayList<File> mFiles = new  ArrayList<File>( 1024 );

        int count  = getMsgsFileList( filter, mFiles );

        if ( mFiles != null )
        {
            int size = mFiles.size();

            if ( size > 0 )
            {
                for ( int i=0; i<size; i++ )
                {
                    messages.add( loadMessage( mFiles.get( i ) ) );
                }
            }
        }

        return count;
    }

    public static ChatMsg loadMessage(File file)
    {
        ChatMsg chatMsg = null;

        FileInputStream fileIn = null;

        try
        {
            fileIn = new FileInputStream( file );

            try (DataInputStream in = new DataInputStream(fileIn))
            {
                chatMsg = (ChatMsg) ChatMsg.fromJSON(in);

                chatMsg.setFilePath(file.getAbsolutePath());
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        finally
        {
            try
            {
                if ( fileIn != null )
                {
                    fileIn.close();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return chatMsg;
    }

    public File pushMessage( ChatMsg message )
    {
        File 				file 		= null;
        DataOutputStream 	dos 		= null;

        try
        {
            String fileName = message.calcMSGFileName();

            file = new File( filesDir, fileName );

            message.setFilePath( file.getAbsolutePath() );

            if ( file.exists() )
            {
                file.delete();
            }

            File parentFolder = file.getParentFile();

            if ( !parentFolder.exists() )
            {
                parentFolder.mkdir();
            }

            message.setFilePath( file.getAbsolutePath() );

            dos = new DataOutputStream( new FileOutputStream( file ) );

            message.toJSON( dos );

            dos.flush();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if ( dos != null )
                {
                    dos.close();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return file;
    }

    public int getSize()
    {
        int count = 0;

        File[] fDirs = filesDir.listFiles();

        int lengthI = fDirs.length;

        for ( int i=0; i<lengthI; i++ )
        {
            File dir = fDirs[i];

            count += dir.listFiles().length;
        }

        return	count;
    }


    public static boolean deleteIfOld(ChatMsg cm)
    {
        if ( cm.time2Delete() )
        {
            cm.deleteFile();

            return true;
        }

        return false;
    }

    private static int addMessages2VectMap( ConcurrentHashMap<String,ArrayList<ChatMsg>> messagesMap, ArrayList<ChatMsg> messages, String filer, boolean deleteOlds)
    {
        int count = 0;

        if ( messages != null )
        {
            for ( ChatMsg message : messages )
            {
                if ( message == null )
                {
                    continue;
                }

                if ( deleteOlds && deleteIfOld( message) )
                {

                }
                else
                {
                    String	discriminName = message.getChatDiscriminationCode();

                    if ( filer == null || filer.equalsIgnoreCase( discriminName ) )
                    {
                        ArrayList<ChatMsg> arrList = messagesMap.get( discriminName );

                        if ( arrList == null )
                        {
                            arrList = new ArrayList<ChatMsg>();

                            messagesMap.put( discriminName, arrList );

                            count++;
                        }

                        arrList.add(message);
                    }
                }
            }
        }

        return count;
    }

    public static ArrayList<ChatMsg> createMessagesArrayList( String filter, boolean storedessgesMode, boolean onlyLast )
    {
        ArrayList<ChatMsg> msgArrayList = new ArrayList<ChatMsg>();

        ConcurrentHashMap<String, ArrayList<ChatMsg>> messagesMap = MSGBoxBox.createMessagesMap( filter, storedessgesMode, onlyLast);

        Iterator<ArrayList<ChatMsg>> iter = messagesMap.values().iterator();

        while ( iter.hasNext() )
        {
            ArrayList<ChatMsg> messages = iter.next();

            for (ChatMsg message : messages)
            {
                if (message != null)
                {
                    msgArrayList.add(message);
                }
            }
        }

        return msgArrayList;
    }


    public static ConcurrentHashMap<String, ArrayList<ChatMsg>> createMessagesMap(  String filter, boolean storedMessagesMode, boolean onlyLast )
    {
        ConcurrentHashMap<String, ArrayList<ChatMsg>> messagesMap = new ConcurrentHashMap<String, ArrayList<ChatMsg>>();

        if ( storedMessagesMode )
        {
            MSGBoxBox archivedMSGBoxBox = new MSGBoxBox( Constants.getArcDirFile() );

            ArrayList<ChatMsg> messages = new ArrayList<ChatMsg>();

            archivedMSGBoxBox.getMessages(null, messages);

            addMessages2VectMap( messagesMap, messages, filter, true );
        }
        else
        {
            MSGBoxBox outgoingMSGBoxBox = new MSGBoxBox(Constants.getOutDirFile());
            MSGBoxBox incomingMSGBoxBox = new MSGBoxBox(Constants.getInbDirFile());
            MSGBoxBox hystoricMSGBoxBox = new MSGBoxBox(Constants.getHysDirFile());

            ArrayList<ChatMsg> messages = new ArrayList<ChatMsg>();

            incomingMSGBoxBox.getMessages(filter, messages);
            outgoingMSGBoxBox.getMessages(filter, messages);
            hystoricMSGBoxBox.getMessages(filter, messages);

            addMessages2VectMap(messagesMap, messages, filter, false );
        }

        return messagesMap;
    }

    /*
    public static int countIncommingMessages( String filter, boolean storedMessagesMode)
    {
        int countValue = 0;

        ConcurrentHashMap<String, ArrayList<ChatMsg>> messagesMap = new ConcurrentHashMap<String, ArrayList<ChatMsg>>();

        if ( storedMessagesMode )
        {
            MSGBoxBox archivedMSGBoxBox = new MSGBoxBox( Constants.getArcDirFile() );

            countValue = addMessages2VectMap(messagesMap, archivedMSGBoxBox.getMessages(null), filter, true);
        }
        else
        {
            MSGBoxBox incomingMSGBoxBox = new MSGBoxBox(Constants.getInbDirFile());

            countValue = addMessages2VectMap(messagesMap, incomingMSGBoxBox.getMessages(null), filter, false);
        }

        return countValue;
    }*/

}
