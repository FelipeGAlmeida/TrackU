package com.fgapps.tracku.activity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.DrawableRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fgapps.tracku.R;
import com.fgapps.tracku.database.RealtimeDatabase;
import com.fgapps.tracku.database.StorageDatabase;
import com.fgapps.tracku.helper.Constants;
import com.fgapps.tracku.helper.Dialogs;
import com.fgapps.tracku.helper.Utils;
import com.fgapps.tracku.listener.MapListener;
import com.fgapps.tracku.service.SaveLoadService;
import com.fgapps.tracku.service.SyncDatabases;
import com.fgapps.tracku.sqlite.SQLDatabase;
import com.fgapps.tracku.sqlite.SQLDefs;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.os.SystemClock.sleep;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static GoogleMap mMap;
    private static LatLng position;

    private static String phone;
    private static String name;
    private static String location;
    private static int mode = 0;
    private static int zoom = 17;

    private FloatingActionButton btnMode;
    private FloatingActionButton btnType;
    private FloatingActionButton btnZoom;
    private Button btnVoltar;
    private TextView txtEndereco;
    private TextView txtEndereco2;
    private SeekBar skZoom;

    private static List<Address> addresses;
    private static ArrayList<Marker> markers;

    private static View mCustomMarkerView;
    private RealtimeDatabase rtdb;
    private MapListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        listener = new MapListener(this);
        markers = new ArrayList<>();

        btnVoltar = findViewById(R.id.voltarMap_id);
        btnVoltar.setOnClickListener(listener);

        btnMode = findViewById(R.id.btnMode_id);
        btnMode.setOnClickListener(listener);

        btnType = findViewById(R.id.btnType_id);
        btnType.setOnClickListener(listener);

        btnZoom = findViewById(R.id.btnZoom_id);
        btnZoom.setOnClickListener(listener);

        txtEndereco = findViewById(R.id.endereco_id);
        txtEndereco.setText("Aguardando localização...");

        txtEndereco2 = findViewById(R.id.endereco2_id);
        txtEndereco2.setText("");

        skZoom = findViewById(R.id.skZoom_id);
        skZoom.setOnSeekBarChangeListener(listener);

        Bundle b = getIntent().getExtras();
        if(b!=null){
            phone = b.getString(Constants.PHONE);
            location = getContactLocationFromDB(phone);
            name = b.getString(Constants.NAME);
            String[] l = getSeparatedLocation();
            if(l != null) {
                position = new LatLng(Double.parseDouble(l[0]), Double.parseDouble(l[1]));
                String a[] = getAdress4Loc(position).split(" - ");
                if(a.length>1) {
                    txtEndereco.setText(a[0]);
                    txtEndereco2.setText(a[1]);
                }
            }
        }
        rtdb = new RealtimeDatabase();
        addresses = null;
    }

    public String getContactLocationFromDB(String phone) {
        SQLDatabase db_initializer = new SQLDatabase(this);
        SQLiteDatabase db = db_initializer.getReadableDatabase();
        String[] fields = {
                SQLDefs.Contact_Table.COLUMN_LOCATION,
        };
        Cursor c = SQLDefs.select(db,Constants.CONTACT,fields, Constants.PHONE, new String[]{phone}, null);
        c.moveToFirst();
        if(c.getCount()>0) {
            return c.getString(c.getColumnIndexOrThrow(SQLDefs.Contact_Table.COLUMN_LOCATION));
        }
        c.close();
        return null;
    }

    public static void updateMap(String lt, String lg, boolean isUser){
        position = new LatLng(Double.parseDouble(lt),Double.parseDouble(lg));
        if(mMap != null) {
            if(!isUser) {
                if (markers.size() > 0) {
                    Marker m = markers.remove(0);
                    m.remove();
                }

                mCustomMarkerView = ((LayoutInflater) MainActivity.currentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.custom_marker, null);


                markers.add(mMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title(name)
                        .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomMarkerView)))));
                location = position.latitude+","+position.longitude;
                if(mode == 0) mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position,zoom));
            }else{
                if(mode == 1) mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position,zoom));
            }
        }
    }

    private static Bitmap getMarkerBitmapFromView(View view) {

        CircleImageView photoView = view.findViewById(R.id.photo_id);
        StorageDatabase sd = new StorageDatabase();
        Bitmap photo = sd.loadPhoto(phone);
        if(photo!=null) photoView.setImageBitmap(photo);
        else  photoView.setImageResource(R.drawable.no_photo);

        Float density = MainActivity.currentActivity.getResources().getDisplayMetrics().density;
        int w = (int)(density * 33.3333); //Normalização de tamanho do ícone
        int h = (int)(density * 47.0588);
        //int r = (w - 2);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        view.layout(0, 0, w, h);
        photoView.layout(0,0,w,w);

        view.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(w, h,Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = view.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        view.draw(canvas);
        return returnedBitmap;
    }

    public static String getAdress4Loc(LatLng loc){
        if(loc != null) {
            if(MainActivity.currentActivity != null) {
                Geocoder geocoder = new Geocoder(MainActivity.currentActivity, Locale.getDefault());
                try {
                    addresses = geocoder.getFromLocation(
                            loc.latitude,
                            loc.longitude,
                            // In this sample, get just a single addresses.
                            1);
                } catch (IOException ioException) {
                    // Catch network or other I/O problems.
                } catch (IllegalArgumentException illegalArgumentException) {
                    // Catch invalid latitude or longitude values.
                }
                if (addresses == null || addresses.size() == 0) {
                } else {
                    Address address = addresses.get(0);

                    String f_address = "";
                    if (address.getThoroughfare() != null) {
                        f_address += address.getThoroughfare(); //Rua
                    }
                    if (address.getSubThoroughfare() != null) {
                        f_address += ", ";
                        f_address += address.getSubThoroughfare(); //N° Rua
                    }
                    if (address.getLocality() != null) {
                        if (address.getSubLocality() != null) {
                            if (!(address.getSubLocality().toLowerCase().equals(Utils.unAcent(address.getLocality().toLowerCase())))) {
                                f_address += ", ";
                                f_address += address.getSubLocality(); //Bairro
                            }
                        }
                        f_address += " - ";
                        f_address += address.getLocality(); //Cidade
                    }
                    if (address.getAdminArea() != null) {
                        f_address += ", ";
                        f_address += address.getAdminArea(); //Estado
                    }
                    if (address.getCountryCode() != null) {
                        f_address += ", ";
                        f_address += address.getCountryCode(); //Sigla do País
                    }
                    return f_address;
                }
            }
            return Double.toString(loc.latitude) + "," + Double.toString(loc.longitude);
        }
        return "";
    }

    //THREAD

    Runnable activityControl = new Runnable() {
        @Override
        public void run() {
            while (MainActivity.currentActivity instanceof MapsActivity) {
                sleep(3000);
                final String[] a = getAdress4Loc(position).split(" - ");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(a.length>1) {
                            txtEndereco.setText(a[0]);
                            txtEndereco2.setText(a[1]);
                        }
                    }
                });
            }
        }
    };

    //OVERRIDE

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(position != null)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, zoom));
        LoginActivity.checkGPSPermission(this);
        mMap.setMyLocationEnabled(true);
        SaveLoadService sls = new SaveLoadService(this);
        int type = sls.getConfigMap();
        if(type == 1) btnType.setImageResource(R.drawable.satellite_icon);
        else btnType.setImageResource(R.drawable.normal_icon);
        mMap.setMapType(type);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        Thread t = new Thread(activityControl);
        t.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(SyncDatabases.isOnline())
            rtdb.stopGettingLocation(phone);
        mode = 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Dialogs.dismissLoadingDialog(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainActivity.currentActivity = this;
        if(SyncDatabases.isOnline())
            rtdb.checkAllowed(phone);
        else{
            Toast.makeText(this, "Você precisa de internet para rastrear um contato", Toast.LENGTH_LONG).show();
        }
        mode = 0;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(SyncDatabases.isOnline())
            rtdb.stopGettingLocation(phone);
        mode = 0;
    }

    //GETTERS AND SETTERS

    public static void showAskAlertDialog(){
        Dialogs.showAskLocationDialog(phone);
    }

    public static void showEndAlertDialog(){
        Dialogs.showEndLocationDialog(phone);
    }

    public static String getPhone() {
        return phone;
    }

    public static String getName() {
        return name;
    }

    public static int getMode() {
        return mode;
    }

    public static void setMode(int mode) {
        MapsActivity.mode = mode;
    }

    public static int getType() {
        return mMap.getMapType();
    }

    public static void setType(int type) {
        mMap.setMapType(type);
    }

    public void setZoom(int value) {
        mMap.animateCamera(CameraUpdateFactory.zoomTo(value));
        skZoom.setProgress(value-11);
        zoom = value;
    }

    public FloatingActionButton getBtnMode() {
        return btnMode;
    }

    public FloatingActionButton getBtnType() {
        return btnType;
    }

    public static String[] getSeparatedLocation(){
        if(location!=null) {
            String[] locAsStr = location.split(",");
            if (locAsStr.length > 1) {
                return locAsStr;
            }
        }
        return null;
    }
}
