package com.annabohm.pracainzynierska;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, DrawerController {

    NavController navController;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;

    private static void closeDrawer(DrawerLayout drawerLayout) {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeDrawer(drawerLayout);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navController = Navigation.findNavController(this, R.id.navHostFragment);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                closeDrawer(drawerLayout);
                navController.navigate(R.id.mainFragment);
                break;

            case R.id.nav_my_invitations:
                closeDrawer(drawerLayout);
                navController.navigate(R.id.mainToInvitationList);
                break;

            case R.id.nav_app_settings:
                closeDrawer(drawerLayout);
                navController.navigate(R.id.mainToAppSettings);
                break;

            case R.id.nav_calendar:
                closeDrawer(drawerLayout);
                navController.navigate(R.id.mainToPastEvents);
                break;

            case R.id.nav_account_settings:
                closeDrawer(drawerLayout);
                navController.navigate(R.id.mainToAccountSettings);
                break;

            case R.id.nav_logout:
                closeDrawer(drawerLayout);
                userSignOut();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        NavDestination current = NavHostFragment.findNavController(getSupportFragmentManager().getPrimaryNavigationFragment().getFragmentManager().getFragments().get(0)).getCurrentDestination();
        int currentId = current.getId();

        if (currentId == R.id.mainFragment) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                userSignOut();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void setDrawerLocked() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public void setDrawerUnlocked() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    public void userSignOut() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.signout_title)
                .setMessage(R.string.signout_message)
                .setNegativeButton(R.string.signout_no, null)
                .setPositiveButton(R.string.signout_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(MainActivity.this, R.string.signout_success, Toast.LENGTH_SHORT).show();
                        navController.popBackStack();
                    }
                })
                .setIcon(R.drawable.ic_logout_gray)
                .show();
    }
}