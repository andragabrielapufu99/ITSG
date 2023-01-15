package com.example.puffy.myapplication.todo.meal

import android.Manifest
import android.animation.ValueAnimator
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.example.puffy.myapplication.R
import com.example.puffy.myapplication.todo.data.Meal
import com.example.puffy.myapplication.todo.data.MealRepository
import kotlinx.android.synthetic.main.fragment_meal_edit.*
import kotlinx.android.synthetic.main.fragment_meal_edit.view.*
import kotlinx.android.synthetic.main.view_meal_list.view.*
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors


class MealEditFragment: Fragment() {

    companion object{
        const val ITEM_ID = "MEAL_ID"
    }

    private lateinit var viewModel: MealEditViewModel
    private var id : Int? = -1
    private var item : Meal? = null
    private val tagName: String = "MealEditFragment"

    //photo stuff
    private val REQUEST_CODE_PERMISSION = 10
    private val REQUEST_CAPTURE_IMAGE = 1
    private val REQUEST_PICK_IMAGE = 2

    private val entity: String = "meal"
    private var errorsView : MutableList<View> = ArrayList()
    lateinit var currentPhotoPath : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(tagName, "onCreate")
        arguments?.let {
            if (it.containsKey(ITEM_ID)) {
                id = it.getInt(ITEM_ID)
            }
        }
        shakeTextViews()
    } //end onCreate

    private fun shakeTextViews(){
        ValueAnimator.ofFloat(
            0F, 25F, (-25).toFloat(), 25F, (-25).toFloat(), 15F,
            (-15).toFloat(), 6F, (-6).toFloat(), 0F
        ).apply {
            duration = 1000
            startDelay = 1000
            start()
            addUpdateListener {
                if(categoryTF != null && servedOnTF != null) {
                    categoryTF.translationX = it.animatedValue as Float
                    servedOnTF.translationX = it.animatedValue as Float
                }

            }
        }
    }

    private fun shakeOneView(view: View){
        ValueAnimator.ofFloat(
            0F, 25F, (-25).toFloat(), 25F, (-25).toFloat(), 15F,
            (-15).toFloat(), 6F, (-6).toFloat(), 0F
        ).apply {
            duration = 1000
            start()
            addUpdateListener {
                view.translationX = it.animatedValue as Float
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
        shakeTextViews()
    }

    private fun checkPermissions(){
        if(activity?.let { ContextCompat.checkSelfPermission(
                it.applicationContext,
                Manifest.permission.CAMERA
            ) } != PackageManager.PERMISSION_GRANTED){
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(Manifest.permission.CAMERA), REQUEST_CODE_PERMISSION
                )
            }
        }
    }

    private fun openGallery(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_PICK_IMAGE)
    }

    private fun openCamera(){
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            activity?.let { intent.resolveActivity(it.packageManager).also {
                val photoFile: File? = try{
                    createTemporaryFile()
                }catch (ex: IOException){
                    null
                }
                Log.d(tagName, "photofile $photoFile")
                photoFile.also {
                    val photoURI = it?.let { it1 ->
                        context?.let { it2 ->
                            FileProvider.getUriForFile(
                                it2,
                                "com.example.puffy.myapplication.fileprovider",
                                it1
                            )
                        }
                    } //end photoURI
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(intent, REQUEST_CAPTURE_IMAGE)
                }
            }
            } //end activity
        } //end Intent
    }

    private fun createTemporaryFile() : File{
        val timestamp: String = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val storageDir = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("PHOTO_${timestamp}", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_CAPTURE_IMAGE){
                val uri = Uri.parse(currentPhotoPath)
                item?.pathImage = currentPhotoPath
                imageView.setImageURI(uri)
                imageView.visibility = View.VISIBLE

            }else if(requestCode == REQUEST_PICK_IMAGE){
                val uri = data?.data
                val file = uri?.let { createFileFromUri(it) }
                if(file != null){
                    item?.pathImage = file.path
                }
                imageView.setImageURI(uri)
                imageView.visibility = View.VISIBLE
            }
        }
    }

    private fun createFileFromUri(uri: Uri) : File? {
        val image : Bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, uri)
        val bytes : ByteArrayOutputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 40, bytes)
        val filetemp : File = createTemporaryFile()
        if(filetemp.exists()){
            val fos : FileOutputStream = FileOutputStream(filetemp)
            fos.write(bytes.toByteArray())
            fos.close()
            return filetemp
        }
        return null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.v(tagName, "onCreateView")
        return inflater.inflate(R.layout.fragment_meal_edit, container, false)
    }

    private fun validateItem(item: Meal){
        var errors: String = ""
        var blacklist: String = "[,:?/.]"
        errorsView = ArrayList()
        if(item.category.equals("") || item.category.contains(blacklist.toRegex()) || item.category.contains(
                '\\'
            )){
            errors += "Category field cannot be empty or cannot contains the next caracters : , : ? / \\ . !"
            errorsView.add(categoryTF)
        }
//        if(item.artist.equals("") || item.artist.contains(blacklist.toRegex()) || item.artist.contains(
//                '\\'
//            )){
//            errors += "Artist field cannot be empty or cannot contains the next caracters : , : ? / \\ . !"
//            errorsView.add(itemArtist)
//        }
//        if(!(item.year >= 1000 && item.year.toInt() <= 9999)){
//            errors += "Year field must be a positive number of 4 digits!"
//            errorsView.add(itemYear)
//        }
//        if(item.genre.equals("") || item.genre.contains(blacklist.toRegex()) || item.genre.contains(
//                '\\'
//            )){
//            errors += "Genre field cannot be empty or cannot contains the next caracters : , : ? / \\ . !"
//            errorsView.add(itemGenre)
//        }
        if(!errors.equals("")){
            throw Exception(errors)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.v(tagName, "onActivityCreated")
        setupViewModel()
        saveBtn.setOnClickListener{
            Log.v(tagName, "update $entity")
            val i = item
            if(i!=null){
                println("item received $i")
                i.category = categoryTF.text.toString()
                i.served_on = servedOnTF.text.toString();
                i.created_at = "";
                i.updated_at = "";
                try {
                    validateItem(i)
                    if(i.id == -1) viewModel.add(i, imageView.drawable.toBitmap())
                    else viewModel.update(i)
                }catch (e: Exception){
                    Toast.makeText(activity, e.message, Toast.LENGTH_LONG).show()
                    errorsView.forEach{view -> shakeOneView(view)}
                }

            }
        }

        categoryTF.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            when (keyCode) {
                KeyEvent.KEYCODE_DEL -> {
                    println("press delete")
                }
                else -> return@OnKeyListener false
            }
            true
        })

        categoryTF.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                println("Before Category new value")
                println(s)
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                println("Category new value")
                println(s)
            }

            override fun afterTextChanged(s: Editable?) {
                println("After Category new value")
                println(s)
            }
        })

        logoutBtn.setOnClickListener {
            viewModel.logout()
            findNavController().navigate(R.id.fragment_login)
        }
        if(id != -1){
            capturePhoto.visibility = View.GONE
            pickPhoto.visibility = View.GONE
        }else {
            capturePhoto.visibility = View.VISIBLE
            capturePhoto.setOnClickListener{
                openCamera()
            }
            pickPhoto.visibility = View.VISIBLE
            pickPhoto.setOnClickListener{
                openGallery()
            }
        }




    } //end onActivityCreated

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd")

        // on below line we are creating a variable for
        // current date and time and calling a simple
        // date format in it.
        val currentDateAndTime = sdf.format(Date())

        // on below line we are setting current
        // date and time to our text view.

        return currentDateAndTime
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this).get(MealEditViewModel::class.java)

        viewModel.fetching.observe(viewLifecycleOwner) {
            Log.v(tagName, "update fetching")
            progress.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.exception.observe(viewLifecycleOwner) {
            if (it != null) {
                Log.v(tagName, "update fetching error")
                Toast.makeText(activity, it.message, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.completed.observe(viewLifecycleOwner) {
            if (it) {
                Log.v(tagName, "completed, navigate back")
                if(viewModel.exception.value == null){
                    if(!MealRepository.getNetworkStatus()){
                        Toast.makeText(activity, "Saved locally", Toast.LENGTH_SHORT).show()
                    }
                    findNavController().navigate(R.id.action_MealEditFragment_to_MealListFragment)
                }
            }
        }

        val id = id
        if (id == -1) {
            servedOnTF.setText(getCurrentDate())
            item = Meal(-1, "", served_on = "", created_at = "", updated_at = "", foods = arrayListOf(), pathImage = "", calories=0F)
        } else {
            if (id != null) {
                viewModel.getById(id).observe(viewLifecycleOwner){
                    if(it != null){
                        item = it
                        categoryTF.setText(it.category)
                        servedOnTF.setText(it.served_on.toString())
                        imageView.visibility = View.GONE
                        if(it.pathImage?.isNotEmpty() == true){
                            getImageFromUrl(imageView, it.pathImage!!)
                            imageView.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    } //end setupViewModel

    fun getImageFromUrl(imageView: ImageView, url: String) {
        val executor = Executors.newSingleThreadExecutor()

        // Once the executor parses the URL
        // and receives the image, handler will load it
        // in the ImageView
        val handler = android.os.Handler(Looper.getMainLooper())

        // Initializing the image
        var image: Bitmap? = null

        // Only for Background process (can take time depending on the Internet speed)
        executor.execute {

            // Image URL
            val imageURL = url
            // Tries to get the image and post it in the ImageView
            // with the help of Handler
            try {
                val `in` = java.net.URL(imageURL).openStream()
                image = BitmapFactory.decodeStream(`in`)

                // Only for making changes in UI
                handler.post {
                    imageView.setImageBitmap(image)
                }
            }

            // If the URL doesnot point to
            // image or any other kind of failure
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onCategorySelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        // On selecting a spinner item
        val item = parent.getItemAtPosition(position).toString()
    }

}
