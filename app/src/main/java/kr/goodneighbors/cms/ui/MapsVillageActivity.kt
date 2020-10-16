package kr.goodneighbors.cms.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.service.model.VillageLocation

class MapsVillageActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val bundle: Bundle? = intent.extras
        bundle?.apply {
            val name: String? = getString("name")
            val lat: String? = getString("lat")
            val lng: String? = getString("lng")

            if (!name.isNullOrBlank() && !lat.isNullOrBlank() && !lng.isNullOrBlank()) {
                try {
                    val position = LatLng(lat.toDouble(), lng.toDouble())
                    mMap.addMarker(MarkerOptions().position(position).title(name)).showInfoWindow()
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 16f))
                }
                catch(e: Exception) {}
            }
        }

        bundle?.apply {
            val items: List<VillageLocation>? = getParcelableArrayList("items")
            items?.forEachIndexed {index, villageLocation->
                val name: String? = villageLocation.VLG_NM
                val lat: String? = villageLocation.LAT
                val lng: String? = villageLocation.LNG

                if (!name.isNullOrBlank() && !lat.isNullOrBlank() && !lng.isNullOrBlank()) {
                    try {
                        val position = LatLng(lat.toDouble(), lng.toDouble())
                        mMap.addMarker(MarkerOptions().position(position).title(name)).showInfoWindow()
                        if (index == 0) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 14f))
                        }
                    }
                    catch(e: Exception) {}
                }
            }
        }
    }
}
