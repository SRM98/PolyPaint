package com.example.polypaintapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils.replace
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.activity_lobby.*
import kotlinx.android.synthetic.main.design_classic.*
import kotlinx.android.synthetic.main.design_classic.buttonHelp
import kotlinx.android.synthetic.main.fragment_tutorial.*
import kotlinx.android.synthetic.main.fragment_tutorial.view.*
import kotlinx.android.synthetic.main.game_modes_choice.*

class Fragment_DesignClassic : Fragment() {
    var imageList = intArrayOf(R.drawable.tut001, R.drawable.tut002, R.drawable.tut003, R.drawable.tut004, R.drawable.tut005)


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.design_classic, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        PLAY.setOnClickListener {VIEW ->

            VIEW.findNavController().navigate(R.id.action_fragment_Game_modes_to_fragment_classique)

        }

        buttonHelp.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(activity!!)
            val inflater = this.layoutInflater
            val dialogView = inflater.inflate(R.layout.fragment_tutorial, null)
            dialogBuilder.setView(dialogView)

            var dialog = dialogBuilder.create();
            dialogView.previousButton.setOnClickListener{
                dialogView.tutorialViewFlipper.setInAnimation(activity!!, android.R.anim.slide_in_left)
                dialogView.tutorialViewFlipper.setOutAnimation(activity!!, android.R.anim.slide_out_right)
                dialog.tutorialViewFlipper.showPrevious()
            }
            dialogView.nextButton.setOnClickListener{
                dialogView.tutorialViewFlipper.setInAnimation(activity!!, android.R.anim.slide_in_left)
                dialogView.tutorialViewFlipper.setOutAnimation(activity!!, android.R.anim.slide_out_right)
                dialogView.tutorialViewFlipper.showNext()
            }
            if (dialogView.tutorialViewFlipper != null) {
                for (image in imageList) {
                    val imageView = ImageView(activity!!)
                    val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    layoutParams.setMargins(30, 30, 30, 30)
                    layoutParams.gravity = Gravity.CENTER
                    imageView.layoutParams = layoutParams
                    imageView.setImageResource(image)
                    dialogView.tutorialViewFlipper.addView(imageView)
                }
            }
            dialogView.tutorialViewFlipper.setDisplayedChild(0)
            dialog.show()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
    }



}



