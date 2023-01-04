package fakeFirebase

import com.google.firebase.firestore.auth.User
import kotlinx.coroutines.flow.Flow

interface UsersFirebase {

    fun getUsers(): MutableList<MutableList<String>>
    fun getUserByEmail(email: String, pw : String): MutableList<String>
    fun insertUser(email:String, pw:String)
    fun deleteUser(email:String, pw: String)
    fun isEmpty() : Boolean
}