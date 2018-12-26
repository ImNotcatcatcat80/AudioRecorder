package it.zerozero.audiorecorder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Created by David on 01/05/2018.
 */

public class RenameFileDialog extends DialogFragment {

    private EditText editTextRenameFile;
    private View fragmentView;
    private String oldFileName = "Old File Name";

    static RenameFileDialog NewInstance(String title) {
        RenameFileDialog fragment = new RenameFileDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOldFileName(String old) {
        oldFileName = old;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // return super.onCreateDialog(savedInstanceState);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        fragmentView = inflater.inflate(R.layout.renamefile_edit, null);
        String title = getArguments().getString("title");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.ic_folder_black_24dp);
        builder.setView(fragmentView);
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((ListActivity) getActivity()).renameFile(editTextRenameFile.getText().toString());
            }
        });
        builder.setTitle(title);
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        editTextRenameFile = fragmentView.findViewById(R.id.EditTextNewName);
        editTextRenameFile.setText(oldFileName);
        editTextRenameFile.selectAll();
    }
}
