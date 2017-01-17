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

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;


import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.net.Uri;

import android.widget.ImageView;



public class ReportActivity extends AppCompatActivity {
    //Knappen för skicka-funktion
    Button submitButton;
    //textrutan användaren skriver sin beskrivning i
    EditText beskrivning;
    //lokal ip-adress som möjliggör användandet av virtuell telefon
    String server_url = "http://10.0.2.2:80/gavle/update_info.php";
    //varibal för nuvarande plats
    Location loc;

    //konstant för att tala om ifall vi ska ta en bild
    private static final int TAKE_PICTURE = 0;
    //Vår imageview
    private ImageView imageView;
    //för att bestämma fil
    private Uri outputFileUri;

    //här börjar Oncreate-metoden
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        //hämta vår imageview från layoutfilen
        imageView = (ImageView)findViewById(R.id.imageView);
        //hämta knappen fullPhoto och sätt en klicklyssnare på den.

        Button photoButton = (Button)findViewById(R.id.buttonPicture);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(MediaStore.ACTION_IMAGE_CAPTURE),TAKE_PICTURE);
            }
        });

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
        //knappen för att skicka till databasen
        submitButton = (Button) findViewById(R.id.btnSubmit);
        //textrutan användaren använder för att besrkiva problemet
        beskrivning = (EditText) findViewById(R.id.anteckningar);
        //ger skicka-knappen sin funktion via en OnClickListener
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringRequest stringRequest = new StringRequest(Request.Method.POST, server_url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String Response) {


                            }
                        }, new Response.ErrorListener() {
                    //Ifall något inte fungerar med databasen händer detta
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

    }//här slutar oncreate-metoden

    //metod för att lägga ut en bild i vår imageView
    protected  void onActivityResult(int requestCode,
                                     int resultCode, Intent data){
        if(requestCode == TAKE_PICTURE){
            if(data != null){
                if(data.hasExtra("data")){
                    //Skapa en bitmap bild för vår imageView
                    Bitmap thumbnail = data.getParcelableExtra("data");
                    imageView.setImageBitmap(thumbnail);
                }else{
                    //fixa till så att bildens bredd och höjd blir samma som vår imageView
                    int width = imageView.getWidth();
                    int height = imageView.getHeight();
                    BitmapFactory.Options factoryOptions = new BitmapFactory.Options();
                    factoryOptions.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(outputFileUri.getPath(), factoryOptions);
                    int imageWidth = factoryOptions.outWidth;
                    int imageHeigth  = factoryOptions.outHeight;
                    int scaleFactor = Math.min(imageWidth/width, imageHeigth/height);
                    factoryOptions.inJustDecodeBounds = false;
                    factoryOptions.inSampleSize = scaleFactor;
                    Bitmap bitmap = BitmapFactory.decodeFile(outputFileUri.getPath(),
                            factoryOptions);
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
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
            //vad som skrivs ut under "plats"
            latlongText = "Latitud: " + lat  + " Longitud: " + lng;
        }//här slutar if-satsen
        //sätt texten för TextView i layouten
        myLocationText.setText(" \n" + latlongText);
    }



//funktion för att ta användaren tillbaka till startsidan
    public void exit(View view) {

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish(); // Call once you redirect to another activities

    }
}

