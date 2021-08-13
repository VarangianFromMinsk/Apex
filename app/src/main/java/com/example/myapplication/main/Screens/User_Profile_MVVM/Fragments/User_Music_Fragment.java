package com.example.myapplication.main.Screens.User_Profile_MVVM.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentUserMusicBinding;
import com.example.myapplication.main.Models.Model_Song;
import com.example.myapplication.main.Screens.Music.Music_List_Activity_MVVM.Music_Adapter;
import com.example.myapplication.main.Screens.User_Profile_MVVM.Profile_ViewModel;

import java.util.ArrayList;


public class User_Music_Fragment extends Fragment {

    private Music_Adapter songAdapter;

    private String selectedUser;

    private FragmentUserMusicBinding binding;
    private Profile_ViewModel viewModel;
    private musicFragmentWasCommitted connectionInterface;

    // TODO: Rename and change types and number of parameters
    public static User_Music_Fragment newInstance(String selectedUser) {
        User_Music_Fragment fragment = new User_Music_Fragment();
        Bundle args = new Bundle();
        args.putString("selectedUser",selectedUser);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            connectionInterface = (musicFragmentWasCommitted) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "activity must implement interface");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedUser = getArguments().getString("selectedUser");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_user__music_,
                container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(getActivity()).get(Profile_ViewModel.class);
        loadSongs();
        connectionInterface.musicFragmentWorks();
    }

    public interface musicFragmentWasCommitted{
        void musicFragmentWorks();
    }

    private void loadSongs() {
        LinearLayoutManager layoutManagerSongs = new LinearLayoutManager(requireActivity());
        binding.songsRecyclerViewUserProfile.setLayoutManager(layoutManagerSongs);

        songAdapter = new Music_Adapter(requireActivity());
        songAdapter.setMusicList(new ArrayList<Model_Song>());
        binding.songsRecyclerViewUserProfile.setAdapter(songAdapter);

        viewModel.getMutCurrentUserMusic().observe(getViewLifecycleOwner(), new Observer<ArrayList<Model_Song>>() {
            @Override
            public void onChanged(ArrayList<Model_Song> songs) {
                   binding.progressBarInMusicFragment.setVisibility(View.GONE);
                if (songs.size() == 0) {
                    binding.noSongsTv.setVisibility(View.VISIBLE);
                } else {
                    binding.noSongsTv.setVisibility(View.GONE);
                    songAdapter.setMusicList(songs);
                }
            }
        });
    }


}