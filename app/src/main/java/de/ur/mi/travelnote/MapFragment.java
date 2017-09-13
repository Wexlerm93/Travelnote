package de.ur.mi.travelnote;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.io.IOException;
import java.util.List;
import de.ur.mi.travelnote.de.ur.mi.travelnote.sqlite.helper.DatabaseHelper;


public class MapFragment extends Fragment implements OnMapReadyCallback {
    private OnFragmentInteractionListener mListener;
    final double LAT_EU = 53.0000;
    final double LNG_EU = 9.0000;
    final int DEFAULT_ZOOM = 3;
    final int ORIGIN_MAP = 1;
    private boolean active;
    String userID;
    String userName;
    EditText editText;
    GoogleMap mGoogleMap;
    MapView mMapView;
    View mView;
    private LocationManager locationManager;
    private LocationListener locationListener;
    DatabaseHelper mDatabaseHelper;


    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mDatabaseHelper = new DatabaseHelper(getActivity());
        getUserInfo();

        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_map, container, false);
        setHasOptionsMenu(true);
        markNewLocation();

        BottomNavigationView bottomNavView = (BottomNavigationView) getActivity().findViewById(R.id.navigation);
        bottomNavView.getMenu().findItem(R.id.navigation_map).setChecked(true);
        return mView;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initMap();
    }


    // Initialize Google Map, if Google Services are available
    private void initMap() {
        if (googleServicesAvailable()) {
            mMapView = (MapView) mView.findViewById(R.id.myMap);
            if (mMapView != null) {
                mMapView.onCreate(null);
                mMapView.onResume();
                mMapView.getMapAsync(this);
            }
        } else {
            displayShortToast(R.string.map_display_failed);
        }

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());
        mGoogleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        goToLocationZoom(LAT_EU, LNG_EU, DEFAULT_ZOOM);
        displayStoredMapMarker();

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuInflater menuInflater = new MenuInflater(getContext());
        menuInflater.inflate(R.menu.action_buttons_map_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_get_gps_location:
                getCurrentLocation();
                return true;
            case R.id.action_delete_coordinates:
                deleteDBCoordinatesDialog();
                return true;
            case R.id.action_show_marker_all_user:
                displayStoredMapMarkerAllUser();
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void displayStoredMapMarker() {
        Cursor data = mDatabaseHelper.getMapCoordinates(userID);
        while (data.moveToNext()) {
            newMapMarker(data.getDouble(1), data.getDouble(2));
        }
    }

    private void displayStoredMapMarkerAllUser() {
        Cursor data = mDatabaseHelper.getMapCoordinatesAllUser(userID);
        if (data == null || data.getCount() < 1) {
            Toast.makeText(getContext(), R.string.no_entries_different_users, Toast.LENGTH_SHORT).show();
        } else {
            try {
                while (data.moveToNext()) {
                    newMapMarkerDiffUser(data.getDouble(1), data.getDouble(2), data.getString(4));
                }
            } finally {
                data.close();
            }
        }
    }

    private void markNewLocation() {
        Button getGeoLocal = (Button) mView.findViewById(R.id.map_get_geo_local);
        getGeoLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setGeoLocaleFromText(view);
            }
        });
    }


    private void setGeoLocaleFromText(View view) {
        editText = (EditText) getActivity().findViewById(R.id.map_geo_location);
        String location = editText.getText().toString();
        Geocoder geocoder = new Geocoder(getActivity());

        if (!location.equals("")) {
            try {
                List<Address> list = geocoder.getFromLocationName(location, 1);
                Address address = list.get(0);
                double lat = address.getLatitude();
                double lng = address.getLongitude();
                newMapMarker(lat, lng);
                addCoordinatesToDB(lat, lng, userID, userName);
                goToLocationZoom(lat, lng, DEFAULT_ZOOM);
            } catch (IOException e) {
                e.printStackTrace();
                displayShortToast(R.string.unexpected_failure);
            }
            editText.setText("");
        } else {
            displayShortToast(R.string.location_missing);
        }


    }


    private void addCoordinatesToDB(double latitude, double longitude, String ID, String name) {
        if (!userID.equals("")) {
            boolean insertData = mDatabaseHelper.addCoordinates(latitude, longitude, ID, name, ORIGIN_MAP);
            if (!insertData) {
                displayShortToast(R.string.entry_failed);
            }
        } else {
            displayShortToast(R.string.entry_success);
        }

    }

    private void displayShortToast(int s) {
        Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
    }


    private void newMapMarker(double latitude, double longitude) {
        LatLng latlng = new LatLng(latitude, longitude);
        MarkerOptions options = new MarkerOptions().position(latlng);
        mGoogleMap.addMarker(options);
    }

    private void newMapMarkerDiffUser(double latitude, double longitude, String name) {
        LatLng latlng = new LatLng(latitude, longitude);
        MarkerOptions options = new MarkerOptions().title(name).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).position(latlng);
        mGoogleMap.addMarker(options);
    }


    private void goToLocationZoom(double lat, double lng, float zoom) {
        LatLng latLng = new LatLng(lat, lng);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mGoogleMap.moveCamera(cameraUpdate);
    }




    /*
        Method to check wether Google Services are available or not.. Google Services are needed to access Google APIs
     */
    private boolean googleServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int isAvailable = apiAvailability.isGooglePlayServicesAvailable(getContext());
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (apiAvailability.isUserResolvableError(isAvailable)) {
            Dialog dialog = apiAvailability.getErrorDialog(getActivity(), isAvailable, 0);
            dialog.show();
        } else {
            displayShortToast(R.string.conn_play_services_failed);
        }
        return false;
    }


    private void getCurrentLocation() {
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double lang = location.getLatitude();
                double lng = location.getLongitude();
                newMapMarker(lang, lng);
                addCoordinatesToDB(lang, lng, userID, userName);
                if(active){
                    displayShortToast(R.string.current_location_marked_success);
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                //check first if Fragment is active, to avoid crashes
                if (active){
                    enableLocationProviderDialog();
                }

            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                }, 10);
                return;
            }
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 6000, 200, locationListener);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 6000, 200, locationListener);
                    } catch (SecurityException e) {
                        displayShortToast(R.string.failed_location_permission);
                    }
                }
        }
    }

    private void enableLocationProviderDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("Standortfreigabe");
        alertDialog.setMessage("Du musst Deinen Stanort freigeben, damit Travelnote Deinen Standort markieren kann.");
        alertDialog.setIcon(R.drawable.ic_warning_black_24dp);

        //if user still clicks yes, then delete db entries
        alertDialog.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        //if user cancels, do nothing
        alertDialog.setNegativeButton("Nein", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing here.
            }
        });
        alertDialog.show();
    }


    private void deleteDBCoordinatesDialog() {
        //if there are db entries build alert dialog to avoid deletion by accident
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.delete_db_map_entries_warning_title);
        alertDialog.setMessage(R.string.delete_db_map_entries_warning_long);
        alertDialog.setIcon(R.drawable.ic_warning_black_24dp);

        //if user still clicks yes, then delete db entries
        alertDialog.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                deleteCoordinateEntries();
            }
        });

        //if user cancels, do nothing
        alertDialog.setNegativeButton("Nein", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing here.

            }
        });
        alertDialog.show();
    }

    /*
        Method to clear all database entries from current user and call fragment again to update UI
     */
    private void deleteCoordinateEntries() {
        mDatabaseHelper.clearTableMapCoordinatesCurrentUser(userID);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content, new MapFragment()).commit();
        displayShortToast(R.string.diary_deleted_toast);
    }

    //Sets user info provided through Firebase, according to currently logged-in user
    private void getUserInfo(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userID = user.getUid();
            userName = user.getDisplayName();
        }
    }




    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        active = true;
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        active = false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    //Required Interface
    public interface OnFragmentInteractionListener {
    }
}
