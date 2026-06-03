package com.sakurajima.galsearch;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private static final String PREFS_NAME = "yukihub_prefs";
    private static final String KEY_DISCLAIMER_ACCEPTED = "disclaimer_accepted";
    private static final String KEY_DISCLAIMER_ACCEPTED_AT = "disclaimer_accepted_at";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // 先设置方向（异步请求，但会在 Handler 回调前生效）
        boolean savedPortrait = prefs.getBoolean("pref_portrait_mode", false);
        setRequestedOrientation(savedPortrait ? 
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : 
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // 延迟前设置深色窗口背景，避免白闪
        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0F1524")));
        }

        // 应用挖孔屏适配设置
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P && getWindow() != null) {
            boolean cutoutEnabled = prefs.getBoolean("cutout_enabled", false);
            WindowManager.LayoutParams attrs = getWindow().getAttributes();
            attrs.layoutInDisplayCutoutMode = cutoutEnabled ?
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES :
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
            getWindow().setAttributes(attrs);
        }

        // 使用 View 的 post 确保在布局完全渲染后才设置，避免方向未生效时布局错误渲染
        // 延迟 600ms 再加载 Splash 布局，此时方向已稳定，不会出现横竖 logo 同时显示的问题
        new android.os.Handler().postDelayed(() -> {
            setContentView(R.layout.activity_splash);

            // 获取元素并启动动画
            ImageView logo = findViewById(R.id.splashLogo);
            TextView title = findViewById(R.id.splashTitle);
            TextView loading = findViewById(R.id.splashLoading);
            if (loading != null) loading.setText("加载中...");

            // logo: 缩放 + 淡入
            logo.setScaleX(0.6f);
            logo.setScaleY(0.6f);
            logo.setAlpha(0f);
            ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(logo, "scaleX", 0.6f, 1.0f);
            ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(logo, "scaleY", 0.6f, 1.0f);
            ObjectAnimator logoAlpha = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f);
            AnimatorSet logoAnim = new AnimatorSet();
            logoAnim.playTogether(logoScaleX, logoScaleY, logoAlpha);
            logoAnim.setDuration(450);
            logoAnim.setInterpolator(new AccelerateDecelerateInterpolator());

            // title: 淡入
            title.setAlpha(0f);
            ObjectAnimator titleAlpha = ObjectAnimator.ofFloat(title, "alpha", 0f, 1f);
            titleAlpha.setDuration(350);
            titleAlpha.setStartDelay(120);

            // loading: 淡入 + 脉冲
            loading.setAlpha(0f);
            ObjectAnimator loadingAlpha = ObjectAnimator.ofFloat(loading, "alpha", 0f, 1f);
            loadingAlpha.setDuration(300);
            loadingAlpha.setStartDelay(240);

            // 一起播放
            AnimatorSet all = new AnimatorSet();
            all.playTogether(logoAnim, titleAlpha, loadingAlpha);
            all.addListener(new Animator.AnimatorListener() {
                @Override public void onAnimationStart(Animator animation) {}
                @Override public void onAnimationEnd(Animator animation) {
                    // 启动呼吸光效
                    View glow = findViewById(R.id.splashGlow);
                    if (glow != null) {
                        ObjectAnimator glowPulse = ObjectAnimator.ofFloat(glow, "alpha", 0.4f, 0.85f);
                        glowPulse.setDuration(1200);
                        glowPulse.setRepeatMode(ObjectAnimator.REVERSE);
                        glowPulse.setRepeatCount(ObjectAnimator.INFINITE);
                        glowPulse.setInterpolator(new AccelerateDecelerateInterpolator());
                        glowPulse.start();
                    }
                    // logo 微微脉动
                    ImageView logoView = findViewById(R.id.splashLogo);
                    if (logoView != null) {
                        ObjectAnimator logoPulse = ObjectAnimator.ofFloat(logoView, "scaleX", 1.0f, 1.06f);
                        logoPulse.setDuration(1200);
                        logoPulse.setRepeatMode(ObjectAnimator.REVERSE);
                        logoPulse.setRepeatCount(ObjectAnimator.INFINITE);
                        logoPulse.setInterpolator(new AccelerateDecelerateInterpolator());
                        logoPulse.start();
                        ObjectAnimator logoPulseY = ObjectAnimator.ofFloat(logoView, "scaleY", 1.0f, 1.06f);
                        logoPulseY.setDuration(1200);
                        logoPulseY.setRepeatMode(ObjectAnimator.REVERSE);
                        logoPulseY.setRepeatCount(ObjectAnimator.INFINITE);
                        logoPulseY.setInterpolator(new AccelerateDecelerateInterpolator());
                        logoPulseY.start();
                    }

                    // 判断跳转
                    long acceptedAt = prefs.getLong(KEY_DISCLAIMER_ACCEPTED_AT, 0L);
                    boolean accepted = prefs.getBoolean(KEY_DISCLAIMER_ACCEPTED, false);
                    if (accepted && acceptedAt > 0) {
                        // 已接受：让用户多看一会儿，延迟 1.5s 再进主界面
                        new android.os.Handler().postDelayed(() -> startMainActivity(), 1500);
                    } else {
                        // 未接受：直接显示免责声明
                        showDisclaimer();
                    }
                }
                @Override public void onAnimationCancel(Animator animation) {}
                @Override public void onAnimationRepeat(Animator animation) {}
            });
            all.start();
        }, 600);
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showDisclaimer() {
        View content = LayoutInflater.from(this).inflate(R.layout.dialog_disclaimer_first_launch, null, false);
        CheckBox agree = content.findViewById(R.id.cbDisclaimerAgree);
        TextView btnExit = content.findViewById(R.id.btnDisclaimerExit);
        TextView btnContinue = content.findViewById(R.id.btnDisclaimerContinue);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(content)
                .setCancelable(false)
                .create();
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            android.util.DisplayMetrics dm = new android.util.DisplayMetrics();
            getWindowManager().getDefaultDisplay().getRealMetrics(dm);
            boolean realPortrait = dm.widthPixels < dm.heightPixels;
            float wRatio = realPortrait ? 0.72f : 0.50f;
            dialog.getWindow().setLayout(
                (int) (dm.widthPixels * wRatio),
                (int) (dm.heightPixels * 0.78f)
            );
        }

        Runnable refreshContinueState = () -> {
            boolean enabled = agree.isChecked();
            btnContinue.setEnabled(enabled);
            btnContinue.setAlpha(enabled ? 1f : 0.45f);
        };
        refreshContinueState.run();

        agree.setOnCheckedChangeListener((buttonView, isChecked) -> refreshContinueState.run());
        btnExit.setOnClickListener(v -> finish());
        btnContinue.setOnClickListener(v -> {
            if (!agree.isChecked()) return;
            prefs.edit()
                    .putBoolean(KEY_DISCLAIMER_ACCEPTED, true)
                    .putLong(KEY_DISCLAIMER_ACCEPTED_AT, System.currentTimeMillis())
                    .apply();
            dialog.dismiss();
            startMainActivity();
        });
    }
}