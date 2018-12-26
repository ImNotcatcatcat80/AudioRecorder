package it.zerozero.audiorecorder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by David on 21/05/2018.
 */

public class RecordingsAdapter extends ArrayAdapter {

    Context context;
    private ArrayList<RecordingFile> recordingFileArrayList;
    private int lastPosition = -1;

    private static class ViewHolder {
        TextView txtFileName;
        TextView txtSize;
        TextView txtIndex;
        ImageView imgMic;
    }

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param objects  The objects to represent in the ListView.
     */
    public RecordingsAdapter(@NonNull Context context, @NonNull List objects) {
        super(context, R.layout.filename_item, objects);
        this.recordingFileArrayList = (ArrayList<RecordingFile>) objects;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        RecordingFile recordingFile = (RecordingFile) getItem(position);
        ViewHolder viewHolder;
        final View result;

         if (convertView == null) {
             viewHolder = new ViewHolder();
             LayoutInflater inflater = LayoutInflater.from(getContext());
             convertView = inflater.inflate(R.layout.filename_item, parent, false);
             viewHolder.txtFileName = (TextView) convertView.findViewById(R.id.textViewItemFileName);
             viewHolder.txtSize = (TextView) convertView.findViewById(R.id.textViewItemSize);
             viewHolder.txtIndex = (TextView) convertView.findViewById(R.id.textViewItemIndex);
             viewHolder.imgMic = (ImageView) convertView.findViewById(R.id.imageViewItemRedMic);
             result = convertView;
             convertView.setTag(viewHolder);
         }
         else {
              viewHolder = (ViewHolder) convertView.getTag();
              result = convertView;
         }

         lastPosition = position;

        try {
            viewHolder.txtFileName.setText(recordingFile.getFileName());
            long fileSize = recordingFile.getSize();
            if (fileSize < 1000000) {
                viewHolder.txtSize.setText(String.format(Locale.US, "%.1f KiB", (float) fileSize / 1000));
            }
            else {
                viewHolder.txtSize.setText(String.format(Locale.US, "%.1f MB", (float) fileSize / 1000000));
            }
            viewHolder.txtIndex.setText(String.format(Locale.US, "# %d", position));
            String extension = getExtension(recordingFile.getFileName());
            Log.i("extension", extension);
            if (extension.equals("m4a")) {
                viewHolder.imgMic.setImageResource(R.drawable.ic_m4a);
            }
            else if (extension.equals("3gp")) {
                viewHolder.imgMic.setImageResource(R.drawable.ic_3gp);
            }
            else {
                viewHolder.imgMic.setImageResource(R.drawable.ic_file_generic);
            }
            viewHolder.imgMic.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;
    }

    public String getExtension(String filePath){
        int strLength = filePath.lastIndexOf(".");
        if(strLength > 0)
            return filePath.substring(strLength + 1).toLowerCase();
        return null;
    }

}
