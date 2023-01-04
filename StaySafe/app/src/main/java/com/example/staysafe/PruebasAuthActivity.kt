package com.example.staysafe

import fakeFirebase.FakeFirebase

object PruebasAuthActivity {

    // Función simple para comprobar condición
    fun checkEmailAndPassword(email : String, password : String): Boolean {
        return email.isNotEmpty() && password.isNotEmpty() && email.contains("@") &&
                (email.contains(".com") || email.contains(".es"))
    }

    // Función para comprobar funcionamiento genérico de setup()
    fun checkSetup(email : String, password : String, noError : Boolean, noError2: Boolean): Int {

        var fake = FakeFirebase()

        var numberShowAlert = 0;

        if (email.isNotEmpty() && password.isNotEmpty() && email.contains("@") &&
            (email.contains(".com") || email.contains(".es"))) {
            fake.insertUser(
                email,
                password
            ) // He modificado esta linea un poco para poder utilizar mi mock de firebase
            if (!fake.isEmpty() && noError) {  // Si no está vacio -> Se ha añadido correctamente. Añado error para simular que no se ha guardado
                numberShowAlert = 2
            } else {
                numberShowAlert = 3
            }
        }

        if (email.isNotEmpty() && password.isNotEmpty() && email.contains("@") &&
            (email.contains(".com") || email.contains(".es"))) {
            fake.insertUser(
                email,
                password
            ) // He modificado esta linea un poco para poder utilizar mi mock de firebase
            if (!fake.isEmpty() && noError2) {
                // Aqui se hacen los HashMap
            } else {
                numberShowAlert = 1
            }
        }

        return numberShowAlert
    }

}