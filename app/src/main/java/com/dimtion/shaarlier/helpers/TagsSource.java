package com.dimtion.shaarlier.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.dimtion.shaarlier.utils.ShaarliAccount;
import com.dimtion.shaarlier.utils.Tag;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dimtion on 12/05/2015.
 * Interface between the table TAGS and the JAVA objects
 */
class TagsSource {
    private final String[] allColumns = {MySQLiteHelper.TAGS_COLUMN_ID,
            MySQLiteHelper.TAGS_COLUMN_ID_ACCOUNT,
            MySQLiteHelper.TAGS_COLUMN_TAG};
    private final MySQLiteHelper dbHelper;
    private SQLiteDatabase db;

    TagsSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    void rOpen() throws SQLException {
        db = dbHelper.getReadableDatabase();
    }

    void wOpen() throws SQLException {
        db = dbHelper.getWritableDatabase();
    }

    void close() {
        dbHelper.close();
    }

    List<Tag> getAllTags() {
        List<Tag> tags = new ArrayList<>();

        Cursor cursor = db.query(MySQLiteHelper.TABLE_TAGS, allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Tag account = cursorToTag(cursor);
            tags.add(account);
            cursor.moveToNext();
        }

        cursor.close();
        return tags;
    }

    Tag createTag(ShaarliAccount masterAccount, String value) {
        Tag tag = new Tag();
        tag.setMasterAccount(masterAccount);
        tag.setValue(value.trim());

        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.TAGS_COLUMN_ID_ACCOUNT, masterAccount.getId());
        values.put(MySQLiteHelper.TAGS_COLUMN_TAG, tag.getValue());

        // If existing, do nothing :
        String[] getTagArgs = {String.valueOf(tag.getMasterAccountId()), tag.getValue()};

        Cursor cursor = db.query(MySQLiteHelper.TABLE_TAGS, allColumns,
                MySQLiteHelper.TAGS_COLUMN_ID_ACCOUNT + " = ? AND " +
                        MySQLiteHelper.TAGS_COLUMN_TAG + " = ?",
                getTagArgs, null, null, null);
        try {
            cursor.moveToFirst();
            if (cursor.isAfterLast()) {
                long insertId = db.insert(MySQLiteHelper.TABLE_TAGS, null, values);
                tag.setId(insertId);
                return tag;
            } else {
                tag = cursorToTag(cursor);
            }
        } catch (Exception e){
            tag = null;
        } finally {
            cursor.close();
        }
        return tag;
    }

    private Tag cursorToTag(@NonNull Cursor cursor) {  // If necessary (later), load the full account in the tag
        Tag tag = new Tag();
        tag.setId(cursor.getLong(0));
        tag.setMasterAccountId(cursor.getLong(1));
        tag.setValue(cursor.getString(2));
        return tag;
    }

    private void deleteAllTags() {
        db.delete(MySQLiteHelper.TABLE_TAGS, null, null);
    }
}
