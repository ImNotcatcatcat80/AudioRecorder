package it.zerozero.audiorecorder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;

/**
 * Created by David on 26/05/2018.
 */

public class ManageDirectoryDialog extends DialogFragment {

    private View fragmentView;

    static ManageDirectoryDialog newInstance(String title){
        ManageDirectoryDialog fragment = new ManageDirectoryDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String title = getArguments().getString("title");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.ic_folder_black_24dp);
        builder.setTitle("Recording " + title);
        builder.setView(R.layout.dialog_manage_files);
        Dialog dialog = builder.create();
        return dialog;
    }
}
