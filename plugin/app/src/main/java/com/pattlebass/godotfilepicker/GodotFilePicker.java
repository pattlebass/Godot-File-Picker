package com.pattlebass.godotfilepicker;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.collection.ArraySet;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.SignalInfo;
import org.godotengine.godot.plugin.UsedByGodot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

public class GodotFilePicker extends org.godotengine.godot.plugin.GodotPlugin {
    private static final String TAG = "godot";
    private Activity activity;
    private Context context;

    private ContentResolver contentResolver;

    private static final int OPEN_FILE = 0;

    public GodotFilePicker(Godot godot) {
        super(godot);
        activity = godot.getActivity();
        context = godot.getContext();
        contentResolver = context.getContentResolver();
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "GodotFilePicker";
    }

    @NonNull
    @Override
    public Set<SignalInfo> getPluginSignals() {
        Set<SignalInfo> signals = new ArraySet<>();

        signals.add(new SignalInfo("file_picked", String.class, String.class));
        return signals;
    }

    @UsedByGodot
    public void openFilePicker(String type) {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType(type.isEmpty() ? "*/*" : type);
        chooseFile = Intent.createChooser(chooseFile, "Choose a project");
        activity.startActivityForResult(chooseFile, OPEN_FILE);
    }

    @Override
    public void onMainActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == OPEN_FILE && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.d(TAG, "Picked file with URI: " + uri.getPath());
                try {
                    emitSignal("file_picked", getFile(context, uri).getPath(),
                                contentResolver.getType(uri));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // From https://stackoverflow.com/questions/65447194/how-to-convert-uri-to-file-android-10

    public static File getFile(Context context, Uri uri) throws IOException {
        String directoryName = context.getFilesDir().getPath() + File.separatorChar + "_temp";
        File directory = new File(directoryName);
        if (! directory.exists()){
            directory.mkdir();
        }
        File destinationFilename = new File(directoryName + File.separatorChar + queryName(context, uri));
        try (InputStream ins = context.getContentResolver().openInputStream(uri)) {
            createFileFromStream(ins, destinationFilename);
        } catch (Exception ex) {
            Log.e("Save File", ex.getMessage());
            ex.printStackTrace();
        }
        return destinationFilename;
    }

    public static void createFileFromStream(InputStream ins, File destination) {
        try (OutputStream os = new FileOutputStream(destination)) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = ins.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
        } catch (Exception ex) {
            Log.e("Save File", ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static String queryName(Context context, Uri uri) {
        Cursor returnCursor =
                context.getContentResolver().query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }
}
