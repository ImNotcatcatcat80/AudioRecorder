package it.zerozero.audiorecorder;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchShowPause;
    private Switch switchSaveExternal;
    private RadioGroup radioGroupFormat;
    private RadioButton radioButton3GPhc;
    private RadioButton radioButton3GPhq;
    private RadioButton radioButtonMP4;
    private RadioGroup radioGroupAudioSource;
    private RadioButton radioButtonMic;
    private RadioButton radioButtonCall;
    private RadioButton radioButtonIncomingCall;
    private SeekBar seekBarBitRate;
    private TextView textViewSeekBarValue;
    private boolean isSaveExternal;
    private boolean isShowPause;
    private int fileFormat;
    private int audioSource;
    private int selectedBitRate;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor preferencesEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences("AudioRecorderRec", MODE_PRIVATE);

        switchShowPause = findViewById(R.id.switchShowPause);
        switchShowPause.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isShowPause = isChecked;
            }
        });

        switchSaveExternal = findViewById(R.id.switchSaveExternal);
        switchSaveExternal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isSaveExternal = isChecked;
                if(!isChecked) {
                    SetSaveExternal setSaveExternal = new SetSaveExternal();
                    setSaveExternal.execute();
                }
            }
        });

        radioGroupFormat = findViewById(R.id.RadioGroupFormat);
        radioButton3GPhc = findViewById(R.id.radioButton3GPhc);
        radioButton3GPhq = findViewById(R.id.radioButton3GPhq);
        radioButtonMP4 = findViewById(R.id.radioButtonMP4);
        radioGroupFormat.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.i("radioGroupFormat", "onCheckedChanged");
                if (checkedId == R.id.radioButton3GPhc) {
                    fileFormat = MainActivity.FORMAT_3GPHC;
                    seekBarBitRate.setMax(3);
                    seekBarBitRate.setProgress(3);
                    seekBarBitRate.setEnabled(false);
                    selectedBitRate = 23850;
                    textViewSeekBarValue.setText(String.format(Locale.US, "23.85 Kbps", selectedBitRate));
                }
                if (checkedId == R.id.radioButton3GPhq) {
                    fileFormat = MainActivity.FORMAT_3GPHQ;
                    seekBarBitRate.setEnabled(true);
                    seekBarBitRate.setMax(7);
                    seekBarBitRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            int bitrate = (progress + 1)*16;
                            textViewSeekBarValue.setText(String.format(Locale.US, "%d Kbps", bitrate));
                            selectedBitRate = bitrate * 1000;
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });
                    seekBarBitRate.setProgress((selectedBitRate / 16000) - 1);
                }
                if (checkedId == R.id.radioButtonMP4) {
                    fileFormat = MainActivity.FORMAT_MP4;
                    seekBarBitRate.setEnabled(true);
                    seekBarBitRate.setMax(7);
                    seekBarBitRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            int bitrate = (progress + 1)*16;
                            textViewSeekBarValue.setText(String.format(Locale.US, "%d Kbps", bitrate));
                            selectedBitRate = bitrate * 1000;
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });
                    seekBarBitRate.setProgress((selectedBitRate / 16000) - 1);
                }
            }
        });

        radioGroupAudioSource = findViewById(R.id.RadioGroupAudioSource);
        radioButtonMic = findViewById(R.id.radioButtonMic);
        radioButtonCall = findViewById(R.id.radioButtonCall);
        radioButtonIncomingCall = findViewById(R.id.radioIncomingCall);
        radioGroupAudioSource.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.i("radioGroupAudioSource", "onCheckedChanged");
                if (checkedId == R.id.radioButtonMic) {
                    audioSource = MainActivity.AUDIO_MIC;
                }
                if (checkedId == R.id.radioButtonCall) {
                    audioSource = MainActivity.AUDIO_CALL;
                }
                if (checkedId == R.id.radioIncomingCall) {
                    audioSource = MainActivity.AUDIO_INCOMING_CALL;
                }
                if (checkedId != R.id.radioButtonMic) {
                    RevertAudioSource revertAudioSource = new RevertAudioSource();
                    revertAudioSource.execute();
                }
            }
        });
        seekBarBitRate = findViewById(R.id.seekBarBitRate);
        textViewSeekBarValue = findViewById(R.id.textViewSeekBarValue);
    }

    @Override
    protected void onResume() {
        super.onResume();

        preferencesEditor = sharedPreferences.edit();
        switchShowPause.setChecked(sharedPreferences.getBoolean("ShowPause", true));
        switchSaveExternal.setChecked(sharedPreferences.getBoolean("SaveExternal", true));
        fileFormat = sharedPreferences.getInt("Format", 1503);
        audioSource = sharedPreferences.getInt("AudioSource", 1851);
        selectedBitRate = sharedPreferences.getInt("SelectedBitRate", 16000);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        if(!switchSaveExternal.isChecked()){
            SetSaveExternal setSaveExternal = new SetSaveExternal();
            setSaveExternal.execute();
        }

        if (fileFormat == MainActivity.FORMAT_3GPHC) {
            radioButton3GPhc.setChecked(true);
        }
        else if (fileFormat == MainActivity.FORMAT_3GPHQ) {
            radioButton3GPhq.setChecked(true);
        }
        else if (fileFormat == MainActivity.FORMAT_MP4) {
            radioButtonMP4.setChecked(true);
        }
        else {
            radioGroupFormat.clearCheck();
        }

        if (audioSource == MainActivity.AUDIO_MIC) {
            radioButtonMic.setChecked(true);
        }
        else if (audioSource == MainActivity.AUDIO_CALL) {
            radioButtonCall.setChecked(true);
        }
        else if (audioSource == MainActivity.AUDIO_INCOMING_CALL) {
            radioButtonIncomingCall.setChecked(true);
        }
        else {
            radioGroupAudioSource.clearCheck();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        preferencesEditor.putBoolean("ShowPause", isShowPause);
        preferencesEditor.putBoolean("SaveExternal", isSaveExternal);
        preferencesEditor.putInt("Format", fileFormat);
        preferencesEditor.putInt("AudioSource", audioSource);
        preferencesEditor.putInt("SelectedBitRate", selectedBitRate);
        preferencesEditor.commit();
        Log.d("Settings oP Format", String.valueOf(fileFormat));
    }

    class SetSaveExternal extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            publishProgress();
            return null;
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);
            switchSaveExternal.setChecked(true);
            Toast.makeText(SettingsActivity.this, "This is not ready yet.", Toast.LENGTH_SHORT).show();
        }

    }

    class RevertAudioSource extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            publishProgress();
            return null;
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);
            radioButtonMic.setChecked(true);
            Toast.makeText(SettingsActivity.this, "This won't work.", Toast.LENGTH_SHORT).show();
        }

    }

}
