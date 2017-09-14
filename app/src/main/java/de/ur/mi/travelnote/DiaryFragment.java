package de.ur.mi.travelnote;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.ArrayList;

import de.ur.mi.travelnote.de.ur.mi.travelnote.sqlite.helper.DatabaseHelper;


public class DiaryFragment extends Fragment {

    private final int ORIGIN = 0;
    String userID;
    String userName;
    long idTemp;
    private boolean fragmentStatus;
    private OnFragmentInteractionListener mListener;
    private DatabaseHelper mDatabaseHelper;
    private DiaryCursorAdapter adapter;
    private TextView mTextView;
    private ListView mListView;
    ArrayList<String> listData;
    Cursor cursor;

    public DiaryFragment() {
        // Required empty public constructor
    }

    public static DiaryFragment newInstance() {
        DiaryFragment fragment = new DiaryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentStatus = true;
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_diary, container, false);
        mDatabaseHelper = new DatabaseHelper(getContext());
        getUserInfo();
        mTextView = (TextView) view.findViewById(R.id.diary_empty_text);
        mListView = (ListView) view.findViewById(R.id.diary_list_view);
        populateListView();

        Button newEntry = (Button) view.findViewById(R.id.new_Entry_Button);
        newEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), NewDiaryEntryActivity.class);
                startActivity(intent);
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Maybe do share dialog?
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                showDeleteSingleEntryDialog(l);
                return true;
            }
        });


        return view;
    }


    private void populateListView(){
        cursor = mDatabaseHelper.getDiaryEntriesCurrentUser(userID);
        listData = new ArrayList<>();
        if (cursor == null || cursor.getCount() < 1) {
            mTextView.setText("Keine Einträge vorhanden!");
        } else {
            try {
                while (cursor.moveToNext()){
                    listData.add(cursor.getString(1));
                }
            } catch (CursorIndexOutOfBoundsException e){
                //...
            }
        }

        adapter = new DiaryCursorAdapter(getContext(), cursor);
        mListView.setAdapter(adapter);
    }



    private void showDeleteSingleEntryDialog(long i) {
        this.idTemp = i;
        final int helper = (int) idTemp;
        //if there are db entries build alert dialog to avoid deletion by accident
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.delete_db_single_diary_entry_warning_title);
        alertDialog.setMessage(R.string.delete_db_single_diary_entry_warning_long);
        alertDialog.setIcon(R.drawable.ic_warning_black_24dp);

        //if user still clicks yes, then delete db entries
        alertDialog.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mDatabaseHelper.clearDiaryEntryCurrentUser(userID, helper);
                refreshFragment();
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

    private void showDeleteAllEntriesDialog() {

        //if there are db entries build alert dialog to avoid deletion by accident
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.delete_db_diary_entries_warning_title);
        alertDialog.setMessage(R.string.delete_db_diary_entries_warning_long);
        alertDialog.setIcon(R.drawable.ic_warning_black_24dp);

        //if user still clicks yes, then delete db entries
        alertDialog.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                boolean stmt = mDatabaseHelper.clearTableDiaryEntriesCurrentUser(userID, ORIGIN);
                if(stmt){
                    Toast.makeText(getContext(), "Alle Tagebucheinträge wurden gelöscht!", Toast.LENGTH_SHORT).show();
                    //refreshArrayList();
                }else{
                    Toast.makeText(getContext(), "Tagebucheinträge konnten nicht gelöscht werden.", Toast.LENGTH_SHORT).show();
                }
                refreshFragment();
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


    private void refreshFragment(){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content, new DiaryFragment()).commit();
    }



    @Override
    public void onResume() {
        super.onResume();
        if(!fragmentStatus){
            fragmentStatus = true;
            refreshFragment();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        fragmentStatus = false;
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
        MenuInflater menuInflater = new MenuInflater(getContext());
        menuInflater.inflate(R.menu.action_buttons_diary_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_email:
                if(listData.isEmpty()){
                    Toast.makeText(getContext(), "Keine Einträge vorhanden!", Toast.LENGTH_SHORT).show();
                }else {
                    sendDbViaMail();
                }
                //sendDbViaMail();
                //mDatabaseHelper.deleteDB();
                return true;
            case R.id.action_delete_diary:
                if(listData.isEmpty()){
                    Toast.makeText(getContext(), "Keine Einträge vorhanden!", Toast.LENGTH_SHORT).show();
                }else{
                    showDeleteAllEntriesDialog();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void sendDbViaMail() {
        Intent intent = null, chooser = null;

        intent = new Intent(Intent.ACTION_SEND);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Reisetagebuch");
        StringBuilder sb = new StringBuilder();

        Cursor data = mDatabaseHelper.getDiaryEntriesCurrentUser(userID);

        if (data == null || data.getCount() < 1) {
            Toast.makeText(getContext(), R.string.no_entries_different_users, Toast.LENGTH_SHORT).show();
        } else {
            try {
                while (data.moveToNext()) {
                    String s = "Titel: " + data.getString(1) + "\n" + "\n";
                    sb.append(s);
                    Toast.makeText(getContext(), "Dings ist: " + data.getString(1), Toast.LENGTH_SHORT).show();
                }
            } finally {
                data.close();
            }
        }

        intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        intent.setType("message/rfc822");
        chooser = Intent.createChooser(intent, "Send Email");
        startActivity(chooser);
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void getUserInfo(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userID = user.getUid();
            userName = user.getDisplayName();
        }
    }
}
