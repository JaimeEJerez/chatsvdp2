package quick_chat.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class User
{
    public static enum USER_KIND
    {
        USERTYPE_A,
        USERTYPE_B,
        USERTIPE_C,
        ADMIN,
        SUPER_ADMIN
    }

    @PrimaryKey
    public int uid;

    @ColumnInfo(name = "level")
    public String level;

    @ColumnInfo(name = "kind")
    public String kind;

    @ColumnInfo(name = "uuuid")
    public String uuuid;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "password")
    public String password;

    @ColumnInfo(name = "nickname")
    public String nickname;

    @ColumnInfo(name = "first_name")
    public String firstName;

    @ColumnInfo(name = "last_name")
    public String lastName;

    public String getVisibleName()
    {
        if ( nickname != null )
        {
            return nickname;
        }

        if( firstName !=null )
        {
            return firstName;
        }

        if( lastName !=null )
        {
            return lastName;
        }

        return email;
    }
}
