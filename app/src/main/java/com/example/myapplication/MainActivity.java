package com.example.myapplication;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentContainerView;

public class MainActivity extends AppCompatActivity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Sets listeners for button clicking. Updates the grid that's displayed based on which button is clicked
        NavigationFragment navigation = ((FragmentContainerView)this.findViewById(R.id.navigationFragment)).getFragment();
        this.findViewById(R.id.selectJobs).setOnClickListener(view -> navigation.updateDisplay('J'));
        this.findViewById(R.id.selectPeople).setOnClickListener(view -> navigation.updateDisplay('P'));

    }


}