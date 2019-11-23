package com.baibuti.biji.ui.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baibuti.biji.R;
import com.baibuti.biji.model.dao.local.DownloadedDao;
import com.baibuti.biji.model.po.Document;
import com.baibuti.biji.service.doc.DocService;
import com.baibuti.biji.ui.IContextHelper;
import com.baibuti.biji.ui.adapter.DocumentAdapter;
import com.baibuti.biji.ui.widget.listView.RecyclerViewEmptySupport;
import com.baibuti.biji.util.filePathUtil.AppPathUtil;
import com.baibuti.biji.util.otherUtil.LayoutUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FileDownloadActivity extends AppCompatActivity implements IContextHelper {

    private DocumentAdapter m_documentAdapter;

    @BindView(R.id.id_download_srl)
    SwipeRefreshLayout m_srl;

    private com.wyt.searchbox.SearchFragment m_searchFragment;

    private Dialog m_LongClickItemPopupMenu;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloaded);
        ButterKnife.bind(this);

        m_documentAdapter = new DocumentAdapter(this);
        RecyclerViewEmptySupport itemListView = findViewById(R.id.id_download_recycler_view);
        itemListView.setEmptyView(findViewById(R.id.id_download_empty_view));
        itemListView.setAdapter(m_documentAdapter);

        m_documentAdapter.setDocumentList(new ArrayList<>());
        m_documentAdapter.setOnDocumentClickListener(this::DocumentListItem_Clicked);
        m_documentAdapter.setOnDocumentLongClickListener(this::DocumentListItem_LongClicked);
        m_srl.setOnRefreshListener(this::initData);

        // Search Frag
        m_searchFragment = com.wyt.searchbox.SearchFragment.newInstance();
        m_searchFragment.setAllowReturnTransitionOverlap(true);
        m_searchFragment.setOnSearchClickListener((keyword) -> {
            if (keyword.trim().isEmpty()) {
                showToast(this, "搜索内容不为空");
                return;
            }
            // TODO
            showToast(this, "未实现");
        });

        initData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.download_act_action, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_DownloadSearch:
                m_searchFragment.show(getSupportFragmentManager(), com.wyt.searchbox.SearchFragment.TAG);
                break;
            case R.id.action_DownloadClear:
                ActionClear_Clicked();
                break;
        }
        return true;
    }

    /**
     * 初始化数据 / srl
     */
    private void initData() {
        DownloadedDao downloadedDao = new DownloadedDao(this);
        List<Document> documentList = downloadedDao.GetAllDownloadedItem();
        Collections.sort(documentList);
        m_documentAdapter.setDocumentList(documentList);
        m_documentAdapter.notifyDataSetChanged();
        if (m_srl.isRefreshing())
            m_srl.setRefreshing(false);
    }

    /**
     * ListView 单击项
     */
    private void DocumentListItem_Clicked(Document document) {
        showAlert(this, "打开文档", "是否打开下载的文档 \"" + document.getBaseFilename() + "\"？",
            "打开", (d, w) -> {

                File file = new File(document.getFilename());
                String path = AppPathUtil.getFilePathByUri(this, Uri.fromFile(file));

                if (!file.exists() || path == null) {
                    showAlert(this, "错误", "文档 \"" + document.getBaseFilename() + "\" 不存在，是否删除下载记录？",
                        "删除", (d1, w1) -> {
                            DownloadedDao downloadedDao = new DownloadedDao(this);
                            if (downloadedDao.DeleteDownloadItem(document.getFilename())) {
                                m_documentAdapter.getDocumentList().remove(document);
                                m_documentAdapter.notifyDataSetChanged();
                            }
                        }, "取消", null
                    );
                } else if (!DocService.openFile(this, file))
                    showAlert(this, "错误", "打开文件错误，文件格式不支持。");
            },
            "取消", null
        );
    }

    /**
     * ListView 长按项弹出菜单
     */
    private void DocumentListItem_LongClicked(Document document) {
        m_LongClickItemPopupMenu = new Dialog(this, R.style.BottomDialog);
        LinearLayout root = LayoutUtil.initPopupMenu(this, m_LongClickItemPopupMenu, R.layout.popup_download_act_long_click_item);

        ((TextView) root.findViewById(R.id.id_download_popup_curr)).setText(String.format(Locale.CHINA, "当前选中项: %s", document.getBaseFilename()));
        root.findViewById(R.id.id_download_popup_delete_file).setOnClickListener((v) -> PopupDeleteItemAndFile_Clicked(document));
        root.findViewById(R.id.id_download_popup_delete_all).setOnClickListener((v) -> ActionClear_Clicked());
        root.findViewById(R.id.id_download_popup_cancel).setOnClickListener((v) -> m_LongClickItemPopupMenu.dismiss());

        m_LongClickItemPopupMenu.show();
    }

    /**
     * Popup 删除
     */
    private void PopupDeleteItemAndFile_Clicked(Document document) {
        m_LongClickItemPopupMenu.dismiss();
        showAlert(this, "删除", "是否删除下载的文件 \"" + document.getBaseFilename() + "\"？",
            "删除", (d, w) -> {
                if (AppPathUtil.deleteFile(document.getFilename())) {
                    DownloadedDao downloadedDao = new DownloadedDao(this);
                    if (downloadedDao.DeleteDownloadItem(document.getFilename())) {
                        m_documentAdapter.getDocumentList().remove(document);
                        m_documentAdapter.notifyDataSetChanged();
                        showToast(this, "文件 \"" + document.getBaseFilename() + "\" 删除成功");
                        return;
                    }
                }
                showToast(this, "文件 \"" + document.getBaseFilename() + "\" 删除失败");
            },
            "取消", null
        );
    }

    /**
     * ActionBar / Popup 清空 删除
     */
    private void ActionClear_Clicked() {
        if (m_LongClickItemPopupMenu != null && m_LongClickItemPopupMenu.isShowing())
            m_LongClickItemPopupMenu.dismiss();

        showAlert(this, "清空", "是否删除所有下载的文件？",
            "删除", (d, w) -> {
                ProgressDialog progressDialog = showProgress(this, "删除文件中...", false, null);
                DownloadedDao downloadedDao = new DownloadedDao(this);
                List<Document> documents = downloadedDao.GetAllDownloadedItem();
                for (Document document : documents) {
                    if (AppPathUtil.deleteFile(document.getFilename())) {
                        downloadedDao.DeleteDownloadItem(document.getFilename());
                        m_documentAdapter.getDocumentList().remove(document);
                    }
                }
                m_documentAdapter.notifyDataSetChanged();
                progressDialog.dismiss();
                if (m_documentAdapter.getDocumentList().isEmpty())
                    showToast(this, "总共删除 " + documents.size() + " 个文件");
                else
                    showAlert(this, "错误", "还剩下 " + m_documentAdapter.getDocumentList().size() + " 个文件删除失败。");
            },
            "取消", null
        );
    }
}
