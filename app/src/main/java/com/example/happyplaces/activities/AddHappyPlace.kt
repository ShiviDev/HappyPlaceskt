package com.example.happyplaces.activities

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.example.happyplaces.R
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.models.HappyPlaceModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class AddHappyPlace : AppCompatActivity(), View.OnClickListener {

    /*Calendar initialisation*/
    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var saveImageToInternalStorage:Uri?=null
    private var mLatitude: Double=0.0
    private var mLongitude: Double = 0.0

    /*View declarations*/
    private lateinit var etdate: EditText
    private lateinit var tvaddImage : TextView
    private lateinit var ivPlaceImage : ImageView
    private lateinit var btnSave: Button
    private lateinit var etTitle: EditText
    private lateinit var etDescription:EditText
    private lateinit var etLocation:EditText

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
        btnSave = findViewById(R.id.btn_save)
        etTitle=findViewById(R.id.et_title)
        etDescription=findViewById(R.id.et_description)
        etLocation=findViewById(R.id.et_location)

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
        updateDateInView()
        //view listeners
            etdate.setOnClickListener(this)
            tvaddImage.setOnClickListener(this)
            btnSave.setOnClickListener(this)

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
            R.id.tv_add_image ->{
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

            R.id.btn_save ->{
                when{
                    etTitle.text.isNullOrEmpty()->{
                        Toast.makeText(this@AddHappyPlace,"Please enter title",Toast.LENGTH_SHORT).show()
                    }
                    etDescription.text.isNullOrEmpty()->{
                        Toast.makeText(this@AddHappyPlace,"Please enter description",Toast.LENGTH_SHORT).show()
                    }
                    etLocation.text.isNullOrEmpty()->{
                        Toast.makeText(this@AddHappyPlace,"Please enter location",Toast.LENGTH_SHORT).show()
                    }
                    saveImageToInternalStorage==null->{
                        Toast.makeText(this@AddHappyPlace,"Please select image",Toast.LENGTH_SHORT).show()
                    }else->{
                        val happyPlaceModel= HappyPlaceModel(
                            0,
                            etTitle.text.toString(),
                            saveImageToInternalStorage.toString(),
                            etDescription.text.toString(),
                            etdate.text.toString(),
                            etLocation.text.toString(),
                            mLatitude,
                            mLongitude
                        )

                    // Here we initialize the database handler class.
                    val dbHandler = DatabaseHandler(this)

                    val addHappyPlace = dbHandler.addHappyPlace(happyPlaceModel)

                    if (addHappyPlace > 0) {
                        Toast.makeText(
                            this,
                            "The happy place details are inserted successfully.",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish();//finishing activity
                    }
                    }
                }
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
                        val selectedImage: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentUri)
                        saveImageToInternalStorage = saveImageToInternalStorage(selectedImage)
                        Log.e("Saved image:","Path:: $saveImageToInternalStorage")
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

    //start of registerOnActivityForCameraResult
    private fun registerOnActivityForCameraResult(){
        cameraImageResultLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data

                val thumbNail : Bitmap = data!!.extras?.get("data") as Bitmap
                saveImageToInternalStorage=saveImageToInternalStorage(thumbNail)
                Log.e("Saved Image : ", "Path :: $saveImageToInternalStorage")

                ivPlaceImage.setImageBitmap(thumbNail)
            }
        }
    }

//end of registeOnActivityForCameraResult
private fun saveImageToInternalStorage(bitmap: Bitmap):Uri{
    val wrapper = ContextWrapper(applicationContext)
    var file = wrapper.getDir("HappyPlacesImages", Context.MODE_PRIVATE)
    file = File(file, "${UUID.randomUUID()}.jpg")
    try {
        val stream: OutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.flush()
        stream.close()
    }catch (e:IOException){
        e.printStackTrace()
    }
    return Uri.parse(file.absolutePath)

}



}