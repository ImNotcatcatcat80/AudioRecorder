package it.zerozero.audiorecorder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by David on 05/03/2018.
 */

public class ManageFileDialog extends DialogFragment {

    private boolean confirmDelete = false;
    private View fragmentView;

    static ManageFileDialog newInstance(String title){
        ManageFileDialog fragment = new ManageFileDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String title = getArguments().getString("title");
        CharSequence[] confdel = new CharSequence[1];
        confdel[0] = "Confirm Delete";
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.ic_folder_black_24dp);
        builder.setTitle("File " + title);
        builder.setMultiChoiceItems(confdel, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        confirmDelete = isChecked;
                        ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(isChecked);
                    }
                });
        builder.setNeutralButton("Load", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((ListActivity) getActivity()).doLoadClick();
                    }
                });
        builder.setPositiveButton("Rename",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((ListActivity) getActivity()).doRenameClick();
                            }
                        }
                );
        builder.setNegativeButton("Delete",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (confirmDelete) {
                                    ((ListActivity) getActivity()).doDeleteClick();
                                    Toast.makeText(getActivity().getApplicationContext(), title + " deleted.", Toast.LENGTH_SHORT).show();
                                }
                                else Toast.makeText(getActivity().getApplicationContext(), "Check confirmation to delete!", Toast.LENGTH_SHORT).show();
                            }
                        }
                );
        Dialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
            }
        });

        return dialog;
    }

}
