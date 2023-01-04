package com.example.staysafe

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.SyncStateContract
import android.telephony.SmsManager
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.google.android.gms.common.api.Status
import com.google.android.gms.common.internal.Constants
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.DistanceMatrixApi
import com.google.maps.GeoApiContext
import com.google.maps.model.TravelMode
import kotlinx.android.synthetic.main.activity_home.*
import java.lang.Exception
import java.sql.Time
import java.util.*


enum class ProviderType{
    BASIC,
    GOOGLE
}



class HomeActivity : AppCompatActivity() {


    private val db= FirebaseFirestore.getInstance()




    //HELP
    private var destination_address1 :String =""
    private var destination_address2 :String =""
    private var ubicacion:String="Coordenadas: 36.715472222222, -4.4751111111111 "
    private var phone:String=""

    //ROUTE
    lateinit var countdown_timer: CountDownTimer
    var isRunning: Boolean = false;
    var time_in_milli_seconds = 0L


    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()

    }


    override fun onRestart() {

        var origLatLng  = ""

        super.onRestart()

        //GOOGLE API PLACES AUTOCOMPLETE

        //ORIGEN

        val apiKey = getString(R.string.android_sdk_places_apikey)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey);
        }
        val placesClient = Places.createClient(this)
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                //Log.i(TAG, "Place: ${place.name}, ${place.id}")

                origLatLng = "${place.latLng.latitude}, ${place.latLng.longitude}"
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.

                Log.i(TAG, "An error occurred: $status")
            }
        })
        


        //DESTINO

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey);
        }
        val autocompleteFragment2 =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment2)
                    as AutocompleteSupportFragment
        autocompleteFragment2.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
        autocompleteFragment2.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: ${place.name}, ${place.id}")

                //val rnds = (20..35).random()
                //time_edit_text.setText(rnds.toString())
                /**Parte modificada para que Google Maps calcule el tiempo automáticamente*/
                try{
                    val context = GeoApiContext.Builder().
                    apiKey(apiKey).build()

                    val req = DistanceMatrixApi.newRequest(context)
                    val destination = "${place.latLng.latitude}, ${place.latLng.longitude}"
                    val trix = req.origins(origLatLng).destinations(destination).
                    mode(TravelMode.WALKING).await()

                    var tiempo = trix.rows[0].elements[0].duration.inSeconds / 60
                    time_edit_text.setText(tiempo.toString())
                }
                catch(e : Exception) {
                    e.message?.let { Log.e("Time", it) }
                }
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: $status")
            }
        })



        //RECUPERADO DE INFORMACIÓN
        val bundle= intent.extras
        val email: String? =bundle?.getString("email")
        val provider:String?=bundle?.getString("provider")
        //


        //GUARDADO DE DATOS PARA PRÓXIMOS INICIOS DE SESION
        val prefs: SharedPreferences.Editor = getSharedPreferences(getString(R.string.prefs_file),
            MODE_PRIVATE
        ).edit()
        prefs.putString("email", email)
        prefs.putString("provider",provider)
        prefs.apply()

        var boton:String=""

        //RECOLECTADO DE INFO DE LA BBDD
        if (email != null) {
            db.collection("users").document(email).get().addOnSuccessListener {
                boton=it.get("help_button") as String
                phone = it.get("phone") as String
            }

            db.collection("contacts").document(email).get().addOnFailureListener {

            }
            db.collection("contacts").document(email).get().addOnSuccessListener {
                destination_address1=it.get("phone1") as String
                destination_address2=it.get("phone2") as String
            }


        }



        //BUTTONS ACTIONS

        btnBlue.setOnClickListener(){
            if (boton=="Botón Azul")
            {

                if(ActivityCompat.checkSelfPermission(this , android.Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.SEND_SMS),1)
                }
                if(destination_address1!="")
                {
                    var obj=SmsManager.getDefault()
                    obj.sendTextMessage(destination_address1,null,"ME ENCUENTRO EN PELIGRO,"+ ubicacion,null,null)
                    Thread.sleep(1000L)
                    if (destination_address2!="")
                    {
                        var obj2=SmsManager.getDefault()
                        obj2.sendTextMessage(destination_address2,null,"ME ENCUENTRO EN PELIGRO,"+ ubicacion,null,null)

                    }

                }

            }
        }
        btnPurple.setOnClickListener(){
            if (boton=="Botón Morado")
            {
                if(ActivityCompat.checkSelfPermission(this , android.Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.SEND_SMS),1)
                }
                if(destination_address1!="")
                {
                    var obj=SmsManager.getDefault()
                    obj.sendTextMessage(destination_address1,null,"ME ENCUENTRO EN PELIGRO,"+ ubicacion,null,null)
                    Thread.sleep(1000L)
                    if (destination_address2!="")
                    {
                        var obj2=SmsManager.getDefault()
                        obj2.sendTextMessage(destination_address2,null,"ME ENCUENTRO EN PELIGRO,"+ ubicacion,null,null)

                    }

                }

            }
        }
        btnOrange.setOnClickListener(){
            if (boton=="Botón Naranja")
            {
                if(ActivityCompat.checkSelfPermission(this , android.Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.SEND_SMS),1)
                }
                if(destination_address1!="")
                {
                    var obj=SmsManager.getDefault()
                    obj.sendTextMessage(destination_address1,null,"ME ENCUENTRO EN PELIGRO,"+ ubicacion,null,null)
                    Thread.sleep(1000L)
                    if (destination_address2!="")
                    {
                        var obj2=SmsManager.getDefault()
                        obj2.sendTextMessage(destination_address2,null,"ME ENCUENTRO EN PELIGRO,"+ ubicacion,null,null)

                    }

                }
            }
        }
        settingsButton.setOnClickListener(){

            showSettings(email ?: "",provider ?: "")

        }
        ContactsButton.setOnClickListener(){

            showContacts(email ?: "")

        }
        helpButton.setOnClickListener(){

            if(ActivityCompat.checkSelfPermission(this , android.Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.SEND_SMS),1)
            }
            if(destination_address1!="")
            {
                var obj=SmsManager.getDefault()
                obj.sendTextMessage(destination_address1,null,"ME ENCUENTRO EN PELIGRO,"+ ubicacion,null,null)
                Thread.sleep(1000L)
                if (destination_address2!="")
                {
                    var obj2=SmsManager.getDefault()
                    obj2.sendTextMessage(destination_address2,null,"ME ENCUENTRO EN PELIGRO,"+ ubicacion,null,null)

                }

            }


        }
        btnIniciarRuta.setOnClickListener(){

            if (isRunning) {
                pauseTimer()
            } else {
                val time  = time_edit_text.text.toString()
                time_in_milli_seconds = time.toLong() *60000L
                startTimer(time_in_milli_seconds,destination_address1,destination_address2)
                if (time_in_milli_seconds>600000L)
                {
                    notificaciones(destination_address1,destination_address2)
                }
            }


        }
        btnPararRuta.setOnClickListener {
            resetTimer()
            showNotification()

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        var origLatLng = ""

        //GOOGLE API PLACES AUTOCOMPLETE

        //ORIGEN

            val apiKey = getString(R.string.android_sdk_places_apikey)
            if (!Places.isInitialized()) {
                Places.initialize(applicationContext, apiKey);
            }
            val placesClient = Places.createClient(this)
            val autocompleteFragment =
                supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                        as AutocompleteSupportFragment
            autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
            autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
                override fun onPlaceSelected(place: Place) {
                    // TODO: Get info about the selected place.
                    Log.i(TAG, "Place: ${place.name}, ${place.id}")
                    origLatLng = "${place.latLng.latitude}, ${place.latLng.longitude}"
                }

                override fun onError(status: Status) {
                    // TODO: Handle the error.

                    Log.i(TAG, "An error occurred: $status")
                }
            })


        //DESTINO

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey);
        }
        val autocompleteFragment2 =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment2)
                    as AutocompleteSupportFragment
        autocompleteFragment2.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
        autocompleteFragment2.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            @SuppressLint("LongLogTag")
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: ${place.name}, ${place.id}")

                //val rnds = (20..35).random()
                //time_edit_text.setText(rnds.toString())
                /**Parte modificada para que Google Maps calcule el tiempo automáticamente*/
                try{
                    val context = GeoApiContext.Builder().
                    apiKey(apiKey).build()

                    val req = DistanceMatrixApi.newRequest(context)
                    val destination = "${place.latLng.latitude}, ${place.latLng.longitude}"
                    val trix = req.origins(origLatLng).destinations(destination).
                    mode(TravelMode.WALKING).await()

                    var tiempo = trix.rows[0].elements[0].duration.inSeconds / 60
                    time_edit_text.setText(tiempo.toString())
                }
                catch(e : Exception) {
                    e.message?.let { Log.e("Time", it) }
                }
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: $status")
            }
        })



        //RECUPERADO DE INFORMACIÓN
        val bundle= intent.extras
        val email: String? =bundle?.getString("email")
        val provider:String?=bundle?.getString("provider")
        //


        //GUARDADO DE DATOS PARA PRÓXIMOS INICIOS DE SESION
        val prefs: SharedPreferences.Editor = getSharedPreferences(getString(R.string.prefs_file),
            MODE_PRIVATE
        ).edit()
        prefs.putString("email", email)
        prefs.putString("provider",provider)
        prefs.apply()

        var boton:String=""

        //RECOLECTADO DE INFO DE LA BBDD
        if (email != null) {
            db.collection("users").document(email).get().addOnSuccessListener {
                boton=it.get("help_button") as String
                phone = it.get("phone") as String
            }

            db.collection("contacts").document(email).get().addOnFailureListener {

            }
            db.collection("contacts").document(email).get().addOnSuccessListener {
                destination_address1=it.get("phone1") as String
                destination_address2=it.get("phone2") as String
            }


        }



        //BUTTONS ACTIONS

        btnBlue.setOnClickListener(){
            if (boton=="Botón Azul")
            {

                if(ActivityCompat.checkSelfPermission(this , android.Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.SEND_SMS),1)
                }
                if(destination_address1!="")
                {
                    var obj=SmsManager.getDefault()
                    obj.sendTextMessage(destination_address1,null,"ME ENCUENTRO EN PELIGRO,"+ ubicacion,null,null)
                    Thread.sleep(1000L)
                    if (destination_address2!="")
                    {
                        var obj2=SmsManager.getDefault()
                        obj2.sendTextMessage(destination_address2,null,"ME ENCUENTRO EN PELIGRO,"+ ubicacion,null,null)

                    }

                }

            }
        }
        btnPurple.setOnClickListener(){
            if (boton=="Botón Morado")
            {
                if(ActivityCompat.checkSelfPermission(this , android.Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.SEND_SMS),1)
                }
                if(destination_address1!="")
                {
                    var obj=SmsManager.getDefault()
                    obj.sendTextMessage(destination_address1,null,"ME ENCUENTRO EN PELIGRO,"+ ubicacion,null,null)
                    Thread.sleep(1000L)
                    if (destination_address2!="")
                    {
                        var obj2=SmsManager.getDefault()
                        obj2.sendTextMessage(destination_address2,null,"ME ENCUENTRO EN PELIGRO,"+ ubicacion,null,null)

                    }

                }

            }
        }
        btnOrange.setOnClickListener(){
            if (boton=="Botón Naranja")
            {
                if(ActivityCompat.checkSelfPermission(this , android.Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.SEND_SMS),1)
                }
                if(destination_address1!="")
                {
                    var obj=SmsManager.getDefault()
                    obj.sendTextMessage(destination_address1,null,"ME ENCUENTRO EN PELIGRO,"+ ubicacion,null,null)
                    Thread.sleep(1000L)
                    if (destination_address2!="")
                    {
                        var obj2=SmsManager.getDefault()
                        obj2.sendTextMessage(destination_address2,null,"ME ENCUENTRO EN PELIGRO,"+ ubicacion,null,null)

                    }

                }
            }
        }
        settingsButton.setOnClickListener(){

            showSettings(email ?: "",provider ?: "")

        }
        ContactsButton.setOnClickListener(){

            showContacts(email ?: "")

        }
        helpButton.setOnClickListener(){

            if(ActivityCompat.checkSelfPermission(this , android.Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.SEND_SMS),1)
            }
            if(destination_address1!="")
            {
                var obj=SmsManager.getDefault()
                obj.sendTextMessage(destination_address1,null,"ME ENCUENTRO EN PELIGRO,"+ ubicacion,null,null)
                Thread.sleep(1000L)
                if (destination_address2!="")
                {
                    var obj2=SmsManager.getDefault()
                    obj2.sendTextMessage(destination_address2,null,"ME ENCUENTRO EN PELIGRO,"+ ubicacion,null,null)

                }

            }


        }
        btnIniciarRuta.setOnClickListener(){

            if (isRunning) {
                pauseTimer()
            } else {
                val time  = time_edit_text.text.toString()
                time_in_milli_seconds = time.toLong() *60000L
                startTimer(time_in_milli_seconds,destination_address1,destination_address2)
                if (time_in_milli_seconds>600000L)
                {
                    notificaciones(destination_address1,destination_address2)
                }
            }


        }
        btnPararRuta.setOnClickListener {
            resetTimer()
            showNotification()

        }


    }




    



    //ROUTE FUNCTIONS
    private fun startTimer(time_in_seconds: Long,destination_1:String, destination_2:String) {
        countdown_timer = object : CountDownTimer(time_in_seconds, 1000) {
            override fun onFinish() {

                showAlert()

                if(destination_address1!="")
                {
                    var obj=SmsManager.getDefault()
                    obj.sendTextMessage(destination_address1,null,"ME ENCUENTRO EN PELIGRO,"+ ubicacion,null,null)
                    Thread.sleep(1000L)
                    if (destination_address2!="")
                    {
                        var obj2=SmsManager.getDefault()
                        obj2.sendTextMessage(destination_address2,null,"ME ENCUENTRO EN PELIGRO,"+ ubicacion,null,null)

                    }

                }
                time_in_milli_seconds = 0
                updateTextUI()
                btnIniciarRuta.isEnabled=true
                btnPararRuta.isVisible=false
                isRunning = false
            }

            override fun onTick(p0: Long) {
                time_in_milli_seconds = p0
                updateTextUI()
            }
        }
        countdown_timer.start()

        isRunning = true
        btnIniciarRuta.isEnabled=false
        btnPararRuta.isVisible = true
    }
    private fun pauseTimer()  {

        countdown_timer.cancel()
        isRunning = false
        btnPararRuta.isVisible=true
    }
    private fun resetTimer()  {
        time_in_milli_seconds = 0
        updateTextUI()
        btnIniciarRuta.isEnabled=true
        btnPararRuta.isVisible=false
        countdown_timer.cancel()
        isRunning = false
    }
    private fun updateTextUI(){
        var hour = (time_in_milli_seconds / 1000) / 3600
        var minute = (time_in_milli_seconds / 1000) / 60
        var seconds = (time_in_milli_seconds / 1000) % 60

        if(time_in_milli_seconds==0L)
        {
            timer.text="00:00:00"
        }
        else if(time_in_milli_seconds<36000000L)
        {
            if(time_in_milli_seconds>3600000L&&time_in_milli_seconds<4200000L)
            {
                minute-=60
                timer.text = "0$hour:0$minute:$seconds"
            }
            else if(time_in_milli_seconds>4200000L&&time_in_milli_seconds<7200000L){
                minute-=60
                timer.text = "0$hour:$minute:$seconds"
            }
            else if(time_in_milli_seconds>7200000L){
                minute-=120
                timer.text = "0$hour:0$minute:$seconds"
            }
            else if(time_in_milli_seconds<600000L)
            {
                timer.text = "0$hour:0$minute:$seconds"
            }

            else{

                timer.text = "0$hour:$minute:$seconds"

            }


        }

        else{
            timer.text = "$hour:$minute:$seconds"

        }
    }
    private fun showNotification()   {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("¡Genial!")
        builder.setMessage("Has llegado a tu destino")
        builder.setPositiveButton("Aceptar",null)
        val dialog: AlertDialog =builder.create()
        dialog.show()
    }
    private fun showAlert()   {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("ATENCIÓN")
        builder.setMessage("El tiempo ha finalizado, enviando mensaje a tus contactos...")
        builder.setPositiveButton("Aceptar",null)
        val dialog: AlertDialog =builder.create()
        dialog.show()
    }
    private fun showWarning() :Boolean  {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("EY")
        builder.setMessage("¿Va todo bien?, pulsa aceptar ")
        builder.setPositiveButton("Aceptar",null)

        val dialog: AlertDialog =builder.create()
        dialog.show()
        return true
    }
    private fun notificaciones(destination_1:String, destination_2:String)    {
        var countdown_timer2 = object : CountDownTimer(60000L, 1000) {
            override fun onFinish() {

                if(showWarning()){
                    var obj = SmsManager.getDefault()
                    obj.sendTextMessage(
                        destination_1,
                        null,
                        "ME ENCUENTRO EN PELIGRO," + ubicacion,
                        null,
                        null
                    )
                    Thread.sleep(1000L)
                    var obj2 = SmsManager.getDefault()
                    obj2.sendTextMessage(
                        destination_2,
                        null,
                        "ME ENCUENTRO EN PELIGRO," + ubicacion,
                        null,
                        null
                    )
                }
                


            }

            override fun onTick(p0: Long) {
                
            }
        }
        countdown_timer2.start()
    }






    //SHOW FUNCTIONS
    private fun showSettings(email:String,provider:String)  {
        val settingsIntent= Intent(this,SettingsActivity::class.java).apply{

            //PARAMETROS A PASAR

            putExtra("email",email)
            putExtra("provider",provider)
        }
        startActivity(settingsIntent)
    }
    private fun showContacts(email:String)   {
        val contactsIntent= Intent(this,ContactsActivity::class.java).apply{

            //PARAMETROS A PASAR

            putExtra("email",email)

        }
        startActivity(contactsIntent)
    }

}


