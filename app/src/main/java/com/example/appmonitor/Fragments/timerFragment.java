package com.example.appmonitor.Fragments;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Base64;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.example.appmonitor.Adapter.AppAdapter;
import com.example.appmonitor.Utilities.appInformation;
import com.example.appmonitor.databinding.FragmentTimerBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class timerFragment extends Fragment {

    private FragmentTimerBinding binding;
    private List<appInformation> appInfoList;
    private AppAdapter adapter;
    private String username;

    DatabaseReference databaseRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTimerBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            username = getArguments().getString("userName");
            Log.d("TEST!!",username.toString());
            if (username != null) {
                databaseRef  = FirebaseDatabase.getInstance().getReference("Users").child(username).child("Apps");
            } else {
                Log.e("timerFragment", "Username is null");
            }
        }

        appInfoList = new ArrayList<>();
        getAppsFromFireBase();

        initViews();
        setupSpinners();
        setupAdapter();

        return binding.getRoot();
    }

    private void getAppsFromFireBase() {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("CHECK!!", dataSnapshot.getChildren().toString());
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
            }
        });
    }

    private void initViews() {
        binding.timerMBTimePick.setOnClickListener(v -> binding.timerCARDTimeOptionCard.setVisibility(View.VISIBLE));

        binding.timerMBAddToTrakList.setOnClickListener(v -> {
            binding.timerCARDTimeOptionCard.setVisibility(View.GONE);
            String packageName = findAppPackageName(requireContext(), binding.spinnerApps.getSelectedItem().toString());
            createNewAppObject(
                    binding.spinnerApps.getSelectedItem().toString(),
                    binding.spinnerHours.getSelectedItem().toString(),
                    binding.spinnerMinutes.getSelectedItem().toString(),
                    packageName
            );
        });
    }

    public String findAppPackageName(Context context, String appNameToFind) {
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packages = packageManager.getInstalledPackages(0);

        for (PackageInfo packageInfo : packages) {
            ApplicationInfo appInfo = packageInfo.applicationInfo;
            String appName = packageManager.getApplicationLabel(appInfo).toString();

            if (appName.equalsIgnoreCase(appNameToFind)) {
                Log.d("PackageName", "Package Name of " + appNameToFind + ": " + packageInfo.packageName);
                return packageInfo.packageName;
            }
        }

        Log.d("PackageName", "App " + appNameToFind + " not found.");
        return null;
    }

    private void setupSpinners() {
        setupTimeSpinner(binding.spinnerHours, 0, 10, "%d");
        setupTimeSpinner(binding.spinnerMinutes, 0, 59, "%02d");
        setupAppSpinner();
    }

    private void setupTimeSpinner(Spinner spinner, int start, int end, String format) {
        List<String> values = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            values.add(String.format(format, i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setupAppSpinner() {
        List<String> appNames = getInstalledAppNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, appNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerApps.setAdapter(adapter);
    }

    private void setupAdapter() {
        adapter = new AppAdapter(appInfoList, requireContext(), 2);
        binding.timerRVAppList.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.timerRVAppList.setAdapter(adapter);
    }

    private void createNewAppObject(String appName, String hours, String minutes, String packageName) {
        if ("0".equals(hours) && "00".equals(minutes)) return;

        String selectedTime = hours + ":" + minutes;
        PackageManager pm = requireActivity().getPackageManager();
        ApplicationInfo appInfo = getApplicationInfoByName(appName, pm);

        if (appInfo != null) {
            String appIconBase64 = convertDrawableToBase64(appInfo.loadIcon(pm));
            appInformation newApp = new appInformation(appName, selectedTime, Integer.parseInt(hours), Integer.parseInt(minutes), appIconBase64, packageName);

//            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Apps");

            if (newApp.getAppName() != null) {
                databaseRef.child(newApp.getAppName()).setValue(newApp)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d("FirebaseSave", "App added successfully: " + appName);
                                appInfoList.add(newApp);
                                adapter.notifyDataSetChanged();
                            } else {
                                Log.e("FirebaseSave", "Error adding app: " + task.getException().getMessage());
                            }
                        });
            } else {
                Log.e("FirebaseSave", "Error generating unique key for app.");
            }
        }
    }

    private ApplicationInfo getApplicationInfoByName(String appName, PackageManager pm) {
        for (ApplicationInfo appInfo : pm.getInstalledApplications(PackageManager.GET_META_DATA)) {
            if (appName.equals(appInfo.loadLabel(pm).toString())) {
                return appInfo;
            }
        }
        return null;
    }

    private String convertDrawableToBase64(Drawable drawable) {
        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof AdaptiveIconDrawable) {
            bitmap = getBitmapFromAdaptiveIcon((AdaptiveIconDrawable) drawable);
        } else {
            throw new IllegalArgumentException("Unsupported drawable type");
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
    }

    private Bitmap getBitmapFromAdaptiveIcon(AdaptiveIconDrawable adaptiveIcon) {
        int size = 128;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        adaptiveIcon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        adaptiveIcon.draw(canvas);
        return bitmap;
    }

    private List<String> getInstalledAppNames() {
        List<String> appNames = new ArrayList<>();
        PackageManager pm = requireActivity().getPackageManager();

        for (ApplicationInfo appInfo : pm.getInstalledApplications(PackageManager.GET_META_DATA)) {
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && pm.getLaunchIntentForPackage(appInfo.packageName) != null) {
                appNames.add(appInfo.loadLabel(pm).toString());
            }
        }

        Collections.sort(appNames, String::compareToIgnoreCase);
        return appNames;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
