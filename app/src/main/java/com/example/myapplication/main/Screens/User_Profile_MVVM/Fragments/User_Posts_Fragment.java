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
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentUserPostsBinding;
import com.example.myapplication.main.Models.Model_Post;
import com.example.myapplication.main.Screens.Posts.Posts_By_Friends_MVVM.Post_Adapter_Friends;
import com.example.myapplication.main.Screens.User_Profile_MVVM.Profile_ViewModel;

import java.util.ArrayList;


public class User_Posts_Fragment extends Fragment {

    private RecyclerView recyclerViewPosts;
    private Post_Adapter_Friends postAdapter;

    private String selectedUser;

    private FragmentUserPostsBinding binding;
    private Profile_ViewModel viewModel;
    private PostsFragmentWasCommitted connectionInterface;


    // TODO: Rename and change types and number of parameters
    public static User_Posts_Fragment newInstance(String selectedUser) {
        User_Posts_Fragment fragment = new User_Posts_Fragment();
        Bundle args = new Bundle();
        args.putString("selectedUser",selectedUser);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            connectionInterface = (PostsFragmentWasCommitted) context;
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
                R.layout.fragment_user__posts_,
                container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerViewPosts = view.findViewById(R.id.postRecyclerViewUserProfile);
        viewModel = new ViewModelProvider(getActivity()).get(Profile_ViewModel.class);
        loadPosts();
    }

    public interface PostsFragmentWasCommitted{
        void PostsFragmentWorks();
    }

    private void loadPosts() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        recyclerViewPosts.setLayoutManager(layoutManager);

        postAdapter = new Post_Adapter_Friends(requireActivity());
        postAdapter.setPostList(new ArrayList<Model_Post>());
        recyclerViewPosts.setAdapter(postAdapter);

        //viewModel.loadPosts(selectedUser);

        viewModel.getMutCurrentUserPostList().observe(getViewLifecycleOwner(), new Observer<ArrayList<Model_Post>>() {
            @Override
            public void onChanged(ArrayList<Model_Post> posts) {
                   binding.progressBarInPostFragment.setVisibility(View.GONE);
                if (posts.size() == 0) {
                      binding.noPostsTv.setVisibility(View.VISIBLE);
                } else {
                    binding.noPostsTv.setVisibility(View.GONE);
                    postAdapter.setPostList(posts);
                }
            }
        });
    }
}