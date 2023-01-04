package com.example.staysafe

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class PruebasAuthActivityTest{

    // Cuando email y contraseña no son null
    @Test
    fun `when email and password are valid`(){
        val email = "hola@uma.es"
        val pw = "hola1234"

        assertTrue(PruebasAuthActivity.checkEmailAndPassword(email, pw))
    }

    // Cuando alguno es null o ambos
    @Test
    fun `when email and password are invalid`(){
        var email = ""
        var pw = "hola1234"

        assertFalse(PruebasAuthActivity.checkEmailAndPassword(email, pw))

        email = "hola@uma.es"
        pw = ""

        assertFalse(PruebasAuthActivity.checkEmailAndPassword(email, pw))

        email = ""
        pw = ""

        assertFalse(PruebasAuthActivity.checkEmailAndPassword(email, pw))
    }

    // Cuando el email no tiene @ o le falta el .com .es
    @Test
    fun `when email is invalid because lacks of special character`(){
        var email = "hola"
        var pw = "hola1234"

        assertFalse(PruebasAuthActivity.checkEmailAndPassword(email, pw))

        email = "hola@holaotravez"
        pw = "hola1234"

        assertFalse(PruebasAuthActivity.checkEmailAndPassword(email, pw))

        email = "hola.es"
        pw = "hola1234"

        assertFalse(PruebasAuthActivity.checkEmailAndPassword(email, pw))
    }

    // Funciones setup con showAlerts
    @Test
    fun `when email and password are valid invoke showAlert 2`(){
        var email = "hola@uma.es"
        var pw = "hola1234"

        assertEquals(PruebasAuthActivity.checkSetup(email, pw, true, true), 2)
    }

    @Test
    fun `when email and password are invalid invoke showAlert 3`(){
        var email = "hola@uma.es"
        var pw = "hola1234"

        assertEquals(PruebasAuthActivity.checkSetup(email, pw, false, true), 3)
    }

    @Test
    fun `when email and password are valid but error before store invoke showAlert 1`(){
        var email = "hola@uma.es"
        var pw = "hola1234"

        // noError no importa si es false o true porque lo que va a fallar es condición antes de almacenar datos -> showAlert
        assertEquals(PruebasAuthActivity.checkSetup(email, pw, true, false), 1)
        assertEquals(PruebasAuthActivity.checkSetup(email, pw, false, false), 1)
    }

    @Test
    fun `when email and password are invalid do nothing`(){
        var email = ""
        var pw = "hola1234"

        assertEquals(PruebasAuthActivity.checkSetup(email, pw, false, false), 0)
    }

}