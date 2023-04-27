package quick_chat.adapters;


public class NotificationItem  implements java.io.Serializable
{
    /**
     *
     */
    private static final long	serialVersionUID	= -3642493315403532373L;

    public enum NotifKind
    {
        USER,
        GROUP
    }

    public NotifKind                kind                = null;
    public String                   userID              = null;
    public long                     msgUID              = 0;
    public String                   name                = null;
    public long                     issueNumber         = 0;
    public int                      nHisMsgs            = 0;
    public int						nNewMsgs			= 0;
    public boolean                  storedMessagesMode  = false;
    public long                     time                = 0;
    public String                   message;
    public int						selectedItem	    = 0;

    public NotificationItem( NotifKind                  kind,
                             String						name,
                             long						issueNumber,
                             String						userID,
                             long                       msgUID,
                             long                       time,
                             String                     message,
                             int						nMesages,
                             int						nNewMsgs,
                             boolean					storedMessagesMode)
    {
        this.kind               = kind;
        this.name               = name;
        this.issueNumber        = issueNumber;
        this.userID             = userID;
        this.msgUID             = msgUID;
        this.time               = time;
        this.message            = message;
        this.nHisMsgs = nMesages;
        this.nNewMsgs 			= nNewMsgs;
        //this.selectedItem 		= messagesVect.size()-1;
        this.storedMessagesMode = storedMessagesMode;
    }

    //public ChatMsg getSelected()
    //{
    //    return messagesVect.get( selectedItem );
    //}

}
