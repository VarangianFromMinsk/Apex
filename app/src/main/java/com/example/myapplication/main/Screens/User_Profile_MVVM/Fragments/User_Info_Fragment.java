package com.example.myapplication.main.Screens.User_Profile_MVVM.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentUserPostsInfoBinding;
import com.example.myapplication.main.Models.Model_User;
import com.example.myapplication.main.Screens.User_Profile_MVVM.Profile_ViewModel;


public class User_Info_Fragment extends Fragment {


    private String selectedUser;
    private boolean isThatCurrentUser;


    private String nameFromBase, emailFromBase, nickFromBase = "", telephone = "";

    private Profile_ViewModel viewModel;
    private FragmentUserPostsInfoBinding binding;

    private checkInterface checkInterfaceState = null;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //todo: для работы с activity
        try {
            checkInterfaceState = (checkInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "activity must implement interface");
        }
    }

    // TODO: Rename and change types and number of parameters
    public static User_Info_Fragment newInstance(String selectedUser, boolean isThatCurrentUser) {
        User_Info_Fragment fragment = new User_Info_Fragment();
        Bundle args = new Bundle();
        args.putString("SelectedUser", selectedUser);
        args.putBoolean("isThatCurrentUser", isThatCurrentUser);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedUser = getArguments().getString("SelectedUser");
            isThatCurrentUser = getArguments().getBoolean("isThatCurrentUser");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_user_posts_info,
                container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //TODO: View уже создано, можно работать
        viewModel = new ViewModelProvider(requireActivity()).get(Profile_ViewModel.class);
        loadUserInfo();
        updateUserInfoBtn();

        //TODO: плохой подход, следует использовать интерфейс
        //((User_Profile_Activity)getActivity()).AddAvatar();

        //TODO: правильный подход
        checkInterfaceState.checkingUserInfoFragmentFinished();

    }

    public interface checkInterface {

        void checkingUserInfoFragmentFinished();
    }

    private void loadUserInfo() {

        //viewModel.loadUserInfo(selectedUser);

        viewModel.getMutCurrentUser().observe(getViewLifecycleOwner(), new Observer<Model_User>() {
            @Override
            public void onChanged(Model_User user) {
                if (!isThatCurrentUser) {
                    disableEditTextAndBtn();
                }

                //TODO: get data
                nameFromBase = user.getName();
                emailFromBase = user.getEmail();


                //TODO: set data
                binding.nameInEdit.setText(nameFromBase);
                binding.emailInEdit.setText(emailFromBase);

                try {
                    nickFromBase = user.getNick();
                    telephone = user.getTelephone();
                } catch (Exception ignored) {
                }

                if (nickFromBase.equals("")) {
                    binding.nicknameInEdit.setTextSize(14);
                    binding.nicknameInEdit.setAlpha(0.8f);
                } else {
                    binding.nicknameInEdit.setTextSize(16);
                    binding.nicknameInEdit.setAlpha(1);
                    binding.nicknameInEdit.setText(nickFromBase);
                }

                if (telephone.equals("")) {
                    binding.telephoneInEdit.setTextSize(14);
                    binding.telephoneInEdit.setAlpha((float) 0.8);
                } else {
                    binding.nicknameInEdit.setTextSize(16);
                    binding.telephoneInEdit.setAlpha(1);
                    binding.telephoneInEdit.setText(telephone);
                }

                binding.progressBarInUserProfile.setVisibility(View.GONE);
            }
        });


    }

    private void updateUserInfoBtn() {
        binding.updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isThatCurrentUser) {
                    if (isNameChanged() || isEmailChanged() || isNickNameCahnged() || isTelephoneChanged()) {
                        if (!nameFromBase.equals(binding.nameInEdit.getText().toString())) {
                            viewModel.changeUserData(selectedUser, "name", binding.nameInEdit.getText().toString().trim());
                        }
                        if (!telephone.equals(binding.telephoneInEdit.getText().toString())) {
                            viewModel.changeUserData(selectedUser, "telephone", binding.telephoneInEdit.getText().toString().trim());
                        }
                        if (!nickFromBase.equals(binding.nicknameInEdit.getText().toString())) {
                            viewModel.changeUserData(selectedUser, "nick", binding.nicknameInEdit.getText().toString().trim());
                        }

                        if (!emailFromBase.equals(binding.emailInEdit.getText().toString())) {
                            Toast.makeText(requireContext(), "You can't change email there", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Data has been updated", Toast.LENGTH_LONG).show();
                        }

                    } else {
                        Toast.makeText(requireContext(), "Data is same and cant be updated", Toast.LENGTH_LONG).show();
                    }

                    viewModel.loadUserInfo(selectedUser);
                }
            }
        });
    }

    private boolean isTelephoneChanged() {
        return !telephone.equals(binding.telephoneInEdit.getText().toString());
    }

    private boolean isNickNameCahnged() {
        return !nickFromBase.equals(binding.nicknameInEdit.getText().toString());
    }

    private boolean isEmailChanged() {
        return !emailFromBase.equals(binding.emailInEdit.getText().toString());
    }

    private boolean isNameChanged() {
        return !nameFromBase.equals(binding.nameInEdit.getText().toString());
    }

    private void disableEditTextAndBtn() {
        //TODO: disable main line
        binding.nameInEdit.setEnabled(false);
        binding.emailInEdit.setEnabled(false);
        binding.nicknameInEdit.setEnabled(false);
        binding.telephoneInEdit.setEnabled(false);

        //TODO: hide functions
        binding.updateProfile.setVisibility(View.GONE);
    }

}