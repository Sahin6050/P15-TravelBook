package com.sahin.kotlinmaps2.view

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.sahin.kotlinmaps2.R
import com.sahin.kotlinmaps2.databinding.ActivityMapsBinding
import com.sahin.kotlinmaps2.model.Place
import com.sahin.kotlinmaps2.model.PlaceDatabase
import com.sahin.kotlinmaps2.roomdb.PlaceDao
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    // konum yönetmek için iki sınıfı kullanmam lazım.
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var locationPermission : ActivityResultLauncher<String>
    private lateinit var sharedPreferences: SharedPreferences
    private  var trackBoolean: Boolean? = null
    private  var selectedLatitude : Double? = null
    private  var selectedLongitude : Double? = null
    private lateinit var db : PlaceDatabase
    private lateinit var placeDao : PlaceDao
    val compositeDisposable = CompositeDisposable()
    var placeFromMain : Place? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // izin için initialize etmem lazım yoksa app çöker.
        registerLauncher()

        // sharedPreferences'i initialize etmek gerekir.
        sharedPreferences = this.getSharedPreferences("com.sahin.kotlinmaps2", MODE_PRIVATE)
        trackBoolean = false

        selectedLatitude = 0.0
        selectedLongitude = 0.0

        // Room veritabanını oluşturdum.
        db = Room.databaseBuilder(applicationContext,PlaceDatabase::class.java,"Places")
            //.allowMainThreadQueries()
            .build()
        placeDao = db.placeDao()

        binding.saveButton.isEnabled = false


    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(this)

        val intent = intent
        val info = intent.getStringExtra("info")

        if(info.equals("new")){
            binding.deleteButton.visibility = View.GONE
            binding.saveButton.visibility = View.VISIBLE
            // casting işlemi uygulanır.
            locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

            locationListener = object : LocationListener{
                // konum her değiştiğinde bize konum bilgilerini verir.
                override fun onLocationChanged(location: Location) {
                    trackBoolean = sharedPreferences.getBoolean("trackBoolean",false)
                    if(trackBoolean == false){
                        val userLocation = LatLng(location.latitude,location.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15f))
                        sharedPreferences.edit().putBoolean("trackBoolean",true).apply()
                    }


                }

            }
            // kullanıcı izinlerini almak.
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                // izin alınmadı izin iste.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                    Snackbar.make(binding.root,"Permission needed for location",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission"){
                        // request permission
                        locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }.show()
                }else{
                    // request permission
                    locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)

                }

            }else{
                // izin alındı.
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)

                // Son alınan konuma kamerayı çevir.
                val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if(lastLocation != null){
                    val lastUserLocation = LatLng(lastLocation.latitude,lastLocation.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15f))
                }
                // benim konumu etkinleştirdik mi?
                mMap.isMyLocationEnabled = true
            }

        }else{
            mMap.clear()
            binding.saveButton.visibility = View.GONE
            binding.deleteButton.visibility = View.VISIBLE
            placeFromMain = intent.getSerializableExtra("selectedPlace") as Place
            placeFromMain?.let { it ->
                binding.placeText.setText(it.name)
                val latLng = LatLng(it.latitude,it.longitude)
                mMap.addMarker(MarkerOptions().position(latLng).title(it.name))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15f))

            }


        }






        /*
        // adanaMerkez koordinatları 36.99178938928225, 35.334229024525285
        //latitude -> 36.99178938928225, longitude -> 35.334229024525285,
        val adanaMerkez = LatLng(36.99178938928225, 35.334229024525285)
        mMap.addMarker(MarkerOptions().position(adanaMerkez).title("Marker in adanaMerkez"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(adanaMerkez,15f))

         */
    }
    private fun registerLauncher(){
        locationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
            if(result){

                if(ContextCompat.checkSelfPermission(this@MapsActivity,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    // permission granted
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
                    // Son alınan konuma kamerayı çevir.
                    val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if(lastLocation != null){
                        val lastUserLocation = LatLng(lastLocation.latitude,lastLocation.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15f))
                    }
                    // benim konumu etkinleştirdik mi?
                    mMap.isMyLocationEnabled = true
                }


            }else{
                // permission denied
                // izin verilmediyse toast mesajı verilir.
                Toast.makeText(this@MapsActivity,"Permission Needed",Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onMapLongClick(p0: LatLng) {
        // herhangi bir marker veya inotasyon eklenmişse silerim.
        mMap.clear()

        mMap.addMarker(MarkerOptions().position(p0).title("My location"))

        selectedLatitude = p0.latitude
        selectedLongitude = p0.longitude

        binding.saveButton.isEnabled = true



    }
    fun save(view : View){
        //Main Thread UI, Default Thread -> CPU , IO Thread ınternet/Database
        if(selectedLatitude != null && selectedLongitude != null){
            val place = Place(binding.placeText.text.toString(),selectedLatitude!!,selectedLongitude!!)
            // compositeDisposable bir garbage collecter gibi çalışır.
            compositeDisposable.add(
                placeDao.insert(place)
                    .subscribeOn(Schedulers.io())// işlemin yapılacağı yer IO
                    .observeOn(AndroidSchedulers.mainThread())// veriyi nerede kullanacağız.
                    .subscribe(this::handleResponse)

            )

        }


    }
    private fun handleResponse(){
        val intent = Intent(this@MapsActivity,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)// bütün aktiviteleri kapat.
        startActivity(intent)
    }
    fun delete(view : View){
        placeFromMain?.let {
            compositeDisposable.add(
                placeDao.delete(it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponse)
            )
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}