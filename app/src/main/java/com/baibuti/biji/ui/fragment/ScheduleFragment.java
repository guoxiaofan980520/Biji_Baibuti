package com.baibuti.biji.ui.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.baibuti.biji.model.dao.DaoStrategyHelper;
import com.baibuti.biji.model.dao.daoInterface.IScheduleDao;
import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.po.MySubject;
import com.baibuti.biji.R;
import com.baibuti.biji.service.scut.ScheduleService;
import com.baibuti.biji.ui.IContextHelper;
import com.baibuti.biji.ui.activity.MainActivity;
import com.baibuti.biji.ui.activity.WebViewActivity;
import com.zhuangfei.timetable.TimetableView;
import com.zhuangfei.timetable.view.WeekView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemSelected;

public class ScheduleFragment extends BaseFragment implements IContextHelper {

    private View view;

    @BindView(R.id.schedulefragment_weekview)
    private WeekView m_weekView;

    @BindView(R.id.schedulefragment_timetableView)
    private TimetableView m_timetableView;

    @BindView(R.id.schedulefragment_week_textview)
    private TextView m_titleTextView;
    private static final int REQ_BROWSER = 100;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null != view) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (null != parent)
                parent.removeView(view);
        } else {
            view = inflater.inflate(R.layout.fragment_schedule, container, false);

            initView(view);
        }
        return view;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    private void initView(View view) {
        setHasOptionsMenu(true);

        // Toolbar
        Toolbar m_toolbar = view.findViewById(R.id.ScheduleFrag_Toolbar);
        m_toolbar.setTitle("课程表");
        m_toolbar.inflateMenu(R.menu.schedule_frag_action);
        m_toolbar.setNavigationIcon(R.drawable.tab_menu);
        m_toolbar.setNavigationOnClickListener((View v) -> {
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) activity.openNavMenu();
        });
        // m_toolbar.setPopupTheme(R.style.popup_theme);

        // WeekView
        m_weekView.curWeek(1)
            .callback((int week) -> {
                int cur = m_timetableView.curWeek();
                // 更新切换后的日期，从当前周cur->切换的周week
                m_timetableView.onDateBuildListener()
                    .onUpdateDate(cur, week);
                m_timetableView.changeWeekOnly(week);
            })
            .callback(this::ChangeCurrWeek_Clicked)
            .isShow(false)
            .showView();
        // m_weekView.source(mySubjects).showView();

        // TimeTable
        m_timetableView.curWeek(1)
            .curTerm("大三下学期")
            .callback((int curWeek) ->
                m_titleTextView.setText(String.format(Locale.CHINA, "第 %d 周", curWeek)))
            .showView();
        // m_timetableView.source(mySubjects).showView();

        // Load data
        ActionRefresh_Clicked();
    }

    /**
     * 主界面点击 点现周次界面
     */
    @OnClick(R.id.schedulefragment_layout)
    private void MainLayout_Clicked() {
        if (m_weekView.isShowing()) {
            m_weekView.isShow(false);
            // 返回当前周
            int cur = m_timetableView.curWeek();
            m_timetableView.onDateBuildListener().onUpdateDate(cur, cur);
            m_timetableView.changeWeekOnly(cur);
        }
        else
            m_weekView.isShow(true);
    }

    /**
     * WeekView 修改当前周次
     */
    private void ChangeCurrWeek_Clicked() {
        final String[] items = new String[m_weekView.itemCount()];
        for (int i = 0; i < m_weekView.itemCount(); i++)
            items[i] = String.format(Locale.CHINA, "第 %d 周", i + 1);

        showAlert(getActivity(), "设置当前周", items, (v, w) -> {
            m_weekView.curWeek(w + 1).updateView();
            m_timetableView.changeWeekForce(w + 1);
        });
    }

    /**
     * 课程表导入回调
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_BROWSER && resultCode == 101) {
            String html = data.getStringExtra("html");
            CbImportSchedule(html);
        }
    }

    /**
     * ActionBar 导入课程表
     */
    @OnItemSelected(R.id.action_import_schedule)
    private void ActionImportSchedule_Clicked() {
        Intent intent = new Intent(getContext(), WebViewActivity.class);
        startActivityForResult(intent, REQ_BROWSER);
    }

    /**
     * 活动返回，解析数据 (onActivityResult)
     */
    private void CbImportSchedule(String html) {
        List<MySubject> mySubjects = ScheduleService.parseHtml(html);
        if (mySubjects.size() == 0) {
            showAlert(getActivity(), "错误", "返回的课程表数据无法解析。");
            return;
        }

        // show
        m_weekView.source(mySubjects).showView();
        m_timetableView.source(mySubjects).updateView();

        // save
        IScheduleDao scheduleDao = DaoStrategyHelper.getInstance().getScheduleDao(getActivity());
        try {
            scheduleDao.updateSchedule(MySubject.toJsons(mySubjects));
        } catch (ServerException ex) {
            ex.printStackTrace();
            showAlert(getActivity(), "错误", ex.getMessage());
        }
    }

    /**
     * ActionBar 刷新课程表
     */
    @OnItemSelected(R.id.action_refresh_schedule)
    private void ActionRefresh_Clicked() {
        ProgressDialog progressDialog = showProgress(getActivity(), "加載中...", false, null);

        // Load Dao
        IScheduleDao scheduleDao = DaoStrategyHelper.getInstance().getScheduleDao(getContext());
        String scheduleJson;
        try {
            scheduleJson = scheduleDao.querySchedule();
        } catch (ServerException ex) {
            if (progressDialog.isShowing()) progressDialog.dismiss();
            showAlert(getActivity(), "错误", ex.getMessage());
            return;
        }
        if (scheduleJson.trim().isEmpty()) {
            if (progressDialog.isShowing()) progressDialog.dismiss();
            showToast(getActivity(), "尚未设置课程表");
            return;
        }

        // Show Schedule
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        List<MySubject> mySubjects = MySubject.fromJson(scheduleJson);
        m_weekView.source(mySubjects).showView();
        m_timetableView.source(mySubjects).updateView();

        if (progressDialog.isShowing())
            progressDialog.dismiss();
        showToast(getActivity(), "刷新完成");
    }

    /**
     * ActionBar 删除课程表
     */
    @OnItemSelected(R.id.action_delete_schedule)
    private void ActionDeleteSchedule_Clicked() {
        showAlert(getActivity(),
            "删除", "是否删除课程表？",
            "确定", (d, v) -> {
                IScheduleDao scheduleDao = DaoStrategyHelper.getInstance().getScheduleDao(getActivity());
                try {
                    if (scheduleDao.deleteSchedule()) {
                        showToast(getActivity(), "删除课表成功");
                        m_weekView.source(new ArrayList<MySubject>()).updateView();
                        m_timetableView.source(new ArrayList<MySubject>()).updateView();
                    }
                } catch (ServerException ex) {
                    ex.printStackTrace();
                    showAlert(getActivity(), "错误", ex.getMessage());
                }
            },
            "取消", null
        );
    }
}
