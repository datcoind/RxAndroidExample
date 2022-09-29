package com.example.rxandroid;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import com.example.rxandroid.models.PhotoModel;

import java.io.File;
import java.util.ArrayList;

public class FileUtils {
    private static final String TAG = "FileUtils";
    private static int LIMIT_PHOTO_LOAD_MORE = 500;

    public static ArrayList<PhotoModel> getAllPhotoFromDevice(Context context, int offset) {
        ArrayList<PhotoModel> arrPhoto = new ArrayList<>();

        Uri contentUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        String[] projection = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA
        };
        String sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " DESC LIMIT " +
                LIMIT_PHOTO_LOAD_MORE + " OFFSET " + (offset * LIMIT_PHOTO_LOAD_MORE);
        try {
            Cursor cursor;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                Bundle bundle = new Bundle();
                bundle.putInt(ContentResolver.QUERY_ARG_LIMIT, LIMIT_PHOTO_LOAD_MORE);
                bundle.putInt(ContentResolver.QUERY_ARG_OFFSET, offset * LIMIT_PHOTO_LOAD_MORE);
                bundle.putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, new String[]{MediaStore.Images.Media.DATE_MODIFIED});
                bundle.putInt(ContentResolver.QUERY_ARG_SORT_DIRECTION, ContentResolver.QUERY_SORT_DIRECTION_DESCENDING);
                cursor = context.getContentResolver().query(contentUri, projection, bundle, null);
            } else {
                cursor = context.getContentResolver().query(
                        contentUri,
                        projection,
                        null,
                        null,
                        sortOrder
                );
            }
            // Cache column indices.
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

            while (cursor.moveToNext()) {
                // Get values of columns for a given video.
                long id = cursor.getLong(idColumn);
                String data = cursor.getString(dataColumn);

                Uri uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);

                // Stores column values and the contentUri in a local object
                // that represents the media file.
                File file = new File(data);
                if (file.exists()) {
                    arrPhoto.add(new PhotoModel(data));
                }
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrPhoto;
    }
}
