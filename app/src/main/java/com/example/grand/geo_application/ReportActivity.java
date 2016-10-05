package com.example.grand.geo_application;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ReportActivity extends AppCompatActivity {


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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView problem = (TextView) findViewById(R.id.problem);

        //hämta vår imageview från layoutfilen
        imageView = (ImageView)findViewById(R.id.picuteview);
        //hämta knappen fullPhoto och sätt en klicklyssnare på den
        Button fullPhotoButton = (Button)findViewById(R.id.buttonPicture);
        fullPhotoButton.setOnClickListener(new View.OnClickListener(){
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
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.INTERNET}
                        ,10
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
        Location loc = locationManager.getLastKnownLocation(provider);
        //Metod som visar vår nya position
        updateWithNewLocation(loc);


   problem.setText(getString(R.string.choice));

    } //här slutar onCreate metoden

    private void updateWithNewLocation (Location loc){
        //Hämta TextView från layouten
        TextView my_location;
        my_location = (TextView)findViewById(R.id.my_location);
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
                finish(); // Call once you redirect to another activity

            }

    public void next(View view) {

        Intent intent = new Intent(this, ReceiptActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish(); // Call once you redirect to another activity

    }



}