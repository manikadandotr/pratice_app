package com.example.practiceapp

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.fragment_first.*
import kotlinx.android.synthetic.main.fragment_first.view.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class FirstFragment : Fragment() {
    private val IMAGE_GALLERY_REQUEST_CODE: Int =2001
    private var uri: Uri?= null
    private lateinit var currentImagePath: String
    var selectedUploadOption:String? = ""
    private var mainContext: MainActivity? = null
    lateinit var iv_img: ImageView
    lateinit var ib_crop: ImageButton
    private var mfile:Uri?=null
    private val TAG = "FirstFragment"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view =inflater.inflate(R.layout.fragment_first, container, false)
        iv_img =view.findViewById<ImageView>(R.id.iv)
        ib_crop =view.findViewById<ImageButton>(R.id.ib_crop)




        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val myDir = File(requireActivity().filesDir.absolutePath)



        enableRuntimePermission()
        view.selectBtn.setOnClickListener{
            val imageUploadOptions= arrayOf("Camera","Gallery")
            val singleChoiceDialog= AlertDialog.Builder(requireContext())
                .setTitle("Choose your method to upload image")
                .setIcon(R.drawable.payments_icon_foreground)
                .setSingleChoiceItems(imageUploadOptions,-1){_,i->
                    selectedUploadOption=imageUploadOptions[i]
//                    Toast.makeText(requireContext(),"we accept ${selectedUploadOption}",Toast.LENGTH_LONG)

                }
                .setPositiveButton("Select"){_,_->
//                    Toast.makeText(requireContext()," ${selectedUploadOption}",Toast.LENGTH_LONG).show()
                    Log.d("aa", "onCreateView: ${selectedUploadOption}")

                    if (selectedUploadOption==imageUploadOptions[0]){
                        Toast.makeText(requireContext(),"you selected ${selectedUploadOption}",Toast.LENGTH_SHORT).show()
                        openCamera()
                    }else if(selectedUploadOption==imageUploadOptions[1]){
                        Toast.makeText(requireContext(),"you selected ${selectedUploadOption}",Toast.LENGTH_SHORT).show()
                        openGallery()
                    }

                }
                .setNegativeButton("Cancel"){_,_->
                    Toast.makeText(requireContext(),"you Cancelled ${selectedUploadOption}",Toast.LENGTH_SHORT).show()
                }.create().show()
        }

        ib_crop.setOnClickListener{
            imageCrop(uri)
            Navigation.findNavController(view).navigate(R.id.navigate_to_secondFragment)


        }


    }

    private fun imageCrop(uri: Uri?) {

        val uCrop = uri?.let { UCrop.of(it, Uri.fromFile(createImageFile()))
            .withAspectRatio(16F,16F)
            .withMaxResultSize(500,500)
            .start(requireContext(),this,UCrop.REQUEST_CROP)
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "onActivityResult: $ ")
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            Log.d("crop", "onActivityResult:$resultUri ")
            if(resultUri!=null){
                iv_img.setImageURI(resultUri)
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
        }
    }
//    fun savefile(sourceuri: Uri): Uri {
//        val sourceFilename: String? = sourceuri.getPath()
//        val destinationFilename =
//            Environment.getExternalStorageDirectory().path + File.separatorChar + "abc.jpeg"
//        var bis: BufferedInputStream? = null
//        var bos: BufferedOutputStream? = null
//        try {
//            bis = BufferedInputStream(FileInputStream(sourceFilename))
//            bos = BufferedOutputStream(FileOutputStream(destinationFilename, false))
//            val buf = ByteArray(1024)
//            bis.read(buf)
//            do {
//                bos.write(buf)
//            } while (bis.read(buf) !== -1)
//        } catch (e: IOException) {
//            e.printStackTrace()
//        } finally {
//            try {
//                if (bis != null) bis.close()
//                if (bos != null) bos.close()
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }
//    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.CAMERA) ===
                                PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(mainContext, "Permission Granted", Toast.LENGTH_SHORT).show()
                        openCamera()
                    }
                } else {
                    Toast.makeText(mainContext, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainContext = context as MainActivity
    }

    private fun openGallery() {

        val intent = Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        intent.type="image/*"
        galleryLauncher.launch(intent)
    }
    var galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
//            if(result.data != null && result.data!!.data == null){
                val image = result.data!!.data
            uri=image
            if(image != null )
            {
                Log.d("aaa", image.toString())
                iv_img.setImageURI(image)
//                view?.iv?.setImageURI(image)
            //            view?.iv?.setImageURI(image)
            }
//                Log.d("aaa", image.toString())
//                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                     val source = ImageDecoder.createSource(requireActivity().contentResolver,image!!)
//                     Log.d("TAG", ": ${source} / ${result.data}")
//
//                     val bitmap=ImageDecoder.decodeBitmap(source)
//                     Log.d("aaa", bitmap.toString())
//
//                     view?.iv?.setImageBitmap(bitmap)
//                 } else {
//                     TODO("VERSION.SDK_INT < P")

//                     Toast.makeText(requireContext(),"VERSION.SDK_INT < P",Toast.LENGTH_SHORT)
//                 }
//            }
        }
    }



    private val getCameraImage =registerForActivityResult(ActivityResultContracts.TakePicture()){
        success ->
        if(success){
            Log.i("TAG","Image location: $uri")
            view?.iv?.setImageURI(uri)
        }else{
            Log.i("TAG","Image location: $uri")

        }
    }

    fun hasCameraPermission() = ContextCompat.checkSelfPermission(requireContext(),android.Manifest.permission.CAMERA)
    fun hasExternalStoragePermission() = ContextCompat.checkSelfPermission(requireContext(),android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageDirectory =requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "img_${timestamp}",".jpg",imageDirectory
        ).apply{
            currentImagePath =absolutePath
        }
    }

    private fun openCamera() {

        if(hasCameraPermission()== PERMISSION_GRANTED && hasExternalStoragePermission() == PERMISSION_GRANTED){
                takePhoto()
        }else{
                requestMutliplePermissionsLauncher.launch(arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ))
        }
//        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        startActivityForResult(cameraIntent, 200)
    }

    private fun takePhoto() {
        val file=createImageFile()
        try {
            uri=FileProvider.getUriForFile(requireContext(),"com.example.practiceapp.fileprovider",file)

        }catch (e: Exception){
            Log.e("camera", "takePhoto:Error ${e.message}")
            var foo = e.message
        }
        getCameraImage.launch(uri)

    }

    private val requestMutliplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()){
        resultsMap ->
        var permissionGranted = false
        resultsMap.forEach{
            if (it.value == true){
                permissionGranted= it.value
            }else{
                permissionGranted =false
                return@forEach
            }
        }
        if(permissionGranted){
            takePhoto()
        }else{
            Toast.makeText(requireContext(),"Unable to open camera without permission",Toast.LENGTH_SHORT).show()
        }
    }



    private fun enableRuntimePermission(){
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) !==
            PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                    Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(Manifest.permission.CAMERA), 1)
            } else {
                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(Manifest.permission.CAMERA), 1)
            }
        }
    }

}
