package com.example.staysafe

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_settings.*
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog

class SettingsActivity : AppCompatActivity() {

    private val db= FirebaseFirestore.getInstance()


    override fun onBackPressed() {
        super.onBackPressed()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val bundle= intent.extras
        val email:String?=bundle?.getString("email")
        val provider:String?=bundle?.getString("provider")
        setup(email ?: "",provider ?: "")


    }
    private fun showAlert()
    {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("¡GUARDADO!")
        builder.setMessage("Recuerda que puedes cambiarlo siempre que quieras")
        builder.setPositiveButton("Aceptar",null)
        val dialog: AlertDialog =builder.create()
        dialog.show()
    }

    private fun setup(email: String, provider:String)
    {

        emailTextView.text=email
        providerTextView.text=provider
        val spinner= findViewById<Spinner>(R.id.spinner_id)
        val list = resources.getStringArray(R.array.spinner_id)
        val adapter= ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,list)
        spinner.adapter=adapter



        var posicion=0

        spinner.onItemSelectedListener= object :
            AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, id: Long) {
                posicion = position
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }


        }

        db.collection("users").document(email).get().addOnSuccessListener {
            phoneTextView.setText(it.get("phone") as String?)



        }



        logOutButton.setOnClickListener()
        {

            //BORRADO DE RECUERDO DE AUTENTICACION
            val prefs: SharedPreferences.Editor = getSharedPreferences(getString(R.string.prefs_file),
                Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()


            //LOG OUT
            FirebaseAuth.getInstance().signOut()
            finishAffinity()
        }

        saveButton.setOnClickListener()
        {

            db.collection("users").document(email).set(

                hashMapOf("provider" to provider,
                    "phone" to phoneTextView.text.toString(),
                    if (posicion==0)
                    {
                        "help_button" to ""
                    }
                    else{
                        "help_button" to list[posicion].toString()
                    })

            )
            showAlert()

        }



    }


}