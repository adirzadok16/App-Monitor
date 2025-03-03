package com.example.appmonitor.Adapter;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.appmonitor.Utilities.appInformation;
import com.example.appmonitor.databinding.HorizontalAppItemBinding;

import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder> {

    private List<appInformation> apps;
    private Context context;
    private int windowNumber;

    public AppAdapter(List<appInformation> apps , Context context , int windowNumber) {
        this.apps = apps; // Ensuring no null values
        this.context=context;
        this.windowNumber = windowNumber;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        HorizontalAppItemBinding binding = HorizontalAppItemBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AppViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        appInformation app = apps.get(position);
        holder.binding.appLBLTitle.setText(app.getAppName());
        Drawable icon = convertBase64ToDrawable(app.getAppIconBase64());
        holder.binding.appIMGIcon.setImageDrawable(icon);
        int hours = app.getHours();
        int minutes = app.getMinutes();
        if(windowNumber == 1){
            int appUsingTime = app.getTimeUse();
            int minutesSpentInApp = 0;
          if(hours ==0){
              minutesSpentInApp = Math.floorDiv(appUsingTime,60);
              holder.binding.appLBLTime.setText(minutesSpentInApp+"m");
          } else{
                  int hoursSpentInApp = Math.floorDiv(appUsingTime,3600);
                  minutesSpentInApp = Math.floorDiv(appUsingTime,60);
                  holder.binding.appLBLTime.setText(hoursSpentInApp+"h " + minutesSpentInApp+"m");
          }
        } else{
            if(hours != 0 && minutes!=0){
                holder.binding.appLBLTime.setText(hours+"h " + minutes+"m");
            } else if (minutes!=0 && hours==0) {
                holder.binding.appLBLTime.setText(minutes+"m");
            }
            else{
                holder.binding.appLBLTime.setText(hours+"h");

            }
        }
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    public void setAppList(List<appInformation> appList){
        this.apps = appList;
    }

    private Drawable convertBase64ToDrawable(String base64String) {
        byte[] byteArray = Base64.decode(base64String, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        return new BitmapDrawable(context.getResources(), bitmap);
    }

    public void setAppInfoList(List<appInformation> appInfoList) {
        this.apps = appInfoList;
    }

    public static class AppViewHolder extends RecyclerView.ViewHolder {
        HorizontalAppItemBinding binding;

        public AppViewHolder(@NonNull HorizontalAppItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}