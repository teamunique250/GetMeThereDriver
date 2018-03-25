package com.corelabsplus.getmetheredriver.utils;

import android.app.Activity;
import android.database.Cursor;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.corelabsplus.getmetheredriver.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

/**
 * Created by nissi on 3/24/18.
 */

public class DrawerUtil {


    public static void getDrawer(final Activity activity, Toolbar toolbar){

//        final UserSessionManager session = new UserSessionManager(activity);
//        dbHandler = new DbHandler(activity);
//        Cursor data = dbHandler.getProfileItems();

//        if(data.getCount() > 0){
//            while (data.moveToNext()){
//                name = data.getString(data.getColumnIndex("name"));
//                email = data.getString(data.getColumnIndex("email"));
//            }
//        }
//        else{
//            if (user != null){
////                loadProfileData(activity);
//                name = user.getDisplayName();
//                email = user.getEmail();
//                userId = user.getUid();
//            }
//        }

//        AccountHeader headerResult = new AccountHeaderBuilder()
//                .withActivity(activity)
//                .withHeaderBackground(R.color.primary_dark)
//                .addProfiles(
//                        new ProfileDrawerItem().withName(String.valueOf(name))
//                                .withEmail(String.valueOf(email))
//                )
//                .withProfileImagesVisible(false)
//                .withSelectionListEnabledForSingleProfile(false)
//                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
//                    @Override
//                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
//                        return false;
//                    }
//                })
//                .build();

        PrimaryDrawerItem empty = new PrimaryDrawerItem().withIdentifier(1)
                .withName("");

        PrimaryDrawerItem profile = new PrimaryDrawerItem().withIdentifier(1)
                .withName("Profile");

        PrimaryDrawerItem logout = new PrimaryDrawerItem().withIdentifier(5)
                .withName("Logout");


        Drawer result = new DrawerBuilder()
                .withActivity(activity)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .withCloseOnClick(true)
                .withSelectedItem(-1)
                .withDrawerWidthDp(280)
                .addDrawerItems(
//                        drawerEmptyItem,drawerEmptyItem,drawerEmptyItem,
                        empty,
                        profile,
                        logout
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        switch (position){
                            case 2:
                                break;
                            case 3:
                                break;
                        }

                        return true;
                    }
                })
                .build();

    }

}
