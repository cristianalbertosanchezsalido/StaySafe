package com.example.staysafe

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Api
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_auth.*
import kotlinx.android.synthetic.main.activity_auth.authLayaout
import kotlinx.android.synthetic.main.activity_contacts.*
import kotlinx.android.synthetic.main.activity_settings.*


class AuthActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val GOOGLE_SIGN_IN =100
    private val db= FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        // Initialize Firebase Auth
        auth = Firebase.auth
        Thread.sleep(2000)
        setTheme(R.style.Theme_StaySafe)   //Después de la muestra del Splash recuperamos el tema por defecto
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)



        //Analytics Google
        val analytics:FirebaseAnalytics=FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message","Integración de Firebase Completa")
        analytics.logEvent("InitScreen",bundle)
        //Setup

        setup()
        session()
    }








    //FUNCION QUE MUESTRA LA PANTALLA DE INICIO
    private fun showHome(email:String,provider:ProviderType)
    {
        val homeIntent= Intent(this,HomeActivity::class.java).apply{

            //PARAMETROS A PASAR

            putExtra("email",email)
            putExtra("provider",provider.name)
        }
        startActivity(homeIntent)
    }


    //FUNCION QUE ENSEÑA UN ALERTA EN LA AUTENTICACION
    private fun showAlert()
    {

        //TODO
        //MEJORA DE ERRORES
        val builder = AlertDialog.Builder(this)
        builder.setTitle("ERROR")
        builder.setMessage("Se ha producido un error en la autenticación")
        builder.setPositiveButton("Aceptar",null)
        val dialog: AlertDialog=builder.create()
        dialog.show()
    }

    private fun showAlert2()
    {


        val builder = AlertDialog.Builder(this)
        builder.setTitle("¡REGISTRADO!")
        builder.setMessage("Pulsa ACCEDER para entrar, y recuerda si quieres utilizar todas las funcionalidades, introduce tus datos en 'perfil' y 'contactos'")
        builder.setPositiveButton("Aceptar",null)
        val dialog: AlertDialog=builder.create()
        dialog.show()
    }

    private fun showAlert3()
    {


        val builder = AlertDialog.Builder(this)
        builder.setTitle("ERROR")
        builder.setMessage("El email ya se encuentra en uso")
        builder.setPositiveButton("Aceptar",null)
        val dialog: AlertDialog=builder.create()
        dialog.show()
    }






    //FUNCIÓN QUE INICIA SESIÓN
    fun setup()
    {

        //INICIO DE SESIÓN CON EMAIL Y CONTRASEÑA

        singUpButton.setOnClickListener(){


            if(emailEditText.text.isNotEmpty()&&passwordEditText.text.isNotEmpty())
            {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailEditText.text.toString()
                    ,passwordEditText.text.toString()).addOnCompleteListener {
                        if (it.isSuccessful){

                            showAlert2()
                        }
                        else
                        {

                            showAlert3()
                        }
                }


            }
        }
        FirebaseAuth.getInstance().signOut()

        logInButton.setOnClickListener(){


            if(emailEditText.text.isNotEmpty()&&passwordEditText.text.isNotEmpty())
            {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(emailEditText.text.toString()
                    ,passwordEditText.text.toString()).addOnCompleteListener {
                    if (it.isSuccessful){
                        db.collection("users").document(emailEditText.text.toString()).set(

                            hashMapOf("provider" to "BASIC",
                                "phone" to "",
                                "help_button" to ""
                            )

                        )
                        db.collection("contacts").document(emailEditText.text.toString()).set(

                            hashMapOf(
                                "name1" to "",
                                "phone1" to "",
                                "name2" to "",
                                "phone2" to ""
                            )
                        )

                        showHome(it.result?.user?.email ?: "", ProviderType.BASIC)
                    }
                    else
                    {
                        //TODO
                            /*la alerta es genérica en ambos botones, hay que hacer una funcion con mensaje personalizado
                            en cada una*/
                        showAlert()
                    }
                }


            }
        }

        //AUTENTICACIÓN CON GOOGLE


        googleButton.setOnClickListener(){
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1007665447592-8at8pmn2tc5jk9tr49rikm51bm71c5h6.apps.googleusercontent.com")
                .requestEmail()
                .build()

            val googleClient: GoogleSignInClient = GoogleSignIn.getClient(this, gso)
            googleClient.signOut()

            startActivityForResult(googleClient.signInIntent,GOOGLE_SIGN_IN)


        }




    }




    //PARA SABER SI EL DISPOSITIVO HA INICIADO SESIÓN ANTES
    private fun session()
    {
        val prefs: SharedPreferences? = getSharedPreferences(getString(R.string.prefs_file),
            Context.MODE_PRIVATE)
        val email=prefs?.getString("email",null)
        val provider=prefs?.getString("provider",null)

        if(email != null && provider!=null)
        {
            authLayaout.visibility= View.INVISIBLE
            showHome(email,ProviderType.valueOf(provider))
        }


    }











// FUNCIONES OVERRIDE PARA INICIOS SESIÓN

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==GOOGLE_SIGN_IN)
        {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                if (account!=null)
                {
                    val credential: AuthCredential = GoogleAuthProvider.getCredential(account.idToken,null)
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                        if(it.isSuccessful)
                        {
                            account.email?.let { it1 ->
                                db.collection("users").document(it1).set(

                                    hashMapOf("provider" to ProviderType.GOOGLE,
                                        "phone" to "",
                                        "help_button" to ""
                                        )

                                )
                                db.collection("contacts").document(it1).set(

                                    hashMapOf(
                                        "name1" to "",
                                        "phone1" to "",
                                        "name2" to "",
                                        "phone2" to ""
                                    )

                                )
                            }
                            showHome(account.email?: "", ProviderType.GOOGLE)
                        }
                        else
                        {
                            showAlert()
                        }


                    }

                }
            }catch (e: ApiException){
                showAlert()
            }




        }

    }



    override fun onStart() {
        super.onStart()
        authLayaout.visibility = View.VISIBLE
        val currentUser = auth.currentUser
        if(currentUser != null){

        }

    }
}
