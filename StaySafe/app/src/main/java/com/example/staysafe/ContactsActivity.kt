package com.example.staysafe

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isInvisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_contacts.*


class ContactsActivity : AppCompatActivity() {
    private val db= FirebaseFirestore.getInstance()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        val bundle= intent.extras
        val email:String?=bundle?.getString("email")


        setup(email ?: "")
    }


    private fun setup(email: String)
    {



        saveButtonContacts.setOnClickListener()
        {
            var modificado = false

            if(editTextPhone1.text.toString().length == 9 && editTextNombre1.text.length != 0 && editTextPhone2.text.toString().length == 9 && editTextNombre2.text.length != 0){
                modificado = true
                guardarContactosVarios(editTextNombre1.text.toString(), editTextPhone1.text.toString(), editTextNombre2.text.toString(), editTextPhone2.text.toString(), email)
            }

            else if (editTextPhone1.text.toString().length == 9 && editTextNombre1.text.length != 0){
                modificado = true
                guardarContactoUnico(1, editTextNombre1.text.toString(), editTextPhone1.text.toString(), email)

            }
            else if(editTextPhone2.text.toString().length == 9 && editTextNombre2.text.length != 0){
                modificado = true
                guardarContactoUnico(1, editTextNombre2.text.toString(), editTextPhone2.text.toString(), email)

            }
            if(modificado)
                showAlertSuccess()
            else {
                editTextNombre1.text.clear()
                editTextNombre2.text.clear()
                editTextPhone1.text.clear()
                editTextPhone2.text.clear()
                showAlertError()
            }
        }

        db.collection("contacts").document(email).get().addOnSuccessListener {

            editTextNombre1.setText(it.get("name1") as String?)
            editTextNombre2.setText(it.get("name2") as String?)
            editTextPhone1.setText(it.get("phone1") as String?)
            editTextPhone2.setText(it.get("phone2") as String?)

            saveButtonContacts.setText("ACTUALIZAR")




        }



    }
    private fun showAlertSuccess()   {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("CONTACTOS")
        builder.setMessage("Se ha guardado con exito")
        builder.setPositiveButton("Aceptar",null)
        val dialog: AlertDialog =builder.create()
        dialog.show()
    }
    private fun showAlertError()   {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("CONTACTOS")
        builder.setMessage("El numero introducido no es un numero de telefono o no se ha introducido nombre")
        builder.setPositiveButton("Aceptar",null)
        val dialog: AlertDialog =builder.create()
        dialog.show()
    }

    private fun guardarContactoUnico(numero:Int, nombre:String, telefono:String, email:String) {
        if(numero == 1) {
            db.collection("contacts").document(email).set(
                hashMapOf
                    (
                    "nombre1" to nombre,
                    "phone1" to telefono,
                    "nombre2" to "",
                    "phone2" to ""
                )
            )
        }
        else{
            db.collection("contacts").document(email).set(
                hashMapOf
                    (
                    "nombre1" to "",
                    "phone1" to "",
                    "nombre2" to nombre,
                    "phone2" to telefono
                )
            )
        }

    }

    private fun guardarContactosVarios(nombre1:String, telefono1:String,nombre2:String, telefono2:String, email:String){
        db.collection("contacts").document(email).set(
            hashMapOf
                (
                 "nombre1" to nombre1,
                 "phone1" to telefono1.toString(),
                 "nombre2" to nombre2,
                 "phone2" to telefono2
            )
        )

    }

}