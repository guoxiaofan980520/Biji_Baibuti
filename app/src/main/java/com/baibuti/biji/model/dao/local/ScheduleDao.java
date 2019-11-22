package com.baibuti.biji.model.dao.local;

import android.content.Context;

import com.baibuti.biji.util.filePathUtil.FileNameUtil;
import com.baibuti.biji.util.imgTextUtil.StringUtil;

import java.io.File;
import java.io.IOException;

public class ScheduleDao {

    public ScheduleDao(Context context) { }

    /**
     * 查询本地课表
     * @return Json, empty for error
     */
    public String querySchedule() {
        String filename = FileNameUtil.getScheduleFileName(FileNameUtil.LOCAL);
        String content = StringUtil.readFromFile(filename);
        return content == null ? "" : content;
    }

    /**
     * 新建 / 更新 本地课表文件
     * @param json Json 格式课表
     * @return 是否新建 / 更新成功
     */
    public boolean updateSchedule(String json) {
        String filename = FileNameUtil.getScheduleFileName(FileNameUtil.LOCAL);
        return StringUtil.writeIntoFile(filename, json);
    }

    /**
     * 删除本地课表文件
     * @return 是否成功删除
     */
    public boolean deleteSchedule() {
        String filename = FileNameUtil.getScheduleFileName(FileNameUtil.LOCAL);
        try {
            File file = new File(filename);
            if (file.exists() && !file.delete())
                throw new IOException();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
