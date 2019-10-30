package com.baibuti.biji.data.dao.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.baibuti.biji.data.dao.DbOpenHelper;
import com.baibuti.biji.data.dao.daoInterface.INoteDao;
import com.baibuti.biji.data.model.Group;
import com.baibuti.biji.data.model.Note;
import com.baibuti.biji.util.otherUtil.DateColorUtil;

import java.util.ArrayList;
import java.util.List;

public class NoteDao implements INoteDao {

    private final static String TBL_NAME = "db_note";

    private final static String COL_ID = "n_id";
    private final static String COL_TITLE = "n_title";
    private final static String COL_CONTENT = "n_content";
    private final static String COL_GROUP_ID = "n_group_id";
    private final static String COL_CREATE_TIME = "n_create_time";
    private final static String COL_UPDATE_TIME = "n_update_time";

    private DbOpenHelper helper;
    private GroupDao groupDao;

    public NoteDao(Context context) {
        helper = new DbOpenHelper(context);
        groupDao = new GroupDao(context);

        // 是否为空
        // if (queryAllNotes().isEmpty())
        //     insertNote(Note.DEF_NOTE);
    }

    /**
     * 查询所有笔记
     * @return 笔记列表
     */
    @Override
    public List<Note> queryAllNotes() {
        return queryNotesByGroupId(-1);
    }

    /**
     * 根据分组查询所有笔记
     * @param groupId -1 for all
     */
    @Override
    public List<Note> queryNotesByGroupId(int groupId) {

        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = null;
        String sql = "select * from " + TBL_NAME + ((groupId < 0) ? "" : " where " + COL_GROUP_ID + " = " + groupId);

        List<Note> noteList = new ArrayList<>();
        Note note;

        try {
            cursor = db.rawQuery(sql, null);

            while (cursor.moveToNext()) {

                note = new Note();
                note.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
                note.setTitle(cursor.getString(cursor.getColumnIndex(COL_TITLE)));
                note.setContent(cursor.getString(cursor.getColumnIndex(COL_CONTENT)));

                Group group = groupDao.queryGroupById(cursor.getInt(cursor.getColumnIndex(COL_GROUP_ID)));
                if (group == null)
                    group = groupDao.queryDefaultGroup();

                note.setGroup(group, false);
                note.setCreateTime(DateColorUtil.Str2Date(cursor.getString(cursor.getColumnIndex(COL_CREATE_TIME))));
                note.setUpdateTime(DateColorUtil.Str2Date(cursor.getString(cursor.getColumnIndex(COL_UPDATE_TIME))));

                noteList.add(note);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed())
                cursor.close();
            if (db != null && db.isOpen())
                db.close();
        }

        return noteList;
    }

    /**
     * 根据 nid 查询笔记
     * @param noteId 笔记 id
     */
    @Override
    public Note queryNoteById(int noteId) {

        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = null;
        String sql = "select * from " + TBL_NAME + " where " + COL_ID + " = " + noteId;

        Note note = null;
        try {
            cursor = db.rawQuery(sql, null);

            while (cursor.moveToNext()) {

                note = new Note();
                note.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
                note.setTitle(cursor.getString(cursor.getColumnIndex(COL_TITLE)));
                note.setContent(cursor.getString(cursor.getColumnIndex(COL_CONTENT)));

                Group group = groupDao.queryGroupById(cursor.getInt(cursor.getColumnIndex(COL_GROUP_ID)));
                if (group == null)
                    group = groupDao.queryDefaultGroup();

                note.setGroup(group, false);
                note.setCreateTime(DateColorUtil.Str2Date(cursor.getString(cursor.getColumnIndex(COL_CREATE_TIME))));
                note.setUpdateTime(DateColorUtil.Str2Date(cursor.getString(cursor.getColumnIndex(COL_UPDATE_TIME))));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed())
                cursor.close();
            if (db != null && db.isOpen())
                db.close();
        }
        return note;
    }

    /**
     * 插入笔记
     * @param note 新笔记，自动编号
     * @return 笔记 id
     */
    public long insertNote(Note note) {

        SQLiteDatabase db = helper.getWritableDatabase();
        String sql =
                "insert into " + TBL_NAME +
                "(" + COL_TITLE + ", " + COL_CONTENT + ", " + COL_GROUP_ID + ", " + COL_CREATE_TIME + ", " + COL_UPDATE_TIME + ")" +
                "values (?, ?, ?, ?, ?)";
        SQLiteStatement stat = db.compileStatement(sql);
        db.beginTransaction();

        long ret_id = -1;
        try {
            stat.bindString(1, note.getTitle()); // COL_TITLE
            stat.bindString(2, note.getContent()); // COL_TITLE
            stat.bindLong(3, note.getGroup().getId()); // COL_GROUP_ID
            stat.bindString(4, DateColorUtil.Date2Str(note.getCreateTime())); // COL_CREATE_TIME
            stat.bindString(5, DateColorUtil.Date2Str(note.getUpdateTime())); // COL_UPDATE_TIME

            ret_id = stat.executeInsert();
            db.setTransactionSuccessful();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            db.endTransaction();
            db.close();
        }

        return ret_id;
    }

    /**
     * 更新笔记
     * @param note 覆盖更新笔记
     * @return 是否成功更新 (更新非 0 项)
     */
    @Override
    public boolean updateNote(Note note) {

        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_TITLE, note.getTitle());
        values.put(COL_CONTENT, note.getContent());
        values.put(COL_GROUP_ID, note.getGroup().getId());
        values.put(COL_UPDATE_TIME, DateColorUtil.Date2Str(note.getUpdateTime()));

        int ret = db.update(TBL_NAME, values, COL_ID + " = ?", new String[] { String.valueOf(note.getId()) });
        db.close();

        return ret != 0;
    }

    /**
     * 删除笔记
     * @param noteId 删除笔记的 id
     * @return 是否成功删除 (删除 1 项)
     */
    @Override
    public boolean deleteNote(int noteId) {

        SQLiteDatabase db = helper.getWritableDatabase();

        int ret = 0;
        try {
            ret = db.delete(TBL_NAME, COL_ID + " = ?", new String[] { String.valueOf(noteId) });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (db != null && db.isOpen())
                db.close();
        }

        return ret == 1;
    }
}
