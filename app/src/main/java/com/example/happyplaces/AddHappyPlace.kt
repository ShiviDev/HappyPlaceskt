package com.example.happyplaces

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
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

    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private lateinit var etdate: EditText
    private lateinit var tvaddImage : TextView
    private lateinit var ivPlaceImage : ImageView
    private lateinit var galleryImageResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_place)

        etdate = findViewById<EditText>(R.id.et_date)
        tvaddImage=findViewById<TextView>(R.id.tv_add_image)
        ivPlaceImage=findViewById(R.id.iv_place_image)

        val toolbar_add_place= findViewById<Toolbar>(R.id.toolbar_add_place)

        setSupportActionBar(toolbar_add_place)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar_add_place.setNavigationOnClickListener {
            onBackPressed()
        }
        dateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                updateDateInView()
            }
            etdate.setOnClickListener(this)
            tvaddImage.setOnClickListener(this)

        registerOnActivityForGalleryResult()
    }
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.et_date -> {
                DatePickerDialog(
                    this@AddHappyPlace,
                    dateSetListener,
                    cal.get(Calendar.YEAR), // Here the cal instance is created globally and used everywhere in the class where it is required.
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
            R.id.tv_add_image->{
                val pictureDialog= AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems= arrayOf("Select photo from Gallery","Capture photo from Camera")
                pictureDialog.setItems(pictureDialogItems){
                    _,which->
                    when(which){
                        0->choosePhotoFromGallery()
                        1->Toast.makeText(this@AddHappyPlace,"opening camera",Toast.LENGTH_SHORT).show()
                    }
                }
                pictureDialog.show()
            }
        }
    }
    private fun updateDateInView(){
        val myFormat="dd.MM.yyyy"
        val sdf= SimpleDateFormat(myFormat, Locale.getDefault())
        etdate.setText(sdf.format(cal.time).toString())
    }
    private fun choosePhotoFromGallery(){
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?)
            {
                if(report!!.areAllPermissionsGranted()){
                Toast.makeText(this@AddHappyPlace,"Storage READ/WRITE permissions are granted",Toast.LENGTH_SHORT).show()
                    val galleryIntent=Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    galleryImageResultLauncher.launch(galleryIntent)
                }
            }
            override fun onPermissionRationaleShouldBeShown(permissions:MutableList<PermissionRequest>, token: PermissionToken)
            {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()
    }
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
}