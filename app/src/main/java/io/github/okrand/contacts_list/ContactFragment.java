package io.github.okrand.contacts_list;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.SyncStateContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

/**
 * Created by Krando67 on 2/23/18.
 */

public class ContactFragment extends Fragment {
//ContactFragmentListener listen;
    List<Contact> theList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false);
    }
    ArrayAdapter<Contact> adapter;
    @SuppressLint("StaticFieldLeak")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ListView theView = getView().findViewById(R.id.the_list_view);
        Log.d("ONVIEWCREATED", "CONTACTFRAGMENT");

        final AppDatabase db = Room.databaseBuilder(getActivity().getApplicationContext(),
                AppDatabase.class, "contact").allowMainThreadQueries().build();

        final RelDatabase relDB = Room.databaseBuilder(getActivity().getApplicationContext(),
                RelDatabase.class, "relation").allowMainThreadQueries().build();

        final ContactDao contactDao = db.contactDao();
        final RelationshipDao relDao = relDB.relationDao();
        theList = contactDao.getAll();

        final List<Contact> deleter = new ArrayList<>();

        adapter = new ArrayAdapter<Contact>(getActivity(), R.layout.list_item_contact, R.id.list_contact_name, theList){
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                final View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(R.id.list_contact_name);
                text1.setText(theList.get(position).getName());
                final CheckBox check = view.findViewById(R.id.contact_check);
                check.setOnCheckedChangeListener(null);
                check.setChecked(false);

                check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked){
                            deleter.add(theList.get(position));
                        }
                        else{
                            deleter.remove(theList.get(position));
                        }
                    }
                });

                text1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT) {
                            Intent myIntent = new Intent(getActivity(), ProfileActivity.class);
                            myIntent.putExtra("name", theList.get(position).getName()); //Optional parameters
                            getActivity().startActivity(myIntent);
                        }
                        else{
                            //swapping fragments
                            Bundle bundl = new Bundle();
                            bundl.putString("name", theList.get(position).getName());
                            ProfileFragment prof = new ProfileFragment();
                            prof.setArguments(bundl);
                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container2,prof,"prof").commit();
                        }
                    }
                });

                return view;
            }
        };
        theView.setAdapter(adapter);

        Button deleteButton = getView().findViewById(R.id.button_delete_contact);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Contact c: deleter){
                    contactDao.delete(c);
                    relDao.deleteRelation(c.getName());
                    theList.remove(c);
                    //delete associated picture
                    String root = Environment.getExternalStorageDirectory().toString();

                    File myDir = new File(root + "/profile-pics");
                    String filename = c.getName() + ".png";
                    File file = new File(myDir, filename);
                    if (file.exists())
                        file.delete();
                }

                adapter.notifyDataSetChanged();
            }
        });

        Button addButton = getView().findViewById(R.id.button_add_contact);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT) {
                    Intent myIntent = new Intent(getActivity(), DetailsActivity.class);
                    //myIntent.putExtra("key", value); //Optional parameters
                    getActivity().startActivityForResult(myIntent, 1);
                }
                else {
                    //swapping fragments
                    DetailsFragment deets = new DetailsFragment();
                    deets.setArguments(getActivity().getIntent().getExtras());

                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container2,deets,"deets").commit();
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        final AppDatabase db = Room.databaseBuilder(getActivity().getApplicationContext(),
                AppDatabase.class, "contact").allowMainThreadQueries().build();

        final ContactDao contactDao = db.contactDao();
        Contact lastC = contactDao.getLast();
        if (!theList.contains(lastC)){
            theList.add(lastC);
            adapter.notifyDataSetChanged();
        }
    }

//    public interface ContactFragmentListener {
//        public void onProfileClick(String name);
//    }

}
