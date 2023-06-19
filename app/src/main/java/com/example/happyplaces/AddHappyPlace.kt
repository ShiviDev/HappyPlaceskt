package com.example.happyplaces

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddHappyPlace : AppCompatActivity(), View.OnClickListener {

    /*Calendar initialisation*/
    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    /*View declarations*/
    private lateinit var etdate: EditText
    private lateinit var tvaddImage : TextView
    private lateinit var ivPlaceImage : ImageView

    /*Image initialisations*/
    private lateinit var galleryImageResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraImageResultLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_place)

        /*Initialise Views*/
        etdate = findViewById<EditText>(R.id.et_date)
        tvaddImage=findViewById<TextView>(R.id.tv_add_image)
        ivPlaceImage=findViewById(R.id.iv_place_image)

        val toolbar_add_place= findViewById<Toolbar>(R.id.toolbar_add_place)

        setSupportActionBar(toolbar_add_place)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar_add_place.setNavigationOnClickListener {
            onBackPressed()
        }
        //calendar popup listener
        dateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                updateDateInView()
            }

        //view listeners
            etdate.setOnClickListener(this)
            tvaddImage.setOnClickListener(this)

        //call for updating image from gallery
        registerOnActivityForGalleryResult()
        registerOnActivityForCameraResult()
    }
    //end of OnCreate

    //listener handler
    override fun onClick(v: View?) {
        when (v!!.id) {
            //setting date text et_date
            R.id.et_date -> {
                DatePickerDialog(
                    this@AddHappyPlace,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            //setting text view to act like a button
            R.id.tv_add_image->{
                //alret pops up asking to select an action
                val pictureDialog= AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                //options to select
                val pictureDialogItems= arrayOf("Select photo from Gallery","Capture photo from Camera")
                pictureDialog.setItems(pictureDialogItems){
                    _,which->
                    when(which){
                        0->choosePhotoFromGallery()//callback function for gallery
                        1->takePhotoFromCamera()
                    }
                }
                pictureDialog.show()
            }
        }
    }
    //end of handler

    //date formatter
    private fun updateDateInView(){
        val myFormat="dd.MM.yyyy"
        val sdf= SimpleDateFormat(myFormat, Locale.getDefault())
        etdate.setText(sdf.format(cal.time).toString())
    }
    //end of date formatter

    //choose photo from gallery
    private fun choosePhotoFromGallery(){
        //Use of dexter
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?)//permission granted
            {
                if(report!!.areAllPermissionsGranted()){
                Toast.makeText(this@AddHappyPlace,"Storage READ/WRITE permissions are granted",Toast.LENGTH_SHORT).show()
                    val galleryIntent=Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)//call the gallery app
                    galleryImageResultLauncher.launch(galleryIntent)
                }
            }
            override fun onPermissionRationaleShouldBeShown(permissions:MutableList<PermissionRequest>, token: PermissionToken)//permission denied
            {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()
    }
    //end of choosePhotoFromGallery

    //click photo throught camera
    private fun takePhotoFromCamera() {
        Dexter.withContext(this).withPermissions(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
        ).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    cameraImageResultLauncher.launch(cameraIntent)
                }
            }
            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken) {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()
    }
    //end of takePhotoFromCamera

    //start of function to direct user to Settings page
    private fun showRationalDialogForPermissions(){
        AlertDialog.Builder(this@AddHappyPlace).setMessage("Allow permissions from settings to contuinue").setPositiveButton("GO TO SETTINGS")
        {
                _, _ ->
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
        }.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }.show()
    }
    //end of showRotationalDialogForPermissions

    //after you get a result from gallery
    private fun registerOnActivityForGalleryResult(){
        galleryImageResultLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result->
            if(result.resultCode== Activity.RESULT_OK){
                val data:Intent?=result.data
                if(data!=null){
                    val contentUri=data.data
                    try {
                        ivPlaceImage.setImageURI(contentUri)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(
                            this,
                            "Failed to load image from gallery",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
//end of registerOnActivityForGalleryResult

    private fun registerOnActivityForCameraResult(){
        cameraImageResultLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data

                val thumbNail : Bitmap = data!!.extras?.get("data") as Bitmap
                ivPlaceImage.setImageBitmap(thumbNail)
            }
        }
    }




}