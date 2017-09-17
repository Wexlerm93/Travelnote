package de.ur.mi.travelnote;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import de.ur.mi.travelnote.de.ur.mi.travelnote.sqlite.helper.DatabaseHelper;


public class DiaryFragment extends Fragment {

    private final int ORIGIN = 0;
    String userID;
    String userName;
    long deleteID;
    long sendID;
    private boolean fragmentStatus;
    private OnFragmentInteractionListener mListener;
    private DatabaseHelper mDatabaseHelper;
    private DiaryCursorAdapter adapter;
    private TextView mTextView;
    private ListView mListView;
    ArrayList<String> listData;

    public DiaryFragment() {
        // Required empty public constructor
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

        initUIElements(view);

        new DisplayEntriesAsyncTask().execute();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Toast.makeText(getContext(), "Dings ist: " + i, Toast.LENGTH_SHORT).show();
                showShareEntryDialog(l);
                //sendDiaryEntry(l);
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                //Here I want to delete the selected entry..
                //both position and id are returning the same value:
                // when there are three items in the list, the position would be three for all values,
                // while the id would be the value of the latest entry.
                showDeleteSingleEntryDialog(id);
                return true;
            }
        });


        return view;
    }

    private void initUIElements(View view){
        mTextView = (TextView) view.findViewById(R.id.diary_empty_text);
        mTextView.setVisibility(View.GONE);
        mListView = (ListView) view.findViewById(R.id.diary_list_view);

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            Button newEntry = (Button) view.findViewById(R.id.new_Entry_Button);
            newEntry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), NewDiaryEntryActivity.class);
                    startActivity(intent);
                }
            });
        }else {
            FloatingActionButton newEntryFloat = (FloatingActionButton) view.findViewById(R.id.new_Entry_Button_Float);
            newEntryFloat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), NewDiaryEntryActivity.class);
                    startActivity(intent);
                }
            });
        }
    }


    private void showDeleteSingleEntryDialog(long i) {
        this.deleteID= i;
        final int helper = (int) deleteID;
        //if there are db entries build alert dialog to avoid deletion by accident
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.delete_db_single_diary_entry_warning_title);
        alertDialog.setMessage(R.string.delete_db_single_diary_entry_warning_long);
        alertDialog.setIcon(R.drawable.ic_warning_black_24dp);

        //if user still clicks yes, then delete db entries
        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mDatabaseHelper.clearDiaryEntryCurrentUser(userID, helper);
                refreshFragment();
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

    private void showDeleteAllEntriesDialog() {

        //if there are db entries build alert dialog to avoid deletion by accident
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.delete_db_diary_entries_warning_title);
        alertDialog.setMessage(R.string.delete_db_diary_entries_warning_long);
        alertDialog.setIcon(R.drawable.ic_warning_black_24dp);

        //if user still clicks yes, then delete db entries
        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
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
        alertDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing here.
            }
        });
        alertDialog.show();
    }

    private void showShareEntryDialog(long id) {
        this.sendID = id;
        //if there are db entries build alert dialog to avoid deletion by accident
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setMessage(R.string.share_diary_entry_text);


        //if user still clicks yes, then delete db entries
        alertDialog.setPositiveButton("Und los!", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                sendDiaryEntry(sendID);
            }
        });

        //if user cancels, do nothing
        alertDialog.setNegativeButton("Lieber doch nicht.", new DialogInterface.OnClickListener() {
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
                    mailDiaryEntries();
                }
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

    private void sendDiaryEntry(long i){
        Cursor data = mDatabaseHelper.getSelectedDiaryEntry(i);
        String sTitle = "";
        String sContent = "";
        String sLocation = "";
        String sDate = "";


        if (data == null || data.getCount() < 1) {
            Toast.makeText(getContext(), "Kein Eintrag..", Toast.LENGTH_SHORT).show();
        } else {
            try {
                data.moveToFirst();
                sTitle = data.getString(1);
                sContent = data.getString(2);
                sLocation = data.getString(3);
                sDate = data.getString(4);
            } catch (CursorIndexOutOfBoundsException e){
                //...
            }finally {
                data.close();
            }
        }


        String shareBody = sTitle + " (vom " + sDate + " in " + sLocation + ")\n" + sContent;
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Mein Reisetagebucheintrag");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Ausgewählen Tagebucheintrag verschicken"));

    }

    private void mailDiaryEntries() {
        Intent intent = null, chooser = null;

        intent = new Intent(Intent.ACTION_SEND);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.subject_text_share_mail) + userName);
        StringBuilder sb = new StringBuilder();
        String openingText = getString(R.string.opening_text_share_mail) + "\n" + "\n";
        sb.append(openingText);
        Cursor data = mDatabaseHelper.getDiaryEntriesCurrentUser(userID);
        int entryCounter = 1;

        if (data == null || data.getCount() < 1) {
            Toast.makeText(getContext(), R.string.no_entries_different_users, Toast.LENGTH_SHORT).show();
        } else {
            try {
                while (data.moveToNext()) {
                    String s = "Eintrag " + entryCounter + ": " + data.getString(1) + " (" + data.getString(3) + ", " + data.getString(4) + ")\n" + data.getString(2) + "\n" + "\n";
                    sb.append(s);
                    entryCounter++;
                }
            } finally {
                data.close();
            }
        }

        intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        intent.setType("message/rfc822");
        chooser = Intent.createChooser(intent, getString(R.string.intent_text_share_diary_mail));
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

    private class DisplayEntriesAsyncTask extends AsyncTask<Void,Void,Void>{
        Cursor data;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            data = mDatabaseHelper.getDiaryEntriesCurrentUser(userID);
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            listData = new ArrayList<>();
            if (data == null || data.getCount() < 1) {
                if(getView() != null){
                    mTextView.setVisibility(View.VISIBLE);
                    mTextView.setText(R.string.no_diary_entries_text);
                }

            } else {
                try {
                    while (data.moveToNext()){
                        listData.add(data.getString(1));
                    }
                } catch (CursorIndexOutOfBoundsException e){
                    e.printStackTrace();
                }
            }
            adapter = new DiaryCursorAdapter(getContext(), data);
            mListView.setAdapter(adapter);
        }
    }
}