package it.zerozero.audiorecorder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.view.menu.MenuBuilder;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView textViewStatus;
    private TextView textViewFileName;
    private ToggleButton toggleButtonRecord;
    private ToggleButton toggleButtonPlay;
    private ToggleButton toggleButtonPause;
    private Button buttonSkipFW;
    private Button buttonSkipBW;
    private MediaRecorder recorder = null;
    private MediaPlayer player = null;
    private SharedPreferences sharedPreferences;
    private boolean recorderPaused = false;
    private boolean playerPaused = false;
    private File recDir = null;
    private File recFile = null;
    private static final int PERMISSION_REC_AUDIO = 5051;
    private static final int PERMISSION_WRITE_EXTERNAL = 5060;
    private static final int PERMISSION_CAPTURE_AUDIO = 5075;
    private MenuItem shareMenuItem = null;
    private View fabView = null;
    private String displayFileName = null;
    private Uri recFileUri = null;
    // private Uri recDirUri = null;
    private boolean fileEmpty;
    public static final int FORMAT_3GPHC = 1501;
    public static final int FORMAT_3GPHQ = 1502;
    public static final int FORMAT_MP4 = 1503;
    public static final int AUDIO_MIC = 1851;
    public static final int AUDIO_CALL = 1852;
    public static final int AUDIO_INCOMING_CALL = 1853;
    public static final int REQUEST_ACT_LIST = 20202;
    private int audioSource = AUDIO_MIC;
    private int fileFormat;
    private int selectedBitRate;
    private boolean isShowPause = true;
    private boolean isPlaying = false;
    private boolean isSaveExternal = true;
    private boolean audioPermissionGranted = false;
    private boolean capturePermissionGranted = false;
    private boolean extRWPermissionGranted = false;

    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("AudioRecorderRec", MODE_PRIVATE);
        isShowPause = sharedPreferences.getBoolean("ShowPause", true);
        isSaveExternal = sharedPreferences.getBoolean("SaveExternal", true);
        fileFormat = sharedPreferences.getInt("Format", 1503);
        audioSource = sharedPreferences.getInt("AudioSource", 1851);
        selectedBitRate = sharedPreferences.getInt("SelectedBitRate", 16000);
        Log.i("selectedBitRate", String.valueOf(selectedBitRate));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabView = view;
                if (!fileEmpty) {
                    boolean uriOK = false;
                    try {
                        recFileUri = FileProvider.getUriForFile(getApplicationContext(), "it.zerozero.audiorecorder.fileprovider", recFile);
                        uriOK = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        uriOK = false;
                    }
                    if (uriOK) {
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.putExtra(Intent.EXTRA_STREAM, recFileUri);
                        if (fileFormat == FORMAT_3GPHC || fileFormat == FORMAT_3GPHQ) {
                            shareIntent.setType("audio/3gp");
                        }
                        if (fileFormat == FORMAT_MP4) {
                            shareIntent.setType("audio/m4a");
                        }
                        try {
                            startActivity(shareIntent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Error sharing recording file.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL);
        }
        else {
            extRWPermissionGranted = true;
        }
        /**
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REC_AUDIO);
        }
        else {
            audioPermissionGranted = true;
        }
        */
        textViewStatus = findViewById(R.id.textViewStatus);
        textViewStatus.setText("Initialized");
        textViewFileName = findViewById(R.id.textViewFileName);
        textViewFileName.setText("...");
        toggleButtonRecord = findViewById(R.id.toggleButtonRecord);
        toggleButtonRecord.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkAudioPermissions();
                boolean audioPermissionOK = (audioSource == AUDIO_MIC && audioPermissionGranted) || (audioSource == AUDIO_CALL && capturePermissionGranted);

                if(isChecked && audioPermissionOK) {
                    startRecording();
                }
                if(!isChecked && audioPermissionOK) {
                    stopRecording();
                }
                if(!audioPermissionOK) {
                    Toast.makeText(MainActivity.this, "No permission was granted to record audio.", Toast.LENGTH_SHORT).show();
                    toggleButtonRecord.setChecked(false);
                }
            }
        });
        toggleButtonPlay = findViewById(R.id.toggleButtonPlay);
        toggleButtonPlay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    startPlaying();
                }
                else {
                    stopPlaying();
                }
            }
        });

        buttonSkipFW = findViewById(R.id.buttonSkipFW);
        buttonSkipBW = findViewById(R.id.buttonSkipBW);

        toggleButtonPause = findViewById(R.id.toggleButtonPause);

        if (extRWPermissionGranted) {
            createRecFile();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        toggleButtonPlay.setChecked(false);
        toggleButtonRecord.setChecked(false);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isShowPause) {
            toggleButtonPause.setVisibility(View.VISIBLE);
            toggleButtonPause.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(View v) {
                    if(recorder != null) {
                        if(!recorderPaused) {
                            recorder.pause();
                            recorderPaused = true;
                            textViewStatus.setText("Paused");
                        }
                        else {
                            recorder.resume();
                            recorderPaused = false;
                            textViewStatus.setText("Recording");
                        }
                    }
                    if(player != null) {
                        if(!playerPaused) {
                            player.pause();
                            playerPaused = true;
                            textViewStatus.setText("Paused");
                        }
                        else {
                            player.start();
                            playerPaused = false;
                            textViewStatus.setText("Playing");
                        }
                    }
                    if(player == null && recorder == null) {
                        toggleButtonPause.setChecked(false);
                    }
                }
            });

            buttonSkipFW.setVisibility(View.VISIBLE);
            buttonSkipFW.setText(">>");
            buttonSkipFW.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    skipPlaying(true);
                }
            });

            buttonSkipBW.setVisibility(View.VISIBLE);
            buttonSkipBW.setText("<<");
            buttonSkipBW.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    skipPlaying(false);
                }
            });

        }
        else {
            toggleButtonPause.setVisibility(View.GONE);
            buttonSkipFW.setVisibility(View.GONE);
            buttonSkipBW.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.d("Main oPR Format", String.valueOf(fileFormat));
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (menu instanceof MenuBuilder) {
            MenuBuilder mb = (MenuBuilder) menu;
            mb.setOptionalIconsVisible(true);
        }
        shareMenuItem = menu.findItem(R.id.action_share);
        if (shareMenuItem != null) {
            shareMenuItem.setEnabled(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_new) {
            NewFileDialog newFileDialog = NewFileDialog.newInstance("Create a new recording file?");
            newFileDialog.show(getFragmentManager(), "New File");
            toggleButtonRecord.setChecked(false);
            toggleButtonPlay.setChecked(false);
            textViewStatus.setText("init");
        }
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        if (id == R.id.action_list) {
            Intent listIntent = new Intent(this, ListActivity.class);
            listIntent.putExtra("recDir", recDir.toString());
            // startActivity(listIntent);
            startActivityForResult(listIntent, REQUEST_ACT_LIST);
            return true;
        }
        if (id == R.id.action_share) {
            boolean uriOK = false;
            try {
                recFileUri = FileProvider.getUriForFile(getApplicationContext(), "it.zerozero.audiorecorder.fileprovider", recFile);
                uriOK = true;
            } catch (Exception e) {
                e.printStackTrace();
                uriOK = false;
            }
            if (uriOK) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, recFileUri);
                if (fileFormat == FORMAT_3GPHC || fileFormat == FORMAT_3GPHQ) {
                    shareIntent.setType("audio/3gp");
                }
                if (fileFormat == FORMAT_MP4) {
                    shareIntent.setType("audio/m4a");
                }
                try {
                    startActivity(shareIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                Toast.makeText(this, "Error sharing recording file.", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPlaying();
        stopRecording();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recFile != null) {
            if (fileEmpty) {
                try {
                    recFile.delete();
                } finally {
                    recFile = null;
                }
            }
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length != 0) {
            if (requestCode == PERMISSION_REC_AUDIO) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    audioPermissionGranted = true;
                }
                else audioPermissionGranted = false;
            }
            if (requestCode == PERMISSION_CAPTURE_AUDIO) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    capturePermissionGranted = true;
                }
                else capturePermissionGranted = false;
            }
            if (requestCode == PERMISSION_WRITE_EXTERNAL) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    extRWPermissionGranted = true;
                }
                else extRWPermissionGranted = false;
            }
            if(!audioPermissionGranted) {
                Toast.makeText(this, "Recording permission denied!", Toast.LENGTH_SHORT).show();
            }
            if(!extRWPermissionGranted) {
                Toast.makeText(this, "Write permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startRecording() {
        if (!fileEmpty) {
            String fName = createRecFile();
            Toast.makeText(this, "New recording file " + fName, Toast.LENGTH_SHORT).show();
        }

        if ((audioSource == AUDIO_MIC && audioPermissionGranted) || (audioSource == AUDIO_CALL && capturePermissionGranted)) {
            recorder = new MediaRecorder();
            if (recorder != null && recFile != null) {
                if (audioSource == AUDIO_CALL) {
                    recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
                }
                else if (audioSource == AUDIO_INCOMING_CALL) {
                    recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_DOWNLINK);
                }
                else {
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                }

                if (fileFormat == FORMAT_MP4) {
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC_ELD);
                    recorder.setAudioEncodingBitRate(selectedBitRate);
                    recorder.setAudioSamplingRate(48000);
                }
                else if (fileFormat == FORMAT_3GPHQ) {
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC_ELD);
                    recorder.setAudioEncodingBitRate(selectedBitRate);
                    recorder.setAudioSamplingRate(48000);
                }
                else {
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
                    recorder.setAudioEncodingBitRate(selectedBitRate);
                    recorder.setAudioSamplingRate(16000);
                }

                recorder.setOutputFile(recFile.toString());
            }

            try {
                recorder.prepare();
                recorder.start();
                textViewStatus.setText("Recording");
                fileEmpty = false;
            }
            catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "recorder.prepare() or start() failed.", Toast.LENGTH_SHORT).show();
                textViewStatus.setText("Recorder failed");
                fileEmpty = true;
            }
        }
        else {
            Toast.makeText(this, "Need audio permission to record!", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (recorder != null) {
            recorder.stop();
            textViewStatus.setText("Stopped");
            recorder.release();
            recorder = null;
            recorderPaused = false;
            toggleButtonPause.setChecked(false);
            toggleButtonPlay.setEnabled(true);
            if (shareMenuItem != null) {
                shareMenuItem.setEnabled(true);
            }
        }
    }

    private void startPlaying() {
        player = new MediaPlayer();
        try {
            player.setDataSource(recFile.toString());
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    toggleButtonPlay.setChecked(false);
                    textViewStatus.setText("Stopped");
                    isPlaying = false;
                }
            });
            player.prepare();
            player.start();
            isPlaying = true;
            textViewStatus.setText("Playing");
            DisplayPlayTime displayPlayTime = new DisplayPlayTime();
            displayPlayTime.execute();
        }
        catch (Exception e) {
            e.printStackTrace();
            textViewStatus.setText("Player failed");
            toggleButtonPlay.setChecked(false);
            isPlaying = false;
        }
    }

    private void stopPlaying() {
        if(player != null) {
            player.stop();
            textViewStatus.setText("Stopped");
            player.release();
            isPlaying = false;
            player = null;
            playerPaused = false;
            toggleButtonPause.setChecked(false);
        }
    }

    private void skipPlaying(boolean isFW) {
        if (isPlaying) {
            if (isFW) {
                int elapsed = player.getCurrentPosition();
                int setpos = elapsed + 5000;
                player.seekTo(setpos);
            }
            if (!isFW) {
                int elapsed = player.getCurrentPosition();
                int setpos = elapsed - 5000;
                player.seekTo(setpos);
            }
        }
    }

    private boolean isExternalStorageWritable(){
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)){
            return true;
        }
        else {
            return false;
        }
    }

    private void checkAudioPermissions() {
        if (audioSource == AUDIO_MIC) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REC_AUDIO);
            }
            else {
                audioPermissionGranted = true;
            }
        }
        if (audioSource == AUDIO_CALL || audioSource == AUDIO_INCOMING_CALL) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAPTURE_AUDIO_OUTPUT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAPTURE_AUDIO_OUTPUT}, PERMISSION_CAPTURE_AUDIO);
            }
            else {
                capturePermissionGranted = true;
            }
        }
    }

    protected void doDialogCancelClick() {
        Log.i("doDialogCancelClick()", "run.");
    }

    protected void doDialogOKClick() {
        if (recFile != null && fileEmpty) {
            recFile.delete();
        }
        createRecFile();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ACT_LIST && resultCode == RESULT_OK) {
            if (data != null) {
                RecordingFile loadRecordingFile = new RecordingFile(new File(data.getData().getPath()));
                displayFileName = loadRecordingFile.getFileName();
                recFile = loadRecordingFile.getFile();
                textViewFileName.setText(loadRecordingFile.getFileName());
                textViewStatus.setText("Loaded file");
                toggleButtonPlay.setEnabled(true);
                if (shareMenuItem != null) {
                    shareMenuItem.setEnabled(true);
                }
                fileEmpty = false;
                Toast.makeText(this, "Loaded " + loadRecordingFile.getFileName(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Nullable
    private String createRecFile() {
        String recFileName = null;
        String recFileExtension = null;
        if (fileFormat == FORMAT_3GPHC || fileFormat == FORMAT_3GPHQ) {
            recFileExtension = ".3gp";
        }
        else  {
            recFileExtension = ".m4a";
        }
        if(isExternalStorageWritable() && extRWPermissionGranted && isSaveExternal) {
            recDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC) + "/AudioRecorder");
            if(!recDir.exists()) {
                boolean mkdirSuccess = recDir.mkdir();
            }
            if(recDir != null) {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
                recFileName = "/" + timeStamp + recFileExtension;
                displayFileName = recFileName;
                try {
                    recFile = new File(recDir, recFileName);
                    textViewFileName.setText(recFileName);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    textViewFileName.setText("<no file>");
                }
            }
        }
        else {
            // TODO: 16/04/2018 Add creation of recFile on internal storage
            Toast.makeText(this, "Should create recFile on internal storage.", Toast.LENGTH_SHORT).show();
            textViewFileName.setText("<no file>");
        }
        textViewStatus.setText("Ready");
        toggleButtonPlay.setEnabled(false);
        if (shareMenuItem != null) {
            shareMenuItem.setEnabled(false);
        }
        fileEmpty = true;
        return recFileName;
    }

    public native String stringFromJNI();

    public class DisplayPlayTime extends AsyncTask {

        int duration = player.getDuration() / 1000;
        int minD = duration / 60;
        int secD = duration % 60;

        @Override
        protected Object doInBackground(Object[] objects) {
            while (isPlaying) {
                int elapsed = 0;
                try {
                    elapsed = player.getCurrentPosition() / 1000;
                } catch (Exception e) {
                    e.printStackTrace();
                    isPlaying = false;
                    break;
                }
                publishProgress(elapsed);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);
            int minE = (int) values[0] / 60;
            int secE = (int) values[0] % 60;
            String toShow = String.format(Locale.US, "%02d:%02d / %02d:%02d", minE, secE, minD, secD);
            textViewFileName.setText(toShow);
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            textViewFileName.setText(displayFileName);
        }
    }

    public class SkipFast extends AsyncTask {

        private boolean isFW;

        public void setFW(boolean fw) {
            isFW = fw;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            Log.i("SkipFast", "doInBackground");
            if (isFW) {
                while (buttonSkipFW.isPressed()){
                    skipPlaying(isFW);
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!isFW) {
                while (buttonSkipBW.isPressed()){
                    skipPlaying(isFW);
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
    }
}
