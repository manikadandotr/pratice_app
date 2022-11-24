package com.example.practiceapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.custom_dialog_box.view.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }
}



/*



//        Simple AlertDialog Box
        val confirmationDialog = AlertDialog.Builder(this)
            .setTitle("Delete Files")
            .setMessage("Are you sure to delete the information")
            .setIcon(R.drawable.delete_icon)
            .setPositiveButton("Yes"){ _ , _ ->
                Toast.makeText(this,"All files deleted",Toast.LENGTH_SHORT).show()

            }
            .setNegativeButton("No"){ _, _ ->
                Toast.makeText(this,"Files not deleted",Toast.LENGTH_SHORT).show()
            }.create()

        simple_alertdialogbtn.setOnClickListener{
            confirmationDialog.show()
        }

//        Single Choice AlertDialog Box
        val paymentOptions= arrayOf("Cash","UPI","Debit card")
        val singleChoiceDialog=AlertDialog.Builder(this)
            .setTitle("Payment Method")
            .setIcon(R.drawable.payments_icon_foreground)
            .setSingleChoiceItems(paymentOptions,0){_,i->
                Toast.makeText(this,"we accept ${paymentOptions[i]}",Toast.LENGTH_LONG)
            }
            .setPositiveButton("Select"){_,_->
                Toast.makeText(this,"you selected the payment method",Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Cancel"){_,_->
                Toast.makeText(this,"you Cancelled the payment method",Toast.LENGTH_LONG).show()
            }.create()


        single_choice_alertdialogbtn.setOnClickListener{
            singleChoiceDialog.show()
        }
//      Custom Dialog Box
        custom_dialog_btn.setOnClickListener{
            val dialog=LayoutInflater.from(this).inflate(R.layout.custom_dialog_box,null)

            val customDialog=AlertDialog.Builder(this)
                .setView(dialog)
                .setTitle("Login Here!")

              val mAlertDialog= customDialog.show()

            dialog.Btnlogin.setOnClickListener{

                mAlertDialog.dismiss()
            }
        }

 */