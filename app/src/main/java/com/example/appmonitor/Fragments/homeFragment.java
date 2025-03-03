package com.example.appmonitor.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appmonitor.Adapter.AppAdapter;
import com.example.appmonitor.LoginScreen;
import com.example.appmonitor.Utilities.SharedPreferencesManager;
import com.example.appmonitor.Utilities.appInformation;
import com.example.appmonitor.databinding.FragmentHomeBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class homeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private AppAdapter adapter;
    private SharedPreferencesManager sharedPreferences;
    private List<appInformation> appInfoList;
    private Gson gson;
    private String username;

    private static final String APP_INFO_LIST_KEY = "APP_INFO_LIST";
    private static final long UPDATE_INTERVAL_MS = 1000; // Changed to 5 second
    private DatabaseReference databaseRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        binding.homeMBBack.setOnClickListener(v->returnToLoginActivity());
        appInfoList = new ArrayList<>();

        if (getArguments() != null) {
            username = getArguments().getString("userName");
            // בדוק אם ה-username לא null לפני שמנסים לגשת ל-Firebase
            if (username != null) {
                databaseRef = FirebaseDatabase.getInstance().getReference("Users").child(username).child("Apps");
            } else {
                Log.e("homeFragment", "Username is null");
            }
        }

        adapter = new AppAdapter(appInfoList, requireContext(), 1);
        binding.HomeRVAppList.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.HomeRVAppList.setAdapter(adapter);

        // הוסף בדיקה אם databaseRef לא null לפני שמנסים לגשת ל-Firebase
        if (databaseRef != null) {
            getAppsFromFireBase();
        } else {
            Log.e("homeFragment", "Database reference is null, unable to fetch data from Firebase.");
        }

        return binding.getRoot();
    }

    private void getAppsFromFireBase() {
        // ודא ש- databaseRef לא null לפני שמנסים להוסיף את ה-ValueEventListener
        if (databaseRef != null) {
            databaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    appInfoList.clear();

                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    for (DataSnapshot donationSnapshot : dataSnapshot.getChildren()) {
                        appInformation app = donationSnapshot.getValue(appInformation.class);
                        if (app != null) {
                            appInfoList.add(app);
                        }
                    }
                    adapter.setAppInfoList(appInfoList);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle possible errors
                    Log.e("homeFragment", "Database error: " + databaseError.getMessage());
                }
            });
        } else {
            Log.e("homeFragment", "Database reference is null, cannot add ValueEventListener.");
        }

    }

    private void returnToLoginActivity() {
        SharedPreferencesManager.getInstance().putBoolean("isLoggedIn", false);
        Intent intent = new Intent(getActivity(), LoginScreen.class);
        startActivity(intent);
    }
}

