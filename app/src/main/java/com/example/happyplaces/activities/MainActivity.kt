package com.example.happyplaces.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Adapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.R
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.models.HappyPlaceModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.happyplaces.adapters.HappyPlacesAdapter
import com.happyplaces.utils.SwipeToDeleteCallback
import pl.kitek.rvswipetodelete.SwipeToEditCallback


class MainActivity : AppCompatActivity() {
    private lateinit var happyPlaceList: RecyclerView
    private lateinit var noRecord :TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        var fabAddHappyPlace= findViewById<FloatingActionButton>(R.id.fabAddHappyPlace)
        happyPlaceList=findViewById(R.id.rv_happy_places_list)
        noRecord=findViewById(R.id.tv_no_records_available)


        fabAddHappyPlace.setOnClickListener {
            val intent = Intent(this@MainActivity, AddHappyPlace::class.java)

            startActivity(intent)
        }

        getHappyPlacesListFromLocalDB()

    }

    override fun onResume() {
        super.onResume()
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

        placesAdapter.setOnClickListener(object :
            HappyPlacesAdapter.OnClickListener {
            override fun onClick(position: Int, model: HappyPlaceModel) {
                val intent = Intent(this@MainActivity, HappyPlaceDetail::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS, model)
                startActivity(intent)
            }
        })

        val editSwipeHandler = object : SwipeToEditCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // TODO (Step 5: Call the adapter function when it is swiped)
                // START
                val adapter = happyPlaceList.adapter as HappyPlacesAdapter
                adapter.notifyEditItem(
                    this@MainActivity,
                    viewHolder.adapterPosition,
                    ADD_PLACE_ACTIVITY_REQUEST_CODE
                )
                // END
            }
        }

        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(happyPlaceList)

        // TODO(Step 3: Bind the delete feature class to recyclerview)
        val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // TODO (Step 6: Call the adapter function when it is swiped for delete)
                // START
                val adapter = happyPlaceList.adapter as HappyPlacesAdapter
                adapter.removeAt(viewHolder.adapterPosition)

                getHappyPlacesListFromLocalDB() // Gets the latest list from the local database after item being delete from it.
                // END
            }
        }
        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(happyPlaceList)
    }
    // END


    // END
    companion object {
        internal const val EXTRA_PLACE_DETAILS = "extra_place_details"
        private const val ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
        // END
    }
}