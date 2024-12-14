package com.example.myapplication;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;

public class NavigationFragment extends Fragment {
    private DatabaseHelper data;
    private char currentTable = 'P';
    private LinearLayout colNames, records;
    private final int padding = 30;
    private Spinner sortingSpinner;
    private Button applySelection, add;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.navigation_fragment, container, false);
        this.sortingSpinner = view.findViewById(R.id.columnsSelection);
        this.data = new DatabaseHelper(this.getContext());
        this.records = view.findViewById(R.id.records);
        this.colNames = view.findViewById(R.id.names);
        this.applySelection = view.findViewById(R.id.applySelection);

        this.add = new Button(this.getContext());
        this.add.setText("Add");
        this.add.setLayoutParams(new ViewGroup.LayoutParams(300, 90));
        this.updateDisplay('J');
        return view;
    }

    public void updateDisplay(char switchTo){
        if(switchTo != this.currentTable){//Make sure that the table requested is different than the current to save time and resources
            this.currentTable = switchTo;
            this.add.setEnabled(true);
            if(switchTo == 'J'){
                Cursor jobNames = this.data.getAllJobs("None");
                while(jobNames.moveToNext()){Log.i("Debugging", "help");}
                this.createColumnsFromCursor(jobNames);
                ArrayList<Button> editButtons = new ArrayList<>(), deleteButtons = new ArrayList<>(); //To communicate disabling while editing
                this.refreshJobRecords(jobNames, editButtons, deleteButtons);
                this.add.setOnClickListener(view -> this.addNewJobRow("Example", this.data.addJob("Example", 100, 100), 100, 100, editButtons, deleteButtons));
                this.applySelection.setOnClickListener(clickView -> {
                    Cursor jobs = this.data.getAllJobs(this.sortingSpinner.getSelectedItem().toString());
                    this.refreshJobRecords(jobs, editButtons, deleteButtons);
                    jobs.close();
                });
                jobNames.close();
            }else if(switchTo == 'P'){
                Cursor peopleNames = this.data.getAllEmployees("None");
                this.createColumnsFromCursor(peopleNames);
                ArrayList<Button> editButtons = new ArrayList<>(), deleteButtons = new ArrayList<>(); //To communicate disabling while editing
                this.refreshEmployeeRecords(peopleNames, editButtons, deleteButtons);
                int job = this.data.getAllJobIds()[0];
                this.add.setOnClickListener(view -> this.addNewEmployeeRow("Example", "March 28", this.data.addEmployee("Example", "March 28", String.valueOf(job)),  job, editButtons, deleteButtons));
                this.applySelection.setOnClickListener(clickView ->{
                    Cursor people = this.data.getAllEmployees(this.sortingSpinner.getSelectedItem().toString());
                    this.refreshEmployeeRecords(people, editButtons, deleteButtons);
                    people.close();
                });
                peopleNames.close();
            }else{throw new IllegalArgumentException("Invalid table to switch to");}
        }
    }

    @SuppressLint("Range") private void refreshEmployeeRecords(Cursor employees, ArrayList<Button> editButtons, ArrayList<Button> deleteButtons){
        if(this.currentTable != 'P'){throw new IllegalStateException("The current table isn't employees");}
        this.records.removeAllViews();
        editButtons.clear();
        deleteButtons.clear();
        employees.moveToFirst();
        employees.moveToPrevious();
        while(employees.moveToNext()){//Fill in the data for the table
            this.addNewEmployeeRow(
                    employees.getString(employees.getColumnIndex("Name"))
                    , employees.getString(employees.getColumnIndex("Date of Birth"))
                    , employees.getInt(employees.getColumnIndex("ID"))
                    , employees.getInt(employees.getColumnIndex("Job ID"))
                    , editButtons
                    , deleteButtons
            );
        }
    }


    @SuppressLint("Range") private void refreshJobRecords(Cursor jobs, ArrayList<Button> editButtons, ArrayList<Button> deleteButtons){
        if(this.currentTable != 'J'){throw new IllegalStateException("The current table isn't jobs");}
        this.records.removeAllViews();
        editButtons.clear();
        deleteButtons.clear();
        jobs.moveToFirst();
        jobs.moveToPrevious();
        while(jobs.moveToNext()){//Fill in the data for the table
            Log.i("Debugging", "help");
            this.addNewJobRow(
                    jobs.getString(jobs.getColumnIndex("Name"))
                    , jobs.getInt(jobs.getColumnIndex("ID"))
                    , jobs.getInt(jobs.getColumnIndex("Salary"))
                    , jobs.getInt(jobs.getColumnIndex("Hours a Week"))
                    , editButtons
                    , deleteButtons
            );
        }
    }

    private void createColumnsFromCursor(Cursor cursor){
        this.colNames.removeAllViews();
        this.colNames.addView(this.add);
        cursor.moveToFirst();
        cursor.moveToPrevious();
        ArrayList<String> columns = new ArrayList<>();
        columns.add("None");
        columns.addAll(Arrays.asList(cursor.getColumnNames()));
        this.sortingSpinner.setAdapter(new ArrayAdapter<>(this.requireContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, columns));
        for(String i : cursor.getColumnNames()){//Create the column names
            TextView txt = new TextView(this.getContext());
            txt.setText(i);
            txt.setPadding(this.padding, 10, this.padding, 10);
            txt.setLayoutParams(new ViewGroup.LayoutParams(300, 90));
            txt.setGravity(Gravity.CENTER);
            GradientDrawable border = new GradientDrawable();
            border.setStroke(10, Color.BLACK);
            txt.setBackground(border);
            Space space = new Space(this.getContext());
            int hMargin = 20;
            space.setLayoutParams(new ViewGroup.LayoutParams(hMargin, 10));
            this.colNames.addView(txt);
            this.colNames.addView(space);
        }
    }

    private void addNewEmployeeRow(String name, String dob, int id, int jobID, ArrayList<Button> editButtons, ArrayList<Button> deleteButtons){
        if(this.currentTable != 'P'){throw new IllegalStateException("Table is not set to Employees");}
        LinearLayout row = new LinearLayout(this.getContext());
        row.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Space rowSpace = new Space(this.getContext()); //Margins
        rowSpace.setLayoutParams(new ViewGroup.LayoutParams(30, 30));
        Button delete = new Button(this.getContext()), edit = new Button(this.getContext());
        editButtons.add(edit);
        deleteButtons.add(delete);
        {
            ViewGroup.LayoutParams spaceParams = new ViewGroup.LayoutParams(15, 10);
            Space space = new Space(this.getContext()), space1 = new Space(this.getContext()), space2 = new Space(this.getContext());
            space.setLayoutParams(spaceParams);
            space1.setLayoutParams(spaceParams);
            space2.setLayoutParams(spaceParams);

            delete.setText("Del");
            edit.setText("Edit");
            delete.setOnClickListener(view -> {
                this.data.deleteEmployeeWithID(id);
                this.records.removeView(row);
                this.records.removeView(rowSpace);
                editButtons.remove(edit);
                deleteButtons.remove(delete);
            });
            ViewGroup.LayoutParams layout = new ViewGroup.LayoutParams(130, 70);
            delete.setLayoutParams(layout);
            edit.setLayoutParams(layout);
            row.addView(space);
            row.addView(delete);
            row.addView(space1);
            row.addView(edit);
            row.addView(space2);
        }

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(300, 70);
        GradientDrawable border = new GradientDrawable();
        border.setStroke(10, Color.BLACK);

        EditText nameEdit = new EditText(this.getContext());
        nameEdit.setEnabled(false);
        nameEdit.setText(name);
        nameEdit.setLayoutParams(params);
        nameEdit.setPadding(this.padding, 10, this.padding, 10);
        nameEdit.setGravity(Gravity.CENTER);
        nameEdit.setBackground(border);


        EditText datePicker = new EditText(this.getContext());
        datePicker.setEnabled(false);
        datePicker.setText(dob);
        datePicker.setLayoutParams(params);
        datePicker.setPadding(this.padding, 10, this.padding, 10);
        datePicker.setGravity(Gravity.CENTER);
        datePicker.setBackground(border);
        TextView idLabel = new TextView(this.getContext());
        idLabel.setText(String.valueOf(id));
        idLabel.setLayoutParams(params);
        idLabel.setPadding(this.padding, 10, this.padding, 10);
        idLabel.setGravity(Gravity.CENTER);
        idLabel.setBackground(border);

        EditText jobChoices = new EditText(this.getContext());
        jobChoices.setText(String.valueOf(jobID));
        jobChoices.setEnabled(false);
        jobChoices.setLayoutParams(params);
        jobChoices.setPadding(this.padding, 10, this.padding, 10);
        jobChoices.setGravity(Gravity.CENTER);
        jobChoices.setBackground(border);

        boolean[] isEditing = new boolean[1];
        edit.setOnClickListener(view ->{//Enables editing for jobs
            isEditing[0] = !isEditing[0];
            nameEdit.setEnabled(isEditing[0]);
            datePicker.setEnabled(isEditing[0]);
            jobChoices.setEnabled(isEditing[0]);
            this.add.setEnabled(!isEditing[0]);
            for(Button i : editButtons){if(i != edit){i.setEnabled(!isEditing[0]);}}
            for(Button i : deleteButtons){i.setEnabled(!isEditing[0]);}
            if(isEditing[0]){edit.setText("Done");}
            else{
                edit.setText("Edit");
                try{this.data.editEmployee(id, nameEdit.getText().toString(), "March 28", String.valueOf(jobChoices.getText().toString()));}
                catch(IllegalArgumentException e){
                    nameEdit.setText(this.data.getIDNamePairsForJobs().get(id));
                    Cursor val = this.data.getReadableDatabase().rawQuery("SELECT \"Job ID\" FROM Employees WHERE \"ID\" = \"" + id + "\";", null);
                    if(val.moveToNext()){jobChoices.setText(String.valueOf(val.getInt(0)));}
                    val.close();
                }
            }
            edit.setEnabled(true);
        });

        View[] views = new View[]{nameEdit, datePicker, idLabel, jobChoices};
        for(View i : views){
            row.addView(i);
            Space space1 = new Space(this.getContext());
            int hMargin = 20;
            space1.setLayoutParams(new ViewGroup.LayoutParams(hMargin, 10));
            row.addView(space1);
        }
        this.records.addView(row);
        this.records.addView(rowSpace);
    }

    private void addNewJobRow(String name, int id, int salary, int hours, ArrayList<Button> editButtons, ArrayList<Button> deleteButtons){
        if(this.currentTable != 'J'){throw new IllegalStateException("Table is not set to Jobs");}
        LinearLayout row = new LinearLayout(this.getContext());
        row.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Space rowSpace = new Space(this.getContext()); //Margins
        rowSpace.setLayoutParams(new ViewGroup.LayoutParams(30, 30));
        Button delete = new Button(this.getContext()), edit = new Button(this.getContext());
        editButtons.add(edit);
        deleteButtons.add(delete);
        {// Create the edit and delete buttons here
            ViewGroup.LayoutParams spaceParams = new ViewGroup.LayoutParams(15, 10);
            Space space = new Space(this.getContext()), space1 = new Space(this.getContext()), space2 = new Space(this.getContext());
            space.setLayoutParams(spaceParams);
            space1.setLayoutParams(spaceParams);
            space2.setLayoutParams(spaceParams);

            delete.setText("Kill");
            edit.setText("Edit");
            delete.setOnClickListener(view -> {
                this.data.deleteJobWithID(id);
                this.records.removeView(row);
                this.records.removeView(rowSpace);
                editButtons.remove(edit);
                deleteButtons.remove(delete);
            });
            ViewGroup.LayoutParams layout = new ViewGroup.LayoutParams(130, 70);
            delete.setLayoutParams(layout);
            edit.setLayoutParams(layout);
            row.addView(space);
            row.addView(delete);
            row.addView(space1);
            row.addView(edit);
            row.addView(space2);
        }
        {//Initialize all view data here
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(300, 70);
            GradientDrawable border = new GradientDrawable();
            border.setStroke(10, Color.BLACK);

            EditText nameEdit = new EditText(this.getContext());
            nameEdit.setEnabled(false);
            nameEdit.setText(name);
            nameEdit.setLayoutParams(params);
            nameEdit.setPadding(this.padding, 10, this.padding, 10);
            nameEdit.setGravity(Gravity.CENTER);
            nameEdit.setBackground(border);

            TextView idLabel = new TextView(this.getContext());
            idLabel.setText(String.valueOf(id));
            idLabel.setLayoutParams(params);
            idLabel.setPadding(this.padding, 10, this.padding, 10);
            idLabel.setGravity(Gravity.CENTER);
            idLabel.setBackground(border);

            NumberPicker salaryLabel = new NumberPicker(this.getContext());
            salaryLabel.setEnabled(false);
            salaryLabel.setMinValue(0);
            salaryLabel.setMaxValue(100000);
            salaryLabel.setValue(salary);
            salaryLabel.setLayoutParams(params);
            salaryLabel.setPadding(padding, 10, padding, 10);
            salaryLabel.setGravity(Gravity.CENTER);
            salaryLabel.setBackground(border);

            NumberPicker hoursLabel = new NumberPicker(this.getContext());
            hoursLabel.setEnabled(false);
            hoursLabel.setMinValue(0);
            hoursLabel.setMaxValue(168);
            hoursLabel.setValue(hours);
            hoursLabel.setLayoutParams(params);
            hoursLabel.setPadding(padding, 10, padding, 10);
            hoursLabel.setGravity(Gravity.CENTER);
            hoursLabel.setBackground(border);

            boolean[] isEditing = new boolean[1];
            edit.setOnClickListener(view ->{//Enables editing for jobs
                isEditing[0] = !isEditing[0];
                nameEdit.setEnabled(isEditing[0]);
                salaryLabel.setEnabled(isEditing[0]);
                hoursLabel.setEnabled(isEditing[0]);
                this.add.setEnabled(!isEditing[0]);
                for(Button i : editButtons){if(i != edit){i.setEnabled(!isEditing[0]);}}
                for(Button i : deleteButtons){i.setEnabled(!isEditing[0]);}
                if(isEditing[0]){edit.setText("Done");}
                else{
                    edit.setText("Edit");
                    try{this.data.editJob(id, nameEdit.getText().toString(), salaryLabel.getValue(), hoursLabel.getValue());}
                    catch(IllegalArgumentException e){nameEdit.setText(this.data.getIDNamePairsForJobs().get(id));}
                }
                edit.setEnabled(true);
            });

            View[] views = new View[]{nameEdit, idLabel, salaryLabel, hoursLabel};
            for(View i : views){
                row.addView(i);
                Space space1 = new Space(this.getContext());
                int hMargin = 20;
                space1.setLayoutParams(new ViewGroup.LayoutParams(hMargin, 10));
                row.addView(space1);
            }
        }
        this.records.addView(row);
        this.records.addView(rowSpace);
    }

}
