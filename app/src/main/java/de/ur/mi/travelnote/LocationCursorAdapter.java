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
    private double lat, lng;

    public LocationCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        this.context = context;
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        lat = cursor.getDouble(1);
        lng = cursor.getDouble(2);
        getAddress(lat, lng);


        TextView localityName = (TextView) view.findViewById(R.id.listitem_locality_name);
        TextView countryName = (TextView) view.findViewById(R.id.listitem_country_name);
        TextView addressName = (TextView) view.findViewById(R.id.listitem_address_line);


        if (getAddress(lat, lng)[0].equals("") || getAddress(lat, lng).equals("")) {
            countryName.setText(R.string.unkown_address);
            addressName.setText("Koordinaten: " + lat + "," + lng);
        } else {
            localityName.setText(getAddress(lat, lng)[0]);
            countryName.setText(getAddress(lat, lng)[1]);
            if (!getAddress(lat, lng)[2].equals("")) {
                addressName.setText(getAddress(lat, lng)[2]);
            } else {
                addressName.setText(R.string.unkown_address);
            }
        }

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        return layoutInflater.inflate(R.layout.listview_location_entries, parent, false);
    }


    private String[] getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String[] result = new String[3];

        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);

            if (addresses.size() > 0) {
                Address obj = addresses.get(0);

                if (obj.getLocality() != null) {
                    result[0] = obj.getLocality();
                } else {
                    result[0] = "";
                }
                if (obj.getCountryName() != null) {
                    result[1] = obj.getCountryName();
                } else {
                    result[1] = "";
                }
                if (obj.getAddressLine(0) != null) {
                    result[2] = obj.getAddressLine(0);
                } else {
                    result[2] = "";
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}
