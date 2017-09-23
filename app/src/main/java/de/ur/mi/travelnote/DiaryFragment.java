package de.ur.mi.travelnote;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
    private String userID, userName;
    private long sendID;
    private boolean fragmentStatus;
    private OnFragmentInteractionListener mListener;
    private DatabaseHelper mDatabaseHelper;
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
        //Set boolean if fragment is "visible" to user
        fragmentStatus = true;
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_diary, container, false);
        mDatabaseHelper = new DatabaseHelper(getContext());
        getUserInfo();
        initUIElements(view);
        new DisplayEntriesAsyncTask().execute();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showShareEntryDialog(l);
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                //Here I want to delete the selected entry..
                showDeleteSingleEntryDialog(id);
                return true;
            }
        });


        return view;
    }

    private void initUIElements(View view) {
        mTextView = (TextView) view.findViewById(R.id.diary_empty_text);
        mTextView.setVisibility(View.GONE);
        mListView = (ListView) view.findViewById(R.id.diary_list_view);


        Button newEntry = (Button) view.findViewById(R.id.new_Entry_Button);
            newEntry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                Intent intent = new Intent(getContext(), NewDiaryEntryActivity.class);
                startActivity(intent);
                }
            });

    }


    private void showDeleteSingleEntryDialog(long i) {

        final long helper = i;
        //if there are db entries build alert dialog to avoid deletion by accident
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.delete_db_single_diary_entry_warning_title);
        alertDialog.setMessage(R.string.delete_db_single_diary_entry_warning_long);
        alertDialog.setIcon(R.drawable.ic_warning_black_24dp);

        //if user still clicks yes, then delete selected diary enty
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

        //if user still clicks yes, then delete diary entries of current user
        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                boolean stmt = mDatabaseHelper.clearTableDiaryEntriesCurrentUser(userID, ORIGIN);
                if (stmt) {
                    Toast.makeText(getContext(), "Alle Tagebucheinträge wurden gelöscht!", Toast.LENGTH_SHORT).show();
                    //refreshArrayList();
                } else {
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
        alertDialog.setPositiveButton(R.string.go, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                sendDiaryEntry(sendID);
            }
        });

        //if user cancels, do nothing
        alertDialog.setNegativeButton(R.string.better_not, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing here.
            }
        });
        alertDialog.show();
    }


    //Method to reload the Fragment
    private void refreshFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content, new DiaryFragment()).commit();
    }

    /*
        Override methods
        set fragmentStatus in onResume and on Pause, according to state of fragment ("visible" or not to user)
     */

    @Override
    public void onResume() {
        super.onResume();
        if (!fragmentStatus) {
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

    /*
        Method to setup options menu of toolbar and inflate the toolbar's options menu
     */

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuInflater menuInflater = new MenuInflater(getContext());
        menuInflater.inflate(R.menu.action_buttons_diary_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /*
        Method for selected toolbar's option item, what happens when a certain item is clicked
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_email:
                //check first if there are any diary entries, show toast if not
                if (listData.isEmpty()) {
                    Toast.makeText(getContext(), R.string.no_db_entry_available, Toast.LENGTH_SHORT).show();
                } else {
                    mailDiaryEntries();
                }
                return true;
            case R.id.action_delete_diary:
                //chekc first if there are any diary entries, show toast if not
                if (listData.isEmpty()) {
                    Toast.makeText(getContext(), R.string.no_db_entry_available, Toast.LENGTH_SHORT).show();
                } else {
                    showDeleteAllEntriesDialog();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
        Method so send selected diary entry
        Every application installed on the users phone, which is able to handle plain text intents
            can take the intent and process it, e.g. messaging apps
        By that user has the choice to select an app to share his diary entry

     */
    private void sendDiaryEntry(long i) {
        Cursor data = mDatabaseHelper.getSelectedDiaryEntry(i);
        String sTitle = "";
        String sContent = "";
        String sLocation = "";
        String sDate = "";

        //check first if the selected diary entry is available..
        if (data == null || data.getCount() < 1) {
            Toast.makeText(getContext(), R.string.no_entry_chosen, Toast.LENGTH_SHORT).show();
        } else {
            //get data from database cursor and store them in local variable
            try {
                data.moveToFirst();
                sTitle = data.getString(1);
                sContent = data.getString(2);
                sLocation = data.getString(3);
                sDate = data.getString(4);
            } catch (CursorIndexOutOfBoundsException e) {
                // do nonthing here
            } finally {
                // in the end: close cursor
                data.close();
            }
        }

        //prepare the content body
        String shareBody = sTitle + " (vom " + sDate + " in " + sLocation + ")\n" + sContent;
        //create intent, and hand over content to intent
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, R.string.Mail_Head);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Ausgewählen Tagebucheintrag verschicken"));
    }

    /*
        Method to send all diary entries of currently logged in user via E-Mail (or other selected apps, which are able to handle "mailto:")
     */
    private void mailDiaryEntries() {
        Intent intent, chooser;
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

    private void getUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userID = user.getUid();
            userName = user.getDisplayName();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDatabaseHelper.close();
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    /*
          Inner AsyncTask class to fetch diary entries from database and display entries using a custom adapter
     */
    private class DisplayEntriesAsyncTask extends AsyncTask<Void, Void, Void> {
        Cursor data;
        DiaryCursorAdapter adapter;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        //Fetch database entries in background..
        @Override
        protected Void doInBackground(Void... voids) {
            data = mDatabaseHelper.getDiaryEntriesCurrentUser(userID);
            return null;
        }


        /*
            Method to do, when background task is finished
            fills ArrayList with data from database cursor , if there are any.
            update UI, when there are no database entries
            finally, set custom adapter to display entries in UI
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            listData = new ArrayList<>();
            if (data == null || data.getCount() < 1) {
                if (getView() != null) {
                    mTextView.setVisibility(View.VISIBLE);
                    mTextView.setText(R.string.no_diary_entries_text);
                }
            } else {
                try {
                    while (data.moveToNext()) {
                        listData.add(data.getString(1));
                    }
                } catch (CursorIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
            adapter = new DiaryCursorAdapter(getContext(), data);
            mListView.setAdapter(adapter);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            data.close();
        }
    }
}