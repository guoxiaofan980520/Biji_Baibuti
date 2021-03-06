package com.baibuti.biji.model.dao.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.baibuti.biji.model.po.DownloadItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * SharedPreferences 存放下载完成的文件
 */
public class DownloadedDao {

    private final static String SP_DOWNLOAD = "download_doc";
    private final static String SP_KEY_DOCS = "doc_list";

    private Context context;

    public DownloadedDao(Context context) {
        this.context = context;
    }

    /**
     * SP 所有的下载记录
     */
    public List<DownloadItem> GetAllDownloadedItem() {
        SharedPreferences sp = context.getSharedPreferences(SP_DOWNLOAD, Context.MODE_PRIVATE);
        String[] contents = sp.getStringSet(SP_KEY_DOCS, new TreeSet<>()).toArray(new String[0]);
        List<DownloadItem> returns = new ArrayList<>();
        for (String content : contents) {
            DownloadItem downloadItem = DownloadItem.fromDownloadContent(content);
            if (downloadItem != null)
                returns.add(downloadItem);
        }
        return returns;
    }

    /**
     * 新的下载记录
     * @param path 完整路径
     */
    public void InsertDownloadItem(String path, Date date) {
        SharedPreferences sp = context.getSharedPreferences(SP_DOWNLOAD, Context.MODE_PRIVATE);
        Set<String> set = sp.getStringSet(SP_KEY_DOCS, new TreeSet<>());
        set.add(new DownloadItem(path, date).toDownloadContent());
        sp.edit().putStringSet(SP_KEY_DOCS, set).apply();
    }

    /**
     * 删除下载记录
     * @param path 完整路径
     * @return 是否删除成功
     */
    public boolean DeleteDownloadItem(String path) {
        boolean deleted = false;
        SharedPreferences sp = context.getSharedPreferences(SP_DOWNLOAD, Context.MODE_PRIVATE);
        String[] contents = sp.getStringSet(SP_KEY_DOCS, new TreeSet<>()).toArray(new String[0]);
        List<String> new_contents = new ArrayList<>();
        for (String content : contents) {
            DownloadItem downloadItem = DownloadItem.fromDownloadContent(content);
            if (downloadItem != null && !downloadItem.getFilename().equals(path))
                new_contents.add(content);
            else
                deleted = true;
        }
        sp.edit().putStringSet(SP_KEY_DOCS, new TreeSet<>(new_contents)).apply();
        return deleted;
    }

     /**
      * 删除所有下载记录
      * @return 是否清空成功
      */
     public boolean DeleteAllDownloadItem() {
         SharedPreferences sp = context.getSharedPreferences(SP_DOWNLOAD, Context.MODE_PRIVATE);
         sp.edit().putStringSet(SP_KEY_DOCS, new TreeSet<>()).apply();
         return sp.getStringSet(SP_KEY_DOCS, new TreeSet<>()).isEmpty();
     }
}
