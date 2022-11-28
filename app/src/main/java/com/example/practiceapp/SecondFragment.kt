package com.example.practiceapp

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.practiceapp.databinding.FragmentSecondBinding
import kotlinx.android.synthetic.main.fragment_second.view.*
import java.io.ByteArrayOutputStream


class SecondFragment : Fragment() {
    private val TAG = "SecondFragment"
    val args: SecondFragmentArgs by navArgs()
    lateinit var iv_croped:ImageView
    lateinit var ib_earse: ImageView
    lateinit var croppedUri:Uri
    var earsebm: Bitmap? =null

//    val bindingSecondBinding: FragmentSecondBinding = TODO()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_second, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        iv_croped=view.findViewById(R.id.iv_croped)
        croppedUri=args.uriInString.toUri()
        Log.d(TAG, "onViewCreated: ${args.uriInString}")
        iv_croped.setImageURI(croppedUri)
        ib_earse=view.findViewById(R.id.ib_earse)

        // Navigate to third fragment
        view.generateBtn.setOnClickListener{
            val navController= Navigation.findNavController(view).navigate(R.id.navigate_to_thirdFragment)
//            val navController=Navigation.findNavController(R.id.navigate_to_thirdFragment)



//            val bundle = Bundle()
//            bundle.putString("name","your value")
//            navController.setGraph(navController.graph,bundle)
            }
        // Erase feature imageButton
        ib_earse.setOnClickListener{

            Log.d(TAG, "onViewCreated: $earsebm")
            earsebm.let {
                changToBitmap();
                earsebm?.let { it1 -> earseActivity(it1) }
            }

//            iv_croped.setOnTouchListener()


        }

    }

    private fun earseActivity(earsebm: Bitmap) {

        val stream = ByteArrayOutputStream()
        earsebm.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray: ByteArray = stream.toByteArray()

        val in1 = Intent(requireContext(), EraseActivity::class.java)
        in1.putExtra("image", byteArray)
        startActivity(in1)
    }

    private fun changToBitmap() {
        croppedUri?.let {
            earsebm = MediaStore.Images.Media.getBitmap(context?.getContentResolver(), it)
        }



    }

}