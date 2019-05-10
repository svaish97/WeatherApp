package com.example.saurabh.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color

import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.Html
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import com.squareup.picasso.Picasso

import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList
data class jsonValues(val temp:String,val city:String)

class MainActivity : AppCompatActivity() {

 var cityName=""
  var lat:Double=0.0
  var longi:Double=0.0
  var myCustomArray=ArrayList<jsonValues>()
    lateinit var adapter: CustomAdapter
   // var locationNotObtained=true
    lateinit var locationManager: LocationManager
    lateinit var listener: LocationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val autocompleteFragment = fragmentManager.findFragmentById(R.id.place_autocomplete_fragment) as PlaceAutocompleteFragment

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.i("Frag", "Place: " + place.getName().toString())
                cityName = place.name.toString()
            }

            override fun onError(status: Status) {
                Log.i("Frag", "An error occurred: $status")
            }
        })

        locationManager=getSystemService(Context.LOCATION_SERVICE) as LocationManager


        listener =object:LocationListener{
            override fun onLocationChanged(location: Location?) {
               // if (locationNotObtained) {
                    location?.run {
                        lat = latitude
                        longi = longitude
                    }
                    Log.e("Coord", lat.toString() + " " + longi.toString())


                val weather =WeatherTask()
                weather.execute("https://openweathermap.org/data/2.5/weather?lat="+lat.toString()+"&lon="+longi.toString()+"&appid=b6907d289e10d714a6e88b30761fae22")

            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }

            override fun onProviderEnabled(provider: String?) {
            }

            override fun onProviderDisabled(provider: String?) {


            }

        }






    btnCurrentLocation.setOnClickListener {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) !=PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) !=PackageManager.PERMISSION_GRANTED
        ){
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this, arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ),123)
            }
        } else {
            tvResult.visibility=View.GONE
            tvCity.visibility=View.GONE
            weatherImage.visibility=View.GONE

            progressBar.visibility=View.VISIBLE
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,50F,listener)


        }
    }




        button.setOnClickListener {
            tvResult.visibility=View.GONE
            tvCity.visibility=View.GONE
            weatherImage.visibility=View.GONE
            progressBar.visibility=View.VISIBLE

            val weather=WeatherTask()
            weather.execute("https://openweathermap.org/data/2.5/weather?q="+cityName+"&appid=b6907d289e10d714a6e88b30761fae22")

            if(cityName.equals("Noida")) {

                val inputStream = assets.open("myFile.json")
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()

                val json = String(buffer, Charset.forName("UTF-8"))
                val jsonArray = JSONArray(json)

                Log.d("json", jsonArray.toString())
                myCustomArray.clear()
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    myCustomArray.add(jsonValues(jsonObject.getString("temp"), jsonObject.getString("city")))
                }
            }
            else if(cityName.equals("New Delhi")){
                val inputStream = assets.open("newDelhi.json")
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()

                val json = String(buffer, Charset.forName("UTF-8"))
                val jsonArray = JSONArray(json)

                Log.d("json", jsonArray.toString())
                myCustomArray.clear()
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    myCustomArray.add(jsonValues(jsonObject.getString("temp"), jsonObject.getString("city")))
                }
            }
            adapter=CustomAdapter(myCustomArray)
            rview.adapter=adapter

        }


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
       if(requestCode==123)
       {
           Log.d("PERM","Permission Granted")
       }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }







    inner class WeatherTask:AsyncTask<String,Void,String>(){
        override fun doInBackground(vararg params: String?): String {
            val string = params[0]

            try{
                val url=URL(string)
                val httpConnection=url.openConnection() as HttpURLConnection
                val inputStream = httpConnection.inputStream
                val s=Scanner(inputStream)
                s.useDelimiter("\\A")
                if(s.hasNext())
                {
                    val str=s.next()
                    return str
                }

            } catch(e:IOException)
            {
                Log.e("Error","Error Occured in weatherTask")
            }
            return """
                Failed to load.Enter the correct city
            """.trimIndent()
        }

        @SuppressLint("SetTextI18n")
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                val jsonObject = JSONObject(result)
                val jsonArray = jsonObject.getJSONArray("weather")
                val jsonitem=jsonArray.getJSONObject(0)
                val image=jsonitem.getString("icon")


                val items=jsonObject.getJSONObject("main")
                val tempCity=jsonObject.getString("name")
                val temp = items.getInt("temp")
                progressBar.visibility=View.GONE
               tvCity.visibility=View.VISIBLE
                tvResult.visibility=View.VISIBLE
                weatherImage.visibility=View.VISIBLE

                tvResult.text=temp.toString()+ 0x00B0.toChar()+"C"
                tvCity.text=tempCity
                Picasso.get().load("http://openweathermap.org/img/w/"+image+".png").resize(300,300).into(weatherImage)

            }
            catch(e:Exception)
            {
                Log.e("Error","Error Occured in postExecute")
            }
        }
    }
}
