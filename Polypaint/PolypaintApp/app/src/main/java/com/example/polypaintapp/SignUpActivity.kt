package com.example.polypaintapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.fragment_signup.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import android.R.attr.data
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore.Images.Media.getBitmap
import androidx.core.net.toUri
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import kotlinx.android.synthetic.main.fragment_signup.view.*
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.*
import kotlin.concurrent.schedule


class FragmentSignUp : Fragment() {

    val PICKFILE_RESULT_CODE = 1
    var fileUri: Uri = "".toUri()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        User.avatarByteArray = null

        //User details while in sign uo draw graph
        var userFirstNameTmp = arguments?.getString("userFirstName")
        var userLastNameTmp = arguments?.getString("userLastName")
        var userUsernameTmp = arguments?.getString("userUsername")
        var userPasswordTmp = arguments?.getString("userPassword")

        editFirstName.setText(userFirstNameTmp)
        editLastName.setText(userLastNameTmp)
        editUsername.setText(userUsernameTmp)
        editPassword.setText(userPasswordTmp)

        //Setting the preview
        var userByteArray: ByteArray? = null
        userByteArray = arguments?.getByteArray("userByteArray")
        if(userByteArray != null){
            ByteArrayTmp.source = "draw"
            val imageConverter = ImageConverter()
            val tmp = imageConverter.toBitmap(userByteArray)
            avatar_preview.setImageBitmap(tmp)
        }

        btn_drawAvatar.setOnClickListener {
            userFirstNameTmp = editFirstName.text.toString()
            userLastNameTmp = editLastName.text.toString()
            userUsernameTmp = editUsername.text.toString()
            userPasswordTmp = editPassword.text.toString()

            val bundle = bundleOf("inSignUp" to true, "userFirstName" to userFirstNameTmp, "userLastName" to userLastNameTmp,
                                            "userUsername" to userUsernameTmp, "userPassword" to userPasswordTmp)

            view.findNavController().navigate(R.id.action_fragmentSignUp_to_fragmentFreeDraw, bundle)
        }

        buttonChooseFile.setOnClickListener {
            var chooseFile: Intent = Intent(Intent.ACTION_GET_CONTENT)
            chooseFile.setType("*/*")
            chooseFile = Intent.createChooser(chooseFile, "Choose your avatar")
            startActivityForResult(chooseFile, PICKFILE_RESULT_CODE)
        }

        signinFromSignUp.setOnClickListener {view ->
            var byteArray: ByteArray? = null
            if(ByteArrayTmp.source == "draw"){
                byteArray = userByteArray
            }else if(ByteArrayTmp.source == "storage"){
                byteArray = ByteArrayTmp.byteArrayOfStorage
            }

            if(byteArray != null){
                val stats = Stats(0,0,0,0,0,0,0)
                val account = Account(editFirstName.text.toString(), editLastName.text.toString(), editUsername.text.toString(),
                    editPassword.text.toString(), byteArray, "", true, emptyArray(), stats)

                val http = HttpRequest()
                context?.let { context ->
                    http.signUpRequest(account, CallbackSignUp(context, view, account))
                }
            } else {
                val stats = Stats(0,0,0,0,0,0,0)
                val account = Account(editFirstName.text.toString(), editLastName.text.toString(), editUsername.text.toString(),
                    editPassword.text.toString(), null, "", true, emptyArray(), stats)

                val http = HttpRequest()
                context?.let { context ->
                    http.signUpRequest(account, CallbackSignUp(context, view, account))
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            PICKFILE_RESULT_CODE -> if (resultCode === -1) {
                fileUri = data?.getData() as Uri

                val bitmap = getBitmap(activity?.contentResolver, fileUri)
                avatar_preview.setImageBitmap(bitmap)

                val stream: ByteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 30, stream)

                val compressedByteArray = stream.toByteArray()
                // val compressedBitmap = BitmapFactory.decodeByteArray(bArray,0, bArray.size)

                //ByteArrayTmp.byteArrayOfStorage = ImageConverter().toByteArray(bitmap)
                ByteArrayTmp.byteArrayOfStorage = compressedByteArray
                ByteArrayTmp.source = "storage"
            }
        }
    }
}

class CallbackSignUp(private val context: Context, private val view: View, private val account: Account): Callback {

    override fun onResponse(call: Call, response: Response) {
        println("Server Response for signIn")
        this.responseState = true
        responseBody = response.message

        if (responseBody == "OK") {
            (context as AppCompatActivity).runOnUiThread {

                val bundle = Bundle()
                bundle.putBoolean("signedUp", true)
                bundle.putString("username", account.username)
                bundle.putString("password", account.password)

                view.findNavController().navigate(R.id.action_fragmentSignUp_to_fragmentSignIn, bundle)
            }
        } else {
            (context as AppCompatActivity).runOnUiThread {
                var tmp = response.body?.string()?.replace("\"", "")
                YoYo.with(Techniques.Wobble).duration(500).playOn(view.signinFromSignUp)
                Toast.makeText(context, tmp, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onFailure(call: Call, e: IOException) {
        Toast.makeText(context, "signIn failed", Toast.LENGTH_SHORT).show()
        responseBody = e.toString()
    }

    var responseState: Boolean = false
    var responseBody: String? = ""
}

object ByteArrayTmp{
    var byteArrayOfStorage: ByteArray? = null
    var source: String = ""
}