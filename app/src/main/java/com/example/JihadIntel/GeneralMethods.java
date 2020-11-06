package com.example.JihadIntel;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.WIFI_SERVICE;

public class GeneralMethods {

    static  int start = 0;
    static int RESULT_SIGN_OUT = -2, RESULT_SIGN_IN = 2, req_code = 111, LOCAL_SIGN_IN_REQ = 112, GET_DETAILS_REQ = 211, GET_DETAILS_RES = 212;
    static String TAG = "GeneralMethods";
    static int fetched_sync = 0;
    static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    static FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    static DatabaseReference dbRef;
    static WorkInBackground workInBackground;

    public static String hash(String s) {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,z");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static void upload_gps (Location location) {

        dbRef = firebaseDatabase.getReference("UserGpsData/" + GlobalData.userData.getId());
        HashMap<String,Double> location_values = new HashMap<>();
        location_values.put("latitude",location.getLatitude());
        location_values.put("longitude",location.getLongitude());
        location_values.put("altitude",location.getAltitude());

        dbRef.child(getDate()).setValue(location_values);
    }

    public static void postLoginCalls(Context context) {
        GeneralMethods.setUserData(MainActivity.sharedPreferences);
        getUserDeviceData(context);
        workInBackground = new WorkInBackground();
        workInBackground.execute(context);
    }

    public static String getDeviceIpMobileData(){
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                 en.hasMoreElements();) {
                NetworkInterface networkinterface = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = networkinterface.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("Current IP", ex.toString());
        }
        return null;
    }

    public static String getWifiIP (Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }

    public static void getUserDeviceData(Context context) {
        dbRef = firebaseDatabase.getReference("UserDeviceData/" + GlobalData.userData.getId());

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] nf = cm.getAllNetworkInfo();
        String ip = "";
        String netType = "";
        for (NetworkInfo n : nf) {
            Log.d(TAG, "getUserDeviceData: " + n.getTypeName());
            if (n.getTypeName().equalsIgnoreCase("WIFI") && n.isConnected()) {
                ip = getWifiIP(context);
                netType = "WI-FI";
                break;
            }
            else if (n.getTypeName().equalsIgnoreCase("MOBILE") && n.isConnected()) {
                ip = getDeviceIpMobileData();
                netType = "MOBILE";
                break;
            }
        }

        if (!ip.equals("")) {
            Map<String,String> ipData = new HashMap<String,String>();
            ipData.put("Network Type", netType);
            ipData.put("IP address", ip);
            dbRef.child(getDate()).setValue(ipData).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "onSuccess: ");
                }
            });
        }

        Log.d(TAG, "doInBackground: " + ip);
    }

    public static void setUserData(SharedPreferences sharedPreferences) {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        GlobalData.userData = new UserDetails();

        GlobalData.userData.setId(user.getUid());
        firebaseFirestore.collection("users")
                .document(user.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        GlobalData.userData.setName((String) documentSnapshot.get("name"));
                        GlobalData.userData.setMobile(documentSnapshot.get("mobile").toString().substring(3));
                        GlobalData.userData.setEmail_id((String) documentSnapshot.get("email_id"));
                        GlobalData.userData.setGender((String) documentSnapshot.get("gender"));
                        GlobalData.userData.setDob((String) documentSnapshot.get("dob"));
                        GlobalData.userData.setPhoto_url(Uri.parse((String) documentSnapshot.get("photo_url")));

                    }
                });
    }
}