package com.example.happyplaces.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.R
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.models.HappyPlaceModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.happyplaces.adapters.HappyPlacesAdapter


class MainActivity : AppCompatActivity() {
    private lateinit var happyPlaceList: RecyclerView
    private lateinit var noRecord :TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var fabAddHappyPlace= findViewById<FloatingActionButton>(R.id.fabAddHappyPlace)
        happyPlaceList=findViewById(R.id.rv_happy_places_list)
        noRecord=findViewById(R.id.tv_no_records_available)

        fabAddHappyPlace.setOnClickListener {
            val intent = Intent(this@MainActivity, AddHappyPlace::class.java)
            startActivity(intent)
        }

        getHappyPlacesListFromLocalDB()

    }

    /**
     * A function to get the list of happy place from local database.
     */
    private fun getHappyPlacesListFromLocalDB() {

        val dbHandler = DatabaseHandler(this)

        val getHappyPlacesList = dbHandler.getHappyPlacesList()


        // START
        if (getHappyPlacesList.size > 0) {
            happyPlaceList.visibility = View.VISIBLE
            noRecord.visibility = View.GONE
            setupHappyPlacesRecyclerView(getHappyPlacesList)
        } else {
            happyPlaceList.visibility = View.GONE
            noRecord.visibility = View.VISIBLE
        }
        // END
    }

    /**
     * A function to populate the recyclerview to the UI.
     */
    private fun setupHappyPlacesRecyclerView(happyPlacesList: ArrayList<HappyPlaceModel>) {

        happyPlaceList.layoutManager = LinearLayoutManager(this)
        happyPlaceList.setHasFixedSize(true)

        val placesAdapter = HappyPlacesAdapter(this, happyPlacesList)
        happyPlaceList.adapter = placesAdapter
    }
    // END
}