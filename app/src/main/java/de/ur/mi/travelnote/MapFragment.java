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
import android.os.AsyncTask;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
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
    private String userID;
    private String userName;
    private GoogleMap mGoogleMap;
    private MapView mMapView;
    private View mView;
    private EditText editText;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private DatabaseHelper mDatabaseHelper;


    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

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

        ImageView imageView = (ImageView) mView.findViewById(R.id.icon_locate_me);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCurrentLocationFromButtonDialog();
            }
        });

        BottomNavigationView bottomNavView = (BottomNavigationView) getActivity().findViewById(R.id.navigation);
        bottomNavView.getMenu().findItem(R.id.navigation_map).setChecked(true);
        return mView;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initMap();
    }


    // Setup Google Map View, if Google Services are available
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



    /*
        Setup Google Map in onMapReady method
        Set Map type, if current one is not the normal map type
        Display all stored locations, using a Async Task
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());
        mGoogleMap = googleMap;
        if (googleMap.getMapType() != GoogleMap.MAP_TYPE_NORMAL) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
        goToLocationZoom(LAT_EU, LNG_EU, DEFAULT_ZOOM);
        new MarkerAsyncTask().execute();
    }

    //Setups toolbar's options menu and inflates the layout
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuInflater menuInflater = new MenuInflater(getContext());
        menuInflater.inflate(R.menu.action_buttons_map_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //Override method for what to do, if item from toolbar's options menu is clicked
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
                new MarkerAsyncTaskAllUsers().execute();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // OnClickListener for UI Button to trigger insert of a new Location
    private void markNewLocation() {
        Button getGeoLocal = (Button) mView.findViewById(R.id.map_get_geo_local);
        getGeoLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setGeoLocaleFromText(view);
            }
        });
    }


    /* Method to determine Location from inputted text
       if there can be a location determined, add also a new map marker and insert location into database
     */
    private void setGeoLocaleFromText(View view) {
        editText = (EditText) getActivity().findViewById(R.id.map_geo_location);
        String location = editText.getText().toString();
        Geocoder geocoder = new Geocoder(getActivity());

        if (!location.equals("")) {
            try {
                List<Address> list = geocoder.getFromLocationName(location, 1);
                if (list.size() > 0) {
                    Address address = list.get(0);
                    double lat = address.getLatitude();
                    double lng = address.getLongitude();
                    newMapMarker(lat, lng);
                    addCoordinatesToDB(lat, lng, userID, userName);
                    goToLocationZoom(lat, lng, DEFAULT_ZOOM);
                } else {
                    displayShortToast(R.string.location_determination_failed);
                }
            } catch (IOException e) {
                e.printStackTrace();
                displayShortToast(R.string.map_entry_failed);
            }
            editText.setText("");
        } else {
            displayShortToast(R.string.location_missing);
        }


    }


    //Method to insert location into database using the database helper class
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

    //Helper method to create a short length toast message
    private void displayShortToast(int s) {
        Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
    }


    //Method to add a map marker to Google Map
    private void newMapMarker(double latitude, double longitude) {
        LatLng latlng = new LatLng(latitude, longitude);
        MarkerOptions options = new MarkerOptions().position(latlng);
        mGoogleMap.addMarker(options);
    }

    /* Method to add a map marker to Google Map for a different user, than the currently logged-in one
        has different color and also a title (with the name of the other user)
     */
    private void newMapMarkerDiffUser(double latitude, double longitude, String name) {
        LatLng latlng = new LatLng(latitude, longitude);
        MarkerOptions options = new MarkerOptions().title(name).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).position(latlng);
        mGoogleMap.addMarker(options);
    }


    // Method to change zoom on Google Map by a given level and coordinates
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

    /*
        Method to get current Location from network location provider
     */

    private void getCurrentLocation() {
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                double lang = location.getLatitude();
                double lng = location.getLongitude();
                newMapMarker(lang, lng);
                addCoordinatesToDB(lang, lng, userID, userName);
                //Display Toast only when Fragment is visible to user, to avoid NullPointerException
                if (active) {
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
                if (active) {
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
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 9000, 200, locationListener);
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
        //build alert dialog, if location provider is disabled
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("Standortfreigabe");
        alertDialog.setMessage("Du musst Deinen Netzwerk-Standort freigeben, damit Travelnote Deinen Standort markieren kann.");
        alertDialog.setIcon(R.drawable.ic_warning_black_24dp);

        //if user wants to enable location provider, start intent to device settings..
        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        //if user cancels, do nothing
        alertDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
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
        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                deleteCoordinateEntries();
            }
        });

        //if user cancels, do nothing
        alertDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing here.

            }
        });
        alertDialog.show();
    }


    private void addCurrentLocationFromButtonDialog() {
        //
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("Standort markieren");
        alertDialog.setMessage("Markiere Deinen aktuellen Standort.");
        alertDialog.setIcon(R.drawable.ic_add_location_travelnote_24dp);

        //if user still clicks yes, then delete db entries
        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                getCurrentLocation();
            }
        });

        //if user cancels, do nothing
        alertDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
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
        displayShortToast(R.string.marker_deleted_toast);
    }

    //Sets user info provided through Firebase, according to currently logged-in user
    private void getUserInfo() {
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


    private class MarkerAsyncTask extends android.os.AsyncTask<Void, Void, Void> {
        Cursor mapCoordinatesCursor;

        @Override
        protected Void doInBackground(Void... voids) {
            mapCoordinatesCursor = mDatabaseHelper.getMapCoordinates(userID);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            while (mapCoordinatesCursor.moveToNext()) {
                newMapMarker(mapCoordinatesCursor.getDouble(1), mapCoordinatesCursor.getDouble(2));
            }
        }
    }

    private class MarkerAsyncTaskAllUsers extends AsyncTask<Void, Void, Void> {
        Cursor mapCoordinatesAllUserCursor;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mapCoordinatesAllUserCursor = mDatabaseHelper.getMapCoordinatesAllUser(userID);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (mapCoordinatesAllUserCursor == null || mapCoordinatesAllUserCursor.getCount() < 1) {
                Toast.makeText(getContext(), R.string.no_entries_different_users, Toast.LENGTH_SHORT).show();
            } else {
                try {
                    while (mapCoordinatesAllUserCursor.moveToNext()) {
                        newMapMarkerDiffUser(mapCoordinatesAllUserCursor.getDouble(1), mapCoordinatesAllUserCursor.getDouble(2), mapCoordinatesAllUserCursor.getString(4));
                    }
                } finally {
                    //close cursor, when job is done
                    mapCoordinatesAllUserCursor.close();
                }
            }
        }
    }


}
