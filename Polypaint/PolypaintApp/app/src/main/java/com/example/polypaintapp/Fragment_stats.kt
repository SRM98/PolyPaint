package com.example.polypaintapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_stats.*


class FragmentStats : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nbGamePlayed.text = User.stats?.nbGames.toString()
        winRate.text = User.stats?.victoryPercentage.toString()


        //average game duration
        val gameDuration = User.stats?.averageMatchesTime as Number
        val numberHours = (gameDuration.toFloat() / 3600).toInt()
        val restesHours = gameDuration.toFloat() % 3600

        val minutes = (restesHours / 60).toInt()
        val secondes = (restesHours % 60).toInt()

        val tmp = numberHours.toString() + "h " + minutes.toString() + "m " + secondes.toString() + "s"
        averageGameDuration.text = tmp

        //total time
        val totalTime = User.stats?.totalMatchesTime as Number
        val hours = (totalTime.toFloat() / 3600).toInt()
        val rHours = totalTime.toFloat() % 3600

        val min = (rHours / 60).toInt()
        val sec = (rHours % 60).toInt()

        val tmp2 = hours.toString() + "h " + min.toString() + "m " + sec.toString() + "s"

        totalTimeSpent.text = tmp2
    }
}