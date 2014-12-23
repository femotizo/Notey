package thomas.jonathan.notey;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AlarmActivity extends FragmentActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    public static final String DATEPICKER_TAG = "datepicker", TIMEPICKER_TAG = "timepicker";
    private final Calendar calendar = Calendar.getInstance();
    private TextView date_tv;
    private TextView time_tv;
    private int year, month, day, hour, minute;
    private PendingIntent alarmPendingIntent;
    public static final SimpleDateFormat format_date = new SimpleDateFormat("EEE, MMM dd, yyyy"), format_time = new SimpleDateFormat("hh:mm a");
    private CheckBox vibrate_cb, wake_cb;
    SharedPreferences sharedPref;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_activity_dialog);

        Intent i = getIntent();

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        id = i.getExtras().getInt("alarm_id", -1);

        if (savedInstanceState != null) {
            DatePickerDialog dpd = (DatePickerDialog) getSupportFragmentManager().findFragmentByTag(DATEPICKER_TAG);
            if (dpd != null) {
                dpd.setOnDateSetListener(this);
            }

            TimePickerDialog tpd = (TimePickerDialog) getSupportFragmentManager().findFragmentByTag(TIMEPICKER_TAG);
            if (tpd != null) {
                tpd.setOnTimeSetListener(this);
            }
        }

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DATE);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        TextView alarm_set_tv = (TextView) findViewById(R.id.alarm_set_tv);
        date_tv = (TextView) findViewById(R.id.date_tv);
        time_tv = (TextView) findViewById(R.id.time_tv);
        TextView alarm_mainTitle = (TextView) findViewById(R.id.alarm_mainTitle);
        Button set_btn = (Button) findViewById(R.id.alarm_set_btn);
        Button cancel_btn = (Button) findViewById(R.id.alarm_cancel_btn);
        ImageButton alarm_delete = (ImageButton) findViewById(R.id.alarm_delete);
        vibrate_cb = (CheckBox) findViewById(R.id.alarm_vibrate);
        wake_cb = (CheckBox) findViewById(R.id.alarm_wake);

        Typeface roboto_light = Typeface.createFromAsset(getAssets(), "ROBOTO-LIGHT.TTF");
        Typeface roboto_reg = Typeface.createFromAsset(getAssets(), "ROBOTO-REGULAR.ttf");
        Typeface roboto_bold = Typeface.createFromAsset(getAssets(), "ROBOTO-BOLD.ttf");

        alarm_set_tv.setTypeface(roboto_bold);
        date_tv.setTypeface(roboto_reg);
        time_tv.setTypeface(roboto_reg);
        alarm_mainTitle.setTypeface(roboto_light);
        set_btn.setTypeface(roboto_light);
        cancel_btn.setTypeface(roboto_light);
        vibrate_cb.setTypeface(roboto_light);
        wake_cb.setTypeface(roboto_light);

        date_tv.setOnClickListener(this);
        time_tv.setOnClickListener(this);
        set_btn.setOnClickListener(this);
        cancel_btn.setOnClickListener(this);
        alarm_delete.setOnClickListener(this);

        //long click listener to delete icon. toast will appear saying what the button does
        View.OnLongClickListener listener = new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                Toast.makeText(getApplicationContext(), getString(R.string.delete) + " " + getString(R.string.alarm), Toast.LENGTH_SHORT).show();
                return true;
            }
        };
        alarm_delete.setOnLongClickListener(listener);

        // if id is valid. also checks the sharedprefs if the user is editing the alarm
        if(id != -1 && sharedPref.getBoolean("vibrate" + Integer.toString(id), true))
            vibrate_cb.setChecked(true);
        if(id != -1 && sharedPref.getBoolean("wake" + Integer.toString(id), true))
            wake_cb.setChecked(true);


        Date date = new Date();

        // if an alarm is already set, show the set alarm date/time. also show the delete button
        if(i.hasExtra("alarm_set_time")) {
            alarm_set_tv.setText(getString(R.string.alarm_set_for));
            date = new Date(Long.valueOf(i.getExtras().getString("alarm_set_time")));
            alarm_delete.setVisibility(View.VISIBLE);

            alarmPendingIntent = (PendingIntent) i.getExtras().get("alarm_pending_intent");

            // update the calendar and the year/mo/day/hour/min to the preset alarm time
            calendar.setTime(date);
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DATE);
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
        }
        else alarm_delete.setVisibility(View.INVISIBLE);

        date_tv.setText(format_date.format(date));
        time_tv.setText(format_time.format(date));

    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.date_tv) {
            DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this, year, month, day, true);
            datePickerDialog.setVibrate(true);
            datePickerDialog.setYearRange(1985, 2028);
            datePickerDialog.setCloseOnSingleTapDay(false);
            datePickerDialog.show(getSupportFragmentManager(), DATEPICKER_TAG);

        } else if (view.getId() == R.id.time_tv) {
            TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(this, hour, minute, false, false);
            timePickerDialog.setVibrate(true);
            timePickerDialog.setCloseOnSingleTapMinute(false);
            timePickerDialog.show(getSupportFragmentManager(), TIMEPICKER_TAG);
        }
        else if(view.getId() == R.id.alarm_set_btn){
            calendar.set(year, month, day, hour, minute); //set the calendar for the new alarm time

            // send the time (in milliseconds) back to MainActivity to set alarm if the user creates the notey

            if(System.currentTimeMillis() < calendar.getTimeInMillis()) { //if set time is greater than current time, then set the alarm
                Intent output = new Intent();
                output.putExtra("alarm_time", Long.toString(calendar.getTimeInMillis()));
                setResult(RESULT_OK, output);

                saveSettings();
            }
            else Toast.makeText(getApplicationContext(), getString(R.string.alarm_not_set), Toast.LENGTH_SHORT).show();


            finish();
        }
        else if(view.getId() == R.id.alarm_cancel_btn){
            finish();
        }
        else if(view.getId() == R.id.alarm_delete){
            Intent output = new Intent();
            output.putExtra("alarm_time", "");
            setResult(RESULT_OK, output);

            //cancel the alarm pending intent
            AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
            alarmManager.cancel(alarmPendingIntent);

            Toast.makeText(getApplicationContext(), getString(R.string.alarm_deleted), Toast.LENGTH_LONG).show();
            finish();
        }

    }

    //save the checkboxes and settings. create a new sharedpref for each alarm, this is using the id as an identifier for the alarmService.
    private void saveSettings(){

        if(id != -1){
            SharedPreferences.Editor editor = sharedPref.edit();

            editor.putBoolean("vibrate" + Integer.toString(id), vibrate_cb.isChecked());
            editor.putBoolean("wake" + Integer.toString(id), wake_cb.isChecked());

            editor.apply();
        }
    }


    @Override
    public void onResume() {
        // Example of reattaching to the fragment
        super.onResume();

    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int y, int mo, int d) {
        year = y;
        month = mo;
        day = d;

        Calendar c = Calendar.getInstance();
        c.set(y, mo, d);

        date_tv.setText(format_date.format(c.getTime()));
    }

    @Override
    public void onTimeSet(RadialPickerLayout radialPickerLayout, int h, int m) {
        hour = h;
        minute = m;

        String AM_PM ;
        if(h < 12) {
            AM_PM = "AM";
        } else {
            if(h!=12) h = h-12;
            AM_PM = "PM";
        }

        String h2 = Integer.toString(h);
        if(h < 10) h2 = "0" + h;

        String min = Integer.toString(m);
        if(m < 10) min = "0" + min;

        time_tv.setText(h2 + ":" + min + " " + AM_PM);
    }
}

