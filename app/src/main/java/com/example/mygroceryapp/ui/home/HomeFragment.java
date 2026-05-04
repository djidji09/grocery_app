package com.example.mygroceryapp.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mygroceryapp.R;
import com.example.mygroceryapp.activities.ViewAllMainActivity;
import com.example.mygroceryapp.adapters.HomeAdapter;
import com.example.mygroceryapp.adapters.PopularAdapters;
import com.example.mygroceryapp.adapters.RecommendedAdapter;
import com.example.mygroceryapp.models.HomeCategory;
import com.example.mygroceryapp.models.PopularModel;
import com.example.mygroceryapp.models.RecommendedModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    ScrollView scrollView;
    ProgressBar progressBar;

    RecyclerView popularRec, homeCatRec, recommendedRec;
    FirebaseFirestore db;

    //popular items
    List<PopularModel> popularModelList;
    PopularAdapters popularAdapters;
    //home category
    List<HomeCategory> categoryList;
    HomeAdapter homeAdapter;
    //recommended
    List<RecommendedModel> recommendedModelList;
    RecommendedAdapter recommendedAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseFirestore.getInstance();
        popularRec = root.findViewById(R.id.pop_rec);
        homeCatRec = root.findViewById(R.id.explore_rec);
        recommendedRec = root.findViewById(R.id.recommended_rec);
        scrollView = root.findViewById(R.id.scroll_view);
        progressBar = root.findViewById(R.id.progressbar);

        // --- Search bar wiring ---
        final EditText searchBox = root.findViewById(R.id.search_box);
        if (searchBox != null) {
            searchBox.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    launchSearch(searchBox.getText().toString());
                    return true;
                }
                return false;
            });
        }

        // --- "View All" links ---
        TextView viewAllPopular = root.findViewById(R.id.view_all_popular);
        TextView viewAllExplore = root.findViewById(R.id.view_all_explore);
        TextView viewAllRecommended = root.findViewById(R.id.view_all_recommended);
        if (viewAllPopular != null) viewAllPopular.setOnClickListener(v -> openAll(null));
        if (viewAllExplore != null) viewAllExplore.setOnClickListener(v -> openAll(null));
        if (viewAllRecommended != null) viewAllRecommended.setOnClickListener(v -> openAll(null));

        progressBar.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);


        //Popular items
        popularRec.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        popularModelList = new ArrayList<>();
        popularAdapters = new PopularAdapters(getActivity(), popularModelList);
        popularRec.setAdapter(popularAdapters);

        db.collection("PopularProducts")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                PopularModel popularModel = document.toObject(PopularModel.class);
                                popularModelList.add(popularModel);
                                popularAdapters.notifyDataSetChanged();

                                progressBar.setVisibility(View.GONE);
                                scrollView.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(getActivity(), "Error" + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
//home category
        homeCatRec.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        categoryList = new ArrayList<>();
        homeAdapter = new HomeAdapter(getActivity(), categoryList);
        homeCatRec.setAdapter(homeAdapter);


        db.collection("HomeCategory")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                HomeCategory homeCategory = document.toObject(HomeCategory.class);
                                categoryList.add(homeCategory);
                                homeAdapter.notifyDataSetChanged();
                            }
                        } else {
                            Toast.makeText(getActivity(), "Error" + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        recommendedRec.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        recommendedModelList = new ArrayList<>();
        recommendedAdapter = new RecommendedAdapter(getActivity(), recommendedModelList);
        recommendedRec.setAdapter(recommendedAdapter);


        db.collection("Recomended")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                RecommendedModel recommendedModel = document.toObject(RecommendedModel.class);
                                recommendedModelList.add(recommendedModel);
                                recommendedAdapter.notifyDataSetChanged();
                            }
                        } else {
                            Toast.makeText(getActivity(), "Error" + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        return root;
    }

    private void launchSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            Toast.makeText(getActivity(), "Type something to search", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getActivity(), ViewAllMainActivity.class);
        intent.putExtra(ViewAllMainActivity.EXTRA_SEARCH, query.trim());
        startActivity(intent);
    }

    private void openAll(String type) {
        Intent intent = new Intent(getActivity(), ViewAllMainActivity.class);
        if (type != null) intent.putExtra(ViewAllMainActivity.EXTRA_TYPE, type);
        startActivity(intent);
    }
}
