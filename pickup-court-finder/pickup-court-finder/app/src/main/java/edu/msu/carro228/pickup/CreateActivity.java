package edu.msu.carro228.pickup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.sql.Date;
import java.sql.Time;
import java.util.Calendar;

public class CreateActivity extends AppCompatActivity {
    // Access a Cloud Firestore instance from your Activity
    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("pickup_games");
    public int current_participants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

    }

    public void setTimeView(int hr, int min){

    }
    public void onSetLocation(View view)  {
        EditText gameType = findViewById(R.id.gameTypeVal);
        EditText gameName = findViewById(R.id.gameName);
        EditText maxPlayers = findViewById(R.id.maxPlayerVal);
        EditText description = findViewById(R.id.gameDescription);
        EditText cost = findViewById(R.id.gameCost);
        CheckBox playerPlaying = findViewById(R.id.creatorParticipant);
        TextView date = findViewById(R.id.dateGame);
        TextView time = findViewById(R.id.gameStartLabel);
        if(playerPlaying.isChecked()){
            current_participants = 1;
        }else{
            current_participants = 0;
        }

        if (gameType.getText().toString().equals("") || gameName.getText().toString().equals("") || maxPlayers.getText().toString().equals("") || description.getText().toString().equals("") || cost.getText().toString().equals("") || time.getText().toString().equals(getString(R.string.start_time)) || date.getText().equals(getString(R.string.start_day))){
            Toast.makeText(view.getContext(), R.string.missing_field_text, Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, SetLocActivity.class);

        intent.putExtra("GAME_TYPE", gameType.getText().toString());
        intent.putExtra("CURRENT_PLAYERS", current_participants);
        intent.putExtra("GAME_NAME", gameName.getText().toString());
        intent.putExtra("MAX_PLAYERS", Integer.parseInt(maxPlayers.getText().toString()));
        intent.putExtra("DESCRIPTION", description.getText().toString());
        intent.putExtra("COST", cost.getText().toString());
        intent.putExtra(Game.START, time.getText().toString());
        intent.putExtra(Game.DATE, date.getText().toString());

        startActivity(intent);
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {



        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            ((TextView)getActivity().findViewById(R.id.gameStartLabel)).setText(hourOfDay + ":" + String.format("%02d", minute));
        }
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            ((TextView)getActivity().findViewById(R.id.dateGame)).setText(month + "/" + day + "/" + year);
        }
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }



}