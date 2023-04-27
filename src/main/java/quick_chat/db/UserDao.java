package quick_chat.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface UserDao
{
    @Query("SELECT * FROM user LIMIT 1")
    User getUser();

    @Insert
    void insertUser(User users);

    @Query("DELETE FROM user")
    void deleteUser();

    /**
     * Updating only kind
     * By user id
     */
    @Query("UPDATE user SET kind=:value WHERE uid=:id")
    void setKind(String value, String id);

}