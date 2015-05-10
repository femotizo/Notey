package thomas.jonathan.notey;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

// Toast.makeText(getApplicationContext(), "**", Toast.LENGTH_SHORT).show();
public class InfoScreenActivity extends Activity implements OnClickListener {
    public static final int CURRENT_ANDROID_VERSION = Build.VERSION.SDK_INT;
    private TextView noteText;
    private ImageButton back_btn;
    private ImageButton edit_btn;
    private ImageButton copy_btn;
    private ImageButton share_btn;
    private ImageButton delete_btn;
    private PopupMenu mPopupMenu;
    private PendingIntent alarmPendingIntent;
    private int imageButtonNumber, spinnerLocation, id, repeat_time;
    private String noteTitle, alarm_time = "";
    private String[] internetStrings = new String[]{"www.", ".com", "http://", "https://"};
    private Notification notification;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_activity_dialog);

        AlarmService.releaseWakeUpWakelock(); // release the wakelock which turns on the device

        initializeGUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // wait a second then cancel the notification created to vibrate the device
        try {
            Thread.sleep(1000);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(AlarmService.VIBRATE_NOTIFICATION_ID);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.info_back_button) {
            finish();
        } else if (view.getId() == R.id.info_edit_button) {
            Intent editIntent = new Intent(this, MainActivity.class);
            editIntent.putExtra("editNotificationID", id);
            editIntent.putExtra("editNote", noteText.getText().toString());
            editIntent.putExtra("editLoc", spinnerLocation);
            editIntent.putExtra("editButton", imageButtonNumber);
            editIntent.putExtra("editTitle", noteTitle);
            editIntent.putExtra("editAlarm", alarm_time);
            editIntent.putExtra("editRepeat", repeat_time);
            editIntent.putExtra("editAlarmPendingIntent", alarmPendingIntent);
            startActivity(editIntent);
            finish();
        } else if (view.getId() == R.id.info_copy_button) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", noteText.getText());
            clipboard.setPrimaryClip(clip);

            Toast.makeText(getApplicationContext(), getString(R.string.text_copied), Toast.LENGTH_SHORT).show();

        } else if (view.getId() == R.id.info_share_button) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, noteText.getText().toString());
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } else if (view.getId() == R.id.info_delete_button) {
            Intent intent = new Intent(this, NotificationDismiss.class);
            intent.putExtra("NotificationID", id);
            sendBroadcast(intent);
            finish();
        } else if (view.getId() == R.id.info_menuButton) {
            mPopupMenu.show();
        }
        // if a web link, open it in the browser
        else if (view.getId() == R.id.info_note_text) {
            String splitString[] = noteText.getText().toString().split("\\s");
            String url;
            for (String s : splitString) {
                url = s.toLowerCase();
                if (s.toLowerCase().startsWith("www.")) // add http:// to the beggining if it doesnt already
                    url = "http://" + s.toLowerCase();
                if (!s.toLowerCase().startsWith("www.") && !s.toLowerCase().startsWith("http"))
                    url = "http://www." + s.toLowerCase();
                if (s.endsWith(".")) // remove ending period if there is one
                    url = url.substring(0, url.length() - 1).toLowerCase();

                try {
                    Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
                    startActivity(viewIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getApplicationContext(), getString(R.string.bad_url), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
            finish();
        } else if (view.getId() == R.id.info_alarm_text) {
            Intent editIntent = new Intent(this, MainActivity.class);
            editIntent.putExtra("editNotificationID", id);
            editIntent.putExtra("editNote", noteText.getText().toString());
            editIntent.putExtra("editLoc", spinnerLocation);
            editIntent.putExtra("editButton", imageButtonNumber);
            editIntent.putExtra("editTitle", noteTitle);
            editIntent.putExtra("editAlarm", alarm_time);
            editIntent.putExtra("editRepeat", repeat_time);
            editIntent.putExtra("editAlarmPendingIntent", alarmPendingIntent);
            editIntent.putExtra("doEditAlarmActivity", true);
            startActivity(editIntent);
            finish();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void initializeGUI() {
        Intent i = this.getIntent();

        noteText = (TextView) findViewById(R.id.info_note_text);
        TextView alarmText = (TextView) findViewById(R.id.info_alarm_text);
        noteText.setMovementMethod(new ScrollingMovementMethod());
        TextView titleText = (TextView) findViewById(R.id.info_title_text);
        TextView mainTitle = (TextView) findViewById(R.id.info_mainTitle);

        ImageView imageView = (ImageView) findViewById(R.id.info_imageView);

        if (i.hasExtra("infoNotificationID")) {
            int infoID = i.getExtras().getInt("infoNotificationID");
            int infoLoc = i.getExtras().getInt("infoLoc");
            int infoBtn = i.getExtras().getInt("infoButton");
            String infoNote = i.getExtras().getString("infoNote");
            String infoTitle = i.getExtras().getString("infoTitle");
            String infoAlarm = i.getExtras().getString("infoAlarm");
            int infoRepeat = i.getExtras().getInt("infoRepeat");
            PendingIntent infoAlarmPI = (PendingIntent) i.getExtras().get("infoAlarmPendingIntent");
            notification = (Notification) i.getExtras().get("infoNotif");

            id = infoID;
            spinnerLocation = infoLoc;
            imageButtonNumber = infoBtn;
            noteTitle = infoTitle;
            alarm_time = infoAlarm;
            repeat_time = infoRepeat;
            alarmPendingIntent = infoAlarmPI;

            noteText.setText(infoNote);

            //only show note if it's not empty
            if (infoNote.equals("")) {
                noteText.setVisibility(View.GONE);
            } else {
                noteText.setVisibility(View.VISIBLE);
                noteText.setText(infoNote);
            }

            //only show alarm if it's not empty
            if (infoAlarm == null || infoAlarm.equals("")) {
                alarmText.setVisibility(View.GONE);
                alarmText.setClickable(false);
                alarmText.setOnClickListener(null);
            } else {
                alarmText.setVisibility(View.VISIBLE);
                Date date = new Date(Long.valueOf(infoAlarm));
                alarmText.setText(getString(R.string.alarm) + ": " + MainActivity.format_short_date.format(date) + ", " + MainActivity.format_short_time.format(date));
                alarmText.setClickable(true);
                alarmText.setOnClickListener(this);
            }

            //show icon
            MySQLiteHelper db = new MySQLiteHelper(this);
            if (db.checkIfExist(infoID)) {
                NoteyNote n = db.getNotey(infoID);
                imageView.setImageResource(getResources().getIdentifier(n.getIconName(), "drawable", getPackageName()));
                imageView.setBackgroundResource(R.drawable.circle);
                imageView.setAlpha(0.5f);
            }

            //only show title if it's not equal to "Notey"
            if (infoTitle.equals(getString(R.string.app_name)) || infoTitle.equals("")) {
                titleText.setVisibility(View.GONE);
            } else {
                titleText.setVisibility(View.VISIBLE);
                titleText.setText(infoTitle);
            }

            for (String s : internetStrings)
                if (noteText.getText().toString().toLowerCase().contains(s)) {
                    noteText.setClickable(true);

                    //selectable background. Only for jelly bean and above
                    if (CURRENT_ANDROID_VERSION >= 16) {
                        int[] attr = new int[]{android.R.attr.selectableItemBackground};
                        TypedArray ta = obtainStyledAttributes(attr);
                        Drawable draw = ta.getDrawable(0); //index zero
                        noteText.setBackground(draw);
                    }

                    noteText.setOnClickListener(this);
                    break;
                } else {
                    noteText.setClickable(false);

                    //off set the selectable background from above
                    if (CURRENT_ANDROID_VERSION >= 16)
                        noteText.setBackgroundColor(Color.TRANSPARENT);
                    noteText.setOnClickListener(null);
                }
        }

        ImageButton menu_btn = (ImageButton) findViewById(R.id.info_menuButton);
        back_btn = (ImageButton) findViewById(R.id.info_back_button);
        edit_btn = (ImageButton) findViewById(R.id.info_edit_button);
        copy_btn = (ImageButton) findViewById(R.id.info_copy_button);
        share_btn = (ImageButton) findViewById(R.id.info_share_button);
        delete_btn = (ImageButton) findViewById(R.id.info_delete_button);

        menu_btn.setOnClickListener(this);
        back_btn.setOnClickListener(this);
        edit_btn.setOnClickListener(this);
        copy_btn.setOnClickListener(this);
        share_btn.setOnClickListener(this);
        delete_btn.setOnClickListener(this);

        setupLongClickListeners(); // Long click listeners for the five buttons. All will display a toast to summarize their action to the user

        //menu popup
        mPopupMenu = new PopupMenu(this, menu_btn);
        MenuInflater menuInflater = mPopupMenu.getMenuInflater();
        mPopupMenu.getMenu().clear(); //clear the menu so list items aren't duplicated
        menuInflater.inflate(R.menu.menu_pro, mPopupMenu.getMenu()); //default pro menu because I don't want to deal with the IAP in this class

        mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.settings:
                        Intent intent = new Intent(InfoScreenActivity.this, Settings.class);
                        startActivity(intent);
                        break;
                    case R.id.about:
                        Intent i = new Intent(InfoScreenActivity.this, About.class);
                        startActivity(i);
                        break;
                    default:
                        return true;
                }
                return true;
            }
        });

        //set font
        Typeface roboto_light = Typeface.createFromAsset(getAssets(), "ROBOTO-LIGHT.TTF");
        Typeface roboto_reg = Typeface.createFromAsset(getAssets(), "ROBOTO-REGULAR.ttf");
        noteText.setTypeface(roboto_light);
        titleText.setTypeface(roboto_reg);
        mainTitle.setTypeface(roboto_light);
    }

    private void setupLongClickListeners() {
        back_btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getApplicationContext(), getString(R.string.go_back), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        edit_btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getApplicationContext(), getString(R.string.edit), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        copy_btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getApplicationContext(), getString(R.string.copy), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        share_btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getApplicationContext(), getString(R.string.share), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        delete_btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getApplicationContext(), getString(R.string.remove), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //stop the led flashing
        NotificationDismiss.clearNotificationLED(this);

        //stop the ringtone if there is one going off
        if(notification != null){
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(AlarmService.LED_SOUND_ID);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
}


