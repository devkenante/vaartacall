package com.varenia.vaarta.fragments;


import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.varenia.vaarta.R;
import com.varenia.vaarta.handler_classes.CallData;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class UsersListFragment extends Fragment {

    private FrameLayout usersListFL;
    private LinearLayout usersListContainerLL;
    private ArrayList<Integer> usersToShow;
    private HashMap<Integer, String> names;
    private int currentUserId;
    private SparseArray<View> allUsers;

    public UsersListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users_list, container, false);

        initialize(view);
        handleVisibility(2);
        designUI();

        return view;
    }

    private void initialize(View view) {
        usersListFL = view.findViewById(R.id.usersListFL);
        usersListContainerLL = view.findViewById(R.id.usersListContainerLL);
        CallData callData = CallData.getInstance(getActivity().getApplicationContext());
//        usersToShow = callData.getUsersToSubsribe();
        currentUserId = callData.getCurrentUserId();
   //     names = callData.getLongNames();
        allUsers = new SparseArray<>();
    }

    private void designUI() {
        for (int i = 0; i < usersToShow.size(); i++) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.custom_users_on_call_row, null);
            ((TextView) v.findViewWithTag("userOnCallNameTV")).setText(names.get(usersToShow.get(i)));
            if (usersToShow.get(i) == currentUserId) {
                v.findViewWithTag("userOnCallStatus").setBackground(getActivity().getResources().getDrawable(R.drawable.user_connected));
            }
            usersListContainerLL.addView(v);
            allUsers.put(usersToShow.get(i), v);
        }
    }

    public void changeStatus(int userId, Boolean active) {
        if (active) {
            View view = allUsers.get(userId);
            if(view!=null){
                View v = view.findViewWithTag("userOnCallStatus");
                if(v!=null){
                    v.setBackground(getActivity().getResources().getDrawable(R.drawable.user_connected));
                }
            }
            //allUsers.get(userId).findViewWithTag("userOnCallStatus").setBackground(getActivity().getResources().getDrawable(R.drawable.user_connected));
        } else {
            View view = allUsers.get(userId);
            if(view!=null){
                View v = view.findViewWithTag("userOnCallStatus");
                if(v!=null){
                    v.setBackground(getActivity().getResources().getDrawable(R.drawable.user_disconneted));
                }
            }
            //allUsers.get(userId).findViewWithTag("userOnCallStatus").setBackground(getActivity().getResources().getDrawable(R.drawable.user_disconneted));
        }
    }

    public void handleVisibility(int code) {
        if (code == 1) {
            usersListFL.setVisibility(View.VISIBLE);
        } else if (code == 2) {
            usersListFL.setVisibility(View.GONE);
        }
    }

}
