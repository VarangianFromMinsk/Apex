package com.example.myapplication.main.Screens.User_Profile_MVVM.Photo_From_Gallery;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.myapplication.main.Screens.User_Profile_MVVM.Profile_ViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ItemListDialogFragment extends BottomSheetDialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private Button back, ready;
    private TextView currentNumber, galleryNumber;

    private boolean oneOrManyImagesForSelect;
    private boolean selectedOneCheck;

    private ArrayList<String> currentImages = new ArrayList<>();
    private ArrayList<String> allImages = new ArrayList<>();
    private int start = 0;
    private int end;

    private Profile_ViewModel viewModel;
    private ItemAdapter itemAdapter;

    private ArrayList<String> imagesToLoad = new ArrayList<>();
    private ActionFromGallery galleryInterface;
    private loadImageFromGallerySelecter loadImageInterface;


    public interface loadImageFromGallerySelecter{
        void loadImage(ArrayList<String> imagesToLoad);
    }


    public static ItemListDialogFragment newInstance(boolean oneOrManyImagesForSelect) {
        final ItemListDialogFragment fragment = new ItemListDialogFragment();
        final Bundle args = new Bundle();
        args.putBoolean("oneOrManyImagesForSelect", oneOrManyImagesForSelect);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            galleryInterface = (ActionFromGallery) context;
            loadImageInterface = (loadImageFromGallerySelecter) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "activity must implement interface");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            oneOrManyImagesForSelect = getArguments().getBoolean("oneOrManyImagesForSelect");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_gallery_images, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        initializing(view);
        loadImages();
        createRecycler(view);

        checkFullPickOrOneImage();
        readyAction();
        closeAction();

        checkCurrentNumberOfSelectedImages();

    }

    private void initializing(View view) {

        viewModel = new ViewModelProvider(requireActivity()).get(Profile_ViewModel.class);

        back = view.findViewById(R.id.backBtn);
        ready = view.findViewById(R.id.readyToUpload);
        currentNumber = view.findViewById(R.id.currentCountOfImages);
        galleryNumber = view.findViewById(R.id.galleryNumber);
    }

    private void loadImages() {
        LoaderManager.getInstance(this).initLoader(1, null, this);
    }

    // todo: loader
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{MediaStore.MediaColumns.DATA, MediaStore.Images.Media.DEFAULT_SORT_ORDER};
        String selection = null;     //Selection criteria
        String[] selectionArgs = new String[0];  //Selection criteria
        String sortOrder = null;
        return new CursorLoader(requireContext(),
                uri,
                projection,
                selection,
                selectionArgs,
                sortOrder);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

        while (data.moveToNext()) {
            int columnIndexData = data.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            //todo : limit
            while (data.moveToNext() && allImages.size() < 200) {
                allImages.add(data.getString(columnIndexData));
            }
        }


            //todo: first load
            if(allImages.size() > 50){
                end = 50;
            }
            else if(allImages.size() > 30){
                end = 30;
            }
            else if(allImages.size() > 20){
                end = 20;
            }
            else if(allImages.size() > 10){
                end = 10;
            }
            else if(allImages.size() > 2){
                end = 2;
            }

            if(allImages.size() > 2){
                List<String> get = allImages.subList(start, end);
                currentImages.addAll(get);

                itemAdapter.setImages(currentImages);

                if (oneOrManyImagesForSelect) {
                    galleryNumber.setText(String.valueOf("Photos (" + allImages.size() + ")"));
                }
            }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    private void createRecycler(View view) {
        final RecyclerView recyclerView = view.findViewById(R.id.recyclerSelectImageFromGallery);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        itemAdapter = new ItemAdapter(recyclerView, requireActivity(), new Gallery_Image_Click_Interface() {
            @Override
            public void onPhotoClick(String path, boolean AddOrNO) {
                if (AddOrNO) {
                    if (!oneOrManyImagesForSelect) {
                        Toast.makeText(requireContext(), "Image has been selected", Toast.LENGTH_SHORT).show();
                        selectedOneCheck = true;
                    } else {
                        selectedOneCheck = false;
                        imagesToLoad.add(path);
                    }
                } else {
                    try {
                        imagesToLoad.remove(path);
                        viewModel.changeCurrentNumber(imagesToLoad.size());
                    } catch (Exception ignored) {
                    }
                }
            }
        });

        recyclerView.setAdapter(itemAdapter);
        itemAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (currentImages.size() <= end) {
                    currentImages.add(null);
                    itemAdapter.notifyItemInserted(currentImages.size() - 1);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            currentImages.remove(currentImages.size() - 1);
                            itemAdapter.notifyItemRemoved(currentImages.size());

                            if (allImages.size() > end + 50) {
                                //todo: another load
                                start = start + 50;
                                end = end + 50;

                                List<String> get = allImages.subList(start, end);
                                currentImages.addAll(get);

                                itemAdapter.setImages(currentImages);
                                itemAdapter.setLoaded();
                            }

                        }
                    }, 5000);
                } else {
                    //Toast.makeText(requireActivity(), "Loading data completed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkFullPickOrOneImage() {
        if (!oneOrManyImagesForSelect) {
            galleryNumber.setText(R.string.select_one);
            currentNumber.setVisibility(View.GONE);
        }
    }

    private void readyAction() {
        ready.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> loadImage = itemAdapter.getSelectedImages();
                if(!loadImage.isEmpty()){
                    loadImageInterface.loadImage(loadImage);
                    Log.d("image", String.valueOf(loadImage.size()));
                }
                else{
                    Toast.makeText(requireContext(), "Select one before load", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void closeAction() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryInterface.hideAction();
            }
        });
    }

    private void checkCurrentNumberOfSelectedImages() {
        viewModel.getCurrentNumberOfSelectedImages().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                currentNumber.setText(String.valueOf(integer));
            }
        });

    }


    //TODO: adapter part
    private static class ViewHolderMain extends RecyclerView.ViewHolder {

        final ImageView image;
        final View back, backCount;
        final TextView count;

        public ViewHolderMain(View view) {
            super(view);
            image = view.findViewById(R.id.galleryImage);
            back = view.findViewById(R.id.back);
            backCount = view.findViewById(R.id.backCount);
            count = view.findViewById(R.id.IndividualCount);
        }

    }

    private static class LoadingViewHolder extends RecyclerView.ViewHolder {

        public ProgressBar progressBar;

        public LoadingViewHolder(View view) {
            super(view);
            progressBar = view.findViewById(R.id.progressBarGalleryImages);
        }

    }

    private class ItemAdapter extends RecyclerView.Adapter {

        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;
        private OnLoadMoreListener onLoadMoreListener;

        private boolean isLoading;
        private final Activity activity;
        private final int visibleThreshold = 5;
        private int lastVisibleItem, totalItemCount;


        private ArrayList<String> images = new ArrayList<>();
        private final Gallery_Image_Click_Interface clickInterface;

        private ArrayList<String> selectedImages = new ArrayList<>();
        private int commonCount = 0;

        public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
            this.onLoadMoreListener = mOnLoadMoreListener;
        }

        public ArrayList<String> getSelectedImages() {
            return selectedImages;
        }

        public void setImages(ArrayList<String> images) {
            this.images = images;
            notifyDataSetChanged();
        }

        public ItemAdapter(RecyclerView recyclerView, Activity activity, Gallery_Image_Click_Interface clickInterface) {
            this.activity = activity;
            this.clickInterface = clickInterface;

            final GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    assert gridLayoutManager != null;
                    totalItemCount = gridLayoutManager.getItemCount();
                    lastVisibleItem = gridLayoutManager.findLastVisibleItemPosition();

                    if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        if (onLoadMoreListener != null) {
                            onLoadMoreListener.onLoadMore();
                        }
                        isLoading = true;
                    }
                }
            });
        }

        @Override
        public int getItemViewType(int position) {
            return images.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View view = LayoutInflater.from(activity).inflate(R.layout.row_imagecard_from_gallery, parent, false);
                return new ViewHolderMain(view);
            } else if (viewType == VIEW_TYPE_LOADING) {
                View view = LayoutInflater.from(activity).inflate(R.layout.row_loadmore_image_from_gallery, parent, false);
                return new LoadingViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, int position) {

            if (holder instanceof ViewHolderMain) {

                final boolean[] isSelected = {false};

                String image = images.get(position);
                ViewHolderMain viewHolderMain = (ViewHolderMain) holder;

                RequestOptions myOptions = new RequestOptions()
                        .optionalCenterCrop()
                        .override(200, 200);

                Glide.with(requireContext()).applyDefaultRequestOptions(myOptions).load(image).into(viewHolderMain.image);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!isSelected[0]) {
                            if (!selectedOneCheck) {
                                selectedImages.add(image);

                                clickInterface.onPhotoClick(image, true);

                                viewHolderMain.back.setVisibility(View.VISIBLE);
                                viewHolderMain.backCount.setVisibility(View.VISIBLE);
                                viewHolderMain.count.setVisibility(View.VISIBLE);

                                isSelected[0] = true;
                                viewHolderMain.count.setText(String.valueOf(selectedImages.size()));
                            }
                        } else {
                            selectedImages.remove(image);

                            clickInterface.onPhotoClick(image, false);

                            viewHolderMain.back.setVisibility(View.GONE);
                            viewHolderMain.backCount.setVisibility(View.GONE);
                            viewHolderMain.count.setVisibility(View.GONE);

                            isSelected[0] = false;
                            viewHolderMain.count.setText(String.valueOf(selectedImages.size()));

                            selectedOneCheck = false;
                        }
                    }
                });
            } else if (holder instanceof LoadingViewHolder) {
                LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
                loadingViewHolder.progressBar.setIndeterminate(true);
            }

        }

        @Override
        public int getItemCount() {
            return images == null ? 0 : images.size();
        }

        public void setLoaded() {
            isLoading = false;
        }

    }

}