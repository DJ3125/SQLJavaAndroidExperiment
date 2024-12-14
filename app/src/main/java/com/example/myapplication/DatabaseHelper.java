package com.example.myapplication;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;


public final class DatabaseHelper extends SQLiteOpenHelper {


    public DatabaseHelper(Context context){
        super(context, context.getPackageName(), null, 1);
    }


    /**
    On creation of the database, create tables Jobs and Employees
        Employees have
            -Name (string)
            -Date of Birth (date (year month date))
            -Id (unique integer)
            -Job ID (unique integer relating to the job table)

       Jobs have
            - Name (string)
            - ID (unique integer)
            - Salary (integer)
            - Hours a week (integer less than 7days/week * 24 hours/day = 168 hours/week)
     */
    @Override public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                "CREATE TABLE Jobs(" +
                    "\"Name\" TEXT NOT NULL" +
                    ", \"ID\" INT NOT NULL" +
                    ", \"Salary\" SMALLINT NOT NULL" +
                    ", \"Hours a Week\" SMALLINT NOT NULL" +
                    ", PRIMARY KEY (\"ID\") " +
                    ", CHECK (\"Hours a Week\" <= 168)" +
                ");"
        );
        sqLiteDatabase.execSQL(
                "CREATE TABLE Employees (" +
                    "\"Name\" TEXT NOT NULL" +
                    ", \"Date of Birth\" TEXT NOT NULL" +
                    ", \"ID\" SMALLINT NOT NULL" +
                    ", \"Job ID\" SMALLINT NOT NULL" +
                    ", UNIQUE(\"ID\")" +
                    ", FOREIGN KEY(\"Job ID\") REFERENCES Jobs(\"ID\")" +
                ");"
        );
    }

    /**
     * Takes the specified parameters an inserts them as a new row in the Employees table
     *
     * @param name (Name of the employee as a string)
     * @param dob (Date of Birth of the employee as a local date object)
     * @param jobID (The job id)
     * @throws IllegalArgumentException (If the date is invalid, the job id is invalid, or the string is an injection)
     */
    public int addEmployee(@NotNull String name, @NotNull String dob, String jobID) throws IllegalArgumentException{
        //Checks if all the characters are alphabetic to prevent sql injection
        for (char character : name.toCharArray()) {if (!Character.isAlphabetic(character)) {throw new IllegalArgumentException("Invalid Characters");}}

        //Checks if the date is after the current date
//        if(dob.isAfter(LocalDate.now())){throw new IllegalArgumentException("Invalid Date");}

        {//Checks if this is a valid job ID
            int intJobID = Integer.parseInt(jobID);
            Cursor allJobIDs = this.getWritableDatabase().rawQuery("SELECT DISTINCT \"ID\" FROM Jobs;", null);
            boolean found = false;
            while(allJobIDs.moveToNext() && !found){if(allJobIDs.getShort(0) == intJobID){found = true;}}
            allJobIDs.close();
            if(!found){throw new IllegalArgumentException("Invalid Job ID");}
        }

        Cursor cursor = this.getReadableDatabase().rawQuery("SELECT \"ID\" FROM Employees ORDER BY \"ID\" ASC;", null);
        int lastNum = 1, index = cursor.getColumnIndex("ID");
        if(index < 0){throw new IllegalArgumentException("Why is ID less than 0?");}
        while(cursor.moveToNext() && cursor.getInt(index) == lastNum){lastNum++;}
        cursor.close();


        //Adds a new row into the employees table
        this.getWritableDatabase().execSQL(
                "INSERT INTO Employees (" +
                        "\"Name\"" +
                        ", \"Date of Birth\"" +
                        ", \"ID\"" +
                        ", \"Job ID\"" +
                ")" +
                "VALUES(" +
                        "\"" + name + "\"" +
                        ",\"" + dob + "\"" +
                        ",\"" + lastNum + "\"" +
                        ",\"" + jobID + "\"" +
                ");"
        );
        return lastNum;
    }


    /**
     * @param choice (String that represents which category to sort by)
     * Gets all the employees from the database and returns all the data as a cursor
     **/
    public Cursor getAllEmployees(String choice){
        if("None".equals(choice)){return this.getReadableDatabase().rawQuery("SELECT * FROM Employees;", null);}
        else{return this.getReadableDatabase().rawQuery("SELECT * FROM Employees ORDER BY \"" + choice +"\";", null);}
    }

    /**
     * Takes the specified parameters an inserts them as a new row in the Jobs table
     *
     * @param name (name of the job as a string)
     * @param salary (salary of the job as a positive int)
     * @param hours (the hours in a week of work)
     * @throws IllegalArgumentException (If the name is an injection, the salary is negative, or the hours negative or is greater than 168)
     * @return The ID of the new job as an int
     * */
    public int addJob(@NotNull String name, int salary, int hours) throws IllegalArgumentException{
        //Checks if all the characters are alphabetic to prevent sql injection
        for (char character : name.toCharArray()) {if (!Character.isAlphabetic(character)) {throw new IllegalArgumentException("Invalid Characters");}}

        //Checks if salary is negative
        if(salary < 0){throw new IllegalArgumentException("Invalid Salary");}

        //Checks if hours is negative or greater than 168
        if(hours < 0 || hours > 168){throw new IllegalArgumentException("Invalid hours");}

        //Tries to select a unique ID that hasn't been chosen
        Cursor cursor = this.getReadableDatabase().rawQuery("SELECT \"ID\" FROM Jobs ORDER BY \"ID\" ASC;", null);
        int lastNum = 1, index = cursor.getColumnIndex("ID");
        if(index < 0){throw new IllegalArgumentException("Why is ID less than 0?");}
        while(cursor.moveToNext() && cursor.getInt(index) == lastNum){lastNum++;}
        cursor.close();


        //Adds the job into the database
        this.getWritableDatabase().execSQL(
                "INSERT INTO Jobs (" +
                    "\"Name\"" +
                    ", \"ID\"" +
                    ", \"Salary\"" +
                    ", \"Hours a Week\"" +
                ")" +
                "VALUES(" +
                    "\"" + name + "\"" +
                    ", \"" + lastNum +"\"" +
                    ", \"" + salary + "\"" +
                    ", \"" + hours + "\"" +
                ");"
        );
        return lastNum;
    }

    /**
     * @param choice (String that represents which category to sort by)
     * Gets all the jobs from the database and returns all the data as a cursor*/
    public Cursor getAllJobs(String choice){
        if(choice.equals("None")){return this.getReadableDatabase().rawQuery("SELECT * FROM Jobs;", null);}
        else{return this.getReadableDatabase().rawQuery("SELECT * FROM \"Jobs\" ORDER BY \"" + choice + "\";", null);}
    }

    /**
     * Gets all the Job ids and links them to their respective names for proper display*/
    public HashMap<Integer, String> getIDNamePairsForJobs(){
        Cursor query = this.getWritableDatabase().rawQuery("SELECT \"Name\", \"ID\" FROM Jobs", null);
        HashMap<Integer, String> map = new HashMap<>();
        while(query.moveToNext()){
            int queryID = query.getColumnIndex("ID"), queryName = query.getColumnIndex("Name");
            if(queryID < 0 || queryName < 0){throw new RuntimeException("Incorrect Names");}
            map.put(query.getInt(queryID), query.getString(queryName));
        }
        query.close();
        return map;
    }

    /**
     * Gets all the Job ids and links them to their respective names for proper display*/
    public Integer[] getAllJobIds(){
        Cursor query = this.getWritableDatabase().rawQuery("SELECT \"ID\" FROM Jobs ORDER BY \"ID\";", null);
//        HashMap<Integer, String> map = new HashMap<>();
        ArrayList<Integer> x = new ArrayList<>();
        while(query.moveToNext()){
            int queryID = query.getColumnIndex("ID");
            x.add(query.getInt(queryID));
        }
        query.close();
        for(Integer i : x){
            Log.i("debugging", i.toString());
        }
        return x.toArray(x.toArray(new Integer[0]));
//        return map;
    }

    /**
     * @param id (integer representing the unique ID of the job that is to be removed from the table)
     * Deletes all entries from the Job table with the specified ID
     * */
    public void deleteJobWithID(int id){this.getWritableDatabase().execSQL("DELETE FROM Jobs WHERE \"ID\" = \"" + id +"\"");}

    public void deleteEmployeeWithID(int id){this.getWritableDatabase().execSQL("DELETE FROM Employees WHERE \"ID\" = \"" + id +"\"");}


    /**@param id (integer representing the id of the item to be updated)
     * @param name (String representing the new name of the item)
     * @param salary (integer representing the new salary for the specified item)
     * @param hours (integer representing the new hours for the specified item)*/

    public void editJob(int id, String name, int salary, int hours) throws IllegalArgumentException{
        //Checks if all the characters are alphabetic to prevent sql injection
        for (char character : name.toCharArray()) {if (!Character.isAlphabetic(character)) {throw new IllegalArgumentException("Invalid Characters");}}
        if(name.isEmpty()){throw new IllegalArgumentException("Empty String");}

        //Checks if salary is negative
        if(salary < 0){throw new IllegalArgumentException("Invalid Salary");}

        //Checks if hours is negative or greater than 168
        if(hours < 0 || hours > 168){throw new IllegalArgumentException("Invalid hours");}

        this.getWritableDatabase().execSQL(
                "UPDATE Jobs " +
                "SET " +
                    "\"Name\" = \"" + name + "\" " +
                    ", \"Salary\" = \"" + salary + "\" " +
                    ", \"Hours a Week\" = \"" + hours + "\" " +
                "WHERE \"ID\" = \"" + id + "\"; "
        );
    }

    public void editEmployee(int id, String name, String date, String jobID){
        //Checks if all the characters are alphabetic to prevent sql injection
        for (char character : name.toCharArray()) {if (!Character.isAlphabetic(character)) {throw new IllegalArgumentException("Invalid Characters");}}
        if(name.isEmpty()){throw new IllegalArgumentException("Empty String");}

        try{
            int intJobID = Integer.parseInt(jobID);
            Cursor allJobIDs = this.getWritableDatabase().rawQuery("SELECT DISTINCT \"ID\" FROM Jobs;", null);
            boolean found = false;
            while(allJobIDs.moveToNext() && !found){if(allJobIDs.getShort(0) == intJobID){found = true;}}
            allJobIDs.close();
            if(!found){throw new IllegalArgumentException("Invalid Job ID");}
        }catch(NumberFormatException e){throw new IllegalArgumentException(e);}
//
//        {//Checks if this is a valid job ID
////            int intJobID = Integer.parseInt(jobID);
//        }

        this.getWritableDatabase().execSQL(
                "UPDATE Employees " +
                        "SET " +
                        "\"Name\" = \"" + name + "\" " +
                        ", \"Date of Birth\" = \"" + date + "\" " +
                        ", \"Job ID\" = \"" + jobID + "\" " +
                        "WHERE \"ID\" = \"" + id + "\"; "
        );
    }


    @Override public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {}
}
