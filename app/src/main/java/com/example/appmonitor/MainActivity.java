package com.example.appmonitor;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.appmonitor.Fragments.homeFragment;
import com.example.appmonitor.Fragments.timerFragment;
import com.example.appmonitor.Service.AppService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Retrieve username from intent
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            username = bundle.getString("Username");
        }

        // Initialize views and set up navigation
        initViews();
        setupBottomNavigation();

        // Set the initial fragment to home
        loadInitialFragment();

        // Ensure permission for usage stats and start service
        ensureUsageStatsPermission();
        checkNotificationPermission();
        startAppService();
    }

    /**
     * Check if the app has notification permission
     */
    private void checkNotificationPermission() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (!notificationManager.areNotificationsEnabled()) {
            requestNotificationPermission();
        }
    }

    /**
     * Request notification permission if not granted
     */
    private void requestNotificationPermission() {
        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        startActivity(intent);
    }

    /**
     * Initialize views and UI components
     */
    private void initViews() {
        bottomNavigationView = findViewById(R.id.main_NB_bottomNavigatorBar);
    }

    /**
     * Set up the bottom navigation bar with a listener for fragment navigation
     */
    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    /**
     * Handle bottom navigation item selection
     *
     * @param item Selected menu item
     * @return true if the fragment was loaded successfully, false otherwise
     */
    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;

        // Pass username to fragments
        Bundle bundle = new Bundle();
        bundle.putString("userName", username);

        switch (item.getItemId()) {
            case R.id.nav_home:
                selectedFragment = new homeFragment();
                break;
            case R.id.nav_timer:
                selectedFragment = new timerFragment();
                break;
            default:
                return false;
        }

        selectedFragment.setArguments(bundle);
        loadFragment(selectedFragment);
        return true;
    }

    /**
     * Load the initial home fragment
     */
    private void loadInitialFragment() {
        Fragment homeFragment = new homeFragment();
        Bundle bundle = new Bundle();
        bundle.putString("userName", username);
        homeFragment.setArguments(bundle);
        loadFragment(homeFragment);
    }

    /**
     * Load the selected fragment into the container
     *
     * @param fragment The fragment to load
     */
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_Fragment_fragmentContainer, fragment)
                .commit();
    }

    /**
     * Ensure the app has the required usage stats permission
     */
    private void ensureUsageStatsPermission() {
        if (!hasUsageStatsPermission()) {
            requestUsageStatsPermission();
        }
    }

    /**
     * Opens the system settings screen for usage access permission
     */
    private void requestUsageStatsPermission() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    }

    /**
     * Checks if the app has permission to access usage statistics
     *
     * @return true if permission is granted, false otherwise
     */
    private boolean hasUsageStatsPermission() {
        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        if (usm == null) return false;

        long currentTime = System.currentTimeMillis();
        List<UsageStats> stats = usm.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                currentTime - 1000 * 60 * 60 * 24, // 24 hours ago
                currentTime
        );

        return stats != null && !stats.isEmpty();
    }

    /**
     * Start the AppUsageService as a foreground service
     */
    private void startAppService() {
        Intent serviceIntent = new Intent(this, AppService.class);
        serviceIntent.putExtra("Username", username);
        ContextCompat.startForegroundService(this, serviceIntent);
    }
}
