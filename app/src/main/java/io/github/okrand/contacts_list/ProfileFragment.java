package io.github.okrand.contacts_list;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

/**
 * Created by Krando67 on 2/26/18.
 */

public class ProfileFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
    private Animator mCurrentAnimator;

    // The system "short" animation time duration, in milliseconds. This
    // duration is ideal for subtle animations or animations that occur
    // very frequently.
    private int mShortAnimationDuration = 300;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        String uName = new String();
        try {
            uName = getActivity().getIntent().getExtras().getString("name");
        } catch (Exception e) {
            Bundle b = getArguments();
            uName = b.getString("name");
        }
        Log.d("NAME", uName);

        final AppDatabase db = Room.databaseBuilder(getActivity().getApplicationContext(),
                AppDatabase.class, "contact").allowMainThreadQueries().build();

        final RelDatabase relDB = Room.databaseBuilder(getActivity().getApplicationContext(),
                RelDatabase.class, "relation").allowMainThreadQueries().build();

        final ContactDao contactDao = db.contactDao();
        final RelationshipDao relDao = relDB.relationDao();

        Contact cont = contactDao.findByName(uName);
        TextView textName = getView().findViewById(R.id.profile_name);
        textName.setText(uName);
        TextView textNum = getView().findViewById(R.id.profile_number);
        textNum.setText(cont.getNumber());

        final List<Relationship> relList = relDao.findRelations(uName);
        final List<Contact> theList = new ArrayList<>();
        for (Relationship r : relList) {
            Contact c1 = new Contact();
            Contact c2 = new Contact();
            c1.setName(r.getName());
            c2.setName(r.getName2());
            if (!Objects.equals(c1.getName(), uName)) {
                theList.add(c1);
            }
            if (!Objects.equals(c2.getName(), uName)) {
                theList.add(c2);
            }
        }

        ListView theView = getView().findViewById(R.id.profile_relationship);
        final List<Contact> checker = new ArrayList<Contact>();
        final ArrayAdapter<Contact> adapter = new ArrayAdapter<Contact>(getActivity(), R.layout.list_item_contact, R.id.list_contact_name, theList) {
            @NonNull
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                final View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(R.id.list_contact_name);
                CheckBox check = view.findViewById(R.id.contact_check);
                LinearLayout lay = view.findViewById(R.id.item_layout);
                lay.removeView(check);
                text1.setText(theList.get(position).getName());

                text1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT) {
                            Intent myIntent = new Intent(getActivity(), ProfileActivity.class);
                            myIntent.putExtra("name", theList.get(position).getName()); //Optional parameters
                            getActivity().startActivity(myIntent);
                        } else {
                            Bundle bundl = new Bundle();
                            bundl.putString("name", theList.get(position).getName());
                            ProfileFragment prof = new ProfileFragment();
                            prof.setArguments(bundl);
                            //listen.onProfileClick(theList.get(position).getName());
                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container2, prof, "prof").commit();
                        }
                    }
                });

                return view;
            }
        };
        theView.setAdapter(adapter);

        //check for profile picture
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/profile-pics");
        String filename = uName + ".png";
        File file = new File(myDir, filename);

        if (file.exists()) {
            Bitmap pictur = BitmapFactory.decodeFile(file.getPath());
            ImageView image = getView().findViewById(R.id.profile_picture);
            image.setImageBitmap(pictur);
        }
        //Camera functionality for picture
        ImageView image = view.findViewById(R.id.profile_picture);
        final String finalUName = uName;
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/profile-pics");
                String filename = finalUName + ".png";
                File file = new File(myDir, filename);
                if (file.exists()) {
                    Bitmap pictu = BitmapFactory.decodeFile(file.getPath());
                    zoomImageFromThumb(v, pictu);
                }
            }
        });
    }

    private void zoomImageFromThumb(final View thumbView, Bitmap imageRes) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView) getView().findViewById(
                R.id.expanded_image);
        expandedImageView.setImageBitmap(imageRes);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        getView().findViewById(R.id.containerLin)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedImageView,
                        View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }
}
