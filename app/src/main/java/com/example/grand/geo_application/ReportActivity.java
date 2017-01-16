package com.example.grand.geo_application;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;


import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.net.Uri;

import android.widget.ImageView;


public class ReportActivity extends AppCompatActivity {
    Button submitButton;
    EditText beskrivning;
    String server_url = "http://10.0.2.2:80/gavle/update_info.php";
    Location loc;

    //konstant för att tale om ifall vi ska ta en bild
    private static final int TAKE_PICTURE = 0;
    //Vår imageView
    private ImageView imageView;
    //för att bestämma fil
    private Uri outputFileUri;

    //metod för att lägga ut en bild i vår imageView
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data){
        if(requestCode == TAKE_PICTURE){
            if(data !=null){
                if(data.hasExtra("data")){
                    //skapa en bitmap bild för vår imageView
                    Bitmap thumbnail = data.getParcelableExtra("data");
                    imageView.setImageBitmap(thumbnail);
                }else{
                    //fixa till så att bildens bredd och höjd blir samma som imageView
                    int width = imageView.getWidth();
                    int height = imageView.getHeight();
                    BitmapFactory.Options factoryOptions = new BitmapFactory.Options();
                    factoryOptions.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(outputFileUri.getPath(), factoryOptions);
                    int imageWidth = factoryOptions.outWidth;
                    int imageHeight = factoryOptions.outHeight;
                    int scaleFactor = Math.min(imageWidth/width, imageHeight/height);
                    factoryOptions.inJustDecodeBounds = false;
                    factoryOptions.inSampleSize = scaleFactor;
                    Bitmap bitmap = BitmapFactory.decodeFile(outputFileUri.getPath(),
                            factoryOptions);
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        //Behöver en locationmanager för att hitta vår
        //GPS provider m.m.
        LocationManager locationManager;
        //Hämta enhetens gps
        String svcName = Context.LOCATION_SERVICE;
        //Hämta provider
        locationManager = (LocationManager) getSystemService(svcName);
        String provider = LocationManager.GPS_PROVIDER;
        //här kommer felhantering
        //sätt upp själva positions lyssnaren
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

            //För att appen ska fråga telefonen om tillåtelse
            // att använda GPS:en när den installeras
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.INTERNET}
                        , 10
                );
            }
            return;
        }
        locationManager.requestLocationUpdates(provider, 0, 0, new
                LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle
                            extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }
                });
        //Hämta senste longitud och latitud

        loc = locationManager.getLastKnownLocation(provider);
        //metod som visar vår nya position
        updateWithNewLocation(loc);

        submitButton = (Button) findViewById(R.id.btnSubmit);
        beskrivning = (EditText) findViewById(R.id.anteckningar);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringRequest stringRequest = new StringRequest(Request.Method.POST, server_url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String Response) {


                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ReportActivity.this, "Error..", Toast.LENGTH_SHORT).show();
                    }
                }
                ) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        //OM vi fått någon GPS POS
                        if (loc != null) {
                            //FÖr att få åtta decimaler
                            DecimalFormat df = new DecimalFormat("#.########");
                            params.put("longitud", df.format(loc.getLongitude()));
                            params.put("latitud", df.format(loc.getLatitude()));
                        } else {
                            params.put("longitud", "0");
                            params.put("latitud", "0");
                        }
                        params.put("beskrivning", beskrivning.getText().toString());
                        return params;
                    }
                };

                MySingleton.getInstance(ReportActivity.this).addToRequestQueue(stringRequest);

                startActivity(new Intent(ReportActivity.this, SentActivity.class));
                finish();

            }

            ;
        });
        //hämta vår imageview från layoutfilen
        imageView = (ImageView) findViewById(R.id.picuteview);
        //hämta knappen fullPhoto och sätt en klicklyssnare på den
        Button fullPhotoButton = (Button) findViewById(R.id.buttonPicture);
        fullPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Hämta sökvägen för filer på den enhet som använder appen och skapa en fil i den mappen
                File file = new File(Environment.getExternalStorageDirectory(),
                        "textbild.jpg");
                //här bestäms själva filen
                Uri outputFileuri = Uri.fromFile(file);
                //skapa en intention atat ta en bild med enheten
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileuri);
                startActivityForResult(intent, TAKE_PICTURE);
            }
        });
    }

    //metoden updateWithNewLocation för att skriva ut vår position
    private void updateWithNewLocation(Location loc) {
        //Hämta TextView från layouten
        TextView myLocationText;
        myLocationText = (TextView) findViewById(R.id.myLocationText);
        //Skapa texten till vår TextView
        String latlongText = "No location found";
        //om vi fått någon position ska detta skrivas
        if (loc != null) {
            //hämta latitud och longitud
            double lat = loc.getLatitude();
            double lng = loc.getLongitude();
            latlongText = "Latitud: " + lat  + " Longitud: " + lng;
        }//här slutar if-satsen
        //sätt texten för TextView i layouten
        myLocationText.setText(" \n" + latlongText);
    }




    public void exit(View view) {

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish(); // Call once you redirect to another activities

    }
}

/*
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;


public class ReportActivity extends AppCompatActivity {

    Button sendButton;
    EditText anteckningar;
    Button submitButton;
    String server_url = "http//10.0.2.2:80/gavle/update_info.php";
    Location loc;


    //konstant för att tale om ifall vi ska ta en bild
    private static final int TAKE_PICTURE = 0;
    //Vår imageView
    private ImageView imageView;
    //för att bestämma fil
    private Uri outputFileUri;

    //metod för att lägga ut en bild i vår imageView
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data){
        if(requestCode == TAKE_PICTURE){
            if(data !=null){
                if(data.hasExtra("data")){
                    //skapa en bitmap bild för vår imageView
                    Bitmap thumbnail = data.getParcelableExtra("data");
                    imageView.setImageBitmap(thumbnail);
                }else{
                    //fixa till så att bildens bredd och höjd blir samma som imageView
                    int width = imageView.getWidth();
                    int height = imageView.getHeight();
                    BitmapFactory.Options factoryOptions = new BitmapFactory.Options();
                    factoryOptions.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(outputFileUri.getPath(), factoryOptions);
                    int imageWidth = factoryOptions.outWidth;
                    int imageHeight = factoryOptions.outHeight;
                    int scaleFactor = Math.min(imageWidth/width, imageHeight/height);
                    factoryOptions.inJustDecodeBounds = false;
                    factoryOptions.inSampleSize = scaleFactor;
                    Bitmap bitmap = BitmapFactory.decodeFile(outputFileUri.getPath(),
                            factoryOptions);
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_report);

        sendButton = (Button) findViewById(R.id.next);
        anteckningar = (EditText) findViewById(R.id.anteckningar);
        submitButton = (Button) findViewById(R.id.btnSubmit);


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringRequest stringRequest = new StringRequest(Request.Method.POST, server_url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String Response) {


                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ReportActivity.this, "Error..", Toast.LENGTH_SHORT).show();
                    }
                }
                ) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        //OM vi fått någon GPS POS
                        if (loc != null) {
                            //FÖr att få åtta decimaler
                            DecimalFormat df = new DecimalFormat("#.########");
                            params.put("longitud", df.format(loc.getLongitude()));
                            params.put("latitud", df.format(loc.getLatitude()));
                        } else {
                            params.put("longitud", "0");
                            params.put("latitud", "0");
                        }
                        params.put("beskrivning", anteckningar.getText().toString());
                        return params;
                    }
                };

                MySingleton.getInstance(ReportActivity.this).addToRequestQueue(stringRequest);

            }

            ;
        });


    //metoden updateWithNewLocation för att skriva ut vår position
    private void updateWithNewLocation(Location loc){
        //Hämta TextView från layouten
        TextView myLocationText;
        myLocationText = (TextView)findViewById(R.id.myLocationText);
        //Skapa texten till vår TextView
        String latlongText = "No location found";
        //om vi fått någon position ska detta skrivas
        if(loc != null){
            //hämta latitud och longitud
            double lat = loc.getLatitude();
            double lng = loc.getLongitude();
            latlongText = "Lat: "+ lat + " Long: "+ lng;
        }//här slutar if-satsen
        //sätt texten för TextView i layouten
        myLocationText.setText("Your current position is: \n"+ latlongText);
    }





        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView problem = (TextView) findViewById(R.id.problem);

        //hämta vår imageview från layoutfilen
        imageView = (ImageView) findViewById(R.id.picuteview);
        //hämta knappen fullPhoto och sätt en klicklyssnare på den
        Button fullPhotoButton = (Button) findViewById(R.id.buttonPicture);
        fullPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Hämta sökvägen för filer på den enhet som använder appen och skapa en fil i den mappen
                File file = new File(Environment.getExternalStorageDirectory(),
                        "textbild.jpg");
                //här bestäms själva filen
                Uri outputFileuri = Uri.fromFile(file);
                //skapa en intention atat ta en bild med enheten
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileuri);
                startActivityForResult(intent, TAKE_PICTURE);
            }
        });


        //Behöver en location_manager för att hitta vår GPS provider m.m.
        LocationManager locationManager;
        //Hämta enhetens gps
        String svcName = Context.LOCATION_SERVICE;
        //Hämta provider
        locationManager = (LocationManager) getSystemService(svcName);
        String provider = LocationManager.GPS_PROVIDER;
        //här kommer felhantering
        //sätt ipp sjävla positionslyssnaren
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            //För att appen ska fråga efter tillåtelse att använda GPS:en när den installeras
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.INTERNET}
                        , 10
                );
            }
            return;
        }
        locationManager.requestLocationUpdates(provider, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        });

        //Hämta senaste longitud och latitud
        loc = locationManager.getLastKnownLocation(provider);
        //metod som visar vår nya position
        updateWithNewLocation(loc);



   //problem.setText(getString(R.string.choice));

    } //här slutar onCreate metoden

    private void updateWithNewLocation (Location loc){
        //Hämta TextView från layouten
        TextView my_location;
        my_location = (TextView)findViewById(R.id.myLocationText);
        //skapa texten till TextView
        String latlongText = "No location found";
        //om vi fått någon position ska detta skrivas
        if (loc!=null){
            //hämta latitud och longitud
            double lat = loc.getLatitude();
            double lng = loc.getLongitude();
            latlongText = "Lat: " + lat + "  Long: "+ lng;
        }// här slutar if-satsen
        //Sätt texten för TextView i layouten
        my_location.setText(latlongText);
    }


            public void exit(View view) {

                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish(); // Call once you redirect to another activities

            }

  //  public void next(View view) {

    //    Intent intent = new Intent(this, SentActivity.class);
      //  intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //startActivity(intent);
       // finish(); // Call once you redirect to another activity

    //}
}
*/

