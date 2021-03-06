package ca.m3dia.iotuselessgoosebox;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.transition.ChangeImageTransform;
import android.transition.Scene;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import ca.m3dia.iotuselessgoosebox.lib.AnimatorPath;
import ca.m3dia.iotuselessgoosebox.lib.PathEvaluator;
import ca.m3dia.iotuselessgoosebox.lib.PathPoint;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleDevice;

/**
 * Created by Umar Bhutta.
 */
public class CustomFragment extends Fragment {
    private static final String TAG = CustomFragment.class.getSimpleName();

    public final static float SCALE_FACTOR      = 13f;
    public final static int ANIMATION_DURATION  = 300;
    public final static int MINIMUN_X_DISTANCE  = 200;

    public static ArrayList<String> name = new ArrayList<>();
    public static ArrayList<String> info = new ArrayList<>();
    public static ArrayList<String> letters = new ArrayList<>();

    private String json = "";
    private boolean mRevealFlag;
    private float mFabSize;

    private ImageButton mFab;
    private FrameLayout mFabContainer;
    private LinearLayout mControlsContainer;
    private RelativeLayout mRelativeLayout;

    private TextView toggleCustomTextView;
    private EditText nameEditText;
    private Spinner lidSpinner, lidLedSpinner, redLedSpinner, armSpinner, soundSpinner;
    private Button addButton, cancelButton;


    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_custom, container, false);

        mFabSize = getResources().getDimensionPixelSize(R.dimen.fab_size);
        bindViews(view);
        setupList(view);

        return view;
    }

    private void setupList(View view) {
        //setup recycler view
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        //setup adapter
        CustomListAdapter customListAdapter = new CustomListAdapter(getActivity());
        //attach adapter to recycler view
        recyclerView.setAdapter(customListAdapter);

        //set LayoutManager for recyclerView. Use vertical list
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //attach layout manager to recyclerView
        recyclerView.setLayoutManager(layoutManager);

        //divider lines
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
    }

    private void bindViews(final View view) {
        mFab = (ImageButton) view.findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onFabPressed(v);
            }
        });

        mRelativeLayout = (RelativeLayout) view.findViewById(R.id.relativeLayout);
        mFabContainer = (FrameLayout) view.findViewById(R.id.fab_container);
        mControlsContainer = (LinearLayout) view.findViewById(R.id.add_custom_container);
        mFabContainer.bringToFront();
        toggleCustomTextView = (TextView) view.findViewById(R.id.toggleCustomTextView);
        nameEditText = (EditText) view.findViewById(R.id.nameEditText);
        addButton = (Button) view.findViewById(R.id.addButton);
        cancelButton = (Button) view.findViewById(R.id.cancelButton);

        lidSpinner = (Spinner) view.findViewById(R.id.lidSpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> lidAdapter = new ArrayAdapter<CharSequence>(getContext(),
                R.layout.spinner_textview_align, getResources().getStringArray(R.array.lid_spinner));
        // Specify the layout to use when the list of choices appears
        lidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the lidAdapter to the spinner
        lidSpinner.setAdapter(lidAdapter);

        lidLedSpinner = (Spinner) view.findViewById(R.id.lidLedSpinner);
        ArrayAdapter<CharSequence> lidLedAdapter = new ArrayAdapter<CharSequence>(getContext(),
                R.layout.spinner_textview_align, getResources().getStringArray(R.array.lid_led_spinner));
        lidLedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lidLedSpinner.setAdapter(lidLedAdapter);

        redLedSpinner = (Spinner) view.findViewById(R.id.redLedSpinner);
        ArrayAdapter<CharSequence> redLedAdapter = new ArrayAdapter<CharSequence>(getContext(),
                R.layout.spinner_textview_align, getResources().getStringArray(R.array.red_led_spinner));
        redLedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        redLedSpinner.setAdapter(redLedAdapter);

        armSpinner = (Spinner) view.findViewById(R.id.armSpinner);
        ArrayAdapter<CharSequence> armAdapter = new ArrayAdapter<CharSequence>(getContext(),
                R.layout.spinner_textview_align, getResources().getStringArray(R.array.arm_spinner));
        armAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        armSpinner.setAdapter(armAdapter);

        soundSpinner = (Spinner) view.findViewById(R.id.soundSpinner);
        ArrayAdapter<CharSequence> soundAdapter = new ArrayAdapter<CharSequence>(getContext(),
                R.layout.spinner_textview_align, getResources().getStringArray(R.array.sound_spinner));
        soundAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        soundSpinner.setAdapter(soundAdapter);

        nameEditText.setVisibility(View.INVISIBLE);
        lidSpinner.setVisibility(View.INVISIBLE);
        lidLedSpinner.setVisibility(View.INVISIBLE);
        redLedSpinner.setVisibility(View.INVISIBLE);
        armSpinner.setVisibility(View.INVISIBLE);
        soundSpinner.setVisibility(View.INVISIBLE);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddButtonPressed(view);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelButtonPressed(view);
            }
        });

        toggleCustomTextView.setText(Html.fromHtml("Define up to 5 actions. <u>Tap here</u> to update the action list and randomly execute the actions via a switch toggle. <br /><br />Alternatively, you can tap a custom action manually to execute it immediately."));

        toggleCustomTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToggleCustomPressed();
            }
        });

    }

    private void onToggleCustomPressed() {
        for (int i = 0; i < name.size(); i++) {
            Log.d(TAG, i + ". " + name.get(i));
        }

        for (int i = 0; i < info.size(); i++) {
            Log.d(TAG, i + ". " + info.get(i));
        }

        //Create json from letters ArrayList
        json = "{\"type\":1, \"data\":[";

        for(String member : letters) {
            json += "\"" + member + "\",";
        }

        json = json.substring(0, json.length() - 1);
        json += "]}";

        new Thread() {
            @Override
            public void run() {
                // Make the Particle call here

                ArrayList<String> jsonList = new ArrayList<>();
                ArrayList<String> toggleType = new ArrayList<>();
                jsonList.add(json);
                toggleType.add("CUSTOM");

                try {
                    int resultCode = Common.currDevice.callFunction("toggleType", toggleType);
                    Common.currDevice.callFunction("jsonParser", jsonList);

                    //capture resultCode from particle function to toast to user
                    if (resultCode == 1) {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getActivity(), "Custom Mode - action list updated.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getActivity(), "Failed to enable Custom Mode and update action list.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                } catch (ParticleCloudException | ParticleDevice.FunctionDoesNotExistException | IOException e) {
                    e.printStackTrace();
                }
                jsonList.clear();
                toggleType.clear();
            }
        }.start();
    }

    private void onCancelButtonPressed(final View view) {
        reverseAnimation(view);
    }

    private void reverseAnimation(final View view) {
        ViewGroup transitionRoot = mRelativeLayout;

        Scene originalScene = Scene.getSceneForLayout(transitionRoot, R.layout.fragment_custom, view.getContext());
        originalScene.setExitAction(new Runnable() {
            @Override
            public void run() {
                mControlsContainer.setVisibility(View.GONE);
            }
        });
        originalScene.setEnterAction(new Runnable() {
            @Override
            public void run() {
                bindViews(view);
                //mFab.setImageDrawable(null);
                setupList(view);

                mRevealFlag = false;
            }
        });

        TransitionManager.go(originalScene, new ChangeImageTransform());
    }

    private void onAddButtonPressed(final View view) {
        mFab.setImageDrawable(null);

        //reset buffer
        String customLetters = "";

        String lidAction = lidSpinner.getSelectedItem().toString();
        String lidLedAction = lidLedSpinner.getSelectedItem().toString();
        String redLedAction = redLedSpinner.getSelectedItem().toString();
        String armAction = armSpinner.getSelectedItem().toString();
        String soundAction = soundSpinner.getSelectedItem().toString();

        String customName = nameEditText.getText().toString();
        String customInfo = lidAction + ", " + lidLedAction + ", " + redLedAction + ", " +
                armAction + ", " + soundAction;

        name.add(customName);
        info.add(customInfo);

        switch(lidAction) {
            case "Normal":
                customLetters = "A";
                break;
            case "Fast":
                customLetters = "B";
                break;
            case "Slow":
                customLetters = "C";
                break;
            case "Shake":
                customLetters = "D";
                break;
            default:
                customLetters = "A";
                break;
        }

        switch(lidLedAction) {
            case "On":
                customLetters += "A";
                break;
            case "Delayed On":
                customLetters += "B";
                break;
            case "Off":
                customLetters += "C";
                break;
            case "Flicker":
                customLetters += "D";
                break;
            default:
                customLetters += "C";
                break;
        }

        switch(redLedAction) {
            case "On":
                customLetters += "A";
                break;
            case "Delayed On":
                customLetters += "B";
                break;
            case "Off":
                customLetters += "C";
                break;
            case "Flicker":
                customLetters += "D";
                break;
            default:
                customLetters += "C";
                break;
        }

        switch(armAction) {
            case "Normal":
                customLetters += "A";
                break;
            case "Fast":
                customLetters += "B";
                break;
            case "Slow":
                customLetters += "C";
                break;
            case "Shake":
                customLetters += "D";
                break;
            default:
                customLetters += "A";
                break;
        }

        switch(soundAction) {
            case "On":
                customLetters += "A";
                break;
            case "Off":
                customLetters += "B";
                break;
            default:
                customLetters += "B";
                break;
        }

        letters.add(customLetters);

        reverseAnimation(view);
    }


    public void onFabPressed(View view) {
        if (info.size() >= 5) {
            Toast.makeText(getActivity(), "Delete actions before adding new ones.", Toast.LENGTH_SHORT).show();
            return;
        }

        nameEditText.setVisibility(View.VISIBLE);
        lidSpinner.setVisibility(View.VISIBLE);
        lidLedSpinner.setVisibility(View.VISIBLE);
        redLedSpinner.setVisibility(View.VISIBLE);
        armSpinner.setVisibility(View.VISIBLE);
        soundSpinner.setVisibility(View.VISIBLE);

        mFab.setImageDrawable(null);
        final float startX = mFab.getX();

        AnimatorPath path = new AnimatorPath();
        path.moveTo(0, 0);
        //path.curveTo(-200, 200, -400, 100, -600, 50);
        path.curveTo(-300, 150, -800, 80, -800, 300);

        final ObjectAnimator anim = ObjectAnimator.ofObject(this, "fabLoc",
                new PathEvaluator(), path.getPoints().toArray());

        anim.setInterpolator(new AccelerateInterpolator());
        anim.setDuration(ANIMATION_DURATION);
        anim.start();

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (Math.abs(startX - mFab.getX()) > MINIMUN_X_DISTANCE) {
                    if (!mRevealFlag) {
                        mFabContainer.setY(mFabContainer.getY() + mFabSize - 17);


                        mFab.animate()
                                .scaleXBy(SCALE_FACTOR)
                                .scaleYBy(SCALE_FACTOR)
                                .setListener(mEndRevealListener)
                                .setDuration(ANIMATION_DURATION);



                        mRevealFlag = true;
                    }
                }
            }
        });
    }

    private AnimatorListenerAdapter mEndRevealListener = new AnimatorListenerAdapter() {

        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            mFab.setVisibility(View.INVISIBLE);

            mFabContainer.setBackgroundColor(getResources()
                    .getColor(R.color.brand_accent));

            mControlsContainer.setTranslationY(-200);

            for (int i = 0; i < mControlsContainer.getChildCount(); i++) {
                View v = mControlsContainer.getChildAt(i);
                ViewPropertyAnimator animator = v.animate()
                        .scaleX(1).scaleY(1)
                        .setDuration(ANIMATION_DURATION);

                animator.setStartDelay(i * 50);
                animator.start();
            }
        }
    };

    /**
     * We need this setter to translate between the information the animator
     * produces (a new "PathPoint" describing the current animated location)
     * and the information that the button requires (an xy location). The
     * setter will be called by the ObjectAnimator given the 'fabLoc'
     * property string.
     */
    public void setFabLoc(PathPoint newLoc) {
        mFab.setTranslationX(newLoc.mX);

        if (mRevealFlag)
            mFab.setTranslationY(newLoc.mY - (mFabSize / 2));
        else
            mFab.setTranslationY(newLoc.mY);
    }
}
