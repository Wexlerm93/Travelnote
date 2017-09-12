package de.ur.mi.travelnote;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.List;


public class MapFragment extends Fragment implements OnMapReadyCallback{
    GoogleMap mGoogleMap;
    MapView mMapView;
    View mView;
    EditText editText;
    String userName;


    DatabaseHelper mDatabaseHelper;


    private OnFragmentInteractionListener mListener;
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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null){
            userName = user.getUid();
        }

        // Inflate the layout for this fragment
        mView  = inflater.inflate(R.layout.fragment_map, container, false);
        setHasOptionsMenu(false);
        markNewLocation();

        BottomNavigationView bottomNavView = (BottomNavigationView) getActivity().findViewById(R.id.navigation);
        bottomNavView.getMenu().findItem(R.id.navigation_map).setChecked(true);
        //bottomNavView.setSelectedItemId(R.id.navigation_map);


        return mView;
    }




    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initMap();
    }
    


    // Initialize Google Map, if Google Services are available
    private void initMap() {
        if(googleServicesAvailable()){
            mMapView = (MapView) mView.findViewById(R.id.myMap);
            if(mMapView != null){
                mMapView.onCreate(null);
                mMapView.onResume();
                mMapView.getMapAsync(this);
            }
        }else {
            Toast.makeText(getContext(), "Karte kann nicht angezeigt werden.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());
        mGoogleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        displayStoredMapMarker();

    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    private void displayStoredMapMarker() {
        Cursor data = mDatabaseHelper.getData(userName);
        while (data.moveToNext()){
            newMapMarker(data.getDouble(1),data.getDouble(2));
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

        if(!location.equals("")){
            try {
                List<Address> list = geocoder.getFromLocationName(location, 1);
                Address address = list.get(0);
                double lat = address.getLatitude();
                double lng = address.getLongitude();
                newMapMarker(lat, lng);
                addCoordinatesToDB(lat,lng, userName);
                goToLocationZoom(lat, lng,6);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Entschuldigung. Ein unerwarteter Fehler ist aufgetreten.", Toast.LENGTH_SHORT).show();
            }
            editText.setText("");
        }else{
            Toast.makeText(getContext(), "Bitte geben Sie einen Ort ein!", Toast.LENGTH_SHORT).show();
        }



    }



    private void addCoordinatesToDB(double latitude, double longitude, String userID ){
        if(!userID.equals("")){
            boolean insertData = mDatabaseHelper.addCoordinates(latitude, longitude, userID );
            if(insertData){
                Toast.makeText(getContext(), "Eintrag erfolgreich", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getContext(), "Eintrag konnte nicht dauerhaft gespeichert werden.", Toast.LENGTH_SHORT).show();
        }

    }


    private void newMapMarker(double latitude, double longitude){
        LatLng latlng = new LatLng(latitude,longitude);
        MarkerOptions options = new MarkerOptions().position(latlng);
        mGoogleMap.addMarker(options);
    }

    private void newMapMarkerWithTitle(String title, double latitude, double longitude){
        MarkerOptions options = new MarkerOptions().title(title).position(new LatLng(latitude,longitude));
        mGoogleMap.addMarker(options);
    }




    private void goToLocationZoom(double lat, double lng, float zoom){
        LatLng latLng = new LatLng(lat, lng);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mGoogleMap.moveCamera(cameraUpdate);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /*
        Method to check wether Google Services are available or not.. Google Services are needed to access Google APIs
     */
    private boolean googleServicesAvailable(){
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int isAvailable = apiAvailability.isGooglePlayServicesAvailable(getContext());
        if(isAvailable == ConnectionResult.SUCCESS){
            return true;
        }else if(apiAvailability.isUserResolvableError(isAvailable)){
            Dialog dialog = apiAvailability.getErrorDialog(getActivity(), isAvailable,0);
            dialog.show();
        }else {
            Toast.makeText(getContext(), "Verbindung zu Google Play Services nicht möglich.", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

}
