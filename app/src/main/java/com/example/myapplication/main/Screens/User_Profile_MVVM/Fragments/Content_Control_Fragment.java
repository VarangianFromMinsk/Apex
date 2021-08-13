package com.example.myapplication.main.Screens.User_Profile_MVVM.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentContetnControlBinding;
import com.example.myapplication.main.Screens.User_Profile_MVVM.Profile_ViewModel;

public class Content_Control_Fragment extends Fragment {

    private String selectedUser;
    private boolean isThatCurrentUser;

    boolean isPost;
    boolean isSongs;

    private Profile_ViewModel viewModel;

    private contentController controllerInterface;
    private final Handler handler = new Handler();

    private FragmentContetnControlBinding binding;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            controllerInterface = (contentController) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "activity must implement interface");
        }
    }

    public static Content_Control_Fragment newInstance(String selectedUser, boolean isThatCurrentUser) {
        Content_Control_Fragment fragment = new Content_Control_Fragment();
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
                R.layout.fragment_contetn__control_,
                container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(Profile_ViewModel.class);

        checkCountOfYourPost();
        checkCountOfYourSongs();

        postElementClickAction();
        musicElementClickAction();
    }


    private void musicElementClickAction() {
        binding.musicCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable runnable;

                if (!isSongs) {
                    isSongs = true;

                    controllerInterface.hideBottomNavigation();

                    runnable = new Runnable() {
                        public void run() {
                            binding.profileCardPosts.setVisibility(View.GONE);
                        }
                    };

                } else {
                    isSongs = false;

                    if (isThatCurrentUser) {
                        controllerInterface.showBottomNavigation();
                    }

                    runnable = new Runnable() {
                        public void run() {
                            binding.profileCardPosts.setVisibility(View.VISIBLE);
                        }
                    };
                }

                int interval = 250;
                handler.postAtTime(runnable, System.currentTimeMillis() + interval);
                handler.postDelayed(runnable, interval);

                controllerInterface.mainSwitchContent(isPost, isSongs);
            }
        });
    }

    private void postElementClickAction() {
        binding.profileCardPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable runnable;

                if (!isPost) {
                    isPost = true;

                    controllerInterface.hideBottomNavigation();

                    runnable = new Runnable() {
                        public void run() {
                            binding.musicCardView.setVisibility(View.GONE);
                        }
                    };

                } else {
                    isPost = false;

                    if (isThatCurrentUser) {
                        controllerInterface.showBottomNavigation();
                    }

                    runnable = new Runnable() {
                        public void run() {
                            binding.musicCardView.setVisibility(View.VISIBLE);
                        }
                    };
                }

                int interval = 250;
                handler.postAtTime(runnable, System.currentTimeMillis() + interval);
                handler.postDelayed(runnable, interval);

                controllerInterface.mainSwitchContent(isPost, isSongs);
            }
        });
    }

    public interface contentController {

        void hideBottomNavigation();

        void showBottomNavigation();

        void mainSwitchContent(boolean isPost, boolean isMusic);
    }

    private void checkCountOfYourPost() {
        viewModel.loadCountOfPost(selectedUser);
        viewModel.getCountOfPosts().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                binding.countLeft.setText(s);
            }
        });
    }

    private void checkCountOfYourSongs() {
        viewModel.loadCountOfSongs(selectedUser);
        viewModel.getCountOfSongs().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                binding.countRight.setText(s);
            }
        });
    }

}