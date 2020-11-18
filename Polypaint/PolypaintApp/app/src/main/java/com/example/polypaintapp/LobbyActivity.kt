package com.example.polypaintapp

import kotlinx.android.synthetic.main.fragment_tutorial.*


import android.app.Activity
import android.content.Intent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore.Images.Media.getBitmap
import android.util.AttributeSet
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.android.synthetic.main.activity_lobby.*
import com.divyanshu.draw.R.*
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IProfile
import kotlinx.android.synthetic.main.fragment_profilview.*
import kotlinx.android.synthetic.main.password_dialog.*
import org.json.JSONArray
import android.widget.*
import android.view.Gravity;
import android.view.View;
import android.widget.ViewFlipper;
import android.app.AlertDialog;
import kotlinx.android.synthetic.main.fragment_tutorial.view.*

class LobbyActivity : AppCompatActivity(){
    var imageList = intArrayOf(R.drawable.tut001, R.drawable.tut002, R.drawable.tut003, R.drawable.tut004, R.drawable.tut005)

    var drawer: Drawer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lobby)


        SocketUser.onMessage(this as Context)
        getMyRooms()
        onFirstTimeUser()

        SocketUser.socket.getOwnRooms()
        SocketUser.socket.checkFirstTimeUser()

        //tutorial dialog here
        //viewflipper init
//        val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//        layoutParams.setMargins(30, 30, 30, 30)
//        layoutParams.gravity = Gravity.CENTER
//        viewFlipper.layoutParams = layoutParams
//        viewFlipper.setFlipInterval(2000)
//        viewFlipper.isAutoStart = true
//
//        viewFlipper.setInAnimation(this, android.R.anim.slide_in_left)
//        viewFlipper.setOutAnimation(this, android.R.anim.slide_out_right)

        supportFragmentManager.popBackStack()
        val transaction = supportFragmentManager.beginTransaction().apply {
            replace(R.id.nav_host_fragment_lobby, FragmentLobby())
            addToBackStack(null)

        }
        transaction.commit()

        if(User.avatarUrl != "noPhoto") {
            val uri = IP + "/" + User.avatarUrl
            Glide.with(this).asBitmap().load(uri.toUri()).into(object :CustomTarget<Bitmap>(){
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    drawer = createDrawer(resource as Bitmap)
                    User.avatarBitmap = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
        } else {
            val bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.no_photo)
            drawer = createDrawer(bitmap)
            User.avatarBitmap = bitmap
        }

        drawerButton.setOnClickListener {
            drawer?.openDrawer()
        }

        buttonHelp.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)
            val inflater = this.layoutInflater
            val dialogView = inflater.inflate(R.layout.fragment_tutorial, null)
            dialogBuilder.setView(dialogView)

            var dialog = dialogBuilder.create();
            dialogView.previousButton.setOnClickListener{
                dialogView.tutorialViewFlipper.setInAnimation(this, android.R.anim.slide_in_left)
                dialogView.tutorialViewFlipper.setOutAnimation(this, android.R.anim.slide_out_right)
                dialog.tutorialViewFlipper.showPrevious()
            }
            dialogView.nextButton.setOnClickListener{
                dialogView.tutorialViewFlipper.setInAnimation(this, android.R.anim.slide_in_left)
                dialogView.tutorialViewFlipper.setOutAnimation(this, android.R.anim.slide_out_right)
                dialogView.tutorialViewFlipper.showNext()
            }
            if (dialogView.tutorialViewFlipper != null) {
                for (image in imageList) {
                    val imageView = ImageView(this)
                    val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    layoutParams.setMargins(30, 30, 30, 30)
                    layoutParams.gravity = Gravity.CENTER
                    imageView.layoutParams = layoutParams
                    imageView.setImageResource(image)
                    dialogView.tutorialViewFlipper.addView(imageView)
                }
            }
            dialog.show()
        }
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
         return super.onCreateView(name, context, attrs)
    }

    fun getMyRooms() {

        SocketUser.socket.socket.on("ownRooms") { data ->
            SocketUser.joinedChannels.clear()
            val json = JSON()
            var ownChan = JSONArray(data[0].toString())
            for (i in 0 until ownChan.length()) {
                SocketUser.joinedChannels.add(ownChan[i].toString())
                val room = Room(User.username, ownChan[i].toString())
                SocketUser.socket.join(json.toJson(room))
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_options, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if(id == R.id.action_settings){
            return true
        } else {
            return super.onOptionsItemSelected(item)

        }
    }

    override fun onBackPressed() {
        if(supportFragmentManager.backStackEntryCount > 1 )
            super.onBackPressed()
        else
            return
    }

    fun createDrawer(bitmap: Bitmap): Drawer{

        val headerResult = AccountHeaderBuilder()
            .withActivity(this)
            .addProfiles(
                ProfileDrawerItem().withName(User.username).withIcon(bitmap).withIdentifier(30)
            )
            .withProfileImagesClickable(true)
            .withOnAccountHeaderProfileImageListener(object: AccountHeader.OnAccountHeaderProfileImageListener{
                override fun onProfileImageClick(view: View, profile: IProfile<*>, current: Boolean): Boolean {
                    val transaction = supportFragmentManager.beginTransaction().apply {
                        replace(R.id.nav_host_fragment_lobby, FragmentProfil(supportFragmentManager))
                        addToBackStack(null)
                    }

                    transaction.commit()

                    return false
                }

                override fun onProfileImageLongClick(
                    view: View,
                    profile: IProfile<*>,
                    current: Boolean
                ): Boolean {
                    return false
                }
            })
            .build()

        val item_gameModes = PrimaryDrawerItem().withIdentifier(1).withName(R.string.item_lobby).withIcon(GoogleMaterial.Icon.gmd_home)
        val item_signOut = SecondaryDrawerItem().withIdentifier(5).withName(R.string.item_signOut).withIcon(GoogleMaterial.Icon.gmd_exit_to_app)

        val result = DrawerBuilder()
            .withAccountHeader(headerResult)
            .withActivity(this)
            .addDrawerItems(
                item_gameModes,
                DividerDrawerItem(),
                item_signOut
            )
            .withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener {
                override fun onItemClick(view: View?, position: Int, drawerItem: IDrawerItem<*>): Boolean {
                    // do something with the clicked item :D

                    when(drawerItem.identifier)
                    {
                        1.toLong() -> {
                            val transaction = supportFragmentManager.beginTransaction().apply {
                                replace(R.id.nav_host_fragment_lobby, FragmentLobby())
                                addToBackStack(null)
                            }
                            transaction.commit()

                        }

                        2.toLong() -> {
                            val transaction = supportFragmentManager.beginTransaction().apply {
                                replace(R.id.nav_host_fragment_lobby, FragmentFreeDraw(false, supportFragmentManager, true))
                                addToBackStack(null)
                            }
                            transaction.commit()
                        }

                        3.toLong() -> {
                            val transaction = supportFragmentManager.beginTransaction().apply {
                                replace(R.id.nav_host_fragment_lobby, FragmentSettings())
                                addToBackStack(null)
                            }
                            transaction.commit()
                        }

                        4.toLong() -> {
                            val transaction = supportFragmentManager.beginTransaction().apply {
                                replace(R.id.nav_host_fragment_lobby, FragmentStats())
                                addToBackStack(null)
                            }
                            transaction.commit()
                        }

                        6.toLong() -> {
                            val transaction = supportFragmentManager.beginTransaction().apply {
                                replace(R.id.nav_host_fragment_lobby, FragmentChannel())
                                addToBackStack(null)
                            }
                            transaction.commit()
                        }

                        5.toLong() -> {
                            User.username = ""
                            User.password = ""
                            User.socketId = ""
                            User.avatarBitmap = null
                            User.avatarByteArray = null
                            User.avatarUrl = ""
                            SocketUser.socket.socket.off("message")
                            SocketUser.notif.cancelAll(this@LobbyActivity)
                            for(objects in SocketUser.joinedChannels){
                                SocketUser.notif.cancel(SocketUser.joinedChannels.indexOf(objects), this@LobbyActivity)
                            }
                            SocketUser.socket.disconnectUser()
                            val intent = Intent(applicationContext, MainActivity::class.java)
                            startActivity(intent)
                        }

                    }

                    return false
                }

            })
            .build()
        return result
    }

//    fun onTutorialDialog(v:android.view.View) {
//        val dialogBuilder = AlertDialog.Builder(this)
//        val inflater = this.layoutInflater
//        val dialogView = inflater.inflate(R.layout.fragment_tutorial, null)
//        dialogBuilder.setView(dialogView)
//
//        var dialog = dialogBuilder.create();
//        dialogView.previousButton.setOnClickListener{
//            dialogView.tutorialViewFlipper.setInAnimation(this, android.R.anim.slide_in_left)
//            dialogView.tutorialViewFlipper.setOutAnimation(this, android.R.anim.slide_out_right)
//            dialog.tutorialViewFlipper.showPrevious()
//        }
//        dialogView.nextButton.setOnClickListener{
//            dialogView.tutorialViewFlipper.setInAnimation(this, android.R.anim.slide_in_left)
//            dialogView.tutorialViewFlipper.setOutAnimation(this, android.R.anim.slide_out_right)
//            dialogView.tutorialViewFlipper.showNext()
//        }
//        if (dialogView.tutorialViewFlipper != null) {
//            for (image in imageList) {
//                val imageView = ImageView(this)
//                val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//                layoutParams.setMargins(30, 30, 30, 30)
//                layoutParams.gravity = Gravity.CENTER
//                imageView.layoutParams = layoutParams
//                imageView.setImageResource(image)
//                dialogView.tutorialViewFlipper.addView(imageView)
//            }
//        }
//        dialog.show()
//    }

    fun onFirstTimeUser() {
        SocketUser.socket.socket.on("isFirstTimeUserThinClient") { data ->
            this.runOnUiThread{
                val dialogBuilder = AlertDialog.Builder(this)
                val inflater = this.layoutInflater
                val dialogView = inflater.inflate(R.layout.fragment_tutorial, null)
                dialogBuilder.setView(dialogView)

                var dialog = dialogBuilder.create();
                dialogView.previousButton.setOnClickListener{
                    dialogView.tutorialViewFlipper.setInAnimation(this, android.R.anim.slide_in_left)
                    dialogView.tutorialViewFlipper.setOutAnimation(this, android.R.anim.slide_out_right)
                    dialog.tutorialViewFlipper.showPrevious()
                }
                dialogView.nextButton.setOnClickListener{
                    dialogView.tutorialViewFlipper.setInAnimation(this, android.R.anim.slide_in_left)
                    dialogView.tutorialViewFlipper.setOutAnimation(this, android.R.anim.slide_out_right)
                    dialogView.tutorialViewFlipper.showNext()
                }
                if (dialogView.tutorialViewFlipper != null) {
                    for (image in imageList) {
                        val imageView = ImageView(this)
                        val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        layoutParams.setMargins(30, 30, 30, 30)
                        layoutParams.gravity = Gravity.CENTER
                        imageView.layoutParams = layoutParams
                        imageView.setImageResource(image)
                        dialogView.tutorialViewFlipper.addView(imageView)
                    }
                }
                dialog.show()
            }
        }
    }
}
