package fakeFirebase

class FakeFirebase : UsersFirebase {

    private val users :MutableList<MutableList<String>> = mutableListOf()

    override fun getUsers(): MutableList<MutableList<String>> {
        return users
    }

    override fun getUserByEmail(email: String, pw : String): MutableList<String> {
        return users[users.indexOf(mutableListOf(email, pw))]
    }

    override fun insertUser(email:String, pw:String) {
        users.add(mutableListOf(email, pw))
    }

    override fun deleteUser(email:String, pw: String) {
        users.remove(mutableListOf(email, pw))
    }

    override fun isEmpty() : Boolean{
        return users.isEmpty()
    }

}