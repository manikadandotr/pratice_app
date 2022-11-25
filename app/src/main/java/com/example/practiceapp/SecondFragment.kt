package com.example.practiceapp

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_first.view.*
import kotlinx.android.synthetic.main.fragment_second.view.*


class SecondFragment : Fragment() {
    private val TAG = "SecondFragment"
    val args: SecondFragmentArgs by navArgs()
    lateinit var iv_croped:ImageView
    lateinit var ib_earse:ImageButton
    lateinit var croppedUri:Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_second, container, false)
        iv_croped=view.findViewById(R.id.iv_croped)
//        ib_earse=view.findViewById(R.id.ib_earse)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        croppedUri=args.uriInString.toUri()
        Log.d(TAG, "onViewCreated: ${args.uriInString}")
        iv_croped.setImageURI(croppedUri)
        view.generateBtn.setOnClickListener{
            Navigation.findNavController(view).navigate(R.id.navigate_to_thirdFragment)

//            ib_earse.setOnClickListener.
//            iv_croped.setOnTouchListener()


        }

    }

}