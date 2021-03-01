package com.example.roominsertissue

import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.room.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        )
            .allowMainThreadQueries()
            .build()
        val userDao = db.userDao()

        // Create 3 users and insert them 1 by 1
        for (i in 0..2) {
            val user = User("first$i", "last$i")
            // First run won't throw any exceptions, but subsequent runs will. Need to catch them.
            try {
                userDao.insertUser(user)
            } catch (e: SQLiteConstraintException) {
                Log.e("Room", "onCreate: ", e)
            }
        }

        // There are now 3 User's in the database
        Log.d("Room", "### First 3 User's")
        for (u in userDao.getAll()) {
            Log.d("Room", u.toString())
        }

        // Create 10 users(some of which have the same first and last names)
        // then do a batch insert
        val userList = mutableListOf<User>()
        for (i in 0..9) {
            userList.add(
                User("first$i", "last$i")
            )
        }
        // Need to catch SQLiteConstraintException
        try {
            userDao.insertUserList(userList)
        } catch (e: SQLiteConstraintException) {
            Log.e("Room", "onCreate: ", e)
        }

        Log.d("Room", "### Next 10 User's")
        // There are still 3 User's in the database
        for (u in userDao.getAll()) {
            Log.d("Room", u.toString())
        }
    }
}

@Entity(
    indices = [
        Index(
            value = ["first_name", "last_name"],
            unique = true
        )
    ]
)
data class User(
    @ColumnInfo(name = "first_name") val firstName: String?,
    @ColumnInfo(name = "last_name") val lastName: String?
) {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
}

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<User>

    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<User>

    @Query(
        "SELECT * FROM user WHERE first_name LIKE :first AND " +
                "last_name LIKE :last LIMIT 1"
    )
    fun findByName(first: String, last: String): User

    @Insert
    fun insertUser(vararg users: User)

    @Insert
    fun insertUserList(users: List<User>)

    @Delete
    fun delete(user: User)
}

@Database(entities = arrayOf(User::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}