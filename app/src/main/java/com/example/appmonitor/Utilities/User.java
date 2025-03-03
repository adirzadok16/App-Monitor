package com.example.appmonitor.Utilities;

public class User {
    private String Name;
    private String Username;
    private String password;

    public User(){

    }

    public User(String Name, String Username, String Password){
        this.Name = Name;
        this.Username = Username;
        this.password = Password;
    }

    public String getUsername() {
        return Username;
    }

    public String getName() {
        return Name;
    }

    public String getPassword() {
        return password;
    }
}
