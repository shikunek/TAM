package com.NudgeMe.petr.testing;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;


public class ProjectInfoActivity extends AppCompatActivity {

    DatabaseReference mData;
    Calendar myCalendar = Calendar.getInstance();
    EditText endDateEditText;
    TextView projectName;
    ArrayList<String> usersToDelete;
    int userId = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mData = FirebaseDatabase.getInstance().getReference();
        final Intent intent = getIntent();
        endDateEditText = (EditText) findViewById(R.id.endEditText);
        endDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setEndingDate(endDateEditText);
            }
        });
        usersToDelete = new ArrayList<>();
        projectName = (TextView) findViewById(R.id.projectName);
        final LinearLayout createProjectLayout = (LinearLayout) findViewById(R.id.usersLayout);
        final Button applyChangesButton = (Button) findViewById(R.id.toolbarButton);
        applyChangesButton.setVisibility(View.VISIBLE);
        applyChangesButton.setText("Edit");
        applyChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (intent.hasExtra("projectName"))
                {
                    saveChanges(intent.getExtras().getString("projectName"));
                }

            }
        });
        final Button deleteProjectButton = (Button) findViewById(R.id.deleteProjectButton);
        deleteProjectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (intent.hasExtra("projectName"))
                {
                    deleteProject(intent.getExtras().getString("projectName"));
                }

            }
        });



        mData.child("Projects").child(intent.getExtras().getString("projectName")).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot choosenProject) {

                        final TextView projectsListView = (TextView) findViewById(R.id.projectName);
                        projectsListView.setText(choosenProject.child("projectName").getValue().toString());
                        Iterator<DataSnapshot> firstUser =  choosenProject.getChildren().iterator();
                        while (firstUser.hasNext())
                        {
                            DataSnapshot firstChild = firstUser.next();
                            if (firstChild.getKey().equals("Ending"))
                            {
                                continue;
                            }
                            Iterator<DataSnapshot> firstDate = firstChild.getChildren().iterator();
                            while (firstDate.hasNext())
                            {
                                TextView startDate = (TextView) findViewById(R.id.startEditText);
                                String myFormat = "dd.MM.yyyy";
                                String myFormat1 = "yyyy-MM-dd";
                                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.GERMANY);
                                SimpleDateFormat sdf1 = new SimpleDateFormat(myFormat1, Locale.GERMANY);
                                try {
                                    Date date = sdf1.parse(firstDate.next().getKey());
                                    startDate.setText(sdf.format(date));
                                }
                                catch (ParseException e)
                                {
                                    startDate.setText("");
                                    return;
                                }

                                break;
                            }

                            break;
                        }


                        if(choosenProject.hasChild("Ending"))
                        {
                            endDateEditText.setText(choosenProject.child("Ending").getValue().toString());
                        }


                        if (intent.hasExtra("InvitedUsers"))
                        {

                            String[] invitedUsers = intent.getStringArrayExtra("InvitedUsers");
                            createProjectLayout.removeAllViews();
                            for (String user : invitedUsers)
                            {
                                final LinearLayout item = new LinearLayout(ProjectInfoActivity.this);
                                item.setOrientation(LinearLayout.HORIZONTAL);
                                TextView tw = new TextView(ProjectInfoActivity.this);
                                tw.setText(user);
                                item.addView(tw);
                                item.setId(user.hashCode());
                                createProjectLayout.addView(item);
                            }
                        }

                        else
                        {
                            for (DataSnapshot user : choosenProject.getChildren())
                            {
                                if (user.getKey().equals("Ending") || user.getKey().equals("projectName") )
                                {
                                    continue;
                                }
                                mData.child("Uzivatel").child(user.getKey()).child("email").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(final DataSnapshot userEmail)
                                    {
                                        final TextView tw = new TextView(ProjectInfoActivity.this);
                                        tw.setTextSize(18F);
                                        tw.setTextColor(Color.BLACK);

                                        mData.child("Uzivatel").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot userProjects) {
                                                ArrayList<String> listOfRegisteredUsers = new ArrayList<String>();
                                                for (DataSnapshot user : userProjects.getChildren())
                                                {
                                                    listOfRegisteredUsers.add(user.child("email").getValue().toString());
                                                }
                                                final LinearLayout item = new LinearLayout(ProjectInfoActivity.this);
                                                item.setOrientation(LinearLayout.HORIZONTAL);
                                                tw.setText(userEmail.getValue().toString());
                                                item.addView(tw);
                                                item.setId(userEmail.hashCode());
                                                createProjectLayout.addView(item);

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


        ImageButton addUserButton = (ImageButton) findViewById(R.id.addUserButton);
        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                Intent intent = new Intent(ProjectInfoActivity.this, AddUsersActivity.class);
                intent.putExtra("projectName", getIntent().getStringExtra("projectName"));
                ArrayList<String> usersOnProject = new ArrayList<>();
                for (int i = 0; i < createProjectLayout.getChildCount(); i++)
                {
                    LinearLayout linearLayout = (LinearLayout) createProjectLayout.getChildAt(i);
                    for (int j = 0; j < linearLayout.getChildCount(); j++)
                    {
                        TextView tw = (TextView) linearLayout.getChildAt(j);
                        usersOnProject.add(tw.getText().toString());
                    }

                }
                intent.putExtra("usersOnProject", usersOnProject);
                startActivity(intent);

            }
        });

    }

    private void updateLabel(Calendar myCalendar, EditText edittext) {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.GERMANY);
        Log.d("NECO","Yesterday's date was "+sdf.format(myCalendar.getTime()));
        edittext.setText(sdf.format(myCalendar.getTime()));
    }

    public void setEndingDate(final EditText editText)
    {
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(myCalendar, editText);
            }

        };
        final DatePickerDialog datePickerDialog = new DatePickerDialog(ProjectInfoActivity.this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Calendar.getInstance().getTime());
        datePickerDialog.show();
    }

    public void saveChanges(final String projectID)
    {
        mData.child("Projects").child(projectID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot currentProject) {

                if (projectName.getText().toString().equals(""))
                {
                    Toast toast = Toast.makeText(getApplicationContext(), "Project name must be filled!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER| Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }
                final Map<String, Object> updatedUserData = new HashMap<>();
                updatedUserData.put("Projects/" + projectID + "/" +
                        "Ending", endDateEditText.getText().toString());
                updatedUserData.put("Projects/" + projectID + "/" +
                        "projectName", projectName.getText().toString());

                for (DataSnapshot user : currentProject.getChildren())
                {
                    if (user.getKey().equals("projectName") || user.getKey().equals("Ending"))
                    {
                        continue;
                    }

                    updatedUserData.put("Uzivatel/" + user.getKey() + "/" +
                            "Projects" + "/" + projectID + "/" + "projectName", projectName
                            .getText().toString());

                }

                final LinearLayout usersToSave = (LinearLayout) findViewById(R.id.usersLayout);

                    mData.child("Uzivatel").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot users) {
                            for (DataSnapshot user : users.getChildren())
                            {
                                for (int i = 0; i < usersToSave.getChildCount(); i++)
                                {
                                    LinearLayout item = (LinearLayout) usersToSave.getChildAt(i);
                                    for (int j = 0; j < item.getChildCount() ; j++)
                                    {
                                        if (item.getChildAt(j) instanceof TextView)
                                        {
                                            TextView userEmail = (TextView) item.getChildAt(j);
                                            if (user.child("email").getValue().toString().equals(userEmail.getText().toString()) &&
                                                    !currentProject.hasChild(user.getKey()))
                                            {
                                                Calendar myCalendar = Calendar.getInstance();
                                                Report report = new Report(0, "", 0);
                                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY);
                                                final String formattedDate = dateFormat.format(myCalendar.getTime());
                                                updatedUserData.put("Projects/" + projectID + "/" + user.getKey() +
                                                        "/" + formattedDate, report);
                                                updatedUserData.put("Uzivatel/" + user.getKey() + "/" +
                                                        "Projects" + "/" + projectID + "/" + "projectName", projectName
                                                        .getText().toString());
                                                updatedUserData.put("Uzivatel/" + user.getKey() + "/" +
                                                        "Active", projectID);

                                            }

                                        }
                                    }
                                }

                                if (getIntent().hasExtra("UsersToDelete"))
                                {
                                    Iterator<String> it = getIntent().getStringArrayListExtra("UsersToDelete").iterator();
                                    while(it.hasNext())
                                    {
                                        if (user.child("email").getValue().toString().equals(it.next()))
                                        {
                                            updatedUserData.put("Projects/" + projectID + "/" + user.getKey(), null);
                                            updatedUserData.put("Uzivatel/" + user.getKey() + "/" +
                                                    "Projects/" + projectID + "/" + "projectName" , null);
                                            updatedUserData.put("Uzivatel/" + user.getKey() + "/" +
                                                    "Active", null);
                                            // Pokud se uzivatel rozhodl smazat aktualne vybrany projekt
                                            if (user.child("Active").getValue().toString().equals(projectID) && user.hasChild("Projects"))
                                            {
                                                for (DataSnapshot firstProject : user.child("Projects").getChildren())
                                                {
                                                    if (!firstProject.getKey().equals(projectID))
                                                    {
                                                        updatedUserData.put("Uzivatel/" + user.getKey() + "/" +
                                                                "Active" , firstProject.getKey());
                                                        break;
                                                    }

                                                }

                                            }
                                        }
                                    }
                                }

                            }
                            mData.updateChildren(updatedUserData);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void deleteProject(final String projectID){

        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mData.child("Uzivatel").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot currentUsersData) {

                if (!currentUsersData.hasChild("Active"))
                {
                    return;
                }
                final Map<String, Object> updatedUserData = new HashMap<>();
                updatedUserData.put("Uzivatel/" + currentUser.getUid() + "/" +
                        "Projects/" + projectID + "/"  , null);

                updatedUserData.put("Projects/" + projectID + "/" +
                        currentUser.getUid() + "/" , null);


                if (projectID.equals(currentUsersData.child("Active").getValue().toString()))
                {
                    updatedUserData.put("Uzivatel/" + currentUser.getUid() + "/Active" , null);
                }


                // pokud je na projektu jediny uzivatel tak je smazan cely
                mData.child("Projects").child(projectID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot remainingUsers) {
                        Iterator<DataSnapshot> project =  remainingUsers.getChildren().iterator();
                        int i = 0;
                        while (project.hasNext())
                        {
                            DataSnapshot firstChild = project.next();
                            if ( firstChild.getKey().equals("projectName") || firstChild.getKey().equals("Ending"))
                            {
                                continue;
                            }
                            i++;
                        }
                        // <= je kvuli tomu, ze pokud existuje jeden uzivatel, tak je i == 1
                        if (i <= 1 && remainingUsers.hasChild(currentUser.getUid()))
                        {
                            updatedUserData.put("Projects/" + projectID + "/" + "projectName" , null);
                        }


                        // Pokud se uzivatel rozhodl smazat aktualne vybrany projekt
                        if (projectID.equals(currentUsersData.child("Active").getValue().toString()) && currentUsersData.hasChild("Projects"))
                        {
                            for (DataSnapshot firstProject : currentUsersData.child("Projects").getChildren())
                            {
                                if (!firstProject.getKey().equals(projectID))
                                {
                                    updatedUserData.put("Uzivatel/" + currentUser.getUid() + "/" +
                                            "Active" , firstProject.getKey());
                                    break;
                                }

                            }

                        }

                        mData.updateChildren(updatedUserData);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



                finish(); // aby uzivatel po stisknuti Back, nesel na jiz neexistujici projekt

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}