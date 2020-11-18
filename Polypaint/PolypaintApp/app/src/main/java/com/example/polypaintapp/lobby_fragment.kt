package com.example.polypaintapp

import kotlinx.android.synthetic.main.fragment_lobby_main.*

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils.replace
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
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
import kotlinx.android.synthetic.main.activity_lobby.*
import kotlinx.android.synthetic.main.activity_lobby.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException


class FragmentLobby : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_lobby_main, container, false)
        v.isFocusableInTouchMode = true
        v.requestFocus()

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as LobbyActivity).nameZoneLobby.text = "MAIN MENU"

        goToChat.setOnClickListener {
            context?.let {context ->
                val intent = Intent(context, ChatActivity::class.java)

                startActivity(intent)
            }
        }

        goToPaint.setOnClickListener {
            context?.let {context ->
                val intent = Intent(context, PaintActivity::class.java)

                startActivity(intent)
            }

        }

        go_to_game_modes.setOnClickListener {
            context?.let {context ->
                val intent = Intent(context, Game_Modes::class.java)

                startActivity(intent)
            }

        }



    }

}