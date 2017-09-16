package de.ur.mi.travelnote;


import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class LocationCursorAdapter extends CursorAdapter {

    Context context;
    private long ident;
    private double lat;
    private double lng;

    private String locationName;

    private String locality; // = "";
    private String country; // = "";
    private String addressLine; // = "";
    //private Geocoder geocoder;


    public LocationCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        this.context = context;
        //geocoder = new Geocoder(context, Locale.getDefault());
    }



    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int ident = (int) cursor.getLong(0);
        lat = cursor.getDouble(1);
        lng = cursor.getDouble(2);
        getAddress(lat,lng);


        TextView localityName = (TextView) view.findViewById(R.id.listitem_locality_name);
        TextView countryName = (TextView) view.findViewById(R.id.listitem_country_name);
        TextView addressName = (TextView) view.findViewById(R.id.listitem_address_line);


        localityName.setTag(ident);





        /*
        if(locality.equals("")||country.equals("")){
            countryName.setText("Unbekannte Adresse");
            addressName.setText("Koordinaten: " + lat + "," + lng);
        }else {
            localityName.setText(locality);
            countryName.setText(country);
            if(!addressLine.equals("")) {
                addressName.setText(addressLine);
            }else {
                addressName.setText("Unbekannte Adresse.");
            }
        }
        */

        if(getAddress(lat, lng)[0].equals("") || getAddress(lat,lng).equals("")){
            countryName.setText("Unbekannte Adresse");
            addressName.setText("Koordinaten: " + lat + "," + lng);
        }else{
            localityName.setText(getAddress(lat,lng)[0]);
            countryName.setText(getAddress(lat,lng)[1]);
            if(!getAddress(lat,lng)[2].equals("")) {
                addressName.setText(getAddress(lat,lng)[2]);
            }else {
                addressName.setText("Unbekannte Adresse.");
            }
        }


          //locality = "";
         // country = "";
          //addressLine = "";


    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        return layoutInflater.inflate(R.layout.listview_location_entries, parent, false);
        //return view;
    }

    @Override
    public long getItemId(int position) {
        return ident;
    }


    private String[] getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        String[] result = new String[3];

        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);

            if(addresses.size() > 0){
                Address obj = addresses.get(0);

                if(obj.getLocality() != null){
                    //locality = obj.getLocality();
                    result[0] = obj.getLocality();
                }else {
                    //locality = "";
                    result[0] = "";
                }
                if(obj.getCountryName() != null){
                    //country = obj.getCountryName();
                    result[1] = obj.getCountryName();
                }else {
                    //country = "";
                    result[1] = "";
                }
                if(obj.getAddressLine(0) != null){
                    //addressLine = obj.getAddressLine(0);
                    result[2] = obj.getAddressLine(0);
                }else {
                    //addressLine = "";
                    result[2] = "";
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}
