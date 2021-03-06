package com.baibuti.biji.ui.fragment;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baibuti.biji.common.interact.ProgressHandler;
import com.baibuti.biji.common.interact.InteractInterface;
import com.baibuti.biji.common.auth.AuthService;
import com.baibuti.biji.R;
import com.baibuti.biji.common.auth.dto.AuthRespDTO;
import com.baibuti.biji.ui.activity.AuthActivity;
import com.baibuti.biji.ui.IContextHelper;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegisterFragment extends Fragment implements IContextHelper {

    public View view;

    @BindView(R.id.registerFrag_layout_username)
    TextInputLayout m_RegisterLayout;

    @BindView(R.id.registerFrag_edt_username)
    TextInputEditText m_RegisterEditText;

    @BindView(R.id.registerFrag_layout_password)
    TextInputLayout m_PasswordLayout;

    @BindView(R.id.registerFrag_edt_password)
    TextInputEditText m_PasswordEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null != view) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (null != parent)
                parent.removeView(view);
        }
        else {
            view = inflater.inflate(R.layout.fragment_register, container, false);
            ButterKnife.bind(this, view);

            initView();
        }

        return view;
    }

    /**
     * 初始化界面，无
     */
    private void initView() { }

    /**
     * 注册按钮
     */
    @OnClick(R.id.registerFrag_btn_register)
    void RegisterButton_Clicked() {
        String username = m_RegisterEditText.getText().toString();
        String password = m_PasswordEditText.getText().toString();

        if (username.length() < 5 || username.length() >= 30) {
            m_RegisterLayout.setError("用户名长度应大于等于 5 而小于 30");
            return;
        }

        if (password.length() < 8 || password.length() >= 20) {
            m_PasswordLayout.setError("密码长度应大于等于 8 而小于 20");
            return;
        }

        ProgressHandler.process(getContext(), "用户注册中...", true,
            AuthService.register(username, password), new InteractInterface<AuthRespDTO>() {

                @Override
                public void onSuccess(AuthRespDTO auth) {
                    showToast(getContext(), String.format(Locale.CHINA, "用户 \"%s\" 注册成功，请登录。", auth.getUsername()));
                }

                @Override
                public void onError(String message) {
                    showAlert(getContext(), "注册", "注册错误：" + message);
                }

                @Override
                public void onFailed(Throwable throwable) {
                    showAlert(getContext(), "错误", "网络错误：" + throwable.getMessage());
                }
            }
        );
    }

    /**
     * 清除按钮
     */
    @OnClick(R.id.registerFrag_btn_clear)
    void ClearButton_Clicked() {
        m_RegisterEditText.setText("");
        m_PasswordEditText.setText("");
    }

    /**
     * 去登录按钮
     */
    @OnClick(R.id.registerFrag_btn_toLogin)
    void ToLoginButton_Clicked() {
        AuthActivity activity = (AuthActivity) getActivity();
        if (activity != null)
            activity.openLogin();
    }
}
