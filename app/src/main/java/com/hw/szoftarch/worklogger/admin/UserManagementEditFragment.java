package com.hw.szoftarch.worklogger.admin;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.hw.szoftarch.worklogger.R;
import com.hw.szoftarch.worklogger.entities.User;
import com.hw.szoftarch.worklogger.entities.UserLevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class UserManagementEditFragment extends DialogFragment {

    public static final String TAG = "UserManagementEdit";
    private User mUser;

    private AppCompatEditText mNameEditText;
    private AppCompatSpinner mLevelSpinner;

    private EditCallback listener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (getTargetFragment() != null) {
            try {
                listener = (EditCallback) getTargetFragment();
            } catch (ClassCastException ce) {
                Log.e(TAG, "Target Fragment does not implement fragment interface!");
            } catch (Exception e) {
                Log.e(TAG, "Unhandled exception!");
                e.printStackTrace();
            }
        } else {
            try {
                listener = (EditCallback) activity;
            } catch (ClassCastException ce) {
                Log.e(TAG, "Parent Activity does not implement fragment interface!");
            } catch (Exception e) {
                Log.e(TAG, "Unhandled exception!");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogFragmentTheme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_user_management, container, false);
        mNameEditText = root.findViewById(R.id.name);
        mNameEditText.setText(mUser.getName());

        mLevelSpinner = root.findViewById(R.id.level);
        final List<UserLevel> levels =  new ArrayList<>(Arrays.asList(UserLevel.values()));
        final ArrayAdapter<UserLevel> userAdapter = new ArrayAdapter<>(getActivity(), R.layout.issue_spinner_item, levels);
        mLevelSpinner.setAdapter(userAdapter);
        final int index = levels.indexOf(mUser.getUserLevel());
        mLevelSpinner.setSelection(index);

        final Button btnAdd = root.findViewById(R.id.btn_ok);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String nameText = mNameEditText.getText().toString().trim();
                if (nameText.equals("")) {
                    Toast.makeText(getActivity(), "Name cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
                final UserLevel level = ((UserLevel) mLevelSpinner.getSelectedItem());

                mUser.setUserLevel(level);
                mUser.setName(nameText);

                if (listener != null) {
                    listener.onUserEdited(mUser);
                }
                dismiss();
            }
        });

        final Button btnCancel = root.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return root;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog != null) {
            final Window window = dialog.getWindow();
            if (window != null) {
                window.requestFeature(Window.FEATURE_NO_TITLE);
            }
        }
        return dialog;
    }

    public void putUser(final User user) {
        mUser = user;
    }

    public interface EditCallback {
        void onUserEdited(User editedUser);
    }
}
