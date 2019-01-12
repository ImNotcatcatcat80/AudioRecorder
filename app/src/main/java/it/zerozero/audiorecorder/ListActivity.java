package it.zerozero.audiorecorder;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    // private EditText editTextList;
    private ListView listViewRecordings;
    private TextView textViewDir;
    private String recDirStr = null;
    private int selectedPosition;
    private List<RecordingFile> recordingsList = new ArrayList<>();
    private RecordingsAdapter recordingsAdapter;
    private File recDir = null;
    private boolean sortInverted = false;
    private boolean sortLastModified = false;
    private boolean sortSize = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        textViewDir = findViewById(R.id.textViewDir);
        /** Standard adapter
        fileListAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.filename_line, fileList);
        */
        recordingsAdapter = new RecordingsAdapter(getApplicationContext(), recordingsList);
        listViewRecordings = findViewById(R.id.listViewRecordings);
        listViewRecordings.setAdapter(recordingsAdapter);
        listViewRecordings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ManageFileDialog manageFileDialog = ManageFileDialog.newInstance(recordingsList.get(position).getFileName());
                selectedPosition = position;
                manageFileDialog.show(getFragmentManager(), "ManageFile");
            }
        });
        listViewRecordings.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPosition = position;
                doLoadClick();
                return false;
            }
        });
        textViewDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ManageDirectoryDialog manageDirectoryDialog = ManageDirectoryDialog.newInstance(textViewDir.getText().toString());
                manageDirectoryDialog.show(getFragmentManager(), "Directory");
            }
        });
        recDirStr = getIntent().getStringExtra("recDir");

        generateFileList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_filelist, menu);
        MenuItem mi = menu.add(0, 7, 1, "SortBy...");
        if (!sortSize & !sortLastModified) mi.setIcon(R.drawable.ic_sort_name_az);
        else if (sortSize) mi.setIcon(R.drawable.ic_sort_size_09);
        else if (sortLastModified) mi.setIcon(R.drawable.ic_sort_date_131);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        MenuItem mx = menu.add(0, 8, 0, "SortInverted");
        mx.setIcon(R.drawable.ic_sort_inverted);
        mx.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        mx.setVisible(sortInverted);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == 7) {
            if (sortInverted && !sortSize & !sortLastModified) {
                sortSize = true;
            }
            else if (sortInverted && sortSize & !sortLastModified) {
                sortSize = false;
                sortLastModified = true;
            }
            else if (sortInverted && !sortSize & sortLastModified){
                sortLastModified = false;
            }
            sortInverted = !sortInverted;
            invalidateOptionsMenu();
            generateFileList();
        }
        return super.onOptionsItemSelected(item);
    }

    public void doLoadClick() {
        Intent data = new Intent();
        data.setData(Uri.fromFile(recordingsList.get(selectedPosition).getFile()));
        setResult(RESULT_OK, data);
        finish();
    }

    public void doDeleteClick() {
        try {
            recordingsList.get(selectedPosition).delete();
            recordingsAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
        generateFileList();
    }

    public void doRenameClick() {
        RenameFileDialog renameFileDialog = RenameFileDialog.NewInstance("Rename file");
        renameFileDialog.setOldFileName(recordingsList.get(selectedPosition).getFileName());
        renameFileDialog.show(getFragmentManager(), "RenameFile");
    }

    public void renameFile(String newname) {
        String fileParent = recordingsList.get(selectedPosition).getFile().getParent();
        try {
            recordingsList.get(selectedPosition).getFile().renameTo(new File(fileParent, newname));
            Toast.makeText(this, "Renamed to " + newname, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        generateFileList();
    }

    private void generateFileList() {
        if (recDirStr != null) {
            textViewDir.setText(recDirStr);
            recDir = new File(recDirStr);
            RecordingFile.setDirectory(recDir);

            recordingsList.clear();
            File[] files = recDir.listFiles();
            for (int n = 0; n < files.length; n++) {
                recordingsList.add(n, new RecordingFile(files[n]));
            }

            Comparator<RecordingFile> comparator = new Comparator<RecordingFile>() {
                @Override
                public int compare(RecordingFile f1, RecordingFile f2) {
                    if (sortLastModified) {
                        long l1 = f1.getLastModified();
                        long l2 = f2.getLastModified();
                        if (!sortInverted) {
                            return (int) (l1 - l2);
                        } else {
                            return (int) (l2 - l1);
                        }
                    }
                    else if (sortSize){
                        long l1 = f1.getSize();
                        long l2 = f2.getSize();
                        if (!sortInverted) {
                            return (int) (l1 - l2);
                        } else {
                            return (int) (l2 - l1);
                        }
                    }
                    else {
                        String s1 = f1.getFileName();
                        String s2 = f2.getFileName();
                        if (!sortInverted) {
                            return s1.compareToIgnoreCase(s2);
                        } else {
                            return s2.compareToIgnoreCase(s1);
                        }
                    }
                }
            };

            Collections.sort(recordingsList, comparator);
            recordingsAdapter.notifyDataSetChanged();
        }
        else textViewDir.setText("...");
    }

}
