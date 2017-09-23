package de.ur.mi.travelnote;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class OverviewFragment extends Fragment {


    private OnFragmentInteractionListener mListener;

    public OverviewFragment() {
        // Required empty public constructor
    }


    public static OverviewFragment newInstance(String param1, String param2) {
        return new OverviewFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(false);
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_overview, container, false);
        Button toDiaryEntryButton = (Button) view.findViewById(R.id.diary_image_button);
        toDiaryEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), NewDiaryEntryActivity.class);
                getActivity().startActivity(intent);
            }
        });

        Button toGalleryEntryButton = (Button) view.findViewById(R.id.gallery_image_button);
        toGalleryEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), NewImageEntryActivity.class);
                getActivity().startActivity(intent);
            }

        });


        Button toMapButton = (Button) view.findViewById(R.id.map_image_button);
        toMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.content, new MapFragment()).commit();
            }
        });


        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction();
    }


}
