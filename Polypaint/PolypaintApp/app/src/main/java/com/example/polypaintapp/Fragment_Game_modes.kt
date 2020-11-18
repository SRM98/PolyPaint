package com.example.polypaintapp

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils.replace
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import com.mikepenz.fastadapter.dsl.genericFastAdapter
import kotlinx.android.synthetic.main.game_modes_choice.*

class Fragment_Game_modes : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.game_modes_choice, container, false)
        //Back pressed Logic for fragment
        v.setFocusableInTouchMode(true)
        v.requestFocus()
        v.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
                if (event.getAction() === KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {

                        val intent = Intent(context, LobbyActivity::class.java)

                        startActivity(intent)
                    }
                }
                return false
            }
        })
        println("onCreateView")
        return v
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        val adapter = GameModes_ViewPager(activity!!.supportFragmentManager)

        adapter.addFragment(Fragment_DesignClassic(), "CLASSIC")
        adapter.addFragment(Fragment_DesignCoop(), "COOP")
        adapter.addFragment(Fragment_DesignSolo(), "SOLO")
        adapter.addFragment(Fragment_DesginDuel(), "DUEL")


        viewPager.adapter = adapter
        tabs.setupWithViewPager(viewPager)
        println(tabs.selectedTabPosition)

        return_lobby.setOnClickListener{
            val intent = Intent(context, LobbyActivity::class.java)
            startActivity(intent)
        }

        chatButton.setOnClickListener {view ->
            SocketUser.gameModes = true
            view.findNavController().navigate(R.id.action_fragment_Game_modes_to_nav_graph_chat)
        }
        SocketUser.gameModes = false
    }

    override fun onDestroyView() {
        viewPager.removeAllViews()
        onDestroy()
        super.onDestroyView()

    }

    override fun onDestroy() {
        super.onDestroy()
    }

}