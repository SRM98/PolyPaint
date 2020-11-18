//package com.example.polypaintapp
//
//import android.content.Context
//import android.view.View
//import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
//import com.mikepenz.materialdrawer.AccountHeader
//import com.mikepenz.materialdrawer.AccountHeaderBuilder
//import com.mikepenz.materialdrawer.Drawer
//import com.mikepenz.materialdrawer.DrawerBuilder
//import com.mikepenz.materialdrawer.model.DividerDrawerItem
//import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
//import com.mikepenz.materialdrawer.model.ProfileDrawerItem
//import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
//import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
//import com.mikepenz.materialdrawer.model.interfaces.IProfile
//
//class Drawer {
//
//
//
//
//
//    fun addDrawer(context: Context){
//
//        val headerResult = AccountHeaderBuilder()
//            .withActivity(this)
//            .addProfiles(
//                ProfileDrawerItem().withName(User.username).withIcon("@drawable/sergiu")
//            )
//            .withProfileImagesClickable(true)
//            .withOnAccountHeaderProfileImageListener(object: AccountHeader.OnAccountHeaderProfileImageListener{
//                override fun onProfileImageClick(view: View, profile: IProfile<*>, current: Boolean): Boolean {
//                    val transaction = supportFragmentManager.beginTransaction().apply {
//                        replace(R.id.nav_host_fragment_lobby, FragmentProfil(supportFragmentManager))
//                        addToBackStack(null)
//                    }
//
//                    transaction.commit()
//
//                    return false
//                }
//
//                override fun onProfileImageLongClick(
//                    view: View,
//                    profile: IProfile<*>,
//                    current: Boolean
//                ): Boolean {
//                    return false
//                }
//            })
//            .build()
//
//        val item_gameModes = PrimaryDrawerItem().withIdentifier(1).withName(R.string.item_lobby).withIcon(
//            GoogleMaterial.Icon.gmd_home)
//        val item_freeDraw = PrimaryDrawerItem().withIdentifier(2).withName(R.string.item_freeDraw)
//        val item_settings = PrimaryDrawerItem().withIdentifier(3).withName(R.string.item_settings).withIcon(
//            GoogleMaterial.Icon.gmd_perm_data_setting)
//        val item_leaderboard = PrimaryDrawerItem().withIdentifier(4).withName(R.string.item_leaderBoard)
//
//        val item_signOut = SecondaryDrawerItem().withIdentifier(5).withName(R.string.item_signOut).withIcon(
//            GoogleMaterial.Icon.gmd_exit_to_app)
//
//        val result = DrawerBuilder()
//            .withAccountHeader(headerResult)
//            .withActivity(this)
//            .addDrawerItems(
//                item_gameModes,
//                item_freeDraw,
//                item_settings,
//                item_leaderboard,
//                DividerDrawerItem(),
//                item_signOut
//            )
//            .withTranslucentStatusBar(false)
//            .withActionBarDrawerToggle(false)
//            .withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener {
//                override fun onItemClick(view: View?, position: Int, drawerItem: IDrawerItem<*>): Boolean {
//                    // do something with the clicked item :D
//
//                    when(drawerItem.identifier)
//                    {
//                        1.toLong() -> {
//                            val transaction = supportFragmentManager.beginTransaction().apply {
//                                replace(R.id.nav_host_fragment_lobby, FragmentLobby())
//                                addToBackStack(null)
//                            }
//                            transaction.commit()
//
//                        }
//
//                        2.toLong() -> {
//                            val transaction = supportFragmentManager.beginTransaction().apply {
//                                replace(R.id.nav_host_fragment_lobby, FragmentFreeDraw())
//                                addToBackStack(null)
//                            }
//                            transaction.commit()
//                        }
//
//                        3.toLong() -> {
//                            val transaction = supportFragmentManager.beginTransaction().apply {
//                                replace(R.id.nav_host_fragment_lobby, FragmentSettings())
//                                addToBackStack(null)
//                            }
//                            transaction.commit()
//                        }
//
//                        4.toLong() -> {
//                            val transaction = supportFragmentManager.beginTransaction().apply {
//                                replace(R.id.nav_host_fragment_lobby, FragmentLeaderBoard())
//                                addToBackStack(null)
//                            }
//                            transaction.commit()
//                        }
//
//                        5.toLong() -> finish()
//                    }
//
//                    return false
//                }
//
//            })
//            .build()
//    }
//}