package io.github.okrand.contacts_list;

import android.Manifest;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.app.Activity.RESULT_OK;
import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

/**
 * Created by Krando67 on 2/26/18.
 */

public class DetailsFragment extends Fragment {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    Bitmap imageBitmap;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    List<Contact> theList = new ArrayList<>();
    List<Contact> theList2 = new ArrayList<>();
    Boolean altPicture = false;
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.d("ONVIEWCREATED", "DETAILSFRAGMENT");
        final AppDatabase db = Room.databaseBuilder(getActivity().getApplicationContext(),
                AppDatabase.class, "contact").allowMainThreadQueries().build();

        final RelDatabase relDB = Room.databaseBuilder(getActivity().getApplicationContext(),
                RelDatabase.class, "relation").allowMainThreadQueries().build();

        final ContactDao contactDao = db.contactDao();
        final RelationshipDao relDao = relDB.relationDao();
        theList = contactDao.getAll();
        theList2 = contactDao.getAll();

        final ListView theView = getView().findViewById(R.id.details_relationship);
        final List<Contact> checker = new ArrayList<Contact>();
        final int[] numChecked = {0};
        final ArrayAdapter<Contact> adapter = new ArrayAdapter<Contact>(getActivity(), R.layout.list_item_contact, R.id.list_contact_name, theList) {
            @NonNull
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                final View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(R.id.list_contact_name);
                final CheckBox check = view.findViewById(R.id.contact_check);
                text1.setText(theList.get(position).getName());
                check.setOnCheckedChangeListener(null);
                if (position < numChecked[0])
                    check.setChecked(true);
                else
                    check.setChecked(false);

                check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked){
                            check.setChecked(false);
                            checker.add(theList.get(position));
                            Contact tmp=theList.get(position);
                            theList.remove(theList.get(position));
                            theList.add(0,tmp);
                            numChecked[0] += 1;
                            notifyDataSetChanged();
                        }
                        else{
                            checker.remove(theList.get(position));
                            Contact tmp = theList.get(position);
                            theList.remove(theList.get(position));
                            theList.add(theList2.indexOf(tmp), tmp);
                            numChecked[0] -= 1;
                            notifyDataSetChanged();
                        }
                    }
                });

                return view;
            }
        };
        theView.setAdapter(adapter);


        Button saveButton = getView().findViewById(R.id.add_person_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText nameBox = getView().findViewById(R.id.details_name);
                EditText numberBox = getView().findViewById(R.id.details_number);
                final Contact newCont = new Contact();
                newCont.setName(nameBox.getText().toString());
                newCont.setNumber(numberBox.getText().toString());

                final ContactDao contactDao = db.contactDao();
                final RelationshipDao relDao = relDB.relationDao();
                contactDao.insertAll(newCont);
                theList.add(newCont);

                for (Contact c : checker) {
                    Relationship rel = new Relationship();
                    rel.setRel(rel, newCont.getName(), c.getName());
                    relDao.insertAll(rel);
                }

                //save picture
                if (altPicture) {
                    if (imageBitmap == null)
                        Log.d("PICTURE", "NULL");
                    String root = Environment.getExternalStorageDirectory().toString();

                    File myDir = new File(root + "/profile-pics");
                    String filename = nameBox.getText().toString() + ".png";
                    File file = new File(myDir, filename);
                    try {
//                        if (!file.exists()){
//                            file.mkdirs();
//                            file.createNewFile();
//                        }
                        Log.d("SAVING TO", file.getPath());
                        myDir.mkdir();
                        FileOutputStream out = new FileOutputStream(file,false);
                        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                        out.flush();
                        out.close();
                        Log.d("PICTURE", "Picture saved");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //navigate
                if (getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT) {
                    getActivity().finish();
                } else {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .remove(getFragmentManager().findFragmentByTag("deets")).commit();
                    ContactFragment CF = new ContactFragment();
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, CF, "contact").commit();
                }
            }
        });

        //Camera functionality for picture
        ImageView image = view.findViewById(R.id.details_picture);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == REQUEST_IMAGE_CAPTURE) && (resultCode == RESULT_OK)) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            ImageView image = getView().findViewById(R.id.details_picture);
            image.setImageBitmap(imageBitmap);
            altPicture = true;
        }
    }

}
