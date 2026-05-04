package com.example.mygroceryapp.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mygroceryapp.R;
import com.example.mygroceryapp.adapters.ViewAllAdapter;
import com.example.mygroceryapp.models.ViewAllModel;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ViewAllMainActivity extends AppCompatActivity {

    public static final String EXTRA_TYPE = "type";
    public static final String EXTRA_SEARCH = "search";

    FirebaseFirestore firestore;
    RecyclerView recyclerView;
    ViewAllAdapter viewAllAdapter;
    List<ViewAllModel> allItems;
    List<ViewAllModel> visibleItems;
    Toolbar toolbar;
    EditText searchEdit;
    TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_all_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        firestore = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.view_all_rec);
        searchEdit = findViewById(R.id.search_edit);
        emptyView = findViewById(R.id.empty_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        allItems = new ArrayList<>();
        visibleItems = new ArrayList<>();
        viewAllAdapter = new ViewAllAdapter(this, visibleItems);
        recyclerView.setAdapter(viewAllAdapter);

        String type = getIntent().getStringExtra(EXTRA_TYPE);
        String initialSearch = getIntent().getStringExtra(EXTRA_SEARCH);

        if (type != null && !type.isEmpty()) {
            String label = Character.toUpperCase(type.charAt(0)) + type.substring(1);
            if (getSupportActionBar() != null) getSupportActionBar().setTitle(label);
        } else if (initialSearch != null && !initialSearch.isEmpty()) {
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Search");
            searchEdit.setText(initialSearch);
        }

        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter(s == null ? "" : s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadProducts(type);
    }

    private void loadProducts(final String type) {
        if (type == null || type.isEmpty()) {
            firestore.collection("AllProducts").get()
                    .addOnCompleteListener(this::handleResult);
            return;
        }

        final String t = type.toLowerCase(Locale.ROOT).trim();
        firestore.collection("AllProducts")
                .whereEqualTo("type", t)
                .get()
                .addOnCompleteListener(task -> {
                    handleResult(task);
                    if (task.isSuccessful() && allItems.isEmpty()) {
                        String fallback = pluralFallback(t);
                        if (fallback != null) {
                            firestore.collection("AllProducts")
                                    .whereEqualTo("type", fallback)
                                    .get()
                                    .addOnCompleteListener(this::handleResult);
                        }
                    }
                });
    }

    private String pluralFallback(String t) {
        switch (t) {
            case "vegetable": return "vegetables";
            case "vegetables": return "vegetable";
            case "fruit": return "fruits";
            case "fruits": return "fruit";
            case "egg": return "eggs";
            case "eggs": return "egg";
            case "fish": return "fishes";
            default: return null;
        }
    }

    private void handleResult(@NonNull Task<QuerySnapshot> task) {
        if (!task.isSuccessful() || task.getResult() == null) {
            applyFilter(searchEdit.getText().toString());
            return;
        }
        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
            ViewAllModel m = doc.toObject(ViewAllModel.class);
            if (m != null) allItems.add(m);
        }
        applyFilter(searchEdit.getText().toString());
    }

    private void applyFilter(String query) {
        visibleItems.clear();
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) {
            visibleItems.addAll(allItems);
        } else {
            for (ViewAllModel m : allItems) {
                String name = m.getName() == null ? "" : m.getName().toLowerCase(Locale.ROOT);
                String desc = m.getDescription() == null ? "" : m.getDescription().toLowerCase(Locale.ROOT);
                String type = m.getType() == null ? "" : m.getType().toLowerCase(Locale.ROOT);
                if (name.contains(q) || desc.contains(q) || type.contains(q)) {
                    visibleItems.add(m);
                }
            }
        }
        viewAllAdapter.notifyDataSetChanged();
        emptyView.setVisibility(visibleItems.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
