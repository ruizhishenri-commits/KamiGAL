package com.sakurajima.galsearch;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.Environment;
import android.os.UserHandle;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import android.content.pm.Signature;
import android.util.Base64;
import android.os.UserManager;
import android.os.PersistableBundle;
import android.media.MediaPlayer;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.DisplayCutout;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.content.res.Configuration;
import android.content.pm.ActivityInfo;
import android.view.Gravity;
import android.graphics.Typeface;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import rikka.shizuku.Shizuku;
   
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
   import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.lang.reflect.Method;

 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.documentfile.provider.DocumentFile;

import com.sakurajima.galsearch.data.GameRepository;
import com.sakurajima.galsearch.data.GameRepository.PlayActivity;
import com.sakurajima.galsearch.data.MetadataRepository;
import com.sakurajima.galsearch.launcher.EmulatorLauncher;
import com.sakurajima.galsearch.metadata.VndbClient;
import com.sakurajima.galsearch.metadata.VnMetadata;
import com.sakurajima.galsearch.model.EngineType;
import com.sakurajima.galsearch.model.Game;
import com.sakurajima.galsearch.ons.OnsSettings;
import com.sakurajima.galsearch.scanner.GameScanner;
import com.sakurajima.galsearch.scanner.ScanResult;
import com.sakurajima.galsearch.ui.GameAdapter;
import com.sakurajima.galsearch.ui.ScanResultAdapter;
import com.sakurajima.galsearch.util.AppExecutors;
import com.sakurajima.galsearch.util.TimeFormatUtil;
import com.sakurajima.galsearch.util.UiScaleUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.text.Collator;
import java.util.Map;
import java.util.Calendar;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.graphics.Point;
import android.util.DisplayMetrics;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        Context context = UiScaleUtil.wrap(newBase);
        // 提前设置Resources方向，确保setContentView加载正确的布局
        boolean isPortrait = SakurajimaApp.isSavedPortrait();
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.orientation = isPortrait ? Configuration.ORIENTATION_PORTRAIT : Configuration.ORIENTATION_LANDSCAPE;
        context = context.createConfigurationContext(config);
        super.attachBaseContext(context);
    }

    private GameRepository repository;
private MetadataRepository metadataRepository;
    private GameAdapter adapter;
    private final List<Game> allGames = new ArrayList<>();
    private String filter = "ALL";
private String query = "";
private String developerFilter = "";
    private TextView tvEmpty, tvStats, tvProfileName, tvProfileInitial;
private ImageView ivProfileAvatar;
private View profileStatusDot;
private LinearLayout detailPanel, detailMetaPanel;
private ImageView sideDetailCover;
    private TextView sideDetailPlaceholder, sideDetailTitle, sideDetailOriginalTitle, sideDetailHint, sideDetailPath, sideDetailDeveloper, sideDetailDate, sideDetailRating, sideDetailLength, sideDetailTags, sideDescToggle, sideTranslateToggle;
private LinearLayout sideTagContainer;
private ImageView sideScreenshot1, sideScreenshot2;
private TextView sideBtnLaunch, sideBtnOptions;
private boolean sideDescExpanded = false;
private boolean sideShowingTranslatedDescription = false;
private String sideFullDescription = "";
private VnMetadata currentSideMetadata;
    private Game selectedGame;
    private Dialog pendingEditDialog;
    private String pendingDirUri, pendingCoverUri;
    private long runningGameId = -1;
private long runningSessionId = -1;
private long sessionStart = 0;
private boolean launchedExternal = false;
private static final long MIN_PLAY_SESSION_MS = 0L;
private static final long MAX_PLAY_SESSION_MS = 12L * 60L * 60L * 1000L;
    private boolean coverScanRunning = false;
    private boolean coverMaintenanceDone = false;
    private boolean autoLibraryScanRunning = false;
    private boolean webDavAutoSyncRunning = false;
    private boolean scanLoadingAnimated = false;
    private boolean isDetonatorActive = false;
    private boolean isDetonatorDead = false;
    private CountDownTimer detonatorTimer;
    private Vibrator detonatorVibrator;
    private CheckBox detonatorCheckRef;
    private java.util.ArrayList<android.animation.Animator> detonatorAnimators;
    private View detonatorAnimRoot;
    private android.os.Handler detonatorVibrateHandler = new android.os.Handler();
    private final Runnable detonatorVibrateTask = new Runnable() {
        @Override
        public void run() {
            if (!isDetonatorActive) return;
            if (detonatorVibrator != null && detonatorVibrator.hasVibrator()) {
                try {
                    detonatorVibrator.vibrate(VibrationEffect.createOneShot(1200, VibrationEffect.DEFAULT_AMPLITUDE));
                } catch (Exception ignored) {}
            }
            detonatorVibrateHandler.postDelayed(this, 1000);
        }
    };
    private boolean isSearchDialogShowing = false;
    private ObjectAnimator scanAnimator;
    private ImageView ivScanLoading;
    private LinearLayout portraitContainer;
    private FrameLayout pageContent;
    private LinearLayout bottomNav;
    private View btnOrientation;
    private TextView navLibrary, navSearchPage, navProfile, navSettingsPage, navEmulators, navImageSearch;
    private boolean isPortrait = false;
    private String pendingSearchQuery = null;
    private View[] portraitPages = new View[6];
    private SharedPreferences prefs;
    private int imgSearchPreviewId = -1;
    private int imgSearchResultAreaId = -1;
    private int imgSearchStatusId = -1;
    private int imgSearchScrollId = -1;
    private LinearLayout imgSearchDialogRoot = null;
    private static final String PREFS_NAME = "yukihub_prefs";
    private static final String KEY_LAST_SCAN_ROOT_URI = "last_scan_root_uri";
    private static final String KEY_SCAN_ROOT_URIS = "scan_root_uris";
    private static final int MAX_SCAN_ROOTS = 3;
    private int pendingScanRootReplaceIndex = -2;
    private LinearLayout activeScanRootList;
    private TextView activeScanRootInfo;
    private static final String KEY_STARTUP_SCAN_DEPTH = "startup_scan_depth";
    private static final String KEY_AUTO_SCAN_ON_STARTUP = "auto_scan_on_startup";
    private static final String KEY_ENGINE_LABEL_POSITION = "engine_label_position";
    private static final String KEY_SIDE_TRANSLATED_PREFIX = "side_translated_";
    private static final int DEFAULT_STARTUP_SCAN_DEPTH = 2;
    private static final int MAX_STARTUP_SCAN_DEPTH = 4;
private static final String KEY_METADATA_SOURCE = "metadata_source";
    private static final String KEY_KR_COMPAT_MODE = "kr_compat_mode";
private static final String KEY_KR_ENGINE_VERSION = "kr_engine_version";
private static final String KEY_KR_SCOPED_SAVE_DIR = "kr_scoped_save_dir";
    private static final String KEY_ARTEMIS_SCOPED_SAVE_DIR = "artemis_scoped_save_dir";
    private static final String KEY_CUTOUT_ENABLED = "cutout_enabled";
    private static final long STORAGE_PROBE_TIMEOUT_MS = 1000L;
    private StorageProbeResult lastStorageProbeResult;
    private long lastStorageProbeAt;
    private static final String SOURCE_VNDB = "vndb";
    private static final String KEY_SORT_MODE = "sort_mode";
private static final String SORT_MODE_RECENT = "recent";
private static final String SORT_MODE_NAME = "name";
private static final String SORT_MODE_NEWEST = "newest";
private static final String KEY_PROFILE_NAME = "profile_name";
    private static final String BROWSER_UA = "Mozilla/5.0 (Linux; Android 15; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.58 Mobile Safari/537.36";
private static final String KEY_PROFILE_SIGNATURE = "profile_signature";
private static final String KEY_PROFILE_AVATAR = "profile_avatar";
private static final String KEY_CUSTOM_BACKGROUND = "custom_background";
private static final String KEY_CUSTOM_BACKGROUND_TYPE = "custom_background_type";
private static final String KEY_BACKGROUND_DIM_ENABLED = "background_dim_enabled";
private static final String KEY_BACKGROUND_VIDEO_SOUND = "background_video_sound";
private static final String KEY_DISCLAIMER_ACCEPTED = "disclaimer_accepted";
private static final String KEY_DISCLAIMER_ACCEPTED_AT = "disclaimer_accepted_at";
private static final int DISCLAIMER_VERSION = 1;

    private ActivityResultLauncher<Uri> scanDirLauncher;
    private ActivityResultLauncher<Uri> editDirLauncher;
private ActivityResultLauncher<String> coverLauncher;
private ActivityResultLauncher<String> profileAvatarLauncher;
private ActivityResultLauncher<String> backgroundPickerLauncher;
private ActivityResultLauncher<String> videoBackgroundPickerLauncher;
private MediaPlayer backgroundMediaPlayer;
private Uri pendingBackgroundVideoUri;
private ActivityResultLauncher<String> backupCreateLauncher;
private ActivityResultLauncher<String[]> backupOpenLauncher;

    private boolean mInitialized = false;

    // 统计系统
    private static final String STATS_BASE_URL = "https://kamistats-fz3vdv2a.manus.space";

    private static final String KEY_DEVICE_ID = "stats_device_id";
    private android.os.Handler statsHandler;
    private Runnable heartbeatTask;
    private android.os.Handler statsPollHandler;
    private Runnable statsPollTask;
    private TextView statsOvOnline;
    private TextView statsOvToday;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        enterImmersiveMode();

        checkSignature();
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isDetonatorDead = prefs.getBoolean("detonator_dead", false);
        // 设置方向（与pref一致）
        boolean savedPortrait = prefs.getBoolean("pref_portrait_mode", false);
        isPortrait = savedPortrait;
        setRequestedOrientation(savedPortrait ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        repository = new GameRepository(this);
        metadataRepository = new MetadataRepository(this);
        if (!ensureDisclaimerAccepted()) {
            return;
        }
        applyCustomBackground();
        repository.deleteSampleGames();
        finishStalePlaySessionsIfAny();
        setupLaunchers();
        setupUi();
        reportLaunch();
        startHeartbeat();
        loadGames();
        checkUpdate(true); // 静默检测，有更新才弹窗
        checkNotice();
        if (ivScanLoading != null)
        if (ivScanLoading != null) ivScanLoading.setVisibility(View.GONE);
        if (prefs != null && prefs.getBoolean(KEY_AUTO_SCAN_ON_STARTUP, false)) {
            autoScanLastRootIfAvailable();
        }
        ensureStoragePermissionForInternalKrkr();
    }

    @Override
    protected void onResume() {
        super.onResume();
        enterImmersiveMode();
        finishCurrentPlaySessionIfAny();
        resumeBackgroundVideoIfNeeded();
        updateProfilePanel();
        maybeAutoWebDavSync();
        // 恢复后台中断的起爆器震动
        if (isDetonatorActive && detonatorVibrateHandler != null) {
            detonatorVibrateHandler.removeCallbacks(detonatorVibrateTask);
            detonatorVibrateTask.run();
        }
    }

    private boolean ensureDisclaimerAccepted() {
        if (prefs == null) return false;
        long acceptedAt = prefs.getLong(KEY_DISCLAIMER_ACCEPTED_AT, 0L);
        boolean accepted = prefs.getBoolean(KEY_DISCLAIMER_ACCEPTED, false);
        if (accepted && acceptedAt > 0) return true;
        View content = LayoutInflater.from(this).inflate(R.layout.dialog_disclaimer_first_launch, null, false);
        CheckBox agree = content.findViewById(R.id.cbDisclaimerAgree);
        TextView btnExit = content.findViewById(R.id.btnDisclaimerExit);
        TextView btnContinue = content.findViewById(R.id.btnDisclaimerContinue);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(content)
                .setCancelable(false)
                .create();
        dialog.show();
        styleAlertDialogDark(dialog);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.72f), (int) (getResources().getDisplayMetrics().heightPixels * 0.78f));
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
            prefs.edit().putBoolean(KEY_DISCLAIMER_ACCEPTED, true).putLong(KEY_DISCLAIMER_ACCEPTED_AT, System.currentTimeMillis()).apply();
            dialog.dismiss();
            recreate();
        });
        return false;
    }

    private void ensureStoragePermissionForInternalKrkr() {
        try {
            if (Build.VERSION.SDK_INT >= 30) {
                if (!Environment.isExternalStorageManager()) {
                    new AlertDialog.Builder(this)
                            .setTitle("需要文件访问权限")
                            .setMessage("内置 KRKR 引擎需要访问外部存储来显示和读取游戏文件。请在系统页面允许“管理所有文件”。")
                            .setPositiveButton("去授权", (d, w) -> {
                                try {
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                                    intent.setData(Uri.parse("package:" + getPackageName()));
                                    startActivity(intent);
                                } catch (Throwable t) {
                                    try { startActivity(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)); } catch (Throwable ignored) { }
                                }
                            })
                            .setNegativeButton("稍后", null)
                            .show();
                }
            } else if (Build.VERSION.SDK_INT >= 23) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1001);
            }
        } catch (Throwable ignored) { }
    }

    private void enterImmersiveMode() {
Window window = getWindow();
applyImmersiveToWindow(window);
}

    private void applyImmersiveToWindow(Window window) {
        if (window == null) return;
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decor = window.getDecorView();
        if (decor == null) return;
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController controller = decor.getWindowInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }
        decor.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
        // 根据设置开关控制挖孔屏适配
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            boolean cutoutEnabled = prefs != null && prefs.getBoolean(KEY_CUTOUT_ENABLED, false);
            WindowManager.LayoutParams attrs = window.getAttributes();
            if (cutoutEnabled) {
                attrs.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            } else {
                attrs.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
            }
            window.setAttributes(attrs);
        }
    }

    // ========== 横竖屏切换逻辑 ==========

    private void toggleOrientation() {
        isPortrait = !isPortrait;
        updateLayoutForOrientation();
        setRequestedOrientation(isPortrait ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        if (prefs != null) {
            prefs.edit().putBoolean("pref_portrait_mode", isPortrait).apply();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        isPortrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT;
        updateLayoutForOrientation();
        // 横竖屏切换时重建起爆器飘落画面（保持震动和计时不变）
        if (isDetonatorActive) {
            // 等布局稳定后再重建，确保屏幕尺寸已更新
            getWindow().getDecorView().post(() -> refreshDetonatorOverlay());
        }
    }

    private void updateLayoutForOrientation() {
        View mainContent = findViewById(R.id.mainContentContainer);
        if (portraitContainer == null || mainContent == null) return;
        if (isPortrait) {
            mainContent.setVisibility(View.GONE);
            portraitContainer.setVisibility(View.VISIBLE);
            enterImmersiveMode();
            switchPortraitPage(0);
        } else {
            portraitContainer.setVisibility(View.GONE);
            mainContent.setVisibility(View.VISIBLE);
            enterImmersiveMode();
        }
    }

    private void switchPortraitPage(int page) {
if (navLibrary == null) return;
navLibrary.setTextColor(page == 0 ? getColorCompat(R.color.yh_primary) : getColorCompat(R.color.yh_text));
navSearchPage.setTextColor(page == 1 ? getColorCompat(R.color.yh_primary) : getColorCompat(R.color.yh_text));
navProfile.setTextColor(page == 2 ? getColorCompat(R.color.yh_primary) : getColorCompat(R.color.yh_text));
navEmulators.setTextColor(page == 3 ? getColorCompat(R.color.yh_primary) : getColorCompat(R.color.yh_text));
navSettingsPage.setTextColor(page == 4 ? getColorCompat(R.color.yh_primary) : getColorCompat(R.color.yh_text));
navImageSearch.setTextColor(page == 5 ? getColorCompat(R.color.yh_primary) : getColorCompat(R.color.yh_text));
pageContent.removeAllViews();

        // 管理统计轮询：离开个人资料页时停止，进入时启动
        if (page != 2) stopStatsPolling();

        // 检查缓存
        if (portraitPages[page] != null) {
            pageContent.addView(portraitPages[page]);
            if (page == 2) startStatsPolling();
            return;
        }
        // 创建新页面并缓存
        switch (page) {
            case 0: showPortraitLibrary(); break;
            case 1: showPortraitSearch(); break;
            case 2: showPortraitProfile(); break;
            case 3: showPortraitEmulators(); break;
            case 4: showPortraitSettings(); break;
            case 5: showPortraitImageSearch(); break;
        }
        if (pageContent.getChildCount() > 0) {
            portraitPages[page] = pageContent.getChildAt(0);
        }
        if (page == 2) startStatsPolling();
    }

    private void showPortraitLibrary() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(4), dp(4), dp(4), 0);

        // 搜索框 + 操作按钮行
        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setPadding(dp(4), dp(4), dp(4), dp(4));

        EditText searchInput = new EditText(this);
        searchInput.setHint("搜索Galgame标题...");
        searchInput.setText(query != null && !query.isEmpty() ? query : "");
        searchInput.setTextColor(getColorCompat(R.color.yh_text));
        searchInput.setHintTextColor(getColorCompat(R.color.yh_text_muted));
        searchInput.setTextSize(12);
        searchInput.setBackgroundResource(R.drawable.bg_input);
        searchInput.setSingleLine(true);
        LinearLayout.LayoutParams searchLp = new LinearLayout.LayoutParams(0, dp(32), 1);
        searchLp.setMargins(0, 0, dp(4), 0);
        topRow.addView(searchInput, searchLp);

        TextView btnAdd = new TextView(this);
        btnAdd.setText("＋添加");
        btnAdd.setGravity(Gravity.CENTER);
        btnAdd.setTextSize(8);
        btnAdd.setTextColor(getColorCompat(R.color.yh_text));
        btnAdd.setBackgroundResource(R.drawable.bg_yuki_button);
        btnAdd.setPadding(dp(8), 0, dp(8), 0);
        topRow.addView(btnAdd, new LinearLayout.LayoutParams(dp(40), dp(36)));

        TextView btnScan = new TextView(this);
        btnScan.setText("⌕扫描");
        btnScan.setGravity(Gravity.CENTER);
        btnScan.setTextSize(8);
        btnScan.setTextColor(getColorCompat(R.color.yh_text));
        btnScan.setBackgroundResource(R.drawable.bg_yuki_button);
        btnScan.setPadding(dp(8), 0, dp(8), 0);
        LinearLayout.LayoutParams scanLp = new LinearLayout.LayoutParams(dp(40), dp(36));
        scanLp.setMargins(dp(4), 0, 0, 0);
        topRow.addView(btnScan, scanLp);

        root.addView(topRow);

        // 筛选按钮行
        LinearLayout filterRow = new LinearLayout(this);
        filterRow.setOrientation(LinearLayout.HORIZONTAL);
        filterRow.setPadding(dp(4), 0, dp(4), dp(4));

        String[][] filters = {{"全部", "ALL"}, {"最近", "RECENT"}, {"在玩", "PLAYING"}, {"玩过", "COMPLETED"}, {"未玩", "UNPLAYED"}};
        for (int i = 0; i < filters.length; i++) {
            TextView btn = new TextView(this);
            String val = filters[i][1];
            btn.setText(filters[i][0]);
            btn.setTextSize(11);
            btn.setGravity(Gravity.CENTER);
            btn.setPadding(dp(8), dp(4), dp(8), dp(4));
            boolean selected = val.equals(filter);
            btn.setTextColor(selected ? getColorCompat(R.color.yh_text) : getColorCompat(R.color.yh_text_muted));
            btn.setAlpha(selected ? 1f : 0.72f);
            btn.setBackgroundResource(R.drawable.bg_sidebar_item);
            LinearLayout.LayoutParams flp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(28));
            if (i > 0) flp.setMargins(dp(4), 0, 0, 0);
            btn.setLayoutParams(flp);
            final String fv = val;
            btn.setOnClickListener(v -> {
                filter = fv;
                developerFilter = "";
                // 更新所有按钮状态
                for (int j = 0; j < filterRow.getChildCount(); j++) {
                    View child = filterRow.getChildAt(j);
                    String tag = (String) child.getTag();
                    boolean sel = tag != null && tag.equals(filter);
                    child.setSelected(sel);
                    child.setAlpha(sel ? 1f : 0.72f);
                    if (child instanceof TextView) {
                        ((TextView) child).setTextColor(sel ? getColorCompat(R.color.yh_text) : getColorCompat(R.color.yh_text_muted));
                    }
                }
                applyFilter();
            });
            btn.setTag(val);
            filterRow.addView(btn);
        }

        root.addView(filterRow);

        // 搜索框输入监听
        searchInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                query = s == null ? "" : s.toString();
                applyFilter();
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        // 添加按钮
        btnAdd.setOnClickListener(v -> showEditDialog(null));
        // 扫描按钮
        btnScan.setOnClickListener(v -> scanLastRootOrChoose());
        btnScan.setOnLongClickListener(v -> { scanDirLauncher.launch(null); return true; });

        // 游戏列表
        RecyclerView portraitRecycler = new RecyclerView(this);
        portraitRecycler.setLayoutManager(new GridLayoutManager(this, 3));
        portraitRecycler.setHasFixedSize(true);
        portraitRecycler.setItemViewCacheSize(20);
        portraitRecycler.setAdapter(adapter);
        portraitRecycler.setClipToPadding(false);
        portraitRecycler.setPadding(dp(2), dp(2), dp(2), dp(2));
        portraitRecycler.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        root.addView(portraitRecycler);

        pageContent.addView(root, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void showPortraitSearch() {
        LinearLayout searchLayout = new LinearLayout(this);
        searchLayout.setOrientation(LinearLayout.VERTICAL);
        searchLayout.setPadding(dp(16), dp(16), dp(16), dp(16));

        TextView title = new TextView(this);
        title.setText("搜索 Galgame");
        title.setTextColor(getColorCompat(R.color.yh_text));
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 0, 0, dp(16));
        searchLayout.addView(title);

        LinearLayout inputRow = new LinearLayout(this);
        inputRow.setOrientation(LinearLayout.HORIZONTAL);
        EditText searchInput = new EditText(this);
        searchInput.setHint("输入游戏标题...");
        searchInput.setTextColor(getColorCompat(R.color.yh_text));
        searchInput.setHintTextColor(getColorCompat(R.color.yh_text_muted));
        searchInput.setTextSize(14);
        searchInput.setBackgroundResource(R.drawable.bg_input);
        searchInput.setPadding(dp(12), dp(8), dp(12), dp(8));
        searchInput.setSingleLine(true);
        searchInput.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH);
        inputRow.addView(searchInput, new LinearLayout.LayoutParams(0, dp(44), 1f));
        Button searchBtn = new Button(this);
        searchBtn.setText("搜索");
        searchBtn.setTextColor(getColorCompat(R.color.yh_text));
        searchBtn.setBackgroundResource(R.drawable.bg_yuki_button);
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(dp(72), dp(44));
        btnLp.leftMargin = dp(8);
        searchBtn.setLayoutParams(btnLp);
        inputRow.addView(searchBtn);
        searchLayout.addView(inputRow);

        // 状态提示文字
        TextView statusText = new TextView(this);
        statusText.setVisibility(View.GONE);
        statusText.setTextColor(getColorCompat(R.color.yh_text_muted));
        statusText.setTextSize(12);
        statusText.setPadding(dp(4), dp(10), dp(4), dp(4));
        searchLayout.addView(statusText);

        // 搜索结果容器
        ScrollView resultScroll = new ScrollView(this);
        resultScroll.setVisibility(View.GONE);
        LinearLayout resultsContainer = new LinearLayout(this);
        resultsContainer.setOrientation(LinearLayout.VERTICAL);
        resultScroll.addView(resultsContainer, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
        searchLayout.addView(resultScroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        // 默认提示
        TextView tip = new TextView(this);
        tip.setText("搜索 VNDB 上的 Galgame 信息，支持日文/英文/中文标题");
        tip.setTextColor(getColorCompat(R.color.yh_text_muted));
        tip.setTextSize(11);
        tip.setPadding(dp(4), dp(10), dp(4), dp(4));
        searchLayout.addView(tip);

        // 搜索逻辑
        Runnable doSearch = () -> {
            String q = searchInput.getText().toString().trim();
            if (q.isEmpty()) {
                Toast.makeText(this, "请输入搜索内容", Toast.LENGTH_SHORT).show();
                return;
            }
            tip.setVisibility(View.GONE);
            statusText.setVisibility(View.VISIBLE);
            statusText.setText("正在搜索...");
            resultScroll.setVisibility(View.VISIBLE);
            resultsContainer.removeAllViews();

            SearchClient client = new SearchClient();
            AppExecutors.runOnIo(() -> {
                try {
                    java.util.List<SearchClient.PlatformResult> results = client.searchGal(q, false);
                    SearchClient.VndbInfo vndb = null;
                    try { vndb = client.searchVndb(q); } catch (Exception ignored) {}

                    final java.util.List<SearchClient.PlatformResult> finalResults = results;
                    final SearchClient.VndbInfo finalVndb = vndb;
                    runOnUiThread(() -> {
                        String statusMsg = "找到 " + results.size() + " 个资源站";
                        if (finalVndb != null) {
                            statusMsg += "  📖 已匹配VNDB";
                        } else {
                            statusMsg += "  ⚠ VNDB未匹配";
                        }
                        statusText.setText(statusMsg);
                        searchBtn.setEnabled(true);

                        if (finalVndb != null) {
                            // 封面图片 - 自适应比例
                            FrameLayout coverFrame = new FrameLayout(MainActivity.this);
                            coverFrame.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            coverFrame.setBackgroundResource(R.drawable.bg_cover_placeholder);
                            ImageView coverImage = new ImageView(MainActivity.this);
                            coverImage.setLayoutParams(new FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            coverImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            coverImage.setAdjustViewBounds(true);
                            coverImage.setMaxHeight(dp(360));
                            coverImage.setVisibility(View.GONE);
                            coverFrame.addView(coverImage);
                            resultsContainer.addView(coverFrame);

                            // VNDB标题 + 评分
                            TextView vndbTitle = new TextView(MainActivity.this);
                            vndbTitle.setText("📖 " + finalVndb.title + (finalVndb.rating != null ? "  ⭐" + String.format("%.1f", finalVndb.rating) : ""));
                            vndbTitle.setTextColor(getColorCompat(R.color.yh_text));
                            vndbTitle.setTextSize(15);
                            vndbTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                            vndbTitle.setPadding(0, dp(8), 0, dp(2));
                            resultsContainer.addView(vndbTitle);

                            // 信息行：发售日 + 可点击的ID
                            LinearLayout infoRow = new LinearLayout(MainActivity.this);
                            infoRow.setOrientation(LinearLayout.HORIZONTAL);
                            infoRow.setPadding(0, 0, 0, dp(4));
                            if (finalVndb.released != null) {
                                TextView releaseText = new TextView(MainActivity.this);
                                releaseText.setText("📅 " + finalVndb.released);
                                releaseText.setTextColor(getColorCompat(R.color.yh_text_muted));
                                releaseText.setTextSize(11);
                                infoRow.addView(releaseText);
                            }
                            if (finalVndb.id != null) {
                                final String vndbUrl = "https://vndb.org/" + finalVndb.id;
                                TextView idText = new TextView(MainActivity.this);
                                idText.setText(" 🆔 " + finalVndb.id + " ↗");
                                idText.setTextColor(getColorCompat(R.color.yh_primary));
                                idText.setTextSize(11);
                                idText.setPadding(dp(8), 0, 0, 0);
                                idText.setClickable(true);
                                idText.setOnClickListener(v -> {
                                    try {
                                        startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW,
                                                android.net.Uri.parse(vndbUrl)));
                                    } catch (Exception ignored) {}
                                });
                                infoRow.addView(idText);
                            }
                            resultsContainer.addView(infoRow);

                            // 完整简介 + 翻译按钮
                            if (finalVndb.description != null && !finalVndb.description.isEmpty()) {
                                final String rawDesc = finalVndb.description.replaceAll("<[^>]+>", "").trim();
                                final TextView descText = new TextView(MainActivity.this);
                                descText.setText(rawDesc);
                                descText.setTextColor(getColorCompat(R.color.yh_text_muted));
                                descText.setTextSize(11);
                                descText.setLineSpacing(dp(2), 1.0f);
                                descText.setPadding(0, dp(4), 0, dp(4));
                                resultsContainer.addView(descText);

                                // 翻译按钮
                                TextView translateBtn = new TextView(MainActivity.this);
                                translateBtn.setText("🌐 翻译简介");
                                translateBtn.setTextColor(getColorCompat(R.color.yh_primary));
                                translateBtn.setTextSize(11);
                                translateBtn.setTypeface(null, Typeface.BOLD);
                                translateBtn.setPadding(dp(4), dp(4), dp(4), dp(8));
                                translateBtn.setClickable(true);
                                translateBtn.setOnClickListener(v -> {
                                    if (translateBtn.getText().toString().contains("原文")) {
                                        descText.setText(rawDesc);
                                        translateBtn.setText("🌐 翻译简介");
                                        return;
                                    }
                                    translateBtn.setEnabled(false);
                                    translateBtn.setText("翻译中...");
                                    AppExecutors.runOnIo(() -> {
                                        try {
                                            String translated = translateTextToChinese(rawDesc);
                                            runOnUiThread(() -> {
                                                if (translated != null && !translated.trim().isEmpty()) {
                                                    descText.setText(translated.trim());
                                                    translateBtn.setText("🌐 原文");
                                                } else {
                                                    Toast.makeText(MainActivity.this, "翻译失败", Toast.LENGTH_SHORT).show();
                                                }
                                                translateBtn.setEnabled(true);
                                            });
                                        } catch (Throwable t) {
                                            runOnUiThread(() -> {
                                                Toast.makeText(MainActivity.this, "翻译失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                                translateBtn.setEnabled(true);
                                            });
                                        }
                                    });
                                });
                                resultsContainer.addView(translateBtn);
                            }

                            View line = new View(MainActivity.this);
                            line.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
                            line.setBackgroundColor(0x33FFFFFF);
                            resultsContainer.addView(line);

                            // 后台加载封面 - 自适应比例
                            if (finalVndb.imageUrl != null && !finalVndb.imageUrl.isEmpty()) {
                                final String imgUrl = finalVndb.imageUrl;
                                AppExecutors.runOnIo(() -> {
                                    try {
                                        java.net.URL url = new java.net.URL(imgUrl);
                                        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                                        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                                        conn.setConnectTimeout(8000);
                                        conn.setReadTimeout(8000);
                                        conn.connect();
                                        java.io.InputStream is = conn.getInputStream();
                                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                                        is.close();
                                        conn.disconnect();
                                        if (bitmap != null) {
                                            final int bmpW = bitmap.getWidth();
                                            final int bmpH = bitmap.getHeight();
                                            runOnUiThread(() -> {
                                                coverImage.setImageBitmap(bitmap);
                                                coverImage.setVisibility(View.VISIBLE);
                                                coverFrame.setBackgroundColor(Color.TRANSPARENT);
                                                // 根据图片比例动态调整封面高度
                                                int screenW = getResources().getDisplayMetrics().widthPixels;
                                                int calcedH = (int) (screenW * (double) bmpH / bmpW);
                                                int maxH = dp(400);
                                                if (calcedH > maxH) calcedH = maxH;
                                                if (calcedH < dp(100)) calcedH = dp(100);
                                                ViewGroup.LayoutParams ivLp = coverImage.getLayoutParams();
                                                ivLp.height = calcedH;
                                                coverImage.setLayoutParams(ivLp);
                                            });
                                        }
                                    } catch (Exception ignored) {}
                                });
                            }
                        }

                        if (finalResults.isEmpty()) {
                            TextView empty = new TextView(MainActivity.this);
                            empty.setText("没有找到资源");
                            empty.setTextColor(getColorCompat(R.color.yh_text_muted));
                            empty.setTextSize(12);
                            empty.setPadding(0, dp(8), 0, 0);
                            resultsContainer.addView(empty);
                            return;
                        }

                        java.util.Set<String> seenPlatforms = new java.util.HashSet<>();
                        for (SearchClient.PlatformResult pr : finalResults) {
                            if (!seenPlatforms.add(pr.platform)) continue;
                            LinearLayout platRow = new LinearLayout(MainActivity.this);
                            platRow.setOrientation(LinearLayout.HORIZONTAL);
                            platRow.setPadding(0, dp(4), 0, dp(4));
                            platRow.setBackgroundResource(R.drawable.bg_sidebar_item);
                            platRow.setClickable(true);
                            platRow.setFocusable(true);

                            String tagStr = "";
                            if (pr.tags != null) {
                                if (pr.tags.contains("NoReq")) tagStr = "🟢";
                                else if (pr.tags.contains("LoginPay")) tagStr = "🔒";
                                else tagStr = "📄";
                            }

                            TextView platName = new TextView(MainActivity.this);
                            platName.setText(tagStr + " " + pr.platform + " (" + pr.items.size() + ")");
                            platName.setTextColor(getColorCompat(R.color.yh_primary));
                            platName.setTextSize(12);
                            platName.setPadding(dp(4), dp(4), dp(4), dp(4));
                            platName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

                            Button viewBtn = new Button(MainActivity.this);
                            viewBtn.setText("查看");
                            viewBtn.setTextSize(10);
                            viewBtn.setBackgroundResource(R.drawable.bg_chip);
                            viewBtn.setPadding(dp(8), dp(2), dp(8), dp(2));

                            platRow.addView(platName);
                            platRow.addView(viewBtn, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(28)));

                            final java.util.List<SearchClient.Item> items = pr.items;
                            final String platTitle = pr.platform;
                            viewBtn.setOnClickListener(btn -> showOnlineResultItems(platTitle, items));

                            resultsContainer.addView(platRow);
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        statusText.setText("搜索失败: " + e.getMessage());
                        searchBtn.setEnabled(true);
                    });
                }
            });
        };

        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
                    || actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                doSearch.run();
                return true;
            }
            return false;
        });
        searchBtn.setOnClickListener(v -> doSearch.run());

        pageContent.addView(searchLayout, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void showPortraitProfile() {
        ScrollView scroll = new ScrollView(this);
        scroll.setClipToPadding(false);
        LinearLayout profileLayout = new LinearLayout(this);
        profileLayout.setOrientation(LinearLayout.VERTICAL);
        profileLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        profileLayout.setPadding(dp(24), dp(40), dp(24), dp(24));

        // 头像
        FrameLayout avatarFrame = new FrameLayout(this);
        LinearLayout.LayoutParams avaLp = new LinearLayout.LayoutParams(dp(72), dp(72));
        avaLp.gravity = Gravity.CENTER_HORIZONTAL;
        avatarFrame.setLayoutParams(avaLp);
        avatarFrame.setBackgroundResource(R.drawable.bg_cover_placeholder);
        ImageView avatarView = new ImageView(this);
        if (ivProfileAvatar != null && ivProfileAvatar.getDrawable() != null) {
            avatarView.setImageDrawable(ivProfileAvatar.getDrawable());
            avatarView.setVisibility(View.VISIBLE);
        }
        avatarView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        avatarView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatarFrame.addView(avatarView);
        TextView initialView = new TextView(this);
        initialView.setText(tvProfileInitial != null ? tvProfileInitial.getText() : "?");
        initialView.setTextColor(getColorCompat(R.color.yh_text));
        initialView.setTextSize(24);
        initialView.setTypeface(null, Typeface.BOLD);
        initialView.setGravity(Gravity.CENTER);
        avatarFrame.addView(initialView);
        profileLayout.addView(avatarFrame);

        // 昵称
        TextView nameView = new TextView(this);
        nameView.setText(tvProfileName != null ? tvProfileName.getText() : "未知");
        nameView.setTextColor(getColorCompat(R.color.yh_text));
        nameView.setTextSize(20);
        nameView.setTypeface(null, Typeface.BOLD);
        nameView.setGravity(Gravity.CENTER);
        nameView.setPadding(0, dp(12), 0, dp(4));
        profileLayout.addView(nameView);

        // 统计卡片行
        long total = totalPlayTime();
        long today = todayTotalPlayTime();
        LinearLayout statCards = new LinearLayout(this);
        statCards.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams statRowLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        statRowLp.setMargins(0, dp(16), 0, 0);
        statCards.setLayoutParams(statRowLp);
        statCards.addView(profileStatCard("游戏", String.valueOf(allGames.size())), new LinearLayout.LayoutParams(0, dp(48), 1));
        LinearLayout.LayoutParams statMid = new LinearLayout.LayoutParams(0, dp(48), 1);
        statMid.setMargins(dp(6), 0, dp(6), 0);
        statCards.addView(profileStatCard("总时长", TimeFormatUtil.playTime(total)), statMid);
        statCards.addView(profileStatCard("今日", TimeFormatUtil.playTime(today)), new LinearLayout.LayoutParams(0, dp(48), 1));
        profileLayout.addView(statCards);

        // 在线数据（来自统计系统）
        LinearLayout onlineRow = new LinearLayout(this);
        onlineRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams onlineRowLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        onlineRowLp.setMargins(0, dp(6), 0, 0);
        onlineRow.setLayoutParams(onlineRowLp);
        final TextView ovOnline = profileStatCard("在线", "...");
        final TextView ovToday = profileStatCard("今日使用", "...");
        statsOvOnline = ovOnline;
        statsOvToday = ovToday;
        LinearLayout.LayoutParams ovMid = new LinearLayout.LayoutParams(0, dp(48), 1);
        ovMid.setMargins(dp(6), 0, dp(6), 0);
        onlineRow.addView(ovOnline, new LinearLayout.LayoutParams(0, dp(48), 1));
        onlineRow.addView(ovToday, ovMid);
        profileLayout.addView(onlineRow);
        AppExecutors.runOnIo(() -> {
            try {
                URL statsUrl = new URL("https://kamistats-fz3vdv2a.manus.space/api/trpc/stats.overview");
                HttpURLConnection conn = (HttpURLConnection) statsUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                int code = conn.getResponseCode();
                if (code == 200) {
                    java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    br.close();
                    org.json.JSONObject root = new org.json.JSONObject(sb.toString());
                    org.json.JSONObject data = root.optJSONObject("result");
                    if (data != null) data = data.optJSONObject("data");
                    if (data != null) data = data.optJSONObject("json");
                    final String onlineStr = String.valueOf(data != null ? data.optInt("onlineNow", 0) : 0);
                    final String todayStr = String.valueOf(data != null ? data.optInt("todayLaunches", 0) : 0);
                    runOnUiThread(() -> {
                        ovOnline.setText("在线\n" + onlineStr);
                        ovToday.setText("今日使用\n" + todayStr);
                    });
                }
            } catch (Throwable ignored) {}
        });

        // 签名
        final String signature = profileSignature();
        if (signature != null && !signature.isEmpty()) {
            TextView sigView = new TextView(this);
            sigView.setText("✏ " + signature);
            sigView.setTextColor(getColorCompat(R.color.yh_text_muted));
            sigView.setTextSize(12);
            sigView.setGravity(Gravity.CENTER);
            sigView.setPadding(dp(8), dp(8), dp(8), dp(8));
            LinearLayout.LayoutParams sigLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            sigLp.setMargins(0, dp(8), 0, 0);
            sigView.setLayoutParams(sigLp);
            profileLayout.addView(sigView);
        }

        // 今日动态
        TextView activityTitle = new TextView(this);
        activityTitle.setText("今日动态");
        activityTitle.setTextColor(getColorCompat(R.color.yh_text));
        activityTitle.setTextSize(14);
        activityTitle.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams actTitleLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        actTitleLp.setMargins(0, dp(16), 0, dp(4));
        activityTitle.setLayoutParams(actTitleLp);
        profileLayout.addView(activityTitle);
        TextView activityText = new TextView(this);
        activityText.setText(buildTodayActivityText());
        activityText.setTextColor(getColorCompat(R.color.yh_text_muted));
        activityText.setTextSize(12);
        activityText.setLineSpacing(dp(1), 1.0f);
        activityText.setBackgroundResource(R.drawable.bg_input);
        activityText.setPadding(dp(10), dp(8), dp(10), dp(8));
        profileLayout.addView(activityText, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // 最近动态
        TextView recentTitle = new TextView(this);
        recentTitle.setText("最近动态");
        recentTitle.setTextColor(getColorCompat(R.color.yh_text));
        recentTitle.setTextSize(14);
        recentTitle.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams recTitleLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        recTitleLp.setMargins(0, dp(12), 0, dp(4));
        recentTitle.setLayoutParams(recTitleLp);
        profileLayout.addView(recentTitle);
        LinearLayout feedList = new LinearLayout(this);
        feedList.setOrientation(LinearLayout.VERTICAL);
        buildRecentActivityViews(feedList);
        profileLayout.addView(feedList, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // 头像点击更换
        avatarFrame.setClickable(true);
        avatarFrame.setOnClickListener(v -> showProfileDialog());

        // 编辑资料按钮
        Button editBtn = new Button(this);
        editBtn.setText("编辑个人资料");
        editBtn.setTextColor(getColorCompat(R.color.yh_text));
        editBtn.setBackgroundResource(R.drawable.bg_yuki_button);
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(40));
        btnLp.setMargins(0, dp(16), 0, 0);
        editBtn.setLayoutParams(btnLp);
        editBtn.setOnClickListener(v -> showProfileDialog());
        profileLayout.addView(editBtn);

        scroll.addView(profileLayout);
        pageContent.addView(scroll, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }
    private void startDetonator() {
        if (isDetonatorDead || isDetonatorActive) return;
        isDetonatorActive = true;
                // 连续震动（Handler每5秒续上，200ms重叠，永不间断）
        detonatorVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        detonatorVibrateHandler.removeCallbacks(detonatorVibrateTask);
        detonatorVibrateTask.run();
        // 超多飘落
        try {
            DisplayMetrics dm = getResources().getDisplayMetrics();
            int screenW = dm.widthPixels;
            int screenH = dm.heightPixels;
            java.util.Random random = new java.util.Random();
            FrameLayout overlay = new FrameLayout(this);
            overlay.setBackgroundColor(Color.TRANSPARENT);
            overlay.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            overlay.setClickable(false);
            overlay.setFocusable(false);
            final int COUNT = 500;
            int cols = 20;
            int stepW = screenW / cols;
            detonatorAnimators = new java.util.ArrayList<>();
            for (int i = 0; i < COUNT; i++) {
                ImageView img = new ImageView(this);
                img.setImageResource(R.drawable.bg_detonator);
                int size = dp(random.nextInt(18) + 18);
                img.setAlpha(0.3f + random.nextFloat() * 0.7f);
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(size, size);
                lp.leftMargin = random.nextInt(Math.max(1, stepW)) + (i % cols) * stepW;
                lp.topMargin = 0;
                img.setClickable(false);
                overlay.addView(img, lp);
                // 无限循环飘落（起始紧贴顶栏，currentPlayTime打散位置）
                ObjectAnimator fall = ObjectAnimator.ofFloat(img, "translationY",
                        random.nextInt(10) - size, screenH + size);
                fall.setDuration(random.nextInt(2500) + 3000);
                fall.setRepeatCount(ObjectAnimator.INFINITE);
                fall.setInterpolator(new android.view.animation.LinearInterpolator());
                fall.start();
                fall.setCurrentPlayTime(random.nextInt((int) fall.getDuration()));
                detonatorAnimators.add(fall);
                // 摇摆
                int swayRange = dp(random.nextInt(10) + 4);
                ObjectAnimator sway = ObjectAnimator.ofFloat(img, "translationX",
                        -swayRange, swayRange, -swayRange);
                sway.setDuration(random.nextInt(800) + 800);
                sway.setRepeatCount(ObjectAnimator.INFINITE);
                sway.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
                sway.start();
                detonatorAnimators.add(sway);
                // 旋转
                ObjectAnimator rotate = ObjectAnimator.ofFloat(img, "rotation",
                        random.nextFloat() * 360, random.nextFloat() * 360 + 180);
                rotate.setDuration(random.nextInt(1500) + 1500);
                rotate.setRepeatCount(ObjectAnimator.INFINITE);
                rotate.setInterpolator(new android.view.animation.LinearInterpolator());
                rotate.start();
                detonatorAnimators.add(rotate);
            }
            ViewGroup contentRoot = findViewById(android.R.id.content);
            contentRoot.addView(overlay, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            overlay.bringToFront();
            detonatorAnimRoot = overlay;
        } catch (Exception ignored) {}
        // 倒计时1分钟 → 没电
        detonatorTimer = new CountDownTimer(60000, 1000) {
            @Override public void onTick(long millisUntilFinished) {}
            @Override public void onFinish() {
                stopDetonator(true);
            }
        }.start();
    }
    private void stopDetonator(boolean batteryDead) {
        if (!isDetonatorActive && !batteryDead) return;
        isDetonatorActive = false;
        if (batteryDead) isDetonatorDead = true;
        // 取消计时器
        if (detonatorTimer != null) {
            detonatorTimer.cancel();
            detonatorTimer = null;
        }
        // 停止震动
        detonatorVibrateHandler.removeCallbacks(detonatorVibrateTask);
        try { if (detonatorVibrator != null) detonatorVibrator.cancel(); } catch (Exception ignored) {}
        // 停止动画
        if (detonatorAnimators != null) {
            for (android.animation.Animator a : detonatorAnimators) {
                if (a.isRunning()) a.cancel();
            }
            detonatorAnimators.clear();
            detonatorAnimators = null;
        }
        // 移除飘落
        try {
            if (detonatorAnimRoot != null) {
                ViewGroup parent = (ViewGroup) detonatorAnimRoot.getParent();
                if (parent != null) parent.removeView(detonatorAnimRoot);
                detonatorAnimRoot = null;
            }
        } catch (Exception ignored) {}
        if (batteryDead) {
            if (prefs != null) prefs.edit().putBoolean("detonator_dead", true).apply();
            if (detonatorCheckRef != null) detonatorCheckRef.setChecked(false);
        }
    }
    private void refreshDetonatorOverlay() {
        // 移除旧飘落
        if (detonatorAnimators != null) {
            for (android.animation.Animator a : detonatorAnimators) {
                if (a.isRunning()) a.cancel();
            }
            detonatorAnimators.clear();
            detonatorAnimators = null;
        }
        try {
            if (detonatorAnimRoot != null) {
                ViewGroup parent = (ViewGroup) detonatorAnimRoot.getParent();
                if (parent != null) parent.removeView(detonatorAnimRoot);
                detonatorAnimRoot = null;
            }
        } catch (Exception ignored) {}
        // 重建飘落
        try {
            final ViewGroup contentRoot = findViewById(android.R.id.content);
            if (contentRoot == null) return;
            // 用内容视图实际宽高，不是DisplayMetrics——横竖屏切换时确保正确
            int screenW = contentRoot.getWidth();
            int screenH = contentRoot.getHeight();
            if (screenW <= 0 || screenH <= 0) {
                // 视图还没布局好，跳过重建
                return;
            }
            java.util.Random random = new java.util.Random();
            FrameLayout overlay = new FrameLayout(this);
            overlay.setBackgroundColor(Color.TRANSPARENT);
            overlay.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            overlay.setClickable(false);
            overlay.setFocusable(false);
            final int COUNT = 500;
            int cols = 20;
            int stepW = screenW / cols;
            detonatorAnimators = new java.util.ArrayList<>();
            for (int i = 0; i < COUNT; i++) {
                ImageView img = new ImageView(this);
                img.setImageResource(R.drawable.bg_detonator);
                int size = dp(random.nextInt(18) + 18);
                img.setAlpha(0.3f + random.nextFloat() * 0.7f);
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(size, size);
                lp.leftMargin = random.nextInt(Math.max(1, stepW)) + (i % cols) * stepW;
                lp.topMargin = 0;
                img.setClickable(false);
                overlay.addView(img, lp);
                ObjectAnimator fall = ObjectAnimator.ofFloat(img, "translationY",
                        random.nextInt(10) - size, screenH + size);
                fall.setDuration(random.nextInt(2500) + 3000);
                fall.setRepeatCount(ObjectAnimator.INFINITE);
                fall.setInterpolator(new android.view.animation.LinearInterpolator());
                fall.start();
                fall.setCurrentPlayTime(random.nextInt((int) fall.getDuration()));
                detonatorAnimators.add(fall);
                int swayRange = dp(random.nextInt(10) + 4);
                ObjectAnimator sway = ObjectAnimator.ofFloat(img, "translationX",
                        -swayRange, swayRange, -swayRange);
                sway.setDuration(random.nextInt(800) + 800);
                sway.setRepeatCount(ObjectAnimator.INFINITE);
                sway.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
                sway.start();
                detonatorAnimators.add(sway);
                ObjectAnimator rotate = ObjectAnimator.ofFloat(img, "rotation",
                        random.nextFloat() * 360, random.nextFloat() * 360 + 180);
                rotate.setDuration(random.nextInt(1500) + 1500);
                rotate.setRepeatCount(ObjectAnimator.INFINITE);
                rotate.setInterpolator(new android.view.animation.LinearInterpolator());
                rotate.start();
                detonatorAnimators.add(rotate);
            }
            contentRoot.addView(overlay, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            overlay.bringToFront();
            detonatorAnimRoot = overlay;
        } catch (Exception ignored) {}
    }
    private void showPortraitSettings() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout settingsLayout = new LinearLayout(this);
        settingsLayout.setOrientation(LinearLayout.VERTICAL);
        settingsLayout.setPadding(dp(20), dp(20), dp(20), dp(20));

        TextView title = new TextView(this);
        title.setText("设置");
        title.setTextColor(getColorCompat(R.color.yh_text));
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 0, 0, dp(20));
        settingsLayout.addView(title);

        // 挖孔屏开关
        LinearLayout cutoutRow = new LinearLayout(this);
        cutoutRow.setOrientation(LinearLayout.HORIZONTAL);
        cutoutRow.setGravity(Gravity.CENTER_VERTICAL);
        cutoutRow.setPadding(dp(4), dp(8), dp(4), dp(8));
        cutoutRow.setBackgroundResource(R.drawable.bg_input);
        TextView cutoutLabel = new TextView(this);
        cutoutLabel.setText("适配挖孔屏/全面屏");
        cutoutLabel.setTextColor(getColorCompat(R.color.yh_text));
        cutoutLabel.setTextSize(14);
        cutoutRow.addView(cutoutLabel, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        CheckBox cutoutCheck = new CheckBox(this);
        cutoutCheck.setChecked(prefs != null && prefs.getBoolean(KEY_CUTOUT_ENABLED, false));
        cutoutCheck.setOnCheckedChangeListener((btn, checked) -> {
            if (prefs != null) {
                prefs.edit().putBoolean(KEY_CUTOUT_ENABLED, checked).apply();
                enterImmersiveMode();
            }
        });
        cutoutRow.addView(cutoutCheck);
        settingsLayout.addView(cutoutRow);

        // 字体大小
        LinearLayout fontSizeRow = new LinearLayout(this);
        fontSizeRow.setOrientation(LinearLayout.HORIZONTAL);
        fontSizeRow.setGravity(Gravity.CENTER_VERTICAL);
        fontSizeRow.setPadding(dp(4), dp(12), dp(4), dp(8));
        fontSizeRow.setBackgroundResource(R.drawable.bg_input);
        fontSizeRow.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView fontSizeLabel = new TextView(this);
        fontSizeLabel.setText("整体字体大小");
        fontSizeLabel.setTextColor(getColorCompat(R.color.yh_text));
        fontSizeLabel.setTextSize(14);
        fontSizeRow.addView(fontSizeLabel, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView fontSizeValue = new TextView(this);
        fontSizeValue.setText("100%");
        fontSizeValue.setTextColor(getColorCompat(R.color.yh_primary));
        fontSizeValue.setTextSize(14);
        fontSizeValue.setTypeface(null, Typeface.BOLD);
        fontSizeRow.addView(fontSizeValue);
        settingsLayout.addView(fontSizeRow);
        // 起爆器
        LinearLayout detonatorRow = new LinearLayout(this);
        detonatorRow.setOrientation(LinearLayout.HORIZONTAL);
        detonatorRow.setGravity(Gravity.CENTER_VERTICAL);
        detonatorRow.setPadding(dp(4), dp(8), dp(4), dp(8));
        detonatorRow.setBackgroundResource(R.drawable.bg_input);
        TextView detonatorLabel = new TextView(this);
        detonatorLabel.setText("起爆器");
        detonatorLabel.setTextColor(getColorCompat(R.color.yh_text));
        detonatorLabel.setTextSize(14);
        detonatorRow.addView(detonatorLabel, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        CheckBox detonatorCheck = new CheckBox(this);
        detonatorCheck.setChecked(isDetonatorActive);
        detonatorCheckRef = detonatorCheck;
        detonatorCheck.setOnCheckedChangeListener((btn, checked) -> {
            if (isDetonatorDead) {
                btn.setChecked(false);
                Toast.makeText(this, "起爆器没电了…", Toast.LENGTH_SHORT).show();
                return;
            }
            if (checked) {
                startDetonator();
            } else {
                stopDetonator(false);
            }
        });
        detonatorRow.addView(detonatorCheck);
        settingsLayout.addView(detonatorRow);
        // 更多设置按钮
        Button moreBtn = new Button(this);
        moreBtn.setText("更多设置");
        moreBtn.setTextColor(getColorCompat(R.color.yh_text));
        moreBtn.setBackgroundResource(R.drawable.bg_yuki_button);
        LinearLayout.LayoutParams moreLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(40));
        moreLp.setMargins(0, dp(16), 0, 0);
        moreBtn.setLayoutParams(moreLp);
        moreBtn.setOnClickListener(v -> showSettingsDialog());
        settingsLayout.addView(moreBtn);
        // 切换横屏按钮
        Button switchOriBtn = new Button(this);
        switchOriBtn.setText("切换到横屏模式");
        switchOriBtn.setTextColor(getColorCompat(R.color.yh_text));
        switchOriBtn.setBackgroundResource(R.drawable.bg_yuki_button);
        LinearLayout.LayoutParams switchOriLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(40));
        switchOriLp.setMargins(0, dp(12), 0, 0);
        switchOriBtn.setLayoutParams(switchOriLp);
        switchOriBtn.setOnClickListener(v -> toggleOrientation());
        settingsLayout.addView(switchOriBtn);

        // 关于按钮
        Button aboutBtn = new Button(this);
        aboutBtn.setText("关于 KamiGAL");
        aboutBtn.setTextColor(getColorCompat(R.color.yh_text));
        aboutBtn.setBackgroundResource(R.drawable.bg_yuki_button);
        LinearLayout.LayoutParams aboutLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(40));
        aboutLp.setMargins(0, dp(12), 0, 0);
        aboutBtn.setLayoutParams(aboutLp);
        aboutBtn.setOnClickListener(v -> showAboutDialog());
        settingsLayout.addView(aboutBtn);

        scroll.addView(settingsLayout);
        pageContent.addView(scroll, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void setupLaunchers() {
        scanDirLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), uri -> {
            if (uri != null) {
                takeFlags(uri);
                boolean changed = addOrReplaceScanRoot(uri.toString(), pendingScanRootReplaceIndex);
                pendingScanRootReplaceIndex = -2;
                if (changed) {
                    refreshActiveScanRootListUi();
                    Toast.makeText(this, "扫描目录已更新", Toast.LENGTH_SHORT).show();
                }
                runLibraryScan(uri, true);
            }
        });
        editDirLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), uri -> {
            if (uri != null) {
                takeFlags(uri);
                pendingDirUri = uri.toString();
                if (pendingCoverUri == null || pendingCoverUri.isEmpty()) {
                    Uri autoCover = findFirstLevelImage(pendingDirUri);
                    if (autoCover != null) pendingCoverUri = copyCoverToInternalStorage(autoCover);
                }
                if (pendingEditDialog != null) {
                    ((TextView) pendingEditDialog.findViewById(R.id.tvSelectedDir)).setText(pendingDirUri);
                    Spinner launchSp = pendingEditDialog.findViewById(R.id.spLaunchTarget);
                    List<String> options = buildLaunchOptions(pendingDirUri);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item_dark, options);
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_dark);
                    launchSp.setAdapter(adapter);
                    ((TextView) pendingEditDialog.findViewById(R.id.tvSelectedCover)).setText(emptyText(pendingCoverUri, "未选择封面"));
                }
            }
        });
        coverLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                pendingCoverUri = copyCoverToInternalStorage(uri);
                if (pendingEditDialog != null) ((TextView) pendingEditDialog.findViewById(R.id.tvSelectedCover)).setText(pendingCoverUri == null ? "封面复制失败" : pendingCoverUri);
            }
        });
profileAvatarLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                String avatar = copyImageToInternalStorage(uri, "avatars", "avatar_", 320, 90);
                if (avatar == null || avatar.isEmpty()) {
                    Toast.makeText(this, "头像保存失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                prefs.edit().putString(KEY_PROFILE_AVATAR, avatar).apply();
                updateProfilePanel();
                showProfileDialog();
            }
        });
        backgroundPickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                String bg = copyImageToInternalStorage(uri, "backgrounds", "bg_", 1920, 88);
                if (bg == null || bg.isEmpty()) {
                    Toast.makeText(this, "背景保存失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                replaceCustomBackground(bg, "image");
                applyCustomBackground();
                Toast.makeText(this, "已设置图片背景", Toast.LENGTH_SHORT).show();
            }
        });
        videoBackgroundPickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                String bg = copyVideoToInternalStorage(uri);
                if (bg == null || bg.isEmpty()) {
                    Toast.makeText(this, "视频背景保存失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                replaceCustomBackground(bg, "video");
                applyCustomBackground();
                Toast.makeText(this, "已设置视频背景", Toast.LENGTH_SHORT).show();
            }
        });

        backupCreateLauncher = registerForActivityResult(new ActivityResultContracts.CreateDocument("application/json"), uri -> {
            if (uri != null) exportLocalBackup(uri);
        });
        backupOpenLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri != null) importLocalBackup(uri);
        });
    }

    private File persistentRemoteCoverDir() {
    File dir = new File(getFilesDir(), "covers_remote");
    if (!dir.exists()) dir.mkdirs();
    return dir;
}

private boolean isMissingFileUri(String uriText) {
    if (uriText == null || uriText.trim().isEmpty()) return false;
    try {
        Uri uri = Uri.parse(uriText);
        if (!"file".equalsIgnoreCase(uri.getScheme())) return false;
        String path = uri.getPath();
        return path == null || !(new File(path).exists());
    } catch (Throwable ignored) {
        return false;
    }
}

private void repairMissingMetadataCoversIfNeeded() {
    if (allGames.isEmpty() || metadataRepository == null) return;
    List<Game> targets = new ArrayList<>();
    for (Game g : allGames) {
        if (g == null || g.id <= 0) continue;
        boolean noCover = !hasCover(g);
        boolean missingFile = isMissingFileUri(g.coverPersistUri) || isMissingFileUri(g.coverUri);
        if (noCover || missingFile) targets.add(g);
    }
    if (targets.isEmpty()) return;
    AppExecutors.runOnIo(() -> {
        int changed = 0;
        for (Game g : targets) {
            try {
                VnMetadata meta = false ? metadataRepository.getVndb(g.id) : metadataRepository.getVndb(g.id);
                if (meta == null) {
                    VnMetadata v = metadataRepository.getVndb(g.id);
                    VnMetadata b = metadataRepository.getVndb(g.id);
                    meta = v != null ? v : b;
                }
                if (meta == null || meta.coverUrl == null || meta.coverUrl.trim().isEmpty()) continue;
                String cover = cacheRemoteImageSync(meta.coverUrl, "repair_cover_" + emptyText(meta.id, String.valueOf(g.id)));
                if (cover == null || cover.isEmpty()) continue;
                g.coverUri = cover;
                g.coverPersistUri = cover;
                g.coverSourceType = 1;
                repository.update(g);
                changed++;
            } catch (Throwable t) {
                Log.w("KamiGAL", "repair cover failed: " + (g == null ? "null" : g.title), t);
            }
        }
        int finalChanged = changed;
        if (finalChanged > 0) runOnUiThread(() -> {
            allGames.clear();
            allGames.addAll(repository.getAll());
            applyFilter();
            Toast.makeText(this, "已恢复 " + finalChanged + " 个同步封面", Toast.LENGTH_SHORT).show();
        });
    });
}

private void deleteInternalFileUri(String uriText) {
    if (uriText == null || uriText.trim().isEmpty()) return;
    try {
        Uri uri = Uri.parse(uriText);
        if (!"file".equalsIgnoreCase(uri.getScheme())) return;
        String path = uri.getPath();
        if (path == null) return;
        File file = new File(path);
        File filesRoot = getFilesDir();
        String fp = file.getCanonicalPath();
        String rp = filesRoot.getCanonicalPath();
        if (fp.startsWith(rp) && file.exists()) file.delete();
    } catch (Throwable ignored) { }
}

private void replaceCustomBackground(String bg, String type) {
    String old = prefs == null ? null : prefs.getString(KEY_CUSTOM_BACKGROUND, "");
    if (prefs != null) prefs.edit().putString(KEY_CUSTOM_BACKGROUND, bg).putString(KEY_CUSTOM_BACKGROUND_TYPE, type).apply();
    if (old != null && !old.equals(bg)) deleteInternalFileUri(old);
}

private String copyCoverToInternalStorage(Uri uri) {
return copyImageToInternalStorage(uri, "covers", "cover_", 720, 88);
}

private void applyCustomBackground() {
    if (prefs == null) return;
    ImageView bgImage = findViewById(R.id.customBackgroundImage);
    TextureView bgVideo = findViewById(R.id.customBackgroundVideo);
    View bgDim = findViewById(R.id.customBackgroundDim);
    if (bgImage == null || bgVideo == null || bgDim == null) return;
    String bg = prefs.getString(KEY_CUSTOM_BACKGROUND, "");
    String type = prefs.getString(KEY_CUSTOM_BACKGROUND_TYPE, "image");
    boolean dimEnabled = prefs.getBoolean(KEY_BACKGROUND_DIM_ENABLED, true);
    if (bg == null || bg.isEmpty()) {
        // 检查是否有默认视频背景
        java.io.File defaultVideo = new java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "default_bg.mp4");
        if (defaultVideo.exists()) {
            bg = Uri.fromFile(defaultVideo).toString();
            type = "video";
        } else {
            stopBackgroundVideo();
            bgImage.setImageDrawable(null);
            bgImage.setVisibility(View.GONE);
            bgVideo.setVisibility(View.GONE);
            bgDim.setVisibility(View.GONE);
            return;
        }
    }
    try {
        if ("video".equals(type)) {
            bgImage.setImageDrawable(null);
            bgImage.setVisibility(View.GONE);
            bgVideo.setVisibility(View.VISIBLE);
            bgDim.setVisibility(dimEnabled ? View.VISIBLE : View.GONE);
            playBackgroundVideo(bgVideo, Uri.parse(bg), true);
        } else {
            stopBackgroundVideo();
            bgVideo.setVisibility(View.GONE);
            bgImage.setImageURI(Uri.parse(bg));
            bgImage.setVisibility(View.VISIBLE);
            bgDim.setVisibility(dimEnabled ? View.VISIBLE : View.GONE);
        }
    } catch (Throwable t) {
        prefs.edit().remove(KEY_CUSTOM_BACKGROUND).remove(KEY_CUSTOM_BACKGROUND_TYPE).apply();
        stopBackgroundVideo();
        bgImage.setImageDrawable(null);
        bgImage.setVisibility(View.GONE);
        bgVideo.setVisibility(View.GONE);
        bgDim.setVisibility(View.GONE);
    }
}

private void playBackgroundVideo(TextureView textureView, Uri uri, boolean forceRestart) {
    pendingBackgroundVideoUri = uri;
    if (forceRestart) releaseBackgroundMediaPlayer();
    textureView.setSurfaceTextureListener(null);
    if (textureView.isAvailable()) {
        textureView.post(() -> startBackgroundMediaPlayer(textureView, uri));
    } else {
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                startBackgroundMediaPlayer(textureView, uri);
            }
            @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                applyVideoCenterCrop(textureView, backgroundMediaPlayer);
            }
            @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                releaseBackgroundMediaPlayer();
                return true;
            }
            @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) { }
        });
    }
}

private void startBackgroundMediaPlayer(TextureView textureView, Uri uri) {
    try {
        releaseBackgroundMediaPlayer();
        MediaPlayer mp = new MediaPlayer();
        backgroundMediaPlayer = mp;
        mp.setDataSource(this, uri);
        Surface surface = new Surface(textureView.getSurfaceTexture());
        mp.setSurface(surface);
        surface.release();
        mp.setLooping(true);
        boolean soundOn = prefs != null && prefs.getBoolean(KEY_BACKGROUND_VIDEO_SOUND, false);
        mp.setVolume(soundOn ? 1f : 0f, soundOn ? 1f : 0f);
        mp.setOnPreparedListener(player -> {
            applyVideoCenterCrop(textureView, player);
            player.start();
        });
        mp.setOnErrorListener((player, what, extra) -> {
            Toast.makeText(this, "视频背景播放失败，请尝试更换视频格式", Toast.LENGTH_SHORT).show();
            releaseBackgroundMediaPlayer();
            return true;
        });
        mp.prepareAsync();
    } catch (Throwable t) {
        if (prefs != null) prefs.edit().remove(KEY_CUSTOM_BACKGROUND).remove(KEY_CUSTOM_BACKGROUND_TYPE).apply();
        applyCustomBackground();
    }
}

private void applyVideoCenterCrop(TextureView textureView, MediaPlayer player) {
    if (textureView == null || player == null) return;
    int viewW = textureView.getWidth();
    int viewH = textureView.getHeight();
    int videoW = player.getVideoWidth();
    int videoH = player.getVideoHeight();
    if (viewW <= 0 || viewH <= 0 || videoW <= 0 || videoH <= 0) return;
    float scale = Math.max((float) viewW / videoW, (float) viewH / videoH);
    float scaledW = videoW * scale;
    float scaledH = videoH * scale;
    Matrix matrix = new Matrix();
    matrix.setScale(scaledW / viewW, scaledH / viewH, viewW / 2f, viewH / 2f);
    textureView.setTransform(matrix);
}

private void releaseBackgroundMediaPlayer() {
    if (backgroundMediaPlayer == null) return;
    try { backgroundMediaPlayer.stop(); } catch (Throwable ignored) { }
    try { backgroundMediaPlayer.release(); } catch (Throwable ignored) { }
    backgroundMediaPlayer = null;
}

private void stopBackgroundVideo() {
    pendingBackgroundVideoUri = null;
    releaseBackgroundMediaPlayer();
}

private String copyVideoToInternalStorage(Uri uri) {
    try {
        java.io.File dir = new java.io.File(getFilesDir(), "backgrounds");
        if (!dir.exists()) dir.mkdirs();
        java.io.File file = new java.io.File(dir, "bg_video_" + System.currentTimeMillis() + ".mp4");
        try (InputStream in = getContentResolver().openInputStream(uri); java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
            if (in == null) return null;
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) != -1) out.write(buf, 0, len);
            out.flush();
        }
        return Uri.fromFile(file).toString();
    } catch (Throwable t) {
        return null;
    }
}

private String copyImageToInternalStorage(Uri uri, String folder, String prefix, int max, int quality) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
            if (bitmap == null) return null;
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            if (w > max || h > max) {
                float scale = Math.min(max / (float) w, max / (float) h);
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, Math.max(1, (int) (w * scale)), Math.max(1, (int) (h * scale)), true);
                bitmap.recycle();
                bitmap = scaled;
            }
            java.io.File dir = new java.io.File(getFilesDir(), folder == null ? "images" : folder);
            if (!dir.exists()) dir.mkdirs();
            java.io.File file = new java.io.File(dir, (prefix == null ? "image_" : prefix) + System.currentTimeMillis() + ".jpg");
            java.io.FileOutputStream out = new java.io.FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
            out.flush();
            out.close();
            bitmap.recycle();
            return Uri.fromFile(file).toString();
        } catch (Exception e) {
            return null;
        }
    }

    private void takeFlags(Uri uri) {
        if (uri == null) return;
        int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
        try {
            getContentResolver().takePersistableUriPermission(uri, flags);
            Log.i("KamiGAL", "persisted tree permission: " + uri);
        } catch (SecurityException writeDenied) {
            try {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Log.i("KamiGAL", "persisted read-only tree permission: " + uri);
            } catch (Exception readDenied) {
                Log.w("KamiGAL", "persist tree permission failed: " + uri, readDenied);
                Toast.makeText(this, "目录授权保存失败，请重新选择 TF 卡目录", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.w("KamiGAL", "persist tree permission failed: " + uri, e);
            Toast.makeText(this, "目录授权保存失败，请重新选择 TF 卡目录", Toast.LENGTH_LONG).show();
        }
    }

    private void scanMissingCoversIfNeeded() {
        if (coverScanRunning || allGames.isEmpty()) return;
        List<Game> targets = new ArrayList<>();
        for (Game g : allGames) {
            if (g == null || g.rootUri == null || g.rootUri.isEmpty()) continue;
            if (hasCover(g)) continue;
            targets.add(g);
        }
        if (targets.isEmpty()) return;
        coverScanRunning = true;
        AppExecutors.runOnIo(() -> {
            int changed = 0;
            for (Game g : targets) {
                try {
                    Uri image = findFirstLevelImage(g.rootUri);
                    if (image == null) continue;
                    String cover = copyCoverToInternalStorage(image);
                    if (cover == null || cover.isEmpty()) continue;
                    g.coverUri = cover;
                    g.coverPersistUri = cover;
                    g.coverSourceType = 1;
                    repository.update(g);
                    changed++;
                } catch (Throwable ignored) { }
            }
            int finalChanged = changed;
            runOnUiThread(() -> {
                coverScanRunning = false;
                if (finalChanged > 0) {
                    allGames.clear();
                    allGames.addAll(repository.getAll());
                    applyFilter();
                }
            });
        });
    }

    private boolean hasCover(Game g) {
        return (g.coverPersistUri != null && !g.coverPersistUri.trim().isEmpty())
                || (g.coverUri != null && !g.coverUri.trim().isEmpty());
    }

    private Uri findFirstLevelImage(String rootUri) {
        try {
            if (rootUri == null || rootUri.trim().isEmpty()) return null;
            DocumentFile dir = null;
            if (rootUri.startsWith("/") || rootUri.startsWith("file://")) {
                File file = new File(rootUri.startsWith("file://") ? Uri.parse(rootUri).getPath() : rootUri);
                dir = DocumentFile.fromFile(file);
            } else {
                dir = DocumentFile.fromTreeUri(this, Uri.parse(rootUri));
            }
            if (dir == null || !dir.isDirectory()) return null;
            DocumentFile[] files = dir.listFiles();
            if (files == null) return null;
            DocumentFile best = null;
            int bestScore = Integer.MIN_VALUE;
            for (DocumentFile f : files) {
                if (f == null || !f.isFile()) continue;
                String name = f.getName();
                if (!isImageFile(name)) continue;
                int score = coverNameScore(name);
                if (best == null || score > bestScore) {
                    best = f;
                    bestScore = score;
                }
            }
            return best == null ? null : best.getUri();
        } catch (Throwable ignored) { return null; }
    }

    private boolean isImageFile(String name) {
        if (name == null) return false;
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp") || lower.endsWith(".bmp");
    }

    private int coverNameScore(String name) {
        if (name == null) return 0;
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.equals("cover.jpg") || lower.equals("cover.png") || lower.equals("cover.webp")) return 100;
        if (lower.equals("folder.jpg") || lower.equals("folder.png") || lower.equals("folder.webp")) return 95;
        if (lower.contains("cover") || lower.contains("folder") || lower.contains("封面")) return 80;
        if (lower.contains("poster") || lower.contains("package") || lower.contains("main")) return 60;
        return 10;
    }
 
    private void setupUi() {
        RecyclerView recycler = findViewById(R.id.recyclerGames);
        tvEmpty = findViewById(R.id.tvEmpty);
tvStats = findViewById(R.id.tvStats);
tvProfileName = findViewById(R.id.tvProfileName);
tvProfileInitial = findViewById(R.id.tvProfileInitial);
profileStatusDot = findViewById(R.id.profileStatusDot);
ivProfileAvatar = findViewById(R.id.ivProfileAvatar);
detailPanel = findViewById(R.id.detailPanel);
        detailMetaPanel = findViewById(R.id.detailMetaPanel);
        sideDetailCover = findViewById(R.id.sideDetailCover);
        sideDetailPlaceholder = findViewById(R.id.sideDetailPlaceholder);
        sideDetailTitle = findViewById(R.id.sideDetailTitle);
sideDetailOriginalTitle = findViewById(R.id.sideDetailOriginalTitle);
sideDetailHint = findViewById(R.id.sideDetailHint);
sideDetailPath = findViewById(R.id.sideDetailPath);
sideDescToggle = findViewById(R.id.sideDescToggle);
sideTranslateToggle = findViewById(R.id.sideTranslateToggle);
        sideDetailDeveloper = findViewById(R.id.sideDetailDeveloper);
        sideDetailDate = findViewById(R.id.sideDetailDate);
        sideDetailRating = findViewById(R.id.sideDetailRating);
sideDetailLength = findViewById(R.id.sideDetailLength);
sideDetailTags = findViewById(R.id.sideDetailTags);
sideTagContainer = findViewById(R.id.sideTagContainer);
sideScreenshot1 = findViewById(R.id.sideScreenshot1);
sideScreenshot2 = findViewById(R.id.sideScreenshot2);
sideBtnLaunch = findViewById(R.id.sideBtnLaunch);
        sideBtnOptions = findViewById(R.id.sideBtnOptions);
        sideBtnLaunch.setOnClickListener(v -> { clickFeedback(v); if (selectedGame != null) launchGame(selectedGame); });
        sideBtnOptions.setOnClickListener(v -> { clickFeedback(v); if (selectedGame != null) showSideOptions(selectedGame); });
        sideDescToggle.setOnClickListener(v -> { clickFeedback(v); sideDescExpanded = !sideDescExpanded; renderSideDescription(); });
if (sideTranslateToggle != null) sideTranslateToggle.setOnClickListener(v -> { clickFeedback(v); toggleOrTranslateDescription(); });
        updateSideDetail(null);
        setupDeveloperToggle();
        adapter = new GameAdapter();
        adapter.setOnGameClickListener(new GameAdapter.OnGameClickListener() {
            @Override public void onGameClick(Game game) {
                if (isPortrait) {
                    // 竖屏模式：单击弹出运行按钮
                    showPortraitLaunchConfirm(game);
                } else {
                    updateSideDetail(game);
                }
            }
            @Override public void onGameDoubleClick(Game game) { if (game != null) launchGame(game); }
            @Override public void onGameLongClick(Game game) {
                if (isPortrait) {
                    // 竖屏长按：弹出菜单（查看详情/编辑/删除）
                    showPortraitGameMenu(game);
                } else {
                    showEditDialog(game);
                }
            }
            @Override public void onStatusClick(Game game) { updateSideDetail(game); showPlayStatusDialog(game, null); }
        });
        recycler.setLayoutManager(new GridLayoutManager(this, 5));
        recycler.setHasFixedSize(true);
        recycler.setItemViewCacheSize(20);
        recycler.setAdapter(adapter);
View addButton = findViewById(R.id.btnAdd);
        View scanButton = findViewById(R.id.btnScan);
        ivScanLoading = findViewById(R.id.ivScanLoading);
  View settingsButton = findViewById(R.id.btnSettings);
        View emulatorsButton = findViewById(R.id.btnEmulators);
View imageSearchButton = findViewById(R.id.btnImageSearch);
btnOrientation = findViewById(R.id.btnOrientation);
applyTopActionFeedback(settingsButton);
applyTopActionFeedback(scanButton);
applyTopActionFeedback(settingsButton);
applyTopActionFeedback(emulatorsButton);
applyTopActionFeedback(imageSearchButton);
 if (btnOrientation != null) {
            applyTopActionFeedback(btnOrientation);
            btnOrientation.setOnClickListener(v -> { clickFeedback(v); toggleOrientation(); });
        }
addButton.setOnClickListener(v -> { clickFeedback(v); showEditDialog(null); });
scanButton.setOnClickListener(v -> { clickFeedback(v); scanLastRootOrChoose(); });
scanButton.setOnLongClickListener(v -> { clickFeedback(v); scanDirLauncher.launch(null); return true; });
settingsButton.setOnClickListener(v -> { clickFeedback(v); showSettingsDialog(); });
        emulatorsButton.setOnClickListener(v -> { clickFeedback(v); showEmulatorsDialog(); });
imageSearchButton.setOnClickListener(v -> { clickFeedback(v); showImageSearchDialog(); });
View btnOnlineSearch = findViewById(R.id.btnOnlineSearch);
if (btnOnlineSearch != null) btnOnlineSearch.setOnClickListener(v -> { clickFeedback(v); showOnlineSearchDialog(); });
        View btnAbout = findViewById(R.id.btnAbout);
        if (btnAbout != null) btnAbout.setOnClickListener(v -> { clickFeedback(v); showAboutDialog(); });
        View profilePanel = findViewById(R.id.profilePanel);
if (profilePanel != null) profilePanel.setOnClickListener(v -> { clickFeedback(v); showProfileDialog(); });
        setupDeveloperToggle();
bindFilter(R.id.filterAll, "ALL"); bindFilter(R.id.filterRecent, "RECENT");
bindFilter(R.id.filterPlaying, "PLAYING"); bindFilter(R.id.filterCompleted, "COMPLETED"); bindFilter(R.id.filterUnplayed, "UNPLAYED");
        updateFilterSelection();
        ((EditText)findViewById(R.id.etSearch)).addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int st, int b, int c) { query = s.toString(); applyFilter(); }
            public void afterTextChanged(Editable e) {}
        });

        // 初始化竖屏控件
        portraitContainer = findViewById(R.id.portraitContainer);
        pageContent = findViewById(R.id.pageContent);
        bottomNav = findViewById(R.id.bottomNav);
        navLibrary = findViewById(R.id.navLibrary);
        navSearchPage = findViewById(R.id.navSearchPage);
        navProfile = findViewById(R.id.navProfile);
        navSettingsPage = findViewById(R.id.navSettingsPage);
        navEmulators = findViewById(R.id.navEmulators);
navImageSearch = findViewById(R.id.navImageSearch);
navSettingsPage = findViewById(R.id.navSettingsPage);
if (navLibrary != null) navLibrary.setOnClickListener(v -> switchPortraitPage(0));
if (navSearchPage != null) navSearchPage.setOnClickListener(v -> switchPortraitPage(1));
if (navProfile != null) navProfile.setOnClickListener(v -> switchPortraitPage(2));
if (navEmulators != null) navEmulators.setOnClickListener(v -> switchPortraitPage(3));
if (navImageSearch != null) navImageSearch.setOnClickListener(v -> switchPortraitPage(5));
if (navSettingsPage != null) navSettingsPage.setOnClickListener(v -> switchPortraitPage(4));
        // 默认横屏启动
// 根据保存的方向偏好设置UI布局（系统方向已在onCreate中通过setRequestedOrientation设置）
            boolean savedPortrait = prefs.getBoolean("pref_portrait_mode", false);
            isPortrait = savedPortrait;
            updateLayoutForOrientation();
    }

    private void applyTopActionFeedback(View view) {
    if (view == null) return;
    view.setOnTouchListener((v, event) -> {
        if (event == null) return false;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            v.animate().cancel();
            v.animate().scaleX(0.92f).scaleY(0.92f).alpha(0.78f).setDuration(70L).start();
        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            v.animate().cancel();
            v.animate().scaleX(1.0f).scaleY(1.0f).alpha(1.0f).setDuration(120L).start();
        }
        return false;
    });
}

private void clickFeedback(View v) {
    if (v == null) return;
    try { v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY); } catch (Throwable ignored) { }
}

private void setScanLoading(boolean loading) {
    if (ivScanLoading == null) return;
    if (loading) {
        ivScanLoading.setVisibility(View.VISIBLE);
        ivScanLoading.setRotation(0f);
        if (scanAnimator == null) {
            scanAnimator = ObjectAnimator.ofFloat(ivScanLoading, View.ROTATION, 0f, 360f);
            scanAnimator.setDuration(900L);
            scanAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            scanAnimator.setInterpolator(new LinearInterpolator());
        }
        if (!scanAnimator.isStarted()) scanAnimator.start();
        scanLoadingAnimated = true;
    } else {
        if (scanAnimator != null) {
            try { scanAnimator.cancel(); } catch (Throwable ignored) { }
        }
        ivScanLoading.setRotation(0f);
        ivScanLoading.setVisibility(View.GONE);
        scanLoadingAnimated = false;
    }
}
private void showPortraitEmulators() { buildEmulatorsPage(pageContent); }
    private void showEmulatorsDialog() {
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this).create();
        dialog.setTitle("改版模拟器");
        ScrollView scrollView = new ScrollView(this);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(20), dp(10), dp(20), dp(10));
        // VPN提示横幅
        TextView vpnBanner = new TextView(this);
        vpnBanner.setText("⚠ 以下链接需要VPN才能访问，请确保已开启VPN再点击下载");
        vpnBanner.setTextColor(0xFFEF5350);
        vpnBanner.setTextSize(11);
        vpnBanner.setPadding(dp(12), dp(10), dp(12), dp(10));
        vpnBanner.setBackgroundColor(0x18EF5350);
        vpnBanner.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams bannerLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bannerLp.setMargins(0, 0, 0, dp(12));
        vpnBanner.setLayoutParams(bannerLp);
        container.addView(vpnBanner);
        buildEmulatorsList(container);
        scrollView.addView(container);
        dialog.setView(scrollView);
        dialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, "关闭", (android.content.DialogInterface.OnClickListener) null);
        styleAlertDialogDark(dialog);
        dialog.show();
    }
    private void buildEmulatorsPage(ViewGroup parent) {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(20), dp(20), dp(20));
        // 标题
        TextView title = new TextView(this);
        title.setText("改版模拟器");
        title.setTextColor(getColorCompat(R.color.yh_text));
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(0, 0, 0, dp(16));
        root.addView(title);
        // VPN提示横幅
        TextView vpnBanner = new TextView(this);
        vpnBanner.setText("⚠ 以下链接需要VPN才能访问，请确保已开启VPN再点击下载");
        vpnBanner.setTextColor(0xFFEF5350);
        vpnBanner.setTextSize(11);
        vpnBanner.setPadding(dp(12), dp(10), dp(12), dp(10));
        vpnBanner.setBackgroundColor(0x18EF5350);
        vpnBanner.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams bannerLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bannerLp.setMargins(0, 0, 0, dp(16));
        vpnBanner.setLayoutParams(bannerLp);
        root.addView(vpnBanner);
        buildEmulatorsList(root);
        scroll.addView(root);
        parent.addView(scroll);
    }
    private void buildEmulatorsList(LinearLayout root) {
        // 模拟器数据：{名称, 描述, 下载URL}
        String[][] emulators = {
            {"GameHub 共存版", "GameHub 5.3.5 共存初始版", "https://github.com/xm486/YukiHub/releases/download/v0.1.0/gamehub_535_gongcun_init.apk"},
            {"GameHub 普通版", "GameHub 5.3.5 初始版，经典模拟器", "https://github.com/xm486/YukiHub/releases/download/v0.1.0/gamehub_535_init.apk"},
            {"WinlatorCN", "WinlatorCN 11.0F Beta1，在Android上运行Windows应用", "https://github.com/xm486/YukiHub/releases/download/v0.1.0/WinlatorCN_11.0F_beta1_activity.apk"}
        };
        for (String[] emu : emulators) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundDrawable(getDrawable(R.drawable.bg_game_card));
            card.setPadding(dp(16), dp(16), dp(16), dp(16));
            LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cardLp.setMargins(0, 0, 0, dp(12));
            card.setLayoutParams(cardLp);
            
            TextView nameText = new TextView(this);
            nameText.setText(emu[0]);
            nameText.setTextColor(getColorCompat(R.color.yh_text));
            nameText.setTextSize(16);
            nameText.setTypeface(null, android.graphics.Typeface.BOLD);
            card.addView(nameText);
            
            TextView descText = new TextView(this);
            descText.setText(emu[1]);
            descText.setTextColor(getColorCompat(R.color.yh_text_muted));
            descText.setTextSize(11);
            descText.setPadding(0, dp(6), 0, dp(12));
            card.addView(descText);
            
            final String dlUrl = emu[2];
            // 下载链接按钮（点击跳转浏览器）
            TextView btnLink = new TextView(this);
            btnLink.setText("🔗 下载链接（点击跳转）");
            btnLink.setTextColor(0xFF4FC3F7);
            btnLink.setTextSize(12);
            btnLink.setTypeface(null, android.graphics.Typeface.BOLD);
            btnLink.setBackgroundDrawable(getDrawable(R.drawable.bg_yuki_button));
            btnLink.setPadding(dp(16), dp(8), dp(16), dp(8));
            btnLink.setGravity(android.view.Gravity.CENTER);
            btnLink.setOnClickListener(v -> {
                try {
                    android.content.Intent browserIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(dlUrl));
                    startActivity(browserIntent);
                } catch (Exception e) {
                    android.widget.Toast.makeText(this, "无法打开浏览器", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
            card.addView(btnLink);
            
            // VPN提示
            TextView vpnTip = new TextView(this);
            vpnTip.setText("⚠ 该链接需要VPN才能访问，请确保网络通畅");
            vpnTip.setTextColor(0xFFEF5350);
            vpnTip.setTextSize(10);
            vpnTip.setPadding(0, dp(8), 0, 0);
            card.addView(vpnTip);
            
            root.addView(card);
        }
    }
    // 使用DownloadManager下载
    private void startDmDownload(String url, String savePath, String fileName, String displayName) {
        try {
            android.app.DownloadManager.Request req = new android.app.DownloadManager.Request(android.net.Uri.parse(url));
            req.setTitle(displayName); req.setDescription("下载完成后点击安装");
            req.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setDestinationUri(android.net.Uri.fromFile(new java.io.File(savePath)));
            req.setMimeType("application/vnd.android.package-archive");
            req.setAllowedNetworkTypes(android.app.DownloadManager.Request.NETWORK_WIFI | android.app.DownloadManager.Request.NETWORK_MOBILE);
            android.app.DownloadManager dm = (android.app.DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (dm != null) {
                long id = dm.enqueue(req);
                prefs.edit().putLong("dm_id_" + fileName, id).putString("dm_name_" + id, displayName).putString("dm_path_" + id, savePath).apply();
                android.widget.Toast.makeText(this, "已开始下载 " + displayName + "，通知栏可查看进度", android.widget.Toast.LENGTH_SHORT).show();
                android.content.BroadcastReceiver receiver = new android.content.BroadcastReceiver() {
                    @Override public void onReceive(android.content.Context ctx, android.content.Intent intent) {
                        long did = intent.getLongExtra(android.app.DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                        if (did == id) {
                            android.widget.Toast.makeText(MainActivity.this, displayName + " 下载完成", android.widget.Toast.LENGTH_SHORT).show();
                            installApk(savePath);
                            try { unregisterReceiver(this); } catch (Exception ignored) {}
                        }
                    }
                };
                registerReceiver(receiver, new android.content.IntentFilter(android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            }
        } catch (Exception e) {
            android.widget.Toast.makeText(this, "下载失败：" + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
        }
    }
    private void installApk(String apkPath) {
        try {
            java.io.File apkFile = new java.io.File(apkPath);
            android.content.Intent installIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                installIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            installIntent.setDataAndType(android.net.Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            installIntent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(installIntent);
        } catch (Exception e) {
            android.widget.Toast.makeText(this, "安装失败：" + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    // ======== 下载记录管理 ========
        private void showProfileDialog() {
        // ========== 横竖屏统一弹窗 ==========
        final boolean portraitNow = isPortrait;
        final String currentName = displayProfileName();
        final String localName = profileName();
    final String currentSignature = profileSignature();
    long total = totalPlayTime();

    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setBackgroundResource(R.drawable.bg_dialog);
    int pad = dp(16);
    root.setPadding(pad, dp(14), pad, dp(10));

    LinearLayout header = new LinearLayout(this);
    header.setOrientation(LinearLayout.HORIZONTAL);
    header.setGravity(android.view.Gravity.CENTER_VERTICAL);

    FrameLayout avatarBox = new FrameLayout(this);
    avatarBox.setBackgroundResource(R.drawable.bg_cover_placeholder);
    ImageView avatar = new ImageView(this);
    avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
    TextView avatarInitial = new TextView(this);
    avatarInitial.setGravity(android.view.Gravity.CENTER);
    avatarInitial.setText(initials(currentName));
    avatarInitial.setTextColor(getColorCompat(R.color.yh_text));
    avatarInitial.setTextSize(24);
    avatarInitial.setTypeface(null, android.graphics.Typeface.BOLD);
    avatarBox.addView(avatar, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
    avatarBox.addView(avatarInitial, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
    loadProfileAvatarInto(avatar, avatarInitial);
    avatarBox.setOnClickListener(v -> profileAvatarLauncher.launch("image/*"));
    header.addView(avatarBox, new LinearLayout.LayoutParams(dp(72), dp(72)));

    LinearLayout info = new LinearLayout(this);
    info.setOrientation(LinearLayout.VERTICAL);
    info.setPadding(dp(12), 0, 0, 0);
    TextView nameView = new TextView(this);
    nameView.setText(currentName);
    nameView.setTextColor(getColorCompat(R.color.yh_text));
    nameView.setTextSize(20);
    nameView.setTypeface(null, android.graphics.Typeface.BOLD);
    TextView statsView = new TextView(this);
    statsView.setText(allGames.size() + " Games · " + TimeFormatUtil.playTime(total) + "\n" + emptyText(currentSignature, "这个人还没有写签名"));
    statsView.setTextColor(getColorCompat(R.color.yh_text_muted));
    statsView.setTextSize(12);
    statsView.setPadding(0, dp(5), 0, 0);
    info.addView(nameView);
    info.addView(statsView);
    header.addView(info, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
    root.addView(header);

    LinearLayout statCards = new LinearLayout(this);
    statCards.setOrientation(LinearLayout.HORIZONTAL);
    statCards.setPadding(0, dp(2), 0, dp(10));
    statCards.addView(profileStatCard("游戏", String.valueOf(allGames.size())), new LinearLayout.LayoutParams(0, dp(48), 1));
    LinearLayout.LayoutParams statMid = new LinearLayout.LayoutParams(0, dp(48), 1);
    statMid.setMargins(dp(6), 0, dp(6), 0);
    statCards.addView(profileStatCard("总时长", TimeFormatUtil.playTime(total)), statMid);
    statCards.addView(profileStatCard("今日", TimeFormatUtil.playTime(todayTotalPlayTime())), new LinearLayout.LayoutParams(0, dp(48), 1));
    root.addView(statCards);

    // 在线数据行
    LinearLayout onlineRow = new LinearLayout(this);
    onlineRow.setOrientation(LinearLayout.HORIZONTAL);
    onlineRow.setPadding(0, 0, 0, dp(4));
    final TextView ovOnline = profileStatCard("在线", "...");
    final TextView ovToday = profileStatCard("今日使用", "...");
    statsOvOnline = ovOnline;
    statsOvToday = ovToday;
    LinearLayout.LayoutParams ovMid = new LinearLayout.LayoutParams(0, dp(48), 1);
    ovMid.setMargins(dp(6), 0, dp(6), 0);
    onlineRow.addView(ovOnline, new LinearLayout.LayoutParams(0, dp(48), 1));
    onlineRow.addView(ovToday, ovMid);
    root.addView(onlineRow);

    TextView nameLabel = profileLabel("昵称");
    root.addView(nameLabel);
    EditText nameInput = profileEdit(localName, "输入昵称");
    root.addView(nameInput, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(42)));

    TextView signLabel = profileLabel("个人签名");
    signLabel.setPadding(0, dp(10), 0, dp(4));
    root.addView(signLabel);
    EditText signatureInput = profileEdit(currentSignature, "写点什么，比如：今天也要认真补完一部作品");
    signatureInput.setSingleLine(false);
    signatureInput.setMinLines(2);
    signatureInput.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
    root.addView(signatureInput, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(62)));

    TextView activityTitle = profileLabel("今日动态");
    activityTitle.setPadding(0, dp(12), 0, dp(4));
    root.addView(activityTitle);
    TextView activity = new TextView(this);
    activity.setText(buildTodayActivityText());
    activity.setTextColor(getColorCompat(R.color.yh_text_muted));
    activity.setTextSize(12);
    activity.setLineSpacing(dp(1), 1.0f);
    activity.setBackgroundResource(R.drawable.bg_input);
    activity.setPadding(dp(10), dp(8), dp(10), dp(8));
    root.addView(activity, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

    TextView recentTitle = profileLabel("最近动态");
    recentTitle.setPadding(0, dp(12), 0, dp(4));
    root.addView(recentTitle);
    LinearLayout feedList = new LinearLayout(this);
    feedList.setOrientation(LinearLayout.VERTICAL);
    buildRecentActivityViews(feedList);
    root.addView(feedList, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

    ScrollView scroll = new ScrollView(this);
    scroll.setFillViewport(false);
    scroll.setBackgroundResource(R.drawable.bg_dialog);
    scroll.addView(root, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

    AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("个人资料")
            .setView(scroll)
            .setPositiveButton("保存", null)
            .setNeutralButton("更换头像", null)
            .setNegativeButton("关闭", null)
            .show();
    startStatsPolling();
    dialog.setOnDismissListener(d -> stopStatsPolling());
    styleAlertDialogDark(dialog);
        if (dialog.getWindow() != null) {
            // 取当前实际屏幕方向来计算宽度，高度自适应内容避免底部空白
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getRealMetrics(dm);
            boolean actuallyPortrait = dm.widthPixels < dm.heightPixels;
            float wRatio = actuallyPortrait ? 0.82f : 0.62f;
            dialog.getWindow().setLayout((int) (dm.widthPixels * wRatio), android.view.WindowManager.LayoutParams.WRAP_CONTENT);
        }
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
        String name = nameInput.getText() == null ? "" : nameInput.getText().toString().trim();
        String sign = signatureInput.getText() == null ? "" : signatureInput.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "昵称不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        prefs.edit().putString(KEY_PROFILE_NAME, name).putString(KEY_PROFILE_SIGNATURE, sign).apply();
        updateProfilePanel();
        Toast.makeText(this, "个人资料已保存", Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    });
    dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> profileAvatarLauncher.launch("image/*"));
    }

    private String profileName() {
    return prefs == null ? "かみ" : prefs.getString(KEY_PROFILE_NAME, "かみ");
}

private String profileSignature() {
    return prefs == null ? "" : prefs.getString(KEY_PROFILE_SIGNATURE, "");
}

private long totalPlayTime() {
    long total = 0;
    for (Game g : allGames) if (g != null) total += g.totalPlayTime;
    return total;
}


private void showAboutDialog() {
    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setBackgroundResource(R.drawable.bg_dialog);
    int pad = dp(16);
    root.setPadding(pad, dp(14), pad, dp(10));

    TextView title = new TextView(this);
    title.setText("关于 KamiGAL");
    title.setTextColor(getColorCompat(R.color.yh_text));
    title.setTextSize(18);
    title.setTypeface(null, android.graphics.Typeface.BOLD);
    root.addView(title);

    TextView version = new TextView(this);
    try {
        String vn = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        int vc = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        version.setText("版本：" + vn + " (Code " + vc + ")");
    } catch (Exception e) {
        version.setText("版本：未知");
    }
    version.setTextColor(getColorCompat(R.color.yh_text));
    version.setTextSize(14);
    version.setPadding(0, dp(8), 0, 0);
    root.addView(version);

    TextView thanks = new TextView(this);
    String html = "\nKamiGAL 是一款 Galgame 启动与管理工具，\n致力于提供简洁优雅的本地游戏管理体验。\n\n感谢 <a href=\"https://github.com/xm486\">xm486</a> 的 YukiHub 项目带来的启发。\n\n有条件的话请支持正版游戏，谢谢~";
    thanks.setText(Html.fromHtml(html));
    thanks.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
    thanks.setLinkTextColor(getColorCompat(R.color.yh_primary));
    thanks.setTextColor(getColorCompat(R.color.yh_text_muted));
    thanks.setTextSize(14);
    thanks.setLineSpacing(dp(3), 1.0f);
    thanks.setPadding(0, dp(4), 0, dp(6));
    root.addView(thanks);

    // 按钮行：检查更新 + 更新日志
    LinearLayout btnRow = new LinearLayout(this);
    btnRow.setOrientation(LinearLayout.HORIZONTAL);
    btnRow.setPadding(0, dp(8), 0, 0);

    Button checkBtn = new Button(this);
    checkBtn.setText("检查更新");
    checkBtn.setTextColor(getColorCompat(R.color.yh_text));
    checkBtn.setBackgroundResource(R.drawable.bg_yuki_button);
    checkBtn.setTextSize(13);
    checkBtn.setOnClickListener(v -> {
        checkUpdate();
        // 如果没更新，给个提示
        Toast.makeText(this, "正在检查更新…", Toast.LENGTH_SHORT).show();
    });
    LinearLayout.LayoutParams checkLp = new LinearLayout.LayoutParams(0, dp(36), 1);
    checkLp.setMargins(0, 0, dp(6), 0);
    btnRow.addView(checkBtn, checkLp);

    Button logBtn = new Button(this);
    logBtn.setText("更新日志");
    logBtn.setTextColor(getColorCompat(R.color.yh_text));
    logBtn.setBackgroundResource(R.drawable.bg_yuki_button);
    logBtn.setTextSize(13);
    logBtn.setOnClickListener(v -> showChangelogDialog());
    LinearLayout.LayoutParams logLp = new LinearLayout.LayoutParams(0, dp(36), 1);
    logLp.setMargins(dp(6), 0, 0, 0);
    btnRow.addView(logBtn, logLp);

    root.addView(btnRow);

    AlertDialog dialog = new AlertDialog.Builder(this)
        .setView(root)
.create();
    styleAlertDialogDark(dialog);
    dialog.show();
}

// ========== 更新日志弹窗 ==========
private void showChangelogDialog() {
    String changelog =
            "v1.0.3 (2026-06-04)\n" +
            "- 公告系统上线\n" +
            "- 新增关于页面，版本号动态显示\n" +
            "- 添加手动检查更新和更新日志按钮\n" +
            "- 添加改版模拟器下载跳转\n" +
            "- 添加起爆器\n" +
            "\n" +
            "v1.0.2 (2026-06-04)\n" +
            "- 统计系统 KamiStats 上线\n" +
            "- UI 细节优化与统一\n" +
            "- 横竖屏切换支持\n" +
            "\n" +
            "v1.0.1 (2026-06-03)\n" +
            "- 基于 YukiHub 改造\n" +
            "- 搜索功能优化\n" +
            "\n" +
            "v1.0.0 (2026-06-02)\n" +
            "- 搜索 VNDB 跟游戏资源";
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("更新日志");
builder.setMessage(changelog);
    AlertDialog dialog = builder.create();
    dialog.show();
    styleAlertDialogDark(dialog);
}

private void showFriendsChatPlaceholder() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("好友 / 聊天")
                .setMessage("好友与聊天功能即将上线，敬请期待。")
                .setPositiveButton("知道了", null)
                .show();
        styleAlertDialogDark(dialog);
    }

    private void showOnlineSearchDialog() {
        if (isSearchDialogShowing) return;
        isSearchDialogShowing = true;
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        root.setPadding(pad, dp(12), pad, dp(4));

        EditText searchInput = new EditText(this);
        searchInput.setHint("输入Galgame名称...");
        searchInput.setTextColor(getColorCompat(R.color.yh_text));
        searchInput.setHintTextColor(getColorCompat(R.color.yh_text_muted));
        searchInput.setBackgroundResource(R.drawable.bg_input);
        searchInput.setPadding(dp(10), dp(8), dp(10), dp(8));
        searchInput.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH);
        if (pendingSearchQuery != null && !pendingSearchQuery.isEmpty()) {
            searchInput.setText(pendingSearchQuery);
            searchInput.setSelection(pendingSearchQuery.length());
            pendingSearchQuery = null;
        }
        root.addView(searchInput, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(44)));

        LinearLayout modeRow = new LinearLayout(this);
        modeRow.setOrientation(LinearLayout.HORIZONTAL);
        modeRow.setPadding(0, dp(8), 0, 0);
        Button btnGameMode = new Button(this);
        btnGameMode.setText("搜游戏");
        btnGameMode.setTextSize(12);
        btnGameMode.setBackgroundResource(R.drawable.bg_button_primary);
        Button btnPatchMode = new Button(this);
        btnPatchMode.setText("搜补丁");
        btnPatchMode.setTextSize(12);
        btnPatchMode.setBackgroundResource(R.drawable.bg_chip);
        btnPatchMode.setTextColor(getColorCompat(R.color.yh_text_muted));
        final boolean[] isPatch = {false};

        btnGameMode.setOnClickListener(v -> {
            isPatch[0] = false;
            btnGameMode.setBackgroundResource(R.drawable.bg_button_primary);
            btnGameMode.setTextColor(0xFF071221);
            btnPatchMode.setBackgroundResource(R.drawable.bg_chip);
            btnPatchMode.setTextColor(getColorCompat(R.color.yh_text_muted));
        });
        btnPatchMode.setOnClickListener(v -> {
            isPatch[0] = true;
            btnPatchMode.setBackgroundResource(R.drawable.bg_button_primary);
            btnPatchMode.setTextColor(0xFF071221);
            btnGameMode.setBackgroundResource(R.drawable.bg_chip);
            btnGameMode.setTextColor(getColorCompat(R.color.yh_text_muted));
        });

        modeRow.addView(btnGameMode, new LinearLayout.LayoutParams(0, dp(34), 1));
        LinearLayout.LayoutParams patchLp = new LinearLayout.LayoutParams(0, dp(34), 1);
        patchLp.setMargins(dp(8), 0, 0, 0);
        modeRow.addView(btnPatchMode, patchLp);
        root.addView(modeRow);

        TextView statusText = new TextView(this);
        statusText.setText("");
        statusText.setTextColor(getColorCompat(R.color.yh_text_muted));
        statusText.setTextSize(12);
        statusText.setPadding(0, dp(6), 0, 0);
        root.addView(statusText);

        ScrollView scrollView = new ScrollView(this);
        LinearLayout resultsContainer = new LinearLayout(this);
        resultsContainer.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(resultsContainer, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
        root.addView(scrollView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("在线搜索")
                .setView(root)
                .setPositiveButton("搜索", null)
                .setNegativeButton("关闭", (d, w) -> { isSearchDialogShowing = false; })
                .setOnDismissListener(d -> { isSearchDialogShowing = false; })
                .show();
        styleAlertDialogDark(dialog);

        final boolean[] isSearching = {false};

        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
                    || actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE
                    || actionId == android.view.inputmethod.EditorInfo.IME_ACTION_UNSPECIFIED) {
                if (!isSearching[0]) {
                    isSearching[0] = true;
                    executeOnlineSearch(searchInput, statusText, resultsContainer, dialog, isPatch, isSearching);
                }
                return true;
            }
            return false;
        });

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (!isSearching[0]) {
                isSearching[0] = true;
                executeOnlineSearch(searchInput, statusText, resultsContainer, dialog, isPatch, isSearching);
            }
        });
    }

    private void executeOnlineSearch(EditText searchInput, TextView statusText, LinearLayout resultsContainer, AlertDialog dialog, boolean[] isPatch, boolean[] isSearching) {
        String query = searchInput.getText().toString().replaceAll("[\\n\\r]", "").trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "请输入游戏名", Toast.LENGTH_SHORT).show();
            isSearching[0] = false;
            return;
        }
        statusText.setText("正在搜索...");
        resultsContainer.removeAllViews();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        SearchClient client = new SearchClient();
        AppExecutors.runOnIo(() -> {
            try {
                java.util.List<SearchClient.PlatformResult> results = client.searchGal(query, isPatch[0]);
                SearchClient.VndbInfo vndb = null;
                try { vndb = client.searchVndb(query); } catch (Exception ignored) {}

                final java.util.List<SearchClient.PlatformResult> finalResults = results;
                final SearchClient.VndbInfo finalVndb = vndb;
                runOnUiThread(() -> {
                    statusText.setText("找到 " + results.size() + " 个资源站");
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    isSearching[0] = false;

                    // VNDB信息
                    if (finalVndb != null) {
                        TextView vndbTitle = new TextView(MainActivity.this);
                        vndbTitle.setText("📖 " + finalVndb.title + (finalVndb.rating != null ? "  ⭐" + String.format("%.1f", finalVndb.rating) : ""));
                        vndbTitle.setTextColor(getColorCompat(R.color.yh_text));
                        vndbTitle.setTextSize(13);
                        vndbTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                        vndbTitle.setPadding(0, dp(4), 0, dp(2));
                        resultsContainer.addView(vndbTitle);
                        if (finalVndb.released != null) {
                            TextView releaseText = new TextView(MainActivity.this);
                            releaseText.setText("发售日: " + finalVndb.released);
                            releaseText.setTextColor(getColorCompat(R.color.yh_text_muted));
                            releaseText.setTextSize(10);
                            releaseText.setPadding(0, 0, 0, dp(4));
                            resultsContainer.addView(releaseText);
                        }
                        View line = new View(MainActivity.this);
                        line.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
                        line.setBackgroundColor(0x33FFFFFF);
                        line.setPadding(0, dp(4), 0, dp(4));
                        resultsContainer.addView(line);
                    }

                    if (finalResults.isEmpty()) {
                        TextView empty = new TextView(MainActivity.this);
                        empty.setText("没有找到资源");
                        empty.setTextColor(getColorCompat(R.color.yh_text_muted));
                        empty.setTextSize(12);
                        empty.setPadding(0, dp(8), 0, 0);
                        resultsContainer.addView(empty);
                        return;
                    }

                    java.util.Set<String> seenPlatforms = new java.util.HashSet<>();
                    for (SearchClient.PlatformResult pr : finalResults) {
                        if (!seenPlatforms.add(pr.platform)) continue;
                        LinearLayout platRow = new LinearLayout(MainActivity.this);
                        platRow.setOrientation(LinearLayout.HORIZONTAL);
                        platRow.setPadding(0, dp(4), 0, dp(4));
                        platRow.setBackgroundResource(R.drawable.bg_sidebar_item);
                        platRow.setClickable(true);
                        platRow.setFocusable(true);

                        String tagStr = "";
                        if (pr.tags != null) {
                            if (pr.tags.contains("NoReq")) tagStr = "🟢";
                            else if (pr.tags.contains("LoginPay")) tagStr = "🔒";
                            else tagStr = "📄";
                        }

                        TextView platName = new TextView(MainActivity.this);
                        platName.setText(tagStr + " " + pr.platform + " (" + pr.items.size() + ")");
                        platName.setTextColor(getColorCompat(R.color.yh_primary));
                        platName.setTextSize(12);
                        platName.setPadding(dp(4), dp(4), dp(4), dp(4));
                        platName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

                        Button viewBtn = new Button(MainActivity.this);
                        viewBtn.setText("查看");
                        viewBtn.setTextSize(10);
                        viewBtn.setBackgroundResource(R.drawable.bg_chip);
                        viewBtn.setPadding(dp(8), dp(2), dp(8), dp(2));

                        platRow.addView(platName);
                        platRow.addView(viewBtn, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(28)));

                        final List<SearchClient.Item> items = pr.items;
                        final String platTitle = pr.platform;
                        viewBtn.setOnClickListener(btn -> showOnlineResultItems(platTitle, items));

                        resultsContainer.addView(platRow);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    statusText.setText("搜索失败: " + e.getMessage());
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    isSearching[0] = false;
                });
            }
        });
    }

    private void showOnlineResultItems(String platform, java.util.List<SearchClient.Item> items) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        root.setPadding(pad, dp(12), pad, dp(4));

        TextView title = new TextView(this);
        title.setText(platform + " - " + items.size() + " 个链接");
        title.setTextColor(getColorCompat(R.color.yh_text));
        title.setTextSize(15);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title);

        ScrollView scroll = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);

        for (SearchClient.Item item : items) {
            Button itemBtn = new Button(this);
            itemBtn.setText(item.name);
            itemBtn.setTextSize(12);
            itemBtn.setTextColor(getColorCompat(R.color.yh_primary));
            itemBtn.setBackgroundResource(R.drawable.bg_chip);
            itemBtn.setPadding(dp(8), dp(6), dp(8), dp(6));
            itemBtn.setAllCaps(false);
            itemBtn.setOnClickListener(v -> {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(item.url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } catch (Exception e) {
                    Toast.makeText(this, "无法打开链接", Toast.LENGTH_SHORT).show();
                }
            });
            list.addView(itemBtn, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) itemBtn.getLayoutParams();
            lp.setMargins(0, dp(3), 0, 0);
        }

        scroll.addView(list);
        root.addView(scroll, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        new AlertDialog.Builder(this)
                .setTitle("选择链接")
                .setView(root)
                .setPositiveButton("关闭", null)
                .show();
    }

    private void shareGame(Game game) {
        StringBuilder text = new StringBuilder();
        text.append("🎮 ").append(emptyText(game.title, "未命名游戏")).append("\n");
        if (game.originalTitle != null && !game.originalTitle.isEmpty()) {
            text.append("📝 ").append(game.originalTitle).append("\n");
        }
        int rating = prefs.getInt("rating_" + game.id, 0);
        if (rating > 0) {
            text.append("⭐ ").append(rating).append("/10\n");
        }
        if (game.totalPlayTime > 0) {
            text.append("⏱ ").append(TimeFormatUtil.playTime(game.totalPlayTime)).append("\n");
        }
        if (game.description != null && !game.description.isEmpty()) {
            String desc = game.description.length() > 100 ? game.description.substring(0, 100) + "…" : game.description;
            text.append("📖 ").append(desc).append("\n");
        }
        text.append("\n—— 来自 KamiGAL");

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text.toString());
        intent.putExtra(Intent.EXTRA_SUBJECT, "分享游戏 - " + game.title);
        try {
            startActivity(Intent.createChooser(intent, "分享游戏"));
        } catch (Exception e) {
            Toast.makeText(this, "无法分享", Toast.LENGTH_SHORT).show();
        }
    }


    private void showRatingDialog(Game game) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        root.setPadding(pad, dp(12), pad, dp(4));

        int currentRating = prefs.getInt("rating_" + game.id, 0);

        TextView hint = new TextView(this);
        String curStr = currentRating > 0 ? "当前评分：" + currentRating + "/10" : "尚未评分";
        hint.setText("给「" + game.title + "」打分\n" + curStr);
        hint.setTextColor(getColorCompat(R.color.yh_text));
        hint.setTextSize(14);
        hint.setPadding(0, 0, 0, dp(8));
        root.addView(hint);

        LinearLayout starRow = new LinearLayout(this);
        starRow.setOrientation(LinearLayout.HORIZONTAL);
        starRow.setGravity(android.view.Gravity.CENTER);
        final int[] selectedRating = {currentRating};

        for (int i = 1; i <= 10; i++) {
            final int score = i;
            TextView star = new TextView(this);
            star.setText(i <= currentRating ? "★" : "☆");
            star.setTextSize(28);
            star.setTextColor(i <= currentRating ? 0xFFFFB74D : 0xFF4A5568);
            star.setPadding(dp(4), 0, dp(4), 0);
            star.setOnClickListener(v -> {
                selectedRating[0] = score;
                // 更新所有星星显示
                for (int j = 0; j < starRow.getChildCount(); j++) {
                    TextView sv = (TextView) starRow.getChildAt(j);
                    int idx = j + 1;
                    sv.setText(idx <= score ? "★" : "☆");
                    sv.setTextColor(idx <= score ? 0xFFFFB74D : 0xFF4A5568);
                }
            });
            starRow.addView(star);
        }
        root.addView(starRow);

        TextView numLabel = new TextView(this);
        numLabel.setGravity(android.view.Gravity.CENTER);
        numLabel.setTextColor(getColorCompat(R.color.yh_text_muted));
        numLabel.setTextSize(12);
        numLabel.setPadding(0, dp(4), 0, 0);
        root.addView(numLabel);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("评分")
                .setView(root)
                .setPositiveButton("确定", (d, w) -> {
                    int rating = selectedRating[0];
                    prefs.edit().putInt("rating_" + game.id, rating).apply();
                    updateSideDetail(game);
                    Toast.makeText(this, "已评分：" + rating + "/10", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("清除评分", (d, w) -> {
                    prefs.edit().remove("rating_" + game.id).apply();
                    updateSideDetail(game);
                    Toast.makeText(this, "已清除评分", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("取消", null)
                .show();
        styleAlertDialogDark(dialog);
    }

    private String accountStatusLabelForDialog() {
        return "本地账户";
    }

    private int accountStatusBackground() {
        return R.drawable.bg_account_status_local;
    }

    private int accountStatusTextColor() {
        return 0xFFDCEBFF;
    }

    private void showAuthPlaceholderDialog() {
        showAccountSettingsDialog();
    }

private void showAuthDialog() {
    if (isLoggedIn()) {
        showAccountSettingsDialog();
        return;
    }
            showAccountSettingsDialog();
}

    private void showAccountSettingsDialog() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        root.setPadding(pad, dp(12), pad, dp(4));
        TextView info = new TextView(this);
        String name = displayProfileName();
        info.setText("当前账户：" + name + "\n（本地账户 · 无需登录）");
        info.setTextColor(getColorCompat(R.color.yh_text_muted));
        info.setTextSize(13);
        info.setLineSpacing(dp(2), 1.0f);
        root.addView(info);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("账号设置")
                .setView(root)
                .setPositiveButton("关闭", null)
                .show();
        styleAlertDialogDark(dialog);
    }

    private void confirmLogout(AlertDialog parent) {
        if (parent != null) parent.dismiss();
    }

private void showWebDavSettingsDialog() {
    com.sakurajima.galsearch.sync.WebDavSettingsDialog dialog = com.sakurajima.galsearch.sync.WebDavSettingsDialog.newInstance();
    dialog.show(getSupportFragmentManager(), "webdav_settings");
}

private void maybeAutoWebDavSync() {
    if (webDavAutoSyncRunning) return;
    com.sakurajima.galsearch.sync.SyncManager sm = new com.sakurajima.galsearch.sync.SyncManager(this);
    if (!sm.isConfigured() || !sm.isAutoSyncEnabled()) return;
    long last = sm.getLastSyncTime();
    if (last > 0 && System.currentTimeMillis() - last < 10L * 60L * 1000L) return;
    webDavAutoSyncRunning = true;
    sm.sync(new com.sakurajima.galsearch.sync.SyncManager.SyncListener() {
        @Override public void onSyncStart() { }
        @Override public void onProgress(String item, boolean changed) { }
        @Override public int onConflict(com.sakurajima.galsearch.sync.SyncManager.Conflict conflict) { return com.sakurajima.galsearch.sync.SyncManager.RESOLVE_MERGE; }
        @Override public void onSyncComplete(com.sakurajima.galsearch.sync.SyncManager.SyncResult result) {
            runOnUiThread(() -> {
                webDavAutoSyncRunning = false;
                if (result != null && result.hasChanges()) {
                    loadGames();
                    updateProfilePanel();
                    Toast.makeText(MainActivity.this, "WebDAV 自动同步完成", Toast.LENGTH_SHORT).show();
                }
            });
        }
        @Override public void onError(String error) {
            runOnUiThread(() -> webDavAutoSyncRunning = false);
        }
    });
}




private JSONObject getJson(String urlStr) throws Exception {
    HttpURLConnection c = (HttpURLConnection) new URL(urlStr).openConnection();
    c.setRequestMethod("GET");
    c.setInstanceFollowRedirects(true);
    c.setConnectTimeout(15000);
    c.setReadTimeout(20000);
    c.setRequestProperty("Accept", "application/json,text/plain,*/*");
    c.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
    c.setRequestProperty("User-Agent", BROWSER_UA);
    c.setRequestProperty("Referer", "https://sakurajima.app/");
    int code = c.getResponseCode();
    String text = readSmallText(code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream());
    if (text != null && text.trim().startsWith("<")) {
        throw new RuntimeException("服务器返回了HTML页面，可能是免费主机防护页/缓存页，请稍后重试");
    }
    if (code < 200 || code >= 300) throw new RuntimeException("HTTP " + code + ": " + text);
    return text == null || text.trim().isEmpty() ? new JSONObject() : new JSONObject(text);
}

private JSONObject postJson(String url, JSONObject body, String bearerToken) throws Exception {
    HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
    c.setRequestMethod("POST");
    c.setInstanceFollowRedirects(true);
    c.setConnectTimeout(15000);
    c.setReadTimeout(20000);
    c.setDoOutput(true);
    c.setRequestProperty("Accept", "application/json");
    c.setRequestProperty("Content-Type", "application/json; charset=utf-8");
    c.setRequestProperty("User-Agent", "KamiGAL/1.0 (Android)");
    if (bearerToken != null && !bearerToken.trim().isEmpty()) c.setRequestProperty("Authorization", "Bearer " + bearerToken.trim());
    byte[] data = body == null ? new byte[0] : body.toString().getBytes(StandardCharsets.UTF_8);
    c.setFixedLengthStreamingMode(data.length);
    try (OutputStream os = new BufferedOutputStream(c.getOutputStream())) { os.write(data); }
    int code = c.getResponseCode();
    String text = readSmallText(code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream());
    if (code < 200 || code >= 300) throw new RuntimeException("HTTP " + code + ": " + text);
    return text == null || text.trim().isEmpty() ? new JSONObject() : new JSONObject(text);
}

/**
 * 用 Refresh Token 获取新的 Access Token
 * @return true 刷新成功，false 刷新失败需要重新登录
 */

/**
 * 带自动刷新的 API 请求
 * 如果请求返回 401，自动尝试刷新 Token 后重试
 */



private TextView profileStatCard(String label, String value) {
    TextView v = new TextView(this);
    v.setText(label + "\n" + value);
    v.setGravity(android.view.Gravity.CENTER);
    v.setTextColor(getColorCompat(R.color.yh_text));
    v.setTextSize(11);
    v.setTypeface(null, android.graphics.Typeface.BOLD);
    v.setLineSpacing(dp(1), 1.0f);
    v.setBackgroundResource(R.drawable.bg_input);
    return v;
}

private TextView profileLabel(String text) {
    TextView v = new TextView(this);
    v.setText(text);
    v.setTextColor(getColorCompat(R.color.yh_text));
    v.setTextSize(13);
    v.setTypeface(null, android.graphics.Typeface.BOLD);
    v.setPadding(0, 0, 0, dp(4));
    return v;
}

private EditText profileEdit(String value, String hint) {
    EditText v = new EditText(this);
    v.setText(value == null ? "" : value);
    v.setHint(hint);
    v.setTextColor(getColorCompat(R.color.yh_text));
    v.setHintTextColor(getColorCompat(R.color.yh_text_muted));
    v.setTextSize(13);
    v.setSingleLine(true);
    v.setBackgroundResource(R.drawable.bg_input);
    v.setPadding(dp(10), 0, dp(10), 0);
    return v;
}

public void openLocalBackupExportFromSyncCenter() {
    backupCreateLauncher.launch("sakurajima_backup_" + System.currentTimeMillis() + ".json");
}

public void openLocalBackupImportFromSyncCenter() {
    confirmImportLocalBackup();
}

private void confirmImportLocalBackup() {
    AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("本地导入")
            .setMessage("将从备份 JSON 导入个人资料、游戏库、游玩记录和元数据。\n\n导入策略：\n- 游戏按 rootUri 去重合并\n- 游玩记录按 session_uuid 去重\n- 图片只恢复 URI/URL，不复制图片文件\n\n是否继续？")
            .setPositiveButton("选择文件", (d, w) -> backupOpenLauncher.launch(new String[]{"application/json", "text/*", "*/*"}))
            .setNegativeButton("取消", null)
            .show();
    styleAlertDialogDark(dialog);
}

private void exportLocalBackup(Uri uri) {
    try {
        JSONObject root = new com.sakurajima.galsearch.sync.SyncManager(this).exportSnapshotForLocalBackup();
        root.put("created_at", System.currentTimeMillis());
        root.put("backup_type", "local_full");
        root.put("note", "Local backup uses the same schema as WebDAV sync, but keeps full play session history.");
        byte[] bytes = root.toString(2).getBytes(StandardCharsets.UTF_8);
        try (OutputStream out = getContentResolver().openOutputStream(uri)) {
            if (out == null) throw new Exception("openOutputStream failed");
            out.write(bytes);
            out.flush();
        }
        Toast.makeText(this, "备份完成：" + (bytes.length / 1024) + "KB", Toast.LENGTH_LONG).show();
    } catch (Throwable t) {
        Toast.makeText(this, "备份失败：" + t.getMessage(), Toast.LENGTH_LONG).show();
        Log.e("KamiGAL", "export backup failed", t);
    }
}

private void importLocalBackup(Uri uri) {
    try {
        String text = readTextFromUri(uri);
        JSONObject root = new JSONObject(text);
        if (!"KamiGAL".equals(root.optString("app", ""))) {
            Toast.makeText(this, "不是有效备份", Toast.LENGTH_LONG).show();
            return;
        }
        new com.sakurajima.galsearch.sync.SyncManager(this).importSnapshotFromLocalBackup(root);
        loadGames();
        applyCustomBackground();
        updateProfilePanel();
        int gameCount = root.optJSONArray("games") == null ? 0 : root.optJSONArray("games").length();
        int sessionCount = root.optJSONArray("play_sessions") == null ? 0 : root.optJSONArray("play_sessions").length();
        int metaCount = root.optJSONArray("metadata_cache") == null ? 0 : root.optJSONArray("metadata_cache").length();
        Toast.makeText(this, "导入完成：游戏 " + gameCount + "，记录 " + sessionCount + "，元数据 " + metaCount, Toast.LENGTH_LONG).show();
    } catch (Throwable t) {
        Toast.makeText(this, "导入失败：" + t.getMessage(), Toast.LENGTH_LONG).show();
        Log.e("KamiGAL", "import backup failed", t);
    }
}

private String readTextFromUri(Uri uri) throws Exception {
    try (InputStream in = getContentResolver().openInputStream(uri); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
        if (in == null) throw new Exception("openInputStream failed");
        byte[] buf = new byte[8192];
        int len;
        while ((len = in.read(buf)) != -1) bos.write(buf, 0, len);
        return bos.toString("UTF-8");
    }
}

private void buildRecentActivityViews(LinearLayout container) {
    if (container == null) return;
    List<PlayActivity> activities = repository == null ? new ArrayList<>() : repository.getRecentPlayActivities(8);
    if (activities.isEmpty()) {
        TextView empty = new TextView(this);
        empty.setText("暂无动态。开始游玩后，这里会记录你的足迹。");
        empty.setTextColor(getColorCompat(R.color.yh_text_muted));
        empty.setTextSize(12);
        empty.setBackgroundResource(R.drawable.bg_input);
        empty.setPadding(dp(10), dp(8), dp(10), dp(8));
        container.addView(empty, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        return;
    }
    for (PlayActivity a : activities) {
        TextView item = new TextView(this);
        item.setText("玩了《" + a.gameTitle + "》 " + TimeFormatUtil.playTime(a.duration) + "\n" + TimeFormatUtil.date(a.endTime) + " · " + launchTypeLabel(a.launchType));
        item.setTextColor(getColorCompat(R.color.yh_text));
        item.setTextSize(12);
        item.setLineSpacing(dp(1), 1.0f);
        item.setBackgroundResource(R.drawable.bg_input);
        item.setPadding(dp(10), dp(8), dp(10), dp(8));
        item.setOnClickListener(v -> showPlayActivityDetail(a));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(6));
        container.addView(item, lp);
    }
}

private void showPlayActivityDetail(PlayActivity a) {
    if (a == null) return;
    String text = "游戏：" + a.gameTitle + "\n"
            + "开始：" + TimeFormatUtil.date(a.startTime) + "\n"
            + "结束：" + TimeFormatUtil.date(a.endTime) + "\n"
            + "时长：" + TimeFormatUtil.playTime(a.duration) + "\n"
            + "启动类型：" + launchTypeLabel(a.launchType) + "\n"
            + "会话ID：" + emptyText(a.sessionUuid, String.valueOf(a.sessionId));
    AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("动态详情")
            .setMessage(text)
            .setPositiveButton("关闭", null)
            .show();
    styleAlertDialogDark(dialog);
}

private String launchTypeLabel(String launchType) {
    String t = launchType == null ? "" : launchType;
    if (t.startsWith("internal.krkr")) return "内置 KRKR";
    if (t.startsWith("internal.ons")) return "内置 ONS";
    if (t.startsWith("internal.tyrano")) return "内置 Tyrano";
    if (t.startsWith("internal.artemis")) return "内置 Artemis";
    return "外部模拟器";
}

private void updateProfilePanel() {
    String name = displayProfileName();
    long total = totalPlayTime();
    if (tvProfileName != null) tvProfileName.setText(name);
    if (tvProfileInitial != null) tvProfileInitial.setText(initials(name));
    updateProfileStatusDot();
    if (tvStats != null) tvStats.setText(allGames.size() + " Games\n" + TimeFormatUtil.playTime(total) + " Played");
    loadProfileAvatarInto(ivProfileAvatar, tvProfileInitial);
}

private void updateProfileStatusDot() {
    if (profileStatusDot == null) return;
    String status = accountStatus();
    if ("local".equals(status)) {
        profileStatusDot.setBackgroundResource(R.drawable.bg_profile_dot_local);
    } else {
        profileStatusDot.setBackgroundResource(R.drawable.bg_profile_dot_local);
    }
}

    private boolean isLoggedIn() {
        return false;
    }

    private String displayProfileName() {
        return profileName();
    }

    private String accountStatus() {
        return "local";
    }

    private void loadProfileAvatarInto(ImageView avatar, TextView initial) {
        if (avatar == null) return;
        String uri = prefs == null ? "" : prefs.getString(KEY_PROFILE_AVATAR, "");
        if (uri == null || uri.isEmpty()) {
            avatar.setVisibility(View.GONE);
            if (initial != null) {
                String name = profileName();
                if (name != null && !name.isEmpty()) {
                    initial.setText(name.substring(0, 1).toUpperCase());
                    initial.setVisibility(View.VISIBLE);
                }
            }
            return;
        }
        try {
            avatar.setImageURI(Uri.parse(uri));
            avatar.setVisibility(View.VISIBLE);
            if (initial != null) initial.setVisibility(View.GONE);
        } catch (Throwable t) {
        }
    }

private long todayTotalPlayTime() {
    if (repository == null) return 0L;
    Calendar start = Calendar.getInstance();
    start.set(Calendar.HOUR_OF_DAY, 0);
    start.set(Calendar.MINUTE, 0);
    start.set(Calendar.SECOND, 0);
    start.set(Calendar.MILLISECOND, 0);
    Calendar end = (Calendar) start.clone();
    end.add(Calendar.DAY_OF_MONTH, 1);
    long total = 0L;
    Map<String, Long> today = repository.getPlayDurationsBetween(start.getTimeInMillis(), end.getTimeInMillis());
    for (Long v : today.values()) total += v == null ? 0L : v;
    return total;
}

private String buildTodayActivityText() {
    if (repository == null) return "今天还没有游玩记录。";
    Calendar start = Calendar.getInstance();
    start.set(Calendar.HOUR_OF_DAY, 0);
    start.set(Calendar.MINUTE, 0);
    start.set(Calendar.SECOND, 0);
    start.set(Calendar.MILLISECOND, 0);
    Calendar end = (Calendar) start.clone();
    end.add(Calendar.DAY_OF_MONTH, 1);
    Map<String, Long> today = repository.getPlayDurationsBetween(start.getTimeInMillis(), end.getTimeInMillis());
    if (today.isEmpty()) return "今天还没有游玩记录。\n启动游戏后，返回应用就会生成动态。";
    StringBuilder sb = new StringBuilder();
    int count = 0;
    long total = 0;
    for (Map.Entry<String, Long> e : today.entrySet()) {
        if (count >= 5) break;
        long duration = e.getValue() == null ? 0L : e.getValue();
        total += duration;
        if (count > 0) sb.append('\n');
        sb.append("今天玩了《").append(e.getKey()).append("》 ").append(TimeFormatUtil.playTime(duration));
        count++;
    }
    if (today.size() > count) sb.append('\n').append("还有 ").append(today.size() - count).append(" 个游戏的记录...");
    sb.append("\n今日合计：").append(TimeFormatUtil.playTime(total));
    return sb.toString();
}

private void setupDeveloperToggle() {
    TextView title = findViewById(R.id.filterDeveloper);
    View list = findViewById(R.id.developerList);
    if (title == null || list == null) return;
    title.setOnClickListener(v -> {
        boolean show = list.getVisibility() != View.VISIBLE;
        list.setVisibility(show ? View.VISIBLE : View.GONE);
        title.setText(show ? "▾ 开发商" : "▸ 开发商");
    });
}

private void rebuildDeveloperFilters() {
    LinearLayout list = findViewById(R.id.developerList);
    TextView title = findViewById(R.id.filterDeveloper);
    if (list == null || title == null) return;
    list.removeAllViews();
    java.util.Map<String, Integer> counts = new java.util.TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    for (Game g : allGames) {
        String dev = developerOf(g);
        if (dev == null || dev.trim().isEmpty() || "-".equals(dev.trim())) continue;
        String[] parts = dev.split("/|、|,|，");
        for (String p : parts) {
            String name = p == null ? "" : p.trim();
            if (name.isEmpty()) continue;
            counts.put(name, counts.containsKey(name) ? counts.get(name) + 1 : 1);
        }
    }
    if (counts.isEmpty()) {
        TextView empty = sidebarDeveloperItem("暂无开发商", "");
        empty.setAlpha(0.45f);
        empty.setEnabled(false);
        list.addView(empty);
        title.setAlpha(0.55f);
        return;
    }
    title.setAlpha(1f);
    TextView all = sidebarDeveloperItem("全部开发商", "");
    list.addView(all);
    for (java.util.Map.Entry<String, Integer> e : counts.entrySet()) {
        list.addView(sidebarDeveloperItem(e.getKey() + " (" + e.getValue() + ")", e.getKey()));
    }
    updateDeveloperFilterSelection();
}

private TextView sidebarDeveloperItem(String text, String developer) {
    TextView v = new TextView(this);
    v.setText(text);
    v.setTag(developer == null ? "" : developer);
    v.setGravity(android.view.Gravity.CENTER_VERTICAL);
    v.setMinHeight(dp(24));
    v.setPadding(dp(14), 0, dp(4), 0);
    v.setTextSize(8);
    v.setSingleLine(true);
    v.setEllipsize(android.text.TextUtils.TruncateAt.END);
    v.setBackgroundResource(R.drawable.bg_sidebar_item);
    v.setTextColor(getColorCompat(R.color.yh_text_muted));
    v.setOnClickListener(view -> {
        developerFilter = developer == null ? "" : developer;
        updateDeveloperFilterSelection();
        applyFilter();
    });
    return v;
}

private void updateDeveloperFilterSelection() {
    LinearLayout list = findViewById(R.id.developerList);
    if (list == null) return;
    for (int i = 0; i < list.getChildCount(); i++) {
        View child = list.getChildAt(i);
        if (!(child instanceof TextView)) continue;
        String dev = child.getTag() instanceof String ? (String) child.getTag() : "";
        boolean selected = (developerFilter == null ? "" : developerFilter).equals(dev);
        child.setSelected(selected);
        child.setAlpha(selected ? 1f : 0.72f);
        ((TextView) child).setTextColor(selected ? getColorCompat(R.color.yh_text) : getColorCompat(R.color.yh_text_muted));
        ((TextView) child).setTypeface(null, selected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
    }
}

private String developerOf(Game game) {
    if (game == null || metadataRepository == null) return "";
    VnMetadata meta = metadataRepository.getVndb(game.id);
    if (meta == null) meta = metadataRepository.getVndb(game.id);
    return meta == null ? "" : emptyText(meta.developer, "");
}

private void bindFilter(int id, String value) {
        View item = findViewById(id);
        item.setOnClickListener(v -> {
            filter = value;
            developerFilter = "";
            updateFilterSelection();
            applyFilter();
        });
    }

    private void updateFilterSelection() {
        updateFilterItem(R.id.filterAll, "ALL");
        updateFilterItem(R.id.filterRecent, "RECENT");
        updateFilterItem(R.id.filterPlaying, "PLAYING");
        updateFilterItem(R.id.filterCompleted, "COMPLETED");
        updateFilterItem(R.id.filterUnplayed, "UNPLAYED");
        updateDeveloperFilterSelection();
    }

    private void updateFilterItem(int id, String value) {
        View view = findViewById(id);
        boolean selected = value.equals(filter);
        view.setSelected(selected);
        view.setAlpha(selected ? 1f : 0.72f);
        if (view instanceof TextView) {
            ((TextView) view).setTextColor(selected ? getColorCompat(R.color.yh_text) : getColorCompat(R.color.yh_text_muted));
            ((TextView) view).setTypeface(null, selected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        }
    }
    private void loadGames() {
allGames.clear();
allGames.addAll(repository.getAll());
rebuildDeveloperFilters();
applyFilter();
runCoverMaintenanceOnceIfNeeded();
}

private void runCoverMaintenanceOnceIfNeeded() {
if (coverMaintenanceDone) return;
coverMaintenanceDone = true;
repairMissingMetadataCoversIfNeeded();
scanMissingCoversIfNeeded();
}

    private void applyFilter() {
        List<Game> shown = new ArrayList<>();
        String q = query == null ? "" : query.toLowerCase(Locale.ROOT).trim();
        long total = 0;
        for (Game g : allGames) {
            total += g.totalPlayTime;
            if (!q.isEmpty() && (g.title == null || !g.title.toLowerCase(Locale.ROOT).contains(q))) continue;
            if ("RECENT".equals(filter) && g.lastPlayedAt <= 0) continue;
            if ("PLAYING".equals(filter) && !"playing".equals(normalizePlayStatus(g.playStatus))) continue;
            if ("COMPLETED".equals(filter) && !"completed".equals(normalizePlayStatus(g.playStatus))) continue;
            if ("UNPLAYED".equals(filter) && !"unplayed".equals(normalizePlayStatus(g.playStatus))) continue;
            if ("KIRIKIRI".equals(filter) && g.engine != EngineType.KIRIKIRI) continue;
            if ("ONS".equals(filter) && g.engine != EngineType.ONS) continue;
            if ("TYRANO".equals(filter) && g.engine != EngineType.TYRANO) continue;
            if ("ARTEMIS".equals(filter) && g.engine != EngineType.ARTEMIS) continue;
            if ("WINLATOR".equals(filter) && g.engine != EngineType.WINLATOR) continue;
            if ("GAMEHUB".equals(filter) && g.engine != EngineType.GAMEHUB) continue;
            if ("UNKNOWN".equals(filter) && g.engine != EngineType.UNKNOWN) continue;
            if (developerFilter != null && !developerFilter.isEmpty()) {
                String dev = developerOf(g);
                if (dev == null || !dev.toLowerCase(Locale.ROOT).contains(developerFilter.toLowerCase(Locale.ROOT))) continue;
            }
            shown.add(g);
        }
        sortGames(shown);
        adapter.submit(shown);
        tvEmpty.setVisibility(shown.isEmpty() ? View.VISIBLE : View.GONE);
        tvStats.setText(allGames.size() + " Games\n" + TimeFormatUtil.playTime(total));
        updateProfilePanel();
        if (shown.isEmpty()) {
            updateSideDetail(null);
        } else if (selectedGame == null || !containsGameId(shown, selectedGame.id)) {
            updateSideDetail(shown.get(0));
        }
    }

    private void sortGames(List<Game> list) {
        if (list == null || list.size() <= 1) return;
        String mode = prefs == null ? SORT_MODE_RECENT : prefs.getString(KEY_SORT_MODE, SORT_MODE_RECENT);
        java.util.Comparator<Game> cmp;
        if (SORT_MODE_NAME.equals(mode)) {
            final Collator collator = Collator.getInstance(Locale.CHINA);
            collator.setStrength(Collator.PRIMARY);
            cmp = (a, b) -> {
                if (a == null && b == null) return 0;
                if (a == null) return 1;
                if (b == null) return -1;
                boolean af = a.favorite;
                boolean bf = b.favorite;
                if (af != bf) return af ? -1 : 1;
                String at = a.title == null ? "" : a.title;
                String bt = b.title == null ? "" : b.title;
                int r = collator.compare(at, bt);
                if (r != 0) return r;
                return Long.compare(b.createdAt, a.createdAt);
            };
        } else if (SORT_MODE_NEWEST.equals(mode)) {
            cmp = (a, b) -> {
                if (a == null && b == null) return 0;
                if (a == null) return 1;
                if (b == null) return -1;
                boolean af = a.favorite;
                boolean bf = b.favorite;
                if (af != bf) return af ? -1 : 1;
                int r = Long.compare(b.createdAt, a.createdAt);
                if (r != 0) return r;
                return Long.compare(b.lastPlayedAt, a.lastPlayedAt);
            };
        } else {
            cmp = (a, b) -> {
                if (a == null && b == null) return 0;
                if (a == null) return 1;
                if (b == null) return -1;
                boolean af = a.favorite;
                boolean bf = b.favorite;
                if (af != bf) return af ? -1 : 1;
                int r = Long.compare(b.lastPlayedAt, a.lastPlayedAt);
                if (r != 0) return r;
                r = Long.compare(b.createdAt, a.createdAt);
                if (r != 0) return r;
                String at = a.title == null ? "" : a.title;
                String bt = b.title == null ? "" : b.title;
                return at.compareToIgnoreCase(bt);
            };
        }
        list.sort(cmp);
    }

    private boolean containsGameId(List<Game> games, long id) {
    if (games == null) return false;
    for (Game g : games) if (g != null && g.id == id) return true;
    return false;
}

private void loadRemoteImage(String url, ImageView target) {
    loadRemoteImage(url, target, "img");
}

private void loadRemoteImage(String url, ImageView target, String prefix) {
    if (target == null) return;
    if (url == null || url.trim().isEmpty()) { target.setImageDrawable(null); return; }
    final String imageUrl = url.trim();
    AppExecutors.runOnIo(() -> {
        try {
            File cacheDir = prefix != null && prefix.startsWith("cover_") ? persistentRemoteCoverDir() : new File(getCacheDir(), "vndb_images");
            if (!cacheDir.exists()) cacheDir.mkdirs();
            File cacheFile = new File(cacheDir, safeCacheName(prefix + "_" + imageUrl));
            Bitmap bitmap = null;
            if (cacheFile.exists() && cacheFile.length() > 0) {
                bitmap = BitmapFactory.decodeFile(cacheFile.getAbsolutePath());
                if (bitmap == null) cacheFile.delete();
            }
            if (bitmap == null) {
                boolean ok = downloadImageAllowVndbWarningPage(imageUrl, cacheFile, 0);
                if (!ok) return;
                bitmap = BitmapFactory.decodeFile(cacheFile.getAbsolutePath());
                if (bitmap == null) { cacheFile.delete(); return; }
            }
            Bitmap finalBitmap = bitmap;
            runOnUiThread(() -> {
                if (finalBitmap == null || target.getWindowToken() == null) return;
                target.setImageBitmap(finalBitmap);
                Object tag = target.getTag();
                if (tag instanceof Game && prefix != null && prefix.startsWith("cover_") && cacheFile.exists()) {
                    Game taggedGame = (Game) tag;
                    String local = Uri.fromFile(cacheFile).toString();
                    if (taggedGame.coverUri == null || taggedGame.coverUri.isEmpty() || isMissingFileUri(taggedGame.coverUri)) {
                        taggedGame.coverUri = local;
                        taggedGame.coverPersistUri = local;
                        taggedGame.coverSourceType = 1;
                        try { repository.update(taggedGame); } catch (Throwable ignored) { }
                        if (adapter != null) adapter.notifyDataSetChanged();
                    }
                }
            });
        } catch (Throwable ignored) { }
    });
}

private String metadataSource() {
    return prefs == null ? SOURCE_VNDB : prefs.getString(KEY_METADATA_SOURCE, SOURCE_VNDB);
}

private String metadataSourceLabel() {
        String source = metadataSource();
        return "VNDB";
    }

    private void fetchSelectedMetadata(Game game) {
    if (game == null) return;

    // 1. 优先显示当前资料源已有缓存。
    VnMetadata cached = currentSourceCachedMetadata(game.id);
    if (cached != null) {
        applyVndbMetadata(cached, game);
        return;
    }

    // 2. 当前源没有缓存时，不要把已经匹配过的其它源资料一刀切成“未匹配”。
    //    例如切到 Bangumi 后，原来 VNDB 已匹配的游戏仍继续显示 VNDB 资料。
    VnMetadata fallback = otherSourceCachedMetadata(game.id);
    if (fallback != null) {
        applyVndbMetadata(fallback, game);
        return;
    }

    // 3. 只有这个游戏完全没有任何资料缓存时，才按当前资料源自动匹配。
    fetchVndbMetadata(game, false);
}

private void fetchSelectedMetadata(Game game, boolean forceRefresh) {
    if (forceRefresh) {
        fetchVndbMetadata(game, true);
    } else {
        fetchSelectedMetadata(game);
    }
}

private VnMetadata currentSourceCachedMetadata(long gameId) {
    if (metadataRepository == null || gameId <= 0) return null;
    return metadataRepository.getVndb(gameId);
}

private VnMetadata otherSourceCachedMetadata(long gameId) {
    if (metadataRepository == null || gameId <= 0) return null;
    return metadataRepository.getVndb(gameId);
}

    private void fetchVndbMetadata(Game game, boolean forceRefresh) {
        if (game == null || game.title == null || game.title.trim().isEmpty()) return;
        final long id = game.id;
        final String keyword = buildMetadataSearchKeyword(game.title);
        VnMetadata cached = metadataRepository == null || forceRefresh ? null : metadataRepository.getVndb(id);
        if (cached != null) {
            applyVndbMetadata(cached, game);
            return;
        }
        setSideDescription("正在从 VNDB 获取资料…");
        VndbClient.searchCandidatesAsync(keyword, 5, new VndbClient.CandidatesCallback() {
        @Override public void onSuccess(List<VnMetadata> data) {
            runOnUiThread(() -> {
                if (selectedGame == null || selectedGame.id != id) return;
                if (data == null || data.isEmpty()) {
                    applyVndbMetadata(null, game);
                } else if (data.size() == 1 || isConfidentMatch(game.title, data.get(0))) {
                    metadataRepository.saveVndb(id, data.get(0));
                    applyVndbMetadata(data.get(0), game);
                } else {
                    showVndbCandidateDialog(game, data);
                }
            });
        }
        @Override public void onError(Exception error) {
            runOnUiThread(() -> {
                if (selectedGame == null || selectedGame.id != id) return;
                setSideDescription(emptyText(game.description, "VNDB 暂未匹配到资料。"));
            });
        }
    });
    }

    private boolean downloadImageAllowVndbWarningPage(String imageUrl, File cacheFile, int depth) {
    if (imageUrl == null || imageUrl.trim().isEmpty() || cacheFile == null || depth > 2) return false;
    try {
        java.net.HttpURLConnection c = (java.net.HttpURLConnection) new java.net.URL(imageUrl).openConnection();
        c.setInstanceFollowRedirects(true);
        c.setConnectTimeout(9000);
        c.setReadTimeout(12000);
        c.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36 KamiGAL/1.0");
        // 不优先请求 AVIF，避免部分 Android BitmapFactory 解码失败。
        c.setRequestProperty("Accept", "image/webp,image/apng,image/*,*/*;q=0.8");
        c.setRequestProperty("Referer", "https://vndb.org/");
        c.setRequestProperty("Cookie", "vndb_img=1; vndb_samesite=1");
        String type = c.getContentType();
        if (type != null && type.toLowerCase(Locale.ROOT).startsWith("image/")) {
            try (InputStream is = c.getInputStream(); FileOutputStream fos = new FileOutputStream(cacheFile)) {
                byte[] buf = new byte[8192];
                int len;
                while ((len = is.read(buf)) != -1) fos.write(buf, 0, len);
            }
            return cacheFile.exists() && cacheFile.length() > 0;
        }
        String html = readSmallText(c.getInputStream());
        String next = extractImageUrlFromHtml(html, imageUrl);
        return next != null && !next.equals(imageUrl) && downloadImageAllowVndbWarningPage(next, cacheFile, depth + 1);
    } catch (Throwable ignored) {
        return false;
    }
}

private String readSmallText(InputStream is) throws Exception {
    if (is == null) return "";
    java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
    byte[] buf = new byte[4096];
    int total = 0, len;
    while ((len = is.read(buf)) != -1 && total < 256 * 1024) {
        bos.write(buf, 0, len);
        total += len;
    }
    return bos.toString("UTF-8");
}

// ========== 签名校验 ==========
private void checkSignature() {
    try {
        android.content.pm.PackageInfo info = getPackageManager().getPackageInfo(
            getPackageName(), android.content.pm.PackageManager.GET_SIGNATURES);
        if (info.signatures != null && info.signatures.length > 0) {
            byte[] cert = info.signatures[0].toByteArray();
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] digest = md.digest(cert);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            String sig = sb.toString();
            boolean isDebug = (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
            if (isDebug) {
                android.util.Log.i("KamiGAL", "Signature SHA1: " + sig);
            } else {
                String correctSig = "a816ec2e79d259e60ec5639c91d70c4f479fac24";
                if (!correctSig.equals(sig)) {
                    finish();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            }
        } else {
            if ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) == 0) { finish(); android.os.Process.killProcess(android.os.Process.myPid()); }
        }
    } catch (Exception e) {
        if ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) == 0) { finish(); android.os.Process.killProcess(android.os.Process.myPid()); }
    }
}

// ========== 版本更新检查 ==========
private void checkUpdate() { checkUpdate(false); }
private void checkUpdate(boolean silent) {
    AppExecutors.runOnIo(() -> {
        try {
            int myVc = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            String myVn = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            JSONObject jsonParam = new JSONObject();
            JSONObject jsonInner = new JSONObject();
            jsonInner.put("vc", myVc);
            jsonParam.put("json", jsonInner);
            String input = URLEncoder.encode(jsonParam.toString(), "UTF-8");
            String url = "https://kamigalapi-bxpldwza.manus.space/api/trpc/kamigal.update?input=" + input;
            JSONObject resp = getJson(url);
            if (resp == null) return;
            JSONObject result = resp.optJSONObject("result");
            if (result == null) return;
            JSONObject data = result.optJSONObject("data");
            if (data == null) return;
            JSONObject json = data.optJSONObject("json");
            if (json == null || json.optInt("code", -1) != 0) return;
            JSONObject updateData = json.optJSONObject("data");
            if (updateData == null) return;
            boolean needUpdate = updateData.optBoolean("needUpdate", false);
            boolean forceUpdate = updateData.optBoolean("forceUpdate", false);
            int latestVc = updateData.optInt("latestVersionCode", 0);
            String latestVn = updateData.optString("latestVersionName", "");
            String updateUrl = updateData.optString("updateUrl", "");
            String updateLog = updateData.optString("updateLog", "");
            // 版本码不同 或 版本名不同，都视为有新版本
            boolean versionCodeChanged = latestVc > myVc;
            boolean versionNameChanged = latestVn != null && myVn != null && !latestVn.equals(myVn);
            if (versionCodeChanged || versionNameChanged) {
                final boolean force = forceUpdate;
                final String urlFinal = updateUrl;
                final String logFinal = updateLog;
                final String vnFinal = latestVn;
                runOnUiThread(() -> showUpdateDialog(force, vnFinal, logFinal, urlFinal));
            } else {
                if (!silent) {
                    runOnUiThread(() -> Toast.makeText(this, "当前已是最新版本", Toast.LENGTH_SHORT).show());
                }
            }
        } catch (Exception ignored) { }
    });
}

// ========== 公告检查 ==========
private void checkNotice() {
    AppExecutors.runOnIo(() -> {
        try {
            JSONObject resp = getJson("https://kamigalapi-bxpldwza.manus.space/api/trpc/kamigal.notice");
            if (resp == null) return;
            JSONObject result = resp.optJSONObject("result");
            if (result == null) return;
            JSONObject data = result.optJSONObject("data");
            if (data == null) return;
            JSONObject json = data.optJSONObject("json");
            if (json == null || json.optInt("code", -1) != 0) return;
            JSONObject noticeData = json.optJSONObject("data");
            if (noticeData == null) return;

            boolean show = noticeData.optBoolean("show", false);
            if (!show) return;

            int version = noticeData.optInt("version", 0);
            String title = noticeData.optString("title", "");
            String content = noticeData.optString("content", "");
            String url = noticeData.optString("url", "");

            int readVersion = prefs.getInt("read_notice_version", 0);
            if (version > readVersion) {
                final String titleFinal = title;
                final String contentFinal = content;
                final String urlFinal = url;
                final int verFinal = version;
                runOnUiThread(() -> {
                    showNoticeDialog(titleFinal, contentFinal, urlFinal);
                    prefs.edit().putInt("read_notice_version", verFinal).apply();
                });
            }
        } catch (Exception ignored) { }
    });
}

// ========== 更新弹窗 ==========
private void showUpdateDialog(boolean force, String latestVn, String log, String url) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("发现新版本 v" + latestVn);
    builder.setMessage(log != null && !log.isEmpty() ? log : "有新版本可用，请更新后使用");
    builder.setPositiveButton("立即更新", null);
    if (!force) {
        builder.setNegativeButton("稍后再说", null);
    }
    AlertDialog dialog = builder.create();
    dialog.setCancelable(!force);
    dialog.setCanceledOnTouchOutside(!force);
    dialog.setOnShowListener(d -> {
        Button positiveBtn = ((AlertDialog) d).getButton(AlertDialog.BUTTON_POSITIVE);
        positiveBtn.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch (Exception e) {
                Toast.makeText(this, "无法打开下载链接", Toast.LENGTH_SHORT).show();
            }
        });
    });
    dialog.show();
    styleAlertDialogDark(dialog);
}

// ========== 公告弹窗 ==========
private void showNoticeDialog(String title, String content, String url) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(title);
    builder.setMessage(content);
    builder.setNegativeButton("知道了", null);
    AlertDialog dialog = builder.create();
    dialog.show();
    styleAlertDialogDark(dialog);
}

private boolean isTranslatedStateFor(long gameId) {
    if (prefs == null || gameId <= 0) return false;
    return prefs.getBoolean(KEY_SIDE_TRANSLATED_PREFIX + gameId, false);
}

private void setTranslatedStateFor(long gameId, boolean translated) {
    if (prefs == null || gameId <= 0) return;
    prefs.edit().putBoolean(KEY_SIDE_TRANSLATED_PREFIX + gameId, translated).apply();
}

private void updateTranslateButtonState() {
    if (sideTranslateToggle == null) return;
    boolean hasMeta = currentSideMetadata != null;
    boolean hasDescription = hasMeta && currentSideMetadata.description != null && !currentSideMetadata.description.trim().isEmpty();
    sideTranslateToggle.setVisibility(hasDescription ? View.VISIBLE : View.GONE);
    if (!hasDescription) return;
    sideTranslateToggle.setText(sideShowingTranslatedDescription ? "原文" : "译文");
    sideTranslateToggle.setEnabled(true);
    sideTranslateToggle.setAlpha(1f);
}

private void toggleOrTranslateDescription() {
    if (selectedGame == null || currentSideMetadata == null) return;
    VnMetadata meta = currentSideMetadata;
    if (sideShowingTranslatedDescription) {
        sideShowingTranslatedDescription = false;
        setTranslatedStateFor(selectedGame.id, false);
        setSideDescription(emptyText(meta.description, "暂无 VNDB 简介。"));
        updateTranslateButtonState();
        return;
    }
    if (meta.translatedDescription != null && !meta.translatedDescription.trim().isEmpty()) {
        sideShowingTranslatedDescription = true;
        setTranslatedStateFor(selectedGame.id, true);
        setSideDescription(meta.translatedDescription);
        updateTranslateButtonState();
        return;
    }
    final long gameId = selectedGame.id;
    sideTranslateToggle.setText("...");
    sideTranslateToggle.setEnabled(false);
    sideTranslateToggle.setAlpha(0.65f);
    AppExecutors.runOnIo(() -> {
        try {
            String translated = translateTextToChinese(meta.description);
            runOnUiThread(() -> {
                if (selectedGame == null || selectedGame.id != gameId || currentSideMetadata != meta) return;
                if (translated == null || translated.trim().isEmpty()) {
                    Toast.makeText(this, "简介翻译失败", Toast.LENGTH_SHORT).show();
                    updateTranslateButtonState();
                    return;
                }
                meta.translatedDescription = translated.trim();
                if (metadataRepository != null) metadataRepository.saveVndb(gameId, meta);
                sideShowingTranslatedDescription = true;
                setTranslatedStateFor(gameId, true);
                setSideDescription(meta.translatedDescription);
                updateTranslateButtonState();
            });
        } catch (Throwable t) {
            Log.w("KamiGAL", "translate description failed", t);
            runOnUiThread(() -> {
                if (selectedGame != null && selectedGame.id == gameId) {
                    Toast.makeText(this, "简介翻译失败", Toast.LENGTH_SHORT).show();
                    updateTranslateButtonState();
                }
            });
        }
    });
}

private String translateTextToChinese(String text) throws Exception {
    if (text == null || text.trim().isEmpty()) return "";
    List<String> parts = splitTextForTranslation(text.trim(), 480);
    StringBuilder out = new StringBuilder();
    Throwable last = null;
    for (String part : parts) {
        if (part == null || part.trim().isEmpty()) continue;
        String translated = null;
        try {
            translated = translateTextByMyMemory(part);
        } catch (Throwable t) {
            last = t;
            try { translated = translateTextByGoogleapis(part); }
            catch (Throwable t2) { last = t2; }
        }
        if (translated == null || translated.trim().isEmpty()) {
            if (last instanceof Exception) throw (Exception) last;
            throw new RuntimeException("Translate empty result");
        }
        if (out.length() > 0) out.append("\n\n");
        out.append(translated.trim());
        try { Thread.sleep(220); } catch (InterruptedException ignored) { }
    }
    return out.toString().trim();
}

private List<String> splitTextForTranslation(String text, int maxLen) {
    List<String> list = new ArrayList<>();
    if (text == null) return list;
    String s = text.trim();
    while (s.length() > maxLen) {
        int cut = Math.max(s.lastIndexOf("\n", maxLen), Math.max(s.lastIndexOf(". ", maxLen), s.lastIndexOf("。", maxLen)));
        if (cut < maxLen / 2) cut = maxLen;
        list.add(s.substring(0, Math.min(cut + 1, s.length())).trim());
        s = s.substring(Math.min(cut + 1, s.length())).trim();
    }
    if (!s.isEmpty()) list.add(s);
    return list;
}

private String translateTextByMyMemory(String q) throws Exception {
    String url = "https://api.mymemory.translated.net/get?q=" + URLEncoder.encode(q, "UTF-8") + "&langpair=en%7Czh-CN";
    HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
    c.setInstanceFollowRedirects(true);
    c.setConnectTimeout(12000);
    c.setReadTimeout(18000);
    c.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36 KamiGAL/1.0");
    c.setRequestProperty("Accept", "application/json,text/plain,*/*");
    c.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
    int code = c.getResponseCode();
    String body = readSmallText(code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream());
    if (code < 200 || code >= 300) throw new RuntimeException("MyMemory HTTP " + code + ": " + body);
    JSONObject root = new JSONObject(body);
    if (root.optInt("responseStatus", 200) >= 400) throw new RuntimeException("MyMemory response " + root.optString("responseDetails", "failed"));
    JSONObject data = root.optJSONObject("responseData");
    String translated = data == null ? "" : data.optString("translatedText", "");
    return translated == null ? "" : translated.trim();
}

private String translateTextByEdge(String q) throws Exception {
    String endpoint = "https://api-edge.cognitive.microsofttranslator.com/translate?api-version=3.0&from=en&to=zh-Hans";
    HttpURLConnection c = (HttpURLConnection) new URL(endpoint).openConnection();
    c.setRequestMethod("POST");
    c.setInstanceFollowRedirects(true);
    c.setConnectTimeout(12000);
    c.setReadTimeout(18000);
    c.setDoOutput(true);
    c.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
    c.setRequestProperty("Accept", "application/json");
    c.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
    c.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120 Safari/537.36 Edg/120");
    c.setRequestProperty("Origin", "https://www.bing.com");
    c.setRequestProperty("Referer", "https://www.bing.com/translator");
    JSONArray req = new JSONArray();
    JSONObject obj = new JSONObject();
    obj.put("Text", q);
    req.put(obj);
    byte[] data = req.toString().getBytes("UTF-8");
    c.setFixedLengthStreamingMode(data.length);
    try (OutputStream os = c.getOutputStream()) { os.write(data); }
    int code = c.getResponseCode();
    String body = readSmallText(code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream());
    if (code < 200 || code >= 300) throw new RuntimeException("Edge Translate HTTP " + code + ": " + body);
    JSONArray root = new JSONArray(body);
    if (root.length() == 0) return "";
    JSONArray translations = root.optJSONObject(0) == null ? null : root.optJSONObject(0).optJSONArray("translations");
    if (translations == null || translations.length() == 0) return "";
    JSONObject first = translations.optJSONObject(0);
    return first == null ? "" : first.optString("text", "").trim();
}

private String translateTextByGoogleapis(String q) throws Exception {
    return translateWithGoogleEndpoint("https://translate.googleapis.com/translate_a/single", q);
}

private String translateWithGoogleEndpoint(String endpoint, String q) throws Exception {
    String url = endpoint + "?client=gtx&sl=auto&tl=zh-CN&dt=t&q=" + URLEncoder.encode(q, "UTF-8");
    HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
    c.setInstanceFollowRedirects(true);
    c.setConnectTimeout(12000);
    c.setReadTimeout(18000);
    c.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36");
    c.setRequestProperty("Accept", "application/json,text/plain,*/*");
    c.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
    int code = c.getResponseCode();
    String body = readSmallText(code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream());
    if (code < 200 || code >= 300) throw new RuntimeException("Translate HTTP " + code + " " + endpoint);
    JSONArray root = new JSONArray(body);
    JSONArray sentences = root.optJSONArray(0);
    StringBuilder sb = new StringBuilder();
    if (sentences != null) {
        for (int i = 0; i < sentences.length(); i++) {
            JSONArray part = sentences.optJSONArray(i);
            if (part != null) sb.append(part.optString(0, ""));
        }
    }
    return sb.toString().trim();
}

private String extractImageUrlFromHtml(String html, String baseUrl) {
    if (html == null || html.isEmpty()) return null;
    java.util.regex.Pattern p = java.util.regex.Pattern.compile("https?://[^\\\"'<> ]+\\.(?:jpg|jpeg|png|webp)(?:\\?[^\\\"'<> ]*)?", java.util.regex.Pattern.CASE_INSENSITIVE);
    java.util.regex.Matcher m = p.matcher(html);
    if (m.find()) return m.group();
    p = java.util.regex.Pattern.compile("(?:src|href)=['\\\"]([^'\\\"]+\\.(?:jpg|jpeg|png|webp)(?:\\?[^'\\\"]*)?)['\\\"]", java.util.regex.Pattern.CASE_INSENSITIVE);
    m = p.matcher(html);
    if (m.find()) {
        String url = m.group(1);
        if (url.startsWith("//")) return "https:" + url;
        if (url.startsWith("/")) return "https://vndb.org" + url;
        if (url.startsWith("http")) return url;
        try { return new java.net.URL(new java.net.URL(baseUrl), url).toString(); } catch (Throwable ignored) { }
    }
    return null;
}

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    // 根据UI布局方向获取弹窗宽度基准，不依赖系统方向是否已生效
    private int getDialogBaseWidth(boolean portrait) {
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(size);
        int w = Math.max(size.x, size.y);
        int h = Math.min(size.x, size.y);
        return portrait ? h : w;
    }

private String safeCacheName(String input) {
    if (input == null) return "cache";
    return input.replaceAll("[^a-zA-Z0-9._-]", "_");
}

private void setSideDescription(String text) {
    sideFullDescription = emptyText(text, "暂无简介。");
    sideDescExpanded = false;
    renderSideDescription();
}

private void renderSideDescription() {
    if (sideDetailHint == null || sideDescToggle == null) return;
    sideDetailHint.setText(sideFullDescription == null ? "" : sideFullDescription);
    boolean longEnough = sideFullDescription != null && (sideFullDescription.length() > 110 || sideFullDescription.contains("\n\n") || sideFullDescription.split("\n").length > 5);
    sideDetailHint.setMaxLines(sideDescExpanded ? Integer.MAX_VALUE : 5);
    sideDetailHint.setEllipsize(sideDescExpanded ? null : android.text.TextUtils.TruncateAt.END);
    sideDescToggle.setVisibility(longEnough ? View.VISIBLE : View.GONE);
    sideDescToggle.setText(sideDescExpanded ? "收起" : "展开");
}

private void renderTagChips(String tagsText) {
    if (sideTagContainer == null || sideDetailTags == null) return;
    sideTagContainer.removeAllViews();
    String source = tagsText == null ? "" : tagsText.trim();
    if (source.isEmpty() || "-".equals(source)) {
        sideTagContainer.addView(sideDetailTags);
        sideDetailTags.setText("-");
        sideDetailTags.setVisibility(View.VISIBLE);
        return;
    }
    sideDetailTags.setVisibility(View.GONE);
    String[] tags = source.split("\\s{2,}|[,，/]");
    LinearLayout row = null;
    int countInRow = 0;
    for (String raw : tags) {
        String tag = raw == null ? "" : raw.trim();
        if (tag.isEmpty()) continue;
        if (row == null || countInRow >= 2) {
            row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            sideTagContainer.addView(row);
            countInRow = 0;
        }
        TextView chip = new TextView(this);
        chip.setText(tag);
        chip.setTextSize(7);
        chip.setTextColor(getResources().getColor(R.color.yh_primary));
        chip.setSingleLine(true);
        chip.setEllipsize(android.text.TextUtils.TruncateAt.END);
        chip.setGravity(android.view.Gravity.CENTER);
        chip.setBackgroundResource(R.drawable.bg_chip);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(20), 1);
        lp.setMargins(0, 0, dp(3), dp(3));
        row.addView(chip, lp);
        countInRow++;
    }
    if (sideTagContainer.getChildCount() == 0) {
        sideTagContainer.addView(sideDetailTags);
        sideDetailTags.setText("-");
        sideDetailTags.setVisibility(View.VISIBLE);
    }
}

private String buildMetadataSearchKeyword(String title) {
        if (title == null) return "";
        String cleaned = title.replaceAll("[【\\[][^】\\]]*[】\\]]", " ");
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        return cleaned.isEmpty() ? title.trim() : cleaned;
    }

    private boolean isConfidentMatch(String localTitle, VnMetadata meta) {
        if (meta == null || localTitle == null) return false;
        String a = buildMetadataSearchKeyword(localTitle).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\u4e00-\\u9fa5ぁ-んァ-ン一-龯]", "");
        String b = (emptyText(meta.chineseTitle, "") + emptyText(meta.originalTitle, "") + emptyText(meta.romanTitle, "")).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\u4e00-\\u9fa5ぁ-んァ-ン一-龯]", "");
        return !a.isEmpty() && !b.isEmpty() && (b.contains(a) || a.contains(b));
    }

private void showVndbCandidateDialog(Game game, List<VnMetadata> list) {
    if (game == null || list == null || list.isEmpty()) return;
    androidx.recyclerview.widget.RecyclerView rv = new androidx.recyclerview.widget.RecyclerView(this);
    rv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
    rv.setPadding(dp(6), dp(6), dp(6), dp(6));
    rv.setClipToPadding(false);
    final AlertDialog[] dialogRef = new AlertDialog[1];
    final List<VnMetadata> items = new ArrayList<>(list);
    items.add(null);
    rv.setAdapter(new androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
        @Override public int getItemViewType(int position) { return position; }
        @Override public int getItemCount() { return items.size(); }
        @Override public androidx.recyclerview.widget.RecyclerView.ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vndb_candidate, parent, false);
            return new androidx.recyclerview.widget.RecyclerView.ViewHolder(v) {};
        }
        @Override public void onBindViewHolder(androidx.recyclerview.widget.RecyclerView.ViewHolder holder, int position) {
            android.view.View itemView = holder.itemView;
            VnMetadata m = items.get(position);
            if (m == null) {
                ((TextView) itemView.findViewById(R.id.tvCandidateTitle)).setText("不匹配 / 暂不使用 VNDB");
                ((TextView) itemView.findViewById(R.id.tvCandidateOriginal)).setText("保留当前本地资料");
                ((TextView) itemView.findViewById(R.id.tvCandidateInfo)).setText("关闭弹窗，不绑定 VNDB");
                ((ImageView) itemView.findViewById(R.id.ivCandidateCover)).setImageDrawable(null);
            } else {
                ((TextView) itemView.findViewById(R.id.tvCandidateTitle)).setText(emptyText(m.chineseTitle, emptyText(m.romanTitle, "未命名")));
                ((TextView) itemView.findViewById(R.id.tvCandidateOriginal)).setText(emptyText(m.originalTitle, m.id));
                ((TextView) itemView.findViewById(R.id.tvCandidateInfo)).setText(emptyText(m.developer, "VNDB 候选"));
                ImageView cover = itemView.findViewById(R.id.ivCandidateCover);
                cover.setImageDrawable(null);
                if (m.coverUrl != null && !m.coverUrl.isEmpty()) loadRemoteImage(m.coverUrl, cover, "cand_" + m.id);
            }
            itemView.setOnClickListener(v -> {
                if (selectedGame == null || selectedGame.id != game.id) return;
                if (position >= 0 && position < list.size()) {
                    VnMetadata chosen = list.get(position);
if (metadataRepository != null) { if (false) metadataRepository.saveVndb(game.id, chosen); else metadataRepository.saveVndb(game.id, chosen); }
applyVndbMetadata(chosen, game);
                } else {
                    sideDetailOriginalTitle.setText("未绑定 VNDB");
                    setSideDescription(emptyText(game.description, "已跳过 VNDB 匹配。"));
                }
                if (dialogRef[0] != null) dialogRef[0].dismiss();
            });
        }
    });
    AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("选择 VNDB 匹配结果")
            .setView(rv)
            .setNegativeButton("取消", null)
            .show();
    dialogRef[0] = dialog;
    if (dialog.getWindow() != null) {
        dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.70f), (int) (getResources().getDisplayMetrics().heightPixels * 0.72f));
    }
}

private void applyVndbMetadata(VnMetadata meta, Game game) {
    currentSideMetadata = meta;
    long gameId = game == null ? -1 : game.id;
    sideShowingTranslatedDescription = gameId > 0 && isTranslatedStateFor(gameId) && meta != null && meta.translatedDescription != null && !meta.translatedDescription.trim().isEmpty();
    if (meta == null) {
        updateTranslateButtonState();
        setSideDescription(emptyText(game.description, "VNDB 暂未匹配到资料。"));
        return;
    }
    sideDetailTitle.setText(emptyText(meta.chineseTitle, emptyText(game.title, "未命名游戏")));
    sideDetailOriginalTitle.setText(emptyText(meta.originalTitle, meta.romanTitle));
    updateTranslateButtonState();
    setSideDescription(sideShowingTranslatedDescription ? meta.translatedDescription : emptyText(meta.description, "暂无 VNDB 简介。"));
    sideDetailDate.setText("发布日期：" + emptyText(meta.released, "-"));
    sideDetailDeveloper.setText("开发商：" + emptyText(meta.developer, "-"));
    if (sideDetailPath != null) sideDetailPath.setText("路径：" + displayPath(game.rootUri));
    sideDetailRating.setText(emptyText(meta.ratingText, "评分：-/10"));
if (sideDetailLength != null) sideDetailLength.setText(emptyText(meta.lengthText, "游玩时长：-"));
renderTagChips(emptyText(meta.tagsText, "-"));
    if (meta.coverUrl != null && !meta.coverUrl.isEmpty()) {
sideDetailCover.setVisibility(View.VISIBLE);
sideDetailPlaceholder.setVisibility(View.GONE);
sideDetailCover.setTag(game);
loadRemoteImage(meta.coverUrl, sideDetailCover, "cover_" + emptyText(meta.id, String.valueOf(game.id)));
// 同时更新游戏记录的封面，让游戏卡片也显示正确的封面
AppExecutors.runOnIo(() -> {
    if (game == null || game.id <= 0) return;
    String localCover = cacheRemoteImageSync(meta.coverUrl, "meta_cover_" + emptyText(meta.id, String.valueOf(game.id)));
    if (localCover != null && !localCover.isEmpty()) {
        if (!localCover.equals(game.coverUri) || !localCover.equals(game.coverPersistUri)) {
            game.coverUri = localCover;
            game.coverPersistUri = localCover;
            game.coverSourceType = 1;
            repository.update(game);
            runOnUiThread(() -> loadGames());
        }
    }
});
}
    if (meta.screenshotUrls.size() > 0) loadRemoteImage(meta.screenshotUrls.get(0), sideScreenshot1, "shot1_" + emptyText(meta.id, String.valueOf(game.id)));
    if (meta.screenshotUrls.size() > 1) loadRemoteImage(meta.screenshotUrls.get(1), sideScreenshot2, "shot2_" + emptyText(meta.id, String.valueOf(game.id)));
}

private void updateSideDetail(Game game) {
        selectedGame = game;
        currentSideMetadata = null;
        sideShowingTranslatedDescription = game != null && isTranslatedStateFor(game.id);
        updateTranslateButtonState();
        if (adapter != null) adapter.setSelectedGameId(game == null ? -1 : game.id);
        if (sideDetailTitle == null) return;
        boolean hasGame = game != null;
        sideBtnLaunch.setEnabled(hasGame);
        sideBtnOptions.setEnabled(hasGame);
        sideBtnLaunch.setAlpha(hasGame ? 1f : 0.45f);
        sideBtnOptions.setAlpha(hasGame ? 1f : 0.45f);
        if (!hasGame) {
            sideDetailTitle.setText("请选择游戏");
            sideDetailOriginalTitle.setText("");
            setSideDescription("点击中间的游戏卡片后，这里会显示封面、启动入口和选项。后续可接 VNDB/APJ 简介与元数据。");
            sideDetailDate.setText("发布日期：-");
sideDetailDeveloper.setText("开发商：-");
if (sideDetailPath != null) sideDetailPath.setText("路径：-");
            sideDetailRating.setText("评分：-/10");
            if (sideDetailLength != null) sideDetailLength.setText("游玩时长：-");
            renderTagChips("-");
            sideDetailCover.setImageDrawable(null);
            sideDetailCover.setVisibility(View.GONE);
            sideDetailPlaceholder.setVisibility(View.VISIBLE);
            sideDetailPlaceholder.setText("选择游戏");
            if (sideScreenshot1 != null) sideScreenshot1.setImageDrawable(null);
            if (sideScreenshot2 != null) sideScreenshot2.setImageDrawable(null);
            return;
        }
        sideDetailTitle.setText(emptyText(game.title, "未命名游戏"));
        sideDetailOriginalTitle.setText("VNDB 匹配中…");
        setSideDescription(emptyText(game.description, "正在从 VNDB 获取简介…"));
        sideDetailDate.setText("发布日期：-");
sideDetailDeveloper.setText("开发商：-");
if (sideDetailPath != null) sideDetailPath.setText("路径：" + displayPath(game.rootUri));
        sideDetailRating.setText("评分：-/10");
        if (sideDetailLength != null) sideDetailLength.setText("游玩时长：-");
        renderTagChips("-");
        if (sideScreenshot1 != null) sideScreenshot1.setImageDrawable(null);
        if (sideScreenshot2 != null) sideScreenshot2.setImageDrawable(null);
        String coverUri = safeCoverUri(game);
        if (coverUri != null && !coverUri.isEmpty()) {
            try {
                sideDetailCover.setImageURI(Uri.parse(coverUri));
                sideDetailCover.setVisibility(View.VISIBLE);
                sideDetailPlaceholder.setVisibility(View.GONE);
            } catch (Throwable t) {
                sideDetailCover.setImageDrawable(null);
                sideDetailCover.setVisibility(View.GONE);
                sideDetailPlaceholder.setVisibility(View.VISIBLE);
                sideDetailPlaceholder.setText(initials(game.title));
            }
        } else {
            sideDetailCover.setImageDrawable(null);
            sideDetailCover.setVisibility(View.GONE);
            sideDetailPlaceholder.setVisibility(View.VISIBLE);
            sideDetailPlaceholder.setText(initials(game.title));
        }
        fetchSelectedMetadata(game);
    }

    private void showCustomVndbSearchDialog(Game game) {
    if (game == null) return;
    EditText input = new EditText(this);
    input.setSingleLine(true);
    input.setText(emptyText(game.title, ""));
    input.setSelectAllOnFocus(true);
    input.setHint("输入 VNDB 搜索关键词或原名");
    input.setTextColor(getResources().getColor(R.color.yh_text));
    input.setHintTextColor(getResources().getColor(R.color.yh_text_muted));
    input.setBackgroundResource(R.drawable.bg_input);
    input.setPadding(dp(12), 0, dp(12), 0);
    new AlertDialog.Builder(this)
            .setTitle("自定义搜索 VNDB")
            .setView(input)
            .setPositiveButton("搜索", (d, w) -> {
                String keyword = input.getText() == null ? "" : input.getText().toString().trim();
                if (keyword.isEmpty()) { Toast.makeText(this, "请输入搜索关键词", Toast.LENGTH_SHORT).show(); return; }
                searchVndbWithKeyword(game, keyword);
            })
            .setNegativeButton("取消", null)
            .show();
}

private void searchVndbWithKeyword(Game game, String keyword) {
    if (game == null || keyword == null || keyword.trim().isEmpty()) return;
    setSideDescription("正在按自定义关键词搜索 VNDB…");
    VndbClient.searchCandidatesAsync(keyword, 8, new VndbClient.CandidatesCallback() {
        @Override public void onSuccess(List<VnMetadata> data) {
            runOnUiThread(() -> {
                if (selectedGame == null || selectedGame.id != game.id) return;
                if (data == null || data.isEmpty()) {
                    Toast.makeText(MainActivity.this, "没有匹配到 VNDB 结果", Toast.LENGTH_SHORT).show();
                    setSideDescription(emptyText(game.description, "VNDB 暂未匹配到资料。"));
                } else {
                    showVndbCandidateDialog(game, data);
                }
            });
        }
        @Override public void onError(Exception error) {
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "VNDB 搜索失败", Toast.LENGTH_SHORT).show());
        }
    });
}

private void syncVndbToGameCard(Game game) {
    if (game == null) return;
    VnMetadata meta = metadataRepository == null ? null : metadataRepository.getVndb(game.id);
    if (meta == null) {
        Toast.makeText(this, "请先匹配 VNDB 资料", Toast.LENGTH_SHORT).show();
        return;
    }
    Toast.makeText(this, "正在同步 VNDB 到游戏卡片…", Toast.LENGTH_SHORT).show();
    AppExecutors.runOnIo(() -> {
        String localCover = null;
        if (meta.coverUrl != null && !meta.coverUrl.isEmpty()) {
            localCover = cacheRemoteImageSync(meta.coverUrl, "card_cover_" + emptyText(meta.id, String.valueOf(game.id)));
        }
        final String cover = localCover;
        runOnUiThread(() -> {
            String newTitle = emptyText(meta.chineseTitle, emptyText(meta.originalTitle, meta.romanTitle));
            if (!newTitle.isEmpty()) game.title = newTitle;
            if (meta.originalTitle != null && !meta.originalTitle.isEmpty()) game.originalTitle = meta.originalTitle;
            if (meta.description != null && !meta.description.isEmpty()) game.description = meta.description;
            if (meta.tagsText != null && !meta.tagsText.isEmpty()) game.tags = meta.tagsText;
            if (cover != null && !cover.isEmpty()) {
                game.coverUri = cover;
                game.coverPersistUri = cover;
                game.coverSourceType = 1;
            }
            repository.update(game);
            loadGames();
            updateSideDetail(game);
            Toast.makeText(this, "已同步 VNDB 中文名和封面到游戏卡片", Toast.LENGTH_SHORT).show();
        });
    });
}

private String cacheRemoteImageSync(String url, String prefix) {
if (url == null || url.trim().isEmpty()) return null;
try {
File cacheDir = persistentRemoteCoverDir();
if (!cacheDir.exists()) cacheDir.mkdirs();
File cacheFile = new File(cacheDir, safeCacheName(prefix + "_" + url.trim()));
if (!cacheFile.exists() || cacheFile.length() == 0 || BitmapFactory.decodeFile(cacheFile.getAbsolutePath()) == null) {
if (cacheFile.exists()) cacheFile.delete();
boolean ok = downloadImageAllowVndbWarningPage(url.trim(), cacheFile, 0);
if (!ok || BitmapFactory.decodeFile(cacheFile.getAbsolutePath()) == null) return null;
}
return Uri.fromFile(cacheFile).toString();
} catch (Throwable t) {
return null;
}
}

private void styleAlertDialogDark(AlertDialog dialog) {
    if (dialog == null) return;
    try {
        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawableResource(R.drawable.bg_dialog);
        }
        int text = getColorCompat(R.color.yh_text);
        int muted = getColorCompat(R.color.yh_text_muted);
        int primary = getColorCompat(R.color.yh_primary);
        int titleId = getResources().getIdentifier("alertTitle", "id", "android");
        TextView title = dialog.findViewById(titleId);
        if (title != null) title.setTextColor(text);
        TextView msg = dialog.findViewById(android.R.id.message);
        if (msg != null) msg.setTextColor(muted);
        Button p = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button n = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        Button neu = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        if (p != null) p.setTextColor(primary);
        if (n != null) n.setTextColor(primary);
        if (neu != null) neu.setTextColor(primary);
        android.widget.ListView list = dialog.getListView();
        if (list != null) {
            list.setBackgroundColor(Color.TRANSPARENT);
            list.setCacheColorHint(Color.TRANSPARENT);
        }
    } catch (Throwable ignored) { }
}

private void showSideOptions(Game game) {
        if (game == null) return;
        String sourceLabel = metadataSourceLabel();
String rematchItem = "重新匹配" + sourceLabel;
        String customSearchItem = false ? "自定义搜索Bangumi" : "自定义搜索VNDB";
        String syncItem = "同步" + sourceLabel + "到卡片";
        String playTimeItem = "修改游玩时长";
        String favoriteItem = game.favorite ? "取消收藏" : "收藏游戏";
        String shareItem = "分享游戏";
        String[] items = (game.engine == EngineType.KIRIKIRI || game.engine == EngineType.ONS)
                ? new String[]{"编辑游戏", "设置游玩状态", shareItem, playTimeItem, favoriteItem, rematchItem, customSearchItem, syncItem, "引擎设置", "详细信息", "删除游戏"}
                : new String[]{"编辑游戏", "设置游玩状态", shareItem, playTimeItem, favoriteItem, rematchItem, customSearchItem, syncItem, "详细信息", "删除游戏"};
        LinearLayout listRoot = new LinearLayout(this);
        listRoot.setOrientation(LinearLayout.VERTICAL);
        listRoot.setBackgroundResource(R.drawable.bg_dialog);
        int hp = dp(18);
        listRoot.setPadding(0, dp(6), 0, dp(6));
        final AlertDialog[] ref = new AlertDialog[1];
        for (String item : items) {
            TextView row = new TextView(this);
            row.setText(item);
            row.setTextColor(getColorCompat("删除游戏".equals(item) ? R.color.yh_secondary : R.color.yh_text));
            row.setTextSize(15);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(hp, 0, hp, 0);
            row.setBackgroundResource(R.drawable.bg_input);
            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(46));
            rlp.setMargins(dp(10), dp(4), dp(10), dp(4));
            listRoot.addView(row, rlp);
            row.setOnClickListener(v -> {
                if (ref[0] != null) ref[0].dismiss();
                String chosen = ((TextView) v).getText().toString();
                if ("编辑游戏".equals(chosen)) showEditDialog(game);
                else if ("设置游玩状态".equals(chosen)) showPlayStatusDialog(game, null);
                else if (playTimeItem.equals(chosen)) showEditPlayTimeDialog(game);
                else if (favoriteItem.equals(chosen)) {
                    game.favorite = !game.favorite;
                    repository.update(game);
                    loadGames();
                    Toast.makeText(this, game.favorite ? "已收藏" : "已取消收藏", Toast.LENGTH_SHORT).show();
                }
                else if (shareItem.equals(chosen)) shareGame(game);
                else if (rematchItem.equals(chosen)) {
                    if (metadataRepository != null) {
                        if (false) metadataRepository.clearVndb(game.id);
                        else metadataRepository.clearVndb(game.id);
                    }
                    fetchSelectedMetadata(game, true);
                }
                else if (customSearchItem.equals(chosen)) { showCustomVndbSearchDialog(game); }
                else if (syncItem.equals(chosen)) syncVndbToGameCard(game);
                else if ("引擎设置".equals(chosen)) { if (game.engine == EngineType.ONS) showOnsSettingsDialog(game); else showKrSettingsDialog(game); }
                else if ("详细信息".equals(chosen)) showDetailDialog(game);
                else if ("删除游戏".equals(chosen)) confirmDeleteGame(game);
            });
        }
        ScrollView optionScroll = new ScrollView(this);
        optionScroll.setFillViewport(false);
        optionScroll.setBackgroundResource(R.drawable.bg_dialog);
        optionScroll.addView(listRoot, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
        AlertDialog optionDialog = new AlertDialog.Builder(this)
                .setTitle(emptyText(game.title, "游戏选项"))
                .setView(optionScroll)
                .show();
        ref[0] = optionDialog;
        styleAlertDialogDark(optionDialog);
        if (optionDialog.getWindow() != null) {
            DisplayMetrics dmOpt = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getRealMetrics(dmOpt);
            boolean optPortrait = dmOpt.widthPixels < dmOpt.heightPixels;
            float optRatio = optPortrait ? 0.72f : 0.48f;
            optionDialog.getWindow().setLayout((int) (dmOpt.widthPixels * optRatio), (int) (dmOpt.heightPixels * 0.78f));
        }
    }

    private void confirmDeleteGame(Game game) {
        if (game == null) return;
        new AlertDialog.Builder(this)
                .setTitle("删除游戏")
                .setMessage("确定删除 “" + game.title + "”？不会删除本体文件。")
                .setPositiveButton("删除", (x,w)->{ repository.delete(game.id); selectedGame = null; loadGames(); })
                .setNegativeButton("取消", null)
                .show();
    }

    private String initials(String title) {
        if (title == null || title.trim().isEmpty()) return "YH";
        return title.trim().substring(0, 1).toUpperCase(Locale.ROOT);
    }

    private String safeCoverUri(Game g) {
        if (g == null) return null;
        if (g.coverPersistUri != null && !g.coverPersistUri.isEmpty()) return g.coverPersistUri;
        if (g.coverUri != null && !g.coverUri.isEmpty()) return g.coverUri;
        return null;
    }

    private void showSettingsDialog() {
        final boolean portraitNow = isPortrait;
        final int rootPad = dp(16);
        final int btnH = dp(40);
        final float titleSz = 14;
        final float infoSz = 11;
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.drawable.bg_dialog);
        root.setPadding(rootPad, dp(12), rootPad, dp(8));

        TextView scanTitle = new TextView(this);
        scanTitle.setText("扫描目录");
        scanTitle.setTextColor(getColorCompat(R.color.yh_text));
        scanTitle.setTextSize(14);
        scanTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(scanTitle);

        TextView scanInfo = new TextView(this);
        scanInfo.setTextColor(getColorCompat(R.color.yh_text_muted));
        scanInfo.setTextSize(11);
        scanInfo.setPadding(0, dp(4), 0, dp(8));
        root.addView(scanInfo, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout scanRootList = new LinearLayout(this);
        scanRootList.setOrientation(LinearLayout.VERTICAL);
        root.addView(scanRootList);
        activeScanRootList = scanRootList;
        activeScanRootInfo = scanInfo;
        refreshScanRootListUi(scanRootList, scanInfo);

        Button addScanRootButton = krButton("+ 添加扫描目录");
        addScanRootButton.setTextColor(getColorCompat(R.color.yh_primary));
        addScanRootButton.setOnClickListener(v -> {
            if (getScanRootUris().size() >= MAX_SCAN_ROOTS) {
                Toast.makeText(this, "最多绑定 " + MAX_SCAN_ROOTS + " 个扫描目录", Toast.LENGTH_SHORT).show();
                return;
            }
            launchScanRootPicker(-1);
        });
        LinearLayout.LayoutParams addScanRootLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(40));
        addScanRootLp.setMargins(0, dp(4), 0, dp(8));
        root.addView(addScanRootButton, addScanRootLp);

        TextView scanDepthTitle = new TextView(this);
        scanDepthTitle.setText("\n启动时扫描最大深度");
        scanDepthTitle.setTextColor(getColorCompat(R.color.yh_text));
        scanDepthTitle.setTextSize(14);
        scanDepthTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(scanDepthTitle);

        TextView scanDepthInfo = new TextView(this);
        int savedDepth = prefs == null ? DEFAULT_STARTUP_SCAN_DEPTH : prefs.getInt(KEY_STARTUP_SCAN_DEPTH, DEFAULT_STARTUP_SCAN_DEPTH);
        savedDepth = Math.max(1, Math.min(MAX_STARTUP_SCAN_DEPTH, savedDepth));
        scanDepthInfo.setText("当前：" + savedDepth + " 层（最深 4 层）");
        scanDepthInfo.setTextColor(getColorCompat(R.color.yh_text_muted));
        scanDepthInfo.setTextSize(11);
        scanDepthInfo.setPadding(0, dp(4), 0, dp(6));
        root.addView(scanDepthInfo);

        LinearLayout depthRow = new LinearLayout(this);
        depthRow.setOrientation(LinearLayout.HORIZONTAL);
        depthRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        SeekBar scanDepthSeek = new SeekBar(this);
        scanDepthSeek.setMax(MAX_STARTUP_SCAN_DEPTH - 1);
        scanDepthSeek.setProgress(savedDepth - 1);
        LinearLayout.LayoutParams seekLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        depthRow.addView(scanDepthSeek, seekLp);
        TextView depthValue = new TextView(this);
        depthValue.setText(String.valueOf(savedDepth));
        depthValue.setTextColor(getColorCompat(R.color.yh_text));
        depthValue.setTextSize(15);
        depthValue.setTypeface(null, android.graphics.Typeface.BOLD);
        depthValue.setPadding(dp(10), 0, 0, 0);
        depthRow.addView(depthValue);
        root.addView(depthRow);
        scanDepthSeek.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int depth = Math.max(1, Math.min(MAX_STARTUP_SCAN_DEPTH, progress + 1));
                depthValue.setText(String.valueOf(depth));
                scanDepthInfo.setText("当前：" + depth + " 层（最深 4 层）");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        scanDepthSeek.setProgress(savedDepth - 1);

        TextView fontTitle = new TextView(this);
        fontTitle.setText("\n整体字体大小");
        fontTitle.setTextColor(getColorCompat(R.color.yh_text));
        fontTitle.setTextSize(14);
        fontTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(fontTitle);

        float savedFontScale = prefs == null ? UiScaleUtil.DEFAULT_FONT_SCALE : prefs.getFloat(UiScaleUtil.KEY_UI_FONT_SCALE, UiScaleUtil.DEFAULT_FONT_SCALE);
        TextView fontInfo = new TextView(this);
        fontInfo.setText("当前：" + UiScaleUtil.percent(savedFontScale) + "%（默认 100%）");
        fontInfo.setTextColor(getColorCompat(R.color.yh_text_muted));
        fontInfo.setTextSize(11);
        fontInfo.setPadding(0, dp(4), 0, dp(6));
        root.addView(fontInfo);

        LinearLayout fontRow = new LinearLayout(this);
        fontRow.setOrientation(LinearLayout.HORIZONTAL);
        fontRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        SeekBar fontSeek = new SeekBar(this);
        fontSeek.setMax((int) ((UiScaleUtil.MAX_FONT_SCALE - UiScaleUtil.MIN_FONT_SCALE) * 100f));
        fontSeek.setProgress(Math.round((savedFontScale - UiScaleUtil.MIN_FONT_SCALE) * 100f));
        LinearLayout.LayoutParams fontSeekLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        fontRow.addView(fontSeek, fontSeekLp);
        TextView fontValue = new TextView(this);
        fontValue.setText(UiScaleUtil.percent(savedFontScale) + "%");
        fontValue.setTextColor(getColorCompat(R.color.yh_text));
        fontValue.setTextSize(15);
        fontValue.setTypeface(null, android.graphics.Typeface.BOLD);
        fontValue.setPadding(dp(10), 0, 0, 0);
        fontRow.addView(fontValue);
        root.addView(fontRow);
        fontSeek.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float scale = UiScaleUtil.clamp(UiScaleUtil.MIN_FONT_SCALE + progress / 100f);
                fontValue.setText(UiScaleUtil.percent(scale) + "%");
                fontInfo.setText("当前：" + UiScaleUtil.percent(scale) + "%（默认 100%）");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        Button fontReset = krButton("恢复默认字体");
        fontReset.setTextColor(getColorCompat(R.color.yh_text));
        fontReset.setOnClickListener(v -> {
            fontSeek.setProgress(Math.round((UiScaleUtil.DEFAULT_FONT_SCALE - UiScaleUtil.MIN_FONT_SCALE) * 100f));
            fontValue.setText("100%");
            fontInfo.setText("当前：100%（默认 100%）");
        });
        LinearLayout.LayoutParams fontResetLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(40));
        fontResetLp.topMargin = dp(6);
        root.addView(fontReset, fontResetLp);

        CheckBox cutoutCheck = krCheckBox("适配挖孔屏/全面屏", prefs.getBoolean(KEY_CUTOUT_ENABLED, false));
        root.addView(cutoutCheck);

        CheckBox autoScanCheck = krCheckBox("进入应用时自动扫描上次目录", prefs == null || prefs.getBoolean(KEY_AUTO_SCAN_ON_STARTUP, true));
root.addView(autoScanCheck);
        CheckBox detonatorCheck = krCheckBox("起爆器", isDetonatorActive);
        detonatorCheckRef = detonatorCheck;
        detonatorCheck.setOnCheckedChangeListener((btn, checked) -> {
            if (isDetonatorDead) {
                btn.setChecked(false);
                Toast.makeText(this, "起爆器没电了…", Toast.LENGTH_SHORT).show();
                return;
            }
            if (checked) {
                startDetonator();
            } else {
                stopDetonator(false);
            }
        });
        root.addView(detonatorCheck);
        TextView sortTitle = new TextView(this);
        sortTitle.setText("\n游戏库排序");
        sortTitle.setTextColor(getColorCompat(R.color.yh_text));
        sortTitle.setTextSize(14);
        sortTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(sortTitle);

        TextView sortInfo = new TextView(this);
        sortInfo.setText("默认按最近游玩排序，收藏会始终置顶。");
        sortInfo.setTextColor(getColorCompat(R.color.yh_text_muted));
        sortInfo.setTextSize(11);
        sortInfo.setPadding(0, dp(4), 0, dp(6));
        root.addView(sortInfo);

        Spinner sortSpinner = new Spinner(this);
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_dark, new String[]{"最近游玩", "最近添加", "名称排序"});
        sortAdapter.setDropDownViewResource(R.layout.spinner_dropdown_dark);
        sortSpinner.setAdapter(sortAdapter);
        String savedSortMode = prefs == null ? SORT_MODE_RECENT : prefs.getString(KEY_SORT_MODE, SORT_MODE_RECENT);
        if (SORT_MODE_NEWEST.equals(savedSortMode)) sortSpinner.setSelection(1);
        else if (SORT_MODE_NAME.equals(savedSortMode)) sortSpinner.setSelection(2);
        else sortSpinner.setSelection(0);
        root.addView(sortSpinner, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(44)));

        TextView personalizationTitle = new TextView(this);
        personalizationTitle.setText("\n个性化功能");
        personalizationTitle.setTextColor(getColorCompat(R.color.yh_text));
        personalizationTitle.setTextSize(14);
        personalizationTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(personalizationTitle);

        TextView engineLabelTitle = new TextView(this);
        engineLabelTitle.setText("游戏引擎标签位置");
        engineLabelTitle.setTextColor(getColorCompat(R.color.yh_text_muted));
        engineLabelTitle.setTextSize(11);
        engineLabelTitle.setPadding(0, dp(4), 0, dp(6));
        root.addView(engineLabelTitle);

        Spinner engineLabelSpinner = new Spinner(this);
        ArrayAdapter<String> engineLabelAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_dark, new String[]{"游戏标题下方", "封面左下角"});
        engineLabelAdapter.setDropDownViewResource(R.layout.spinner_dropdown_dark);
        engineLabelSpinner.setAdapter(engineLabelAdapter);
        String engineLabelPos = prefs == null ? "title" : prefs.getString(KEY_ENGINE_LABEL_POSITION, "title");
        engineLabelSpinner.setSelection("cover".equals(engineLabelPos) ? 1 : 0);
        root.addView(engineLabelSpinner, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(44)));
        TextView disclaimerTitle = new TextView(this);
        disclaimerTitle.setText("\n使用说明");
        disclaimerTitle.setTextColor(getColorCompat(R.color.yh_text));
        disclaimerTitle.setTextSize(14);
        disclaimerTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(disclaimerTitle);
        TextView disclaimerInfo = new TextView(this);
        disclaimerInfo.setText("本软件仅提供游戏数据管理、资源信息与启动辅助功能。\n\n" +
                "有条件的话请支持正版游戏，谢谢~");
        disclaimerInfo.setTextColor(getColorCompat(R.color.yh_text_muted));
        disclaimerInfo.setTextSize(11);
        disclaimerInfo.setLineSpacing(dp(2), 1.0f);
        disclaimerInfo.setPadding(0, dp(4), 0, dp(6));
        root.addView(disclaimerInfo);

        TextView sourceTitle = new TextView(this);
        sourceTitle.setText("\n右侧资料源");
        sourceTitle.setTextColor(getColorCompat(R.color.yh_text));
        sourceTitle.setTextSize(14);
        sourceTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(sourceTitle);

        Spinner sourceSpinner = new Spinner(this);
        ArrayAdapter<String> sourceAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_dark, new String[]{"VNDB（默认）"});
        sourceAdapter.setDropDownViewResource(R.layout.spinner_dropdown_dark);
        sourceSpinner.setAdapter(sourceAdapter);
        root.addView(sourceSpinner, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(44)));

        TextView bgTitle = new TextView(this);
        bgTitle.setText("\n界面背景");
        bgTitle.setTextColor(getColorCompat(R.color.yh_text));
        bgTitle.setTextSize(14);
        bgTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(bgTitle);

        TextView bgInfo = new TextView(this);
        String customBg = prefs.getString(KEY_CUSTOM_BACKGROUND, "");
        String customBgType = prefs.getString(KEY_CUSTOM_BACKGROUND_TYPE, "image");
        bgInfo.setText(customBg == null || customBg.isEmpty() ? "当前：默认动态背景" : ("video".equals(customBgType) ? "当前：自定义视频背景" : "当前：自定义图片背景"));
        bgInfo.setTextColor(getColorCompat(R.color.yh_text_muted));
        bgInfo.setTextSize(11);
        bgInfo.setPadding(0, dp(4), 0, dp(6));
        root.addView(bgInfo);

        LinearLayout bgActions = new LinearLayout(this);
        bgActions.setOrientation(LinearLayout.HORIZONTAL);
        Button chooseBgButton = krButton("图片背景");
        Button chooseVideoBgButton = krButton("视频背景");
        Button resetBgButton = krButton("恢复默认");
        chooseBgButton.setTextColor(getColorCompat(R.color.yh_primary));
        chooseVideoBgButton.setTextColor(getColorCompat(R.color.yh_primary));
        resetBgButton.setTextColor(getColorCompat(R.color.yh_text));
        bgActions.addView(chooseBgButton, new LinearLayout.LayoutParams(0, dp(40), 1));
        LinearLayout.LayoutParams videoBgLp = new LinearLayout.LayoutParams(0, dp(40), 1);
        videoBgLp.setMargins(dp(6), 0, 0, 0);
        bgActions.addView(chooseVideoBgButton, videoBgLp);
        LinearLayout.LayoutParams resetBgLp = new LinearLayout.LayoutParams(0, dp(40), 1);
        resetBgLp.setMargins(dp(6), 0, 0, 0);
        bgActions.addView(resetBgButton, resetBgLp);
        root.addView(bgActions);

        CheckBox bgDimEnabled = krCheckBox("背景遮罩（提高文字可读性）", prefs.getBoolean(KEY_BACKGROUND_DIM_ENABLED, true));
        CheckBox bgVideoSound = krCheckBox("视频背景声音", prefs.getBoolean(KEY_BACKGROUND_VIDEO_SOUND, false));
        root.addView(bgDimEnabled);
        root.addView(bgVideoSound);

        TextView krTitle = new TextView(this);
        krTitle.setText("\nKRKR 引擎");
        krTitle.setTextColor(getColorCompat(R.color.yh_text));
        krTitle.setTextSize(14);
        krTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(krTitle);

        TextView krInfo = new TextView(this);
        krInfo.setText("启动参数兼容模式会补齐旧式参数；引擎版本由此处统一指定。若部分机型因外部存储授权导致闪退，可开启独立存档目录。");
        krInfo.setTextColor(getColorCompat(R.color.yh_text_muted));
        krInfo.setTextSize(11);
        krInfo.setPadding(0, dp(4), 0, dp(6));
        root.addView(krInfo);

        root.addView(krLabel("KR 引擎版本"));
        Spinner krEngineVersion = krSpinner(new String[]{"自动", "1.3.9", "1.3.4"}, krEngineVersionToLabel(prefs.getString(KEY_KR_ENGINE_VERSION, "auto")));
        root.addView(krEngineVersion);

        CheckBox krCompatMode = krCheckBox("KR 启动参数兼容模式", prefs.getBoolean(KEY_KR_COMPAT_MODE, false));
        CheckBox krScopedSaveDir = krCheckBox("KR 独立存档目录（App 外部私有目录，实验）", prefs.getBoolean(KEY_KR_SCOPED_SAVE_DIR, false));
        CheckBox artemisScopedSaveDir = krCheckBox("Artemis 独立存档目录（实验，暂不建议开启）", prefs.getBoolean(KEY_ARTEMIS_SCOPED_SAVE_DIR, false));
        root.addView(krCompatMode);
        root.addView(krScopedSaveDir);
        root.addView(artemisScopedSaveDir);

        Button nativeKrkrButton = krButton("进入原生KRKR");
        nativeKrkrButton.setTextColor(getColorCompat(R.color.yh_primary));
        nativeKrkrButton.setOnClickListener(v -> {
            try {
                startActivity(EmulatorLauncher.buildInternalKrkrIntent(this, "", "", true));
            } catch (Throwable t) {
                Toast.makeText(this, "无法进入原生KRKR", Toast.LENGTH_SHORT).show();
            }
        });
        LinearLayout.LayoutParams nativeKrkrLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(42));
        nativeKrkrLp.setMargins(0, dp(10), 0, dp(4));
        root.addView(nativeKrkrButton, nativeKrkrLp);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        scroll.setBackgroundResource(R.drawable.bg_dialog);
        scroll.addView(root, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("设置")
                .setView(scroll)
                .setPositiveButton("保存", null)
                .setNeutralButton("更换扫描目录", null)
                .setNegativeButton("关闭", null)
                .show();
        styleAlertDialogDark(dialog);
        if (dialog.getWindow() != null) {
            // 取当前实际屏幕尺寸和方向，无论系统方向变化是否完成都能正确显示
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getRealMetrics(dm);
            boolean actuallyPortrait = dm.widthPixels < dm.heightPixels;
            float wRatio = actuallyPortrait ? 0.88f : 0.58f;
            dialog.getWindow().setLayout((int) (dm.widthPixels * wRatio), (int) (dm.heightPixels * 0.78f));
        }
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            int depth = Math.max(1, Math.min(MAX_STARTUP_SCAN_DEPTH, scanDepthSeek.getProgress() + 1));
            float fontScale = UiScaleUtil.clamp(UiScaleUtil.MIN_FONT_SCALE + fontSeek.getProgress() / 100f);
            String sortMode = SORT_MODE_RECENT;
            int sortSelection = sortSpinner.getSelectedItemPosition();
            if (sortSelection == 1) sortMode = SORT_MODE_NEWEST;
            else if (sortSelection == 2) sortMode = SORT_MODE_NAME;
            prefs.edit()
                    .putInt(KEY_STARTUP_SCAN_DEPTH, depth)
                    .putBoolean(KEY_AUTO_SCAN_ON_STARTUP, autoScanCheck.isChecked())
                    .putString(KEY_ENGINE_LABEL_POSITION, engineLabelSpinner.getSelectedItemPosition() == 1 ? "cover" : "title")
                    .putString(KEY_SORT_MODE, sortMode)
                    .putBoolean(KEY_BACKGROUND_DIM_ENABLED, bgDimEnabled.isChecked())
                .putBoolean(KEY_BACKGROUND_VIDEO_SOUND, bgVideoSound.isChecked())
                .putString(KEY_KR_ENGINE_VERSION, krEngineVersionFromLabel(String.valueOf(krEngineVersion.getSelectedItem())))
.putBoolean(KEY_KR_COMPAT_MODE, krCompatMode.isChecked())
.putBoolean(KEY_KR_SCOPED_SAVE_DIR, krScopedSaveDir.isChecked())
.putBoolean(KEY_ARTEMIS_SCOPED_SAVE_DIR, artemisScopedSaveDir.isChecked())
.putBoolean(KEY_CUTOUT_ENABLED, cutoutCheck.isChecked())
                    .putFloat(UiScaleUtil.KEY_UI_FONT_SCALE, fontScale)
                    .apply();
            applyCustomBackground();
            Toast.makeText(this, "已保存设置，扫描深度：" + depth + " 层，字体：" + UiScaleUtil.percent(fontScale) + "%", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            recreate();
        });
        chooseBgButton.setOnClickListener(v -> {
            dialog.dismiss();
            backgroundPickerLauncher.launch("image/*");
        });
        chooseVideoBgButton.setOnClickListener(v -> {
            dialog.dismiss();
            videoBackgroundPickerLauncher.launch("video/*");
        });
        resetBgButton.setOnClickListener(v -> {
            String oldBg = prefs.getString(KEY_CUSTOM_BACKGROUND, "");
            prefs.edit().remove(KEY_CUSTOM_BACKGROUND).remove(KEY_CUSTOM_BACKGROUND_TYPE).apply();
            deleteInternalFileUri(oldBg);
            applyCustomBackground();
            bgInfo.setText("当前：默认动态背景");
            Toast.makeText(this, "已恢复默认背景", Toast.LENGTH_SHORT).show();
        });
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
            dialog.dismiss();
            scanDirLauncher.launch(null);
        });
    }

    private void openExternalUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Throwable t) {
            Toast.makeText(this, "无法打开链接", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDisclaimerDialog() {
    String text = "免责声明\n\n" +
    "1. 本应用仅用于管理、整理和启动用户本人有权使用的游戏与应用。\n\n" +
    "2. 用户应自行确保所添加资源以及第三方服务的合法性、完整性与可用性。\n\n" +
    "3. 本应用可提供游戏下载链接/信息来源为第三方，用户需自行确认资源的合法性与安全性。\n\n" +
    "4. VNDB、系统存储权限等能力均依赖第三方应用或系统环境，可能因设备、系统版本、权限状态或服务变更而不可用。\n\n" +
    "5. 因第三方服务、系统限制、用户误操作或资源本身问题造成的数据丢失、同步异常、启动失败、兼容性问题或其他损失，开发者不承担额外责任。\n\n" +
    "6. 如果你不同意以上说明，请停止使用相关功能。";
    TextView tv = new TextView(this);
    int pad = dp(18);
    tv.setPadding(pad, pad, pad, pad);
    tv.setTextColor(getColorCompat(R.color.yh_text_muted));
    tv.setTextSize(13);
    tv.setLineSpacing(dp(3), 1.08f);
    tv.setText(text);
    ScrollView scroll = new ScrollView(this);
    scroll.addView(tv);
    AlertDialog dialog = new AlertDialog.Builder(this)
    .setTitle("免责声明")
    .setView(scroll)
    .setPositiveButton("知道了", null)
    .show();
    styleAlertDialogDark(dialog);
    if (dialog.getWindow() != null) {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        boolean realPortrait = dm.widthPixels < dm.heightPixels;
        float wRatio = realPortrait ? 0.70f : 0.48f;
        dialog.getWindow().setLayout((int) (dm.widthPixels * wRatio), (int) (dm.heightPixels * 0.72f));
    }
}

    private String normalizePlayStatus(String status) {
    if (status == null) return "unplayed";
    String s = status.trim().toLowerCase(Locale.ROOT);
    if ("completed".equals(s) || "played".equals(s) || "done".equals(s)) return "completed";
    if ("playing".equals(s) || "current".equals(s)) return "playing";
    return "unplayed";
}

private String playStatusLabel(String status) {
    String s = normalizePlayStatus(status);
    if ("completed".equals(s)) return "🏆 玩过";
    if ("playing".equals(s)) return "🎮 在玩";
    return "☆ 未玩";
}

private int playStatusIndex(String status) {
    String s = normalizePlayStatus(status);
    if ("playing".equals(s)) return 1;
    if ("completed".equals(s)) return 2;
    return 0;
}

private String playStatusFromIndex(int index) {
    if (index == 1) return "playing";
    if (index == 2) return "completed";
    return "unplayed";
}

private void showPlayStatusDialog(Game game, Dialog parentDialog) {
    if (game == null) return;
    String[] labels = new String[]{"☆ 未玩", "🎮 在玩", "🏆 玩过"};
    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setBackgroundResource(R.drawable.bg_dialog);
    root.setPadding(dp(14), dp(8), dp(14), dp(8));
    final AlertDialog[] ref = new AlertDialog[1];
    int selected = playStatusIndex(game.playStatus);
    for (int i = 0; i < labels.length; i++) {
        final int index = i;
        TextView row = new TextView(this);
        row.setText((index == selected ? "●  " : "○  ") + labels[index]);
        row.setTextColor(getColorCompat(index == selected ? R.color.yh_primary : R.color.yh_text));
        row.setTextSize(18);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setBackgroundResource(R.drawable.bg_input);
        row.setPadding(dp(16), 0, dp(16), 0);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48));
        lp.setMargins(0, dp(4), 0, dp(4));
        root.addView(row, lp);
        row.setOnClickListener(v -> {
            game.playStatus = playStatusFromIndex(index);
            repository.update(game);
            Toast.makeText(this, "已标记为：" + playStatusLabel(game.playStatus), Toast.LENGTH_SHORT).show();
            if (ref[0] != null) ref[0].dismiss();
            if (parentDialog != null) parentDialog.dismiss();
            loadGames();
            updateSideDetail(game);
        });
    }
    AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("设置游玩状态")
            .setView(root)
            .setNegativeButton("取消", null)
            .show();
    ref[0] = dialog;
    styleAlertDialogDark(dialog);
    if (dialog.getWindow() != null) {
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getRealMetrics(dm);
            boolean realPortrait = dm.widthPixels < dm.heightPixels;
            float wRatio = realPortrait ? 0.70f : 0.42f;
            dialog.getWindow().setLayout((int) (dm.widthPixels * wRatio), android.view.WindowManager.LayoutParams.WRAP_CONTENT);
    }
}

    // 竖屏单击启动确认弹窗
    private void showPortraitLaunchConfirm(Game game) {
        if (game == null) return;
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.drawable.bg_dialog);
        int pad = dp(18);
        root.setPadding(pad, dp(12), pad, dp(12));

        // 游戏标题
        TextView titleView = new TextView(this);
        titleView.setText(emptyText(game.title, "未命名游戏"));
        titleView.setTextColor(getColorCompat(R.color.yh_text));
        titleView.setTextSize(18);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setGravity(Gravity.CENTER);
        titleView.setPadding(0, 0, 0, dp(8));
        root.addView(titleView);

        // 启动按钮
        Button launchBtn = new Button(this);
        launchBtn.setText("▶ 启动");
        launchBtn.setTextSize(16);
        launchBtn.setTextColor(getColorCompat(R.color.yh_text));
        launchBtn.setBackgroundResource(R.drawable.bg_yuki_button);
        launchBtn.setPadding(0, dp(10), 0, dp(10));
        launchBtn.setAllCaps(false);
        root.addView(launchBtn, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));

        // 取消按钮
        Button cancelBtn = new Button(this);
        cancelBtn.setText("取消");
        cancelBtn.setTextSize(14);
        cancelBtn.setTextColor(getColorCompat(R.color.yh_text_muted));
        cancelBtn.setBackgroundResource(R.drawable.bg_input);
        cancelBtn.setPadding(0, dp(8), 0, dp(8));
        cancelBtn.setAllCaps(false);
        LinearLayout.LayoutParams cancelLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(40));
        cancelLp.setMargins(0, dp(8), 0, 0);
        root.addView(cancelBtn, cancelLp);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(root)
                .show();
        styleAlertDialogDark(dialog);
        if (dialog.getWindow() != null) {
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getRealMetrics(dm);
            boolean realPortrait = dm.widthPixels < dm.heightPixels;
            float wRatio = realPortrait ? 0.62f : 0.38f;
            dialog.getWindow().setLayout((int) (dm.widthPixels * wRatio), android.view.WindowManager.LayoutParams.WRAP_CONTENT);
        }
        launchBtn.setOnClickListener(v -> { dialog.dismiss(); launchGame(game); });
        cancelBtn.setOnClickListener(v -> dialog.dismiss());
    }


    private void showPortraitGameMenu(Game game) {
        if (game == null) return;
        String sourceLabel = metadataSourceLabel();
        String rematchItem = "重新匹配" + sourceLabel;
        String customSearchItem = false ? "自定义搜索Bangumi" : "自定义搜索VNDB";
        String syncItem = "同步" + sourceLabel + "到卡片";
        String playTimeItem = "修改游玩时长";
        String favoriteItem = game.favorite ? "取消收藏" : "收藏游戏";
        String shareItem = "分享游戏";
        String[] items = (game.engine == EngineType.KIRIKIRI || game.engine == EngineType.ONS)
                ? new String[]{"编辑游戏", "设置游玩状态", shareItem, playTimeItem, favoriteItem, rematchItem, customSearchItem, syncItem, "引擎设置", "详细信息", "删除游戏"}
                : new String[]{"编辑游戏", "设置游玩状态", shareItem, playTimeItem, favoriteItem, rematchItem, customSearchItem, syncItem, "详细信息", "删除游戏"};
        LinearLayout listRoot = new LinearLayout(this);
        listRoot.setOrientation(LinearLayout.VERTICAL);
        listRoot.setBackgroundResource(R.drawable.bg_dialog);
        int hp = dp(18);
        listRoot.setPadding(0, dp(6), 0, dp(6));
        final AlertDialog[] ref = new AlertDialog[1];
        for (String item : items) {
            TextView row = new TextView(this);
            row.setText(item);
            row.setTextColor(getColorCompat("删除游戏".equals(item) ? R.color.yh_secondary : R.color.yh_text));
            row.setTextSize(15);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(hp, 0, hp, 0);
            row.setBackgroundResource(R.drawable.bg_input);
            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(46));
            rlp.setMargins(dp(10), dp(4), dp(10), dp(4));
            listRoot.addView(row, rlp);
            row.setOnClickListener(v -> {
                if (ref[0] != null) ref[0].dismiss();
                String chosen = ((TextView) v).getText().toString();
                if ("编辑游戏".equals(chosen)) showEditDialog(game);
                else if ("设置游玩状态".equals(chosen)) showPlayStatusDialog(game, null);
                else if (playTimeItem.equals(chosen)) showEditPlayTimeDialog(game);
                else if (favoriteItem.equals(chosen)) {
                    game.favorite = !game.favorite;
                    repository.update(game);
                    loadGames();
                    Toast.makeText(this, game.favorite ? "已收藏" : "已取消收藏", Toast.LENGTH_SHORT).show();
                }
                else if (shareItem.equals(chosen)) shareGame(game);
                else if (rematchItem.equals(chosen)) {
                    selectedGame = game;
                    if (metadataRepository != null) {
                        metadataRepository.clearVndb(game.id);
                    }
                    fetchSelectedMetadata(game, true);
                }
                else if (customSearchItem.equals(chosen)) { selectedGame = game; showCustomVndbSearchDialog(game); }
                else if (syncItem.equals(chosen)) { selectedGame = game; syncVndbToGameCard(game); }
                else if ("引擎设置".equals(chosen)) { if (game.engine == EngineType.ONS) showOnsSettingsDialog(game); else showKrSettingsDialog(game); }
                else if ("详细信息".equals(chosen)) showDetailDialog(game);
                else if ("删除游戏".equals(chosen)) confirmDeleteGame(game);
            });
        }
        ScrollView optionScroll = new ScrollView(this);
        optionScroll.setFillViewport(false);
        optionScroll.setBackgroundResource(R.drawable.bg_dialog);
        optionScroll.addView(listRoot, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
        AlertDialog optionDialog = new AlertDialog.Builder(this)
                .setTitle(emptyText(game.title, "游戏选项"))
                .setView(optionScroll)
                .show();
        ref[0] = optionDialog;
        styleAlertDialogDark(optionDialog);
        if (optionDialog.getWindow() != null) {
            DisplayMetrics dmOpt = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getRealMetrics(dmOpt);
            boolean optPortrait = dmOpt.widthPixels < dmOpt.heightPixels;
            float optRatio = optPortrait ? 0.72f : 0.48f;
            optionDialog.getWindow().setLayout((int) (dmOpt.widthPixels * optRatio), (int) (dmOpt.heightPixels * 0.78f));
        }
    }

    private void showDetailDialog(Game game) {
        Dialog d = new Dialog(this);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.getWindow();
        d.setOnShowListener(dialog -> {
            applyImmersiveToWindow(d.getWindow());
            enterImmersiveMode();
        });
        d.setOnDismissListener(dialog -> enterImmersiveMode());
        d.setContentView(R.layout.dialog_game_detail);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            d.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.88f), android.view.WindowManager.LayoutParams.WRAP_CONTENT);
            d.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            applyImmersiveToWindow(d.getWindow());
        }

        // 填充基本信息
        ((TextView)d.findViewById(R.id.detailTitle)).setText(emptyText(game.title, "未命名"));
        ((TextView)d.findViewById(R.id.detailOriginalTitle)).setText("");
        ((TextView)d.findViewById(R.id.detailInfo)).setText("状态：" + playStatusLabel(game.playStatus)
                + " · " + game.engine.getDisplayName()
                + " · " + TimeFormatUtil.playTime(game.totalPlayTime)
                + "\n最近：" + TimeFormatUtil.date(game.lastPlayedAt)
                + "\n模拟器：" + getAppLabel(game.emulatorPackage));
        ((TextView)d.findViewById(R.id.detailPath)).setText("路径：" + displayPath(game.rootUri));
        ((TextView)d.findViewById(R.id.detailRating)).setText("评分：-");
        ((TextView)d.findViewById(R.id.detailDeveloper)).setText("开发商：-");
        ((TextView)d.findViewById(R.id.detailDate)).setText("日期：-");
        ((TextView)d.findViewById(R.id.detailLength)).setText("时长：-");

        // 封面
        ImageView cover = d.findViewById(R.id.detailCover);
        TextView ph = d.findViewById(R.id.detailCoverPlaceholder);
        String safeCover = safeCoverUri(game);
        if (safeCover != null && !safeCover.isEmpty()) {
            try {
                Uri u = Uri.parse(safeCover);
                cover.setImageURI(u);
                cover.setVisibility(View.VISIBLE);
                ph.setVisibility(View.GONE);
            } catch (Throwable e) {
                cover.setImageDrawable(null);
                cover.setVisibility(View.GONE);
                ph.setVisibility(View.VISIBLE);
                ph.setText(initials(game.title));
            }
        } else {
            ph.setText(initials(game.title));
        }

        // 从数据库加载VNDB元数据
        VnMetadata meta = metadataRepository != null ? metadataRepository.getVndb(game.id) : null;
        if (meta != null) {
            fillDetailWithMetadata(d, meta, game);
        } else {
            // 没有缓存，异步获取
            fetchDetailMetadata(game, d);
        }

        // 简介展开
        TextView descView = d.findViewById(R.id.detailDescription);
        TextView descToggle = d.findViewById(R.id.detailDescToggle);
        descToggle.setOnClickListener(v -> {
            boolean expanded = descView.getMaxLines() == Integer.MAX_VALUE;
            descView.setMaxLines(expanded ? 4 : Integer.MAX_VALUE);
            descToggle.setText(expanded ? "展开全文" : "收起");
        });

        // 译文切换（需在fillDetailWithMetadata中设置可见性和监听）
        // 先在fillDetailWithMetadata中处理

        // 按钮事件
        d.findViewById(R.id.btnStatus).setOnClickListener(v -> showPlayStatusDialog(game, d));
        d.findViewById(R.id.btnEdit).setOnClickListener(v -> { d.dismiss(); showEditDialog(game); });
        boolean hasEngineSettings = game.engine == EngineType.KIRIKIRI || game.engine == EngineType.ONS;
        d.findViewById(R.id.btnKrSettings).setVisibility(hasEngineSettings ? View.VISIBLE : View.GONE);
        d.findViewById(R.id.btnKrSettings).setOnClickListener(v -> {
            if (game.engine == EngineType.ONS) showOnsSettingsDialog(game); else showKrSettingsDialog(game);
        });
        d.findViewById(R.id.btnDelete).setOnClickListener(v ->
            new AlertDialog.Builder(this)
                .setTitle("删除游戏")
                .setMessage("确定删除 “" + game.title + "”？不会删除本体文件。")
                .setPositiveButton("删除", (x,w)->{ repository.delete(game.id); d.dismiss(); loadGames(); })
                .setNegativeButton("取消", null)
                .show());
        Button launchBtn = d.findViewById(R.id.btnLaunch);
        android.text.SpannableString ss = new android.text.SpannableString("▶启动");
        ss.setSpan(new android.text.style.RelativeSizeSpan(0.4f), 0, 1, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        launchBtn.setText(ss);
        launchBtn.setOnClickListener(v -> launchGame(game));

        d.show();
        applyImmersiveToWindow(d.getWindow());
        enterImmersiveMode();
        if (d.getWindow() != null) {
            d.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.88f), android.view.WindowManager.LayoutParams.WRAP_CONTENT);
            applyImmersiveToWindow(d.getWindow());
            d.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            applyImmersiveToWindow(d.getWindow());
        }
    }

    // 用缓存的元数据填充详情弹窗
    private void fillDetailWithMetadata(Dialog d, VnMetadata meta, Game game) {
        ((TextView)d.findViewById(R.id.detailOriginalTitle)).setText(
            emptyText(meta.originalTitle, emptyText(meta.romanTitle, "")));
        ((TextView)d.findViewById(R.id.detailRating)).setText(emptyText(meta.ratingText, "评分：-/10"));
        ((TextView)d.findViewById(R.id.detailDeveloper)).setText("开发商：" +
            emptyText(meta.developer, "-"));
        ((TextView)d.findViewById(R.id.detailDate)).setText("日期：" +
            emptyText(meta.released, "-"));
        ((TextView)d.findViewById(R.id.detailLength)).setText("时长：" +
            (meta.lengthMinutes > 0 ? TimeFormatUtil.playTime(meta.lengthMinutes * 60000L) : "-"));

        // 简介
        String desc = game != null && game.description != null && !game.description.isEmpty()
            ? game.description : (meta.description != null ? meta.description : "暂无简介");
        TextView descView = d.findViewById(R.id.detailDescription);
        descView.setText(desc);
        TextView descToggle = d.findViewById(R.id.detailDescToggle);
        descToggle.setVisibility(desc.length() > 150 ? View.VISIBLE : View.GONE);

        // 标签
        String tags = meta.tagsText;
        if (tags != null && !tags.isEmpty()) {
            ((TextView)d.findViewById(R.id.detailTags)).setText(tags);
        }

        // 截图
        if (meta.screenshotUrls != null && meta.screenshotUrls.size() > 0) {
            loadRemoteImage(meta.screenshotUrls.get(0),
                (ImageView)d.findViewById(R.id.detailScreenshot1), "dshot1_" + meta.id);
        }
        if (meta.screenshotUrls != null && meta.screenshotUrls.size() > 1) {
            loadRemoteImage(meta.screenshotUrls.get(1),
                (ImageView)d.findViewById(R.id.detailScreenshot2), "dshot2_" + meta.id);
        }

        // 译文切换
        TextView translateToggle = d.findViewById(R.id.detailTranslateToggle);
        boolean hasTranslation = meta.translatedDescription != null && !meta.translatedDescription.isEmpty();
        if (hasTranslation) {
            translateToggle.setVisibility(View.VISIBLE);
            final boolean[] showingTranslation = {false};
            translateToggle.setOnClickListener(v -> {
                showingTranslation[0] = !showingTranslation[0];
                translateToggle.setText(showingTranslation[0] ? "原文" : "译文");
                descView.setText(showingTranslation[0] ? meta.translatedDescription : desc);
                // 如果切换，重置展开状态
                descView.setMaxLines(4);
                descToggle.setText(descView.length() > 150 ? "展开全文" : "");
                descToggle.setVisibility(descView.length() > 150 ? View.VISIBLE : View.GONE);
            });
        }
    }

    // 异步获取VNDB元数据并填充详情
    private void fetchDetailMetadata(Game game, Dialog d) {
        if (metadataRepository == null) return;
        AppExecutors.runOnSingle(() -> {
            try {
                VnMetadata meta = metadataRepository.getVndb(game.id);
                final VnMetadata result = meta;
                runOnUiThread(() -> {
                    if (result != null) {
                        fillDetailWithMetadata(d, result, game);
                    } else {
                        // 没有元数据，显示本地描述
                        if (game.description != null && !game.description.isEmpty()) {
                            ((TextView)d.findViewById(R.id.detailDescription)).setText(game.description);
                        }
                    }
                });
            } catch (Throwable ignored) {}
        });
    }

    // 渲染详情弹窗的标签
    private void renderDetailTagChips(Dialog d, List<String> tags) {
        LinearLayout container = d.findViewById(R.id.detailTagContainer);
        TextView tagsView = d.findViewById(R.id.detailTags);
        if (tags == null || tags.isEmpty()) {
            tagsView.setText("-");
            return;
        }
        // 简化显示：用逗号分隔
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(tags.size(), 20); i++) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(tags.get(i));
        }
        tagsView.setText(sb.toString());
    }

    private void showGameHubShortcutPicker(EditText titleTarget, EditText pkgTarget, EditText gamehubIdTarget) {
        if (requestShizukuPermissionIfNeeded()) return;
        AppExecutors.runOnSingle(() -> {
            try {
                List<GameHubShortcutItem> items = loadGameHubShortcuts();
                runOnUiThread(() -> {
                    if (items.isEmpty()) {
                        new AlertDialog.Builder(this)
                                .setTitle("导入快捷方式")
                                .setMessage("没有读取到可用的 GameHub 快捷方式。\n\n请确认：1）Shizuku 正在运行并已授权；2）GameHub 已创建桌面快捷方式；3）补丁包包名为 com.xiaoji.egggamz 或原包 com.xiaoji.egggame。也可以粘贴 shortcut dump 参数导入。")
                                .setPositiveButton("粘贴参数", (x, w) -> showGameHubShortcutTextImport(titleTarget, pkgTarget, gamehubIdTarget))
                                .setNegativeButton("知道了", null)
                                .show();
                        return;
                    }
                    showGameHubShortcutListDialog(items, titleTarget, pkgTarget, gamehubIdTarget);
                });
            } catch (Throwable t) {
                runOnUiThread(() -> new AlertDialog.Builder(this)
                        .setTitle("导入失败")
                        .setMessage("读取快捷方式失败：" + t.getClass().getSimpleName() + "\n\n如果系统没有授予读取桌面快捷方式的权限，这属于系统限制。")
                        .setPositiveButton("知道了", null)
                        .show());
            }
        });
    }

    private void showGameHubShortcutListDialog(List<GameHubShortcutItem> source, EditText titleTarget, EditText pkgTarget, EditText gamehubIdTarget) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_gamehub_shortcut_picker);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.74f), (int) (getResources().getDisplayMetrics().heightPixels * 0.82f));
        }
        RecyclerView rv = dialog.findViewById(R.id.recyclerGameHubShortcuts);
        EditText search = dialog.findViewById(R.id.etGameHubShortcutSearch);
        TextView hint = dialog.findViewById(R.id.tvGameHubShortcutHint);
        rv.setLayoutManager(new LinearLayoutManager(this));
        Drawable icon = getGameHubIcon();
        for (GameHubShortcutItem item : source) {
            if (item != null && item.icon == null) item.icon = icon;
        }
        final GameHubShortcutAdapter[] adapterRef = new GameHubShortcutAdapter[1];
        adapterRef[0] = new GameHubShortcutAdapter(source, item -> {
            if (item == null) return;
            if (gamehubIdTarget != null) gamehubIdTarget.setText(item.localGameId);
            if (titleTarget != null && (titleTarget.getText() == null || titleTarget.getText().toString().trim().isEmpty())) titleTarget.setText(item.localAppName);
            if (pkgTarget != null && (pkgTarget.getText() == null || pkgTarget.getText().toString().trim().isEmpty())) pkgTarget.setText(guessInstalledGameHubPackage());
            Toast.makeText(this, "已导入 GameHub 快捷方式", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        rv.setAdapter(adapterRef[0]);
        hint.setText("共 " + adapterRef[0].getItemCount() + " 个快捷方式，可搜索游戏名或ID");
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                if (adapterRef[0] == null) return;
                adapterRef[0].filter(s == null ? "" : s.toString());
                hint.setText("共 " + source.size() + " 个快捷方式，当前显示 " + adapterRef[0].getItemCount() + " 个");
            }
            public void afterTextChanged(Editable e) {}
        });
        dialog.findViewById(R.id.btnCloseGameHubShortcutPicker).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.74f), (int) (getResources().getDisplayMetrics().heightPixels * 0.82f));
        }
    }

    private Drawable getGameHubIcon() {
        try { return getPackageManager().getApplicationIcon(guessInstalledGameHubPackage()); } catch (Throwable ignored) { }
        try { return getPackageManager().getApplicationIcon("com.xiaoji.egggame"); } catch (Throwable ignored) { }
        return null;
    }

    private interface GameHubShortcutCallback { void onPick(GameHubShortcutItem item); }

    private class GameHubShortcutAdapter extends RecyclerView.Adapter<GameHubShortcutAdapter.Holder> {
        private final List<GameHubShortcutItem> allItems;
        private final List<GameHubShortcutItem> items = new ArrayList<>();
        private final GameHubShortcutCallback callback;
        GameHubShortcutAdapter(List<GameHubShortcutItem> source, GameHubShortcutCallback callback) {
            this.allItems = source == null ? new ArrayList<>() : new ArrayList<>(source);
            this.items.addAll(this.allItems);
            this.callback = callback;
        }
        void filter(String query) {
            String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
            items.clear();
            if (q.isEmpty()) {
                items.addAll(allItems);
            } else {
                for (GameHubShortcutItem item : allItems) {
                    if (item == null) continue;
                    String label = item.displayLabel == null ? "" : item.displayLabel.toLowerCase(Locale.ROOT);
                    String name = item.localAppName == null ? "" : item.localAppName.toLowerCase(Locale.ROOT);
                    String id = item.localGameId == null ? "" : item.localGameId.toLowerCase(Locale.ROOT);
                    if (label.contains(q) || name.contains(q) || id.contains(q)) items.add(item);
                }
            }
            notifyDataSetChanged();
        }
        @Override public Holder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_picker, parent, false);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, dp(76));
            lp.setMargins(0, 0, 0, dp(8));
            v.setLayoutParams(lp);
            return new Holder(v);
        }
        @Override public void onBindViewHolder(Holder h, int position) {
            GameHubShortcutItem item = items.get(position);
            h.label.setText(emptyText(item.displayLabel, item.localAppName));
            h.id.setText(item.localGameId);
            if (item.icon != null) h.icon.setImageDrawable(item.icon); else h.icon.setImageResource(android.R.mipmap.sym_def_app_icon);
            h.itemView.setOnClickListener(v -> { if (callback != null) callback.onPick(item); });
        }
        @Override public int getItemCount() { return items.size(); }
        class Holder extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView label, id;
            Holder(View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.ivAppIcon);
                label = itemView.findViewById(R.id.tvAppLabel);
                id = itemView.findViewById(R.id.tvAppPackage);
            }
        }
    }

    private void showGameHubShortcutTextImport(EditText titleTarget, EditText pkgTarget, EditText gamehubIdTarget) {
        final EditText input = new EditText(this);
        input.setMinLines(5);
        input.setMaxLines(10);
        input.setGravity(android.view.Gravity.TOP);
        input.setHint("粘贴包含 localGameId=local_xxx 或 steamAppId=123456 的快捷方式参数");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(pad, pad / 2, pad, pad / 2);
        new AlertDialog.Builder(this)
                .setTitle("粘贴 GameHub 快捷方式参数")
                .setView(input)
                .setPositiveButton("导入", (d, w) -> {
                    GameHubShortcutItem item = parseGameHubShortcutText(input.getText() == null ? "" : input.getText().toString());
                    if (item == null || item.localGameId.isEmpty()) {
                        Toast.makeText(this, "未识别到 localGameId 或 steamAppId", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (gamehubIdTarget != null) gamehubIdTarget.setText(item.localGameId);
                    if (titleTarget != null && (titleTarget.getText() == null || titleTarget.getText().toString().trim().isEmpty())) titleTarget.setText(item.localAppName);
                    if (pkgTarget != null && (pkgTarget.getText() == null || pkgTarget.getText().toString().trim().isEmpty())) pkgTarget.setText(guessInstalledGameHubPackage());
                    Toast.makeText(this, "已导入 GameHub 快捷方式参数", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private GameHubShortcutItem parseGameHubShortcutText(String text) {
        if (text == null) return null;
        text = text.replace('\0', ' ');
        String localGameId = matchFirst(text, "localGameId\\s*=\\s*([^,}\\]\\s]+)");
        if (localGameId == null || localGameId.trim().isEmpty()) localGameId = matchFirst(text, "local_[0-9a-fA-F\\-]{8,}");
        String steamAppId = matchFirst(text, "steamAppI[dD]\\s*=\\s*([^,}\\]\\s]+)");
        String storedId = localGameId == null || localGameId.trim().isEmpty() ? null : localGameId.trim();
        if ((storedId == null || storedId.isEmpty()) && steamAppId != null && !steamAppId.trim().isEmpty() && !"0".equals(steamAppId.trim())) storedId = "steam:" + steamAppId.trim();
        if (storedId == null || storedId.trim().isEmpty()) return null;
        String localAppName = matchFirst(text, "localAppName\\s*=\\s*([^,}\\]]+)");
        if (localAppName == null || localAppName.trim().isEmpty()) localAppName = matchFirst(text, "gameName\\s*=\\s*([^,}\\]]+)");
        if (localAppName == null || localAppName.trim().isEmpty()) localAppName = storedId;
        return new GameHubShortcutItem(localAppName.trim(), localAppName.trim(), storedId.trim());
    }

    private String matchFirst(String text, String regex) {
        try {
            Matcher m = Pattern.compile(regex).matcher(text);
            return m.find() ? m.group(1) : null;
        } catch (Throwable ignored) { return null; }
    }

    private List<GameHubShortcutItem> loadGameHubShortcuts() {
        List<GameHubShortcutItem> items = new ArrayList<>();
        items.addAll(loadGameHubShortcutsFromShizuku());
        if (!items.isEmpty()) return items;
        try {
            LauncherApps launcherApps = (LauncherApps) getSystemService(Context.LAUNCHER_APPS_SERVICE);
            if (launcherApps == null) return items;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    if (!launcherApps.hasShortcutHostPermission()) {
                        Log.w("KamiGAL", "LauncherApps shortcut permission missing");
                    }
                } catch (Throwable ignored) { }
            }
            List<ShortcutInfo> shortcuts = new ArrayList<>();
            for (String ghPkg : new String[]{"com.xiaoji.egggamz", "com.xiaoji.egggame"}) {
                try {
                    LauncherApps.ShortcutQuery query = new LauncherApps.ShortcutQuery();
                    query.setPackage(ghPkg);
                    query.setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC | LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED | LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST);
                    List<ShortcutInfo> part = launcherApps.getShortcuts(query, android.os.Process.myUserHandle());
                    if (part != null) shortcuts.addAll(part);
                } catch (Throwable ignored) { }
            }
            if (shortcuts.isEmpty()) return items;
            for (ShortcutInfo si : shortcuts) {
                if (si == null) continue;
                String localGameId = extractGameHubLocalGameId(si);
                if (localGameId == null || localGameId.trim().isEmpty()) continue;
                String localAppName = extractGameHubLocalAppName(si);
                String label = String.valueOf(si.getShortLabel());
                if (label == null || label.trim().isEmpty() || "null".equalsIgnoreCase(label.trim())) label = localAppName;
                if (label == null || label.trim().isEmpty()) label = localGameId;
                Drawable shortcutIcon = null;
                try { shortcutIcon = launcherApps.getShortcutIconDrawable(si, getResources().getDisplayMetrics().densityDpi); } catch (Throwable ignored) { }
                items.add(new GameHubShortcutItem(label, localAppName, localGameId, shortcutIcon));
            }
            items.sort((a, b) -> a.displayLabel.compareToIgnoreCase(b.displayLabel));
        } catch (Throwable t) {
            Log.w("KamiGAL", "loadGameHubShortcuts failed", t);
        }
        if (items.isEmpty()) items.addAll(loadGameHubShortcutsFromExternalLogs());
        return items;
    }

    private boolean requestShizukuPermissionIfNeeded() {
        try {
            if (!Shizuku.pingBinder()) return false;
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) return false;
            Shizuku.requestPermission(62001);
            Toast.makeText(this, "请在 Shizuku 弹窗中授权，授权后再点一次导入快捷方式", Toast.LENGTH_LONG).show();
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private List<GameHubShortcutItem> loadGameHubShortcutsFromShizuku() {
        List<GameHubShortcutItem> items = new ArrayList<>();
        try {
            if (!Shizuku.pingBinder() || Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) return items;
            String cmd = "cmd shortcut get-shortcuts --user 0 --flags 31 com.xiaoji.egggamz; cmd shortcut get-shortcuts --user 0 --flags 31 com.xiaoji.egggame";
            Process p = null;
            try {
                Method m = Shizuku.class.getDeclaredMethod("newProcess", String[].class, String[].class, String.class);
                m.setAccessible(true);
                p = (Process) m.invoke(null, new Object[]{new String[]{"/system/bin/sh", "-c", cmd}, null, null});
            } catch (Throwable reflectError) {
                // Shizuku newProcess 反射失败（可能是隐藏API限制），尝试直接 Runtime.exec
                try {
                    p = Runtime.getRuntime().exec(new String[]{"/system/bin/sh", "-c", cmd});
                } catch (Throwable e2) {
                    throw new RuntimeException("Shizuku newProcess and Runtime.exec both unavailable", reflectError);
                }
            }
            String out = readProcessStream(p.getInputStream()) + "\n" + readProcessStream(p.getErrorStream());
            try { p.waitFor(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            java.util.HashSet<String> seen = new java.util.HashSet<>();
            String[] lines = out.split("\\r?\\n");
            for (String line : lines) {
                if (line == null || (!line.contains("localGameId") && !line.contains("local_") && !line.contains("steamAppId") && !line.contains("steamAppid"))) continue;
                GameHubShortcutItem item = parseGameHubShortcutText(line);
                if (item == null || item.localGameId == null || item.localGameId.isEmpty() || seen.contains(item.localGameId)) continue;
                seen.add(item.localGameId);
                items.add(item);
            }
            items.sort((a, b) -> a.displayLabel.compareToIgnoreCase(b.displayLabel));
        } catch (Throwable t) {
            Log.w("KamiGAL", "loadGameHubShortcutsFromShizuku failed", t);
        }
        return items;
    }

    private String readProcessStream(InputStream in) {
        if (in == null) return "";
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) >= 0) bos.write(buf, 0, n);
            return bos.toString("UTF-8");
        } catch (Throwable ignored) {
            return "";
        }
    }

    private List<GameHubShortcutItem> loadGameHubShortcutsFromExternalLogs() {
        List<GameHubShortcutItem> items = new ArrayList<>();
        String[] roots = new String[]{
                "/sdcard/Android/data/com.xiaoji.egggamz/files/log",
                "/sdcard/Android/data/com.xiaoji.egggamz/files/logs",
                "/sdcard/Android/data/com.xiaoji.egggamz/files/Documents/XiaoKunLogcat",
                "/sdcard/Android/data/com.xiaoji.egggame/files/log",
                "/sdcard/Android/data/com.xiaoji.egggame/files/logs",
                "/sdcard/Android/data/com.xiaoji.egggame/files/Documents/XiaoKunLogcat"
        };
        java.util.HashSet<String> seen = new java.util.HashSet<>();
        for (String root : roots) {
            collectGameHubShortcutItemsFromDir(new File(root), items, seen, 2);
        }
        items.sort((a, b) -> a.displayLabel.compareToIgnoreCase(b.displayLabel));
        return items;
    }

    private void collectGameHubShortcutItemsFromDir(File dir, List<GameHubShortcutItem> out, java.util.HashSet<String> seen, int depth) {
        if (dir == null || out == null || seen == null || depth < 0 || !dir.exists()) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f == null) continue;
            if (f.isDirectory()) {
                collectGameHubShortcutItemsFromDir(f, out, seen, depth - 1);
                continue;
            }
            String name = f.getName() == null ? "" : f.getName().toLowerCase(Locale.ROOT);
            if (!(name.endsWith(".txt") || name.endsWith(".log") || name.endsWith(".json") || name.endsWith(".xml"))) continue;
            if (f.length() > 1024L * 1024L * 4L) continue;
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.contains("localGameId") && !line.contains("local_") && !line.contains("steamAppId") && !line.contains("steamAppid")) continue;
                    GameHubShortcutItem item = parseGameHubShortcutText(line);
                    if (item == null || item.localGameId.isEmpty() || seen.contains(item.localGameId)) continue;
                    seen.add(item.localGameId);
                    out.add(item);
                }
            } catch (Throwable ignored) { }
        }
    }

    private String extractGameHubLocalGameId(ShortcutInfo si) {
        if (si == null) return null;
        try {
            Intent[] intents = si.getIntents();
            if (intents != null && intents.length > 0) {
                for (int i = intents.length - 1; i >= 0; i--) {
                    Intent intent = intents[i];
                    if (intent == null) continue;
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        String localGameId = extras.getString("localGameId");
                        if (localGameId != null && !localGameId.trim().isEmpty()) return localGameId.trim();
                    }
                }
            }
        } catch (Throwable ignored) { }
        try {
            PersistableBundle extras = si.getExtras();
            if (extras != null) {
                String localGameId = extras.getString("localGameId");
                if (localGameId != null && !localGameId.trim().isEmpty()) return localGameId.trim();
            }
        } catch (Throwable ignored) { }
        return null;
    }

    private String extractGameHubLocalAppName(ShortcutInfo si) {
        if (si == null) return "";
        try {
            Intent[] intents = si.getIntents();
            if (intents != null && intents.length > 0) {
                for (int i = intents.length - 1; i >= 0; i--) {
                    Intent intent = intents[i];
                    if (intent == null) continue;
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        String name = extras.getString("localAppName");
                        if (name != null && !name.trim().isEmpty()) return name.trim();
                    }
                }
            }
        } catch (Throwable ignored) { }
        try {
            PersistableBundle extras = si.getExtras();
            if (extras != null) {
                String name = extras.getString("localAppName");
                if (name != null && !name.trim().isEmpty()) return name.trim();
            }
        } catch (Throwable ignored) { }
        CharSequence shortLabel = null;
        try { shortLabel = si.getShortLabel(); } catch (Throwable ignored) { }
        return shortLabel == null ? "" : shortLabel.toString();
    }

    private static class GameHubShortcutItem {
        final String displayLabel;
        final String localAppName;
        final String localGameId;
        Drawable icon;
        GameHubShortcutItem(String displayLabel, String localAppName, String localGameId) {
            this(displayLabel, localAppName, localGameId, null);
        }
        GameHubShortcutItem(String displayLabel, String localAppName, String localGameId, Drawable icon) {
            this.displayLabel = displayLabel == null ? "" : displayLabel;
            this.localAppName = localAppName == null ? "" : localAppName;
            this.localGameId = localGameId == null ? "" : localGameId;
            this.icon = icon;
        }
    }

    private void showInstalledAppPicker(EditText target) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_app_picker);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.74f), (int) (getResources().getDisplayMetrics().heightPixels * 0.82f));
        }
        RecyclerView rv = dialog.findViewById(R.id.recyclerAppPicker);
        View loading = dialog.findViewById(R.id.layoutAppLoading);
        TextView hint = dialog.findViewById(R.id.tvAppPickerHint);
        EditText search = dialog.findViewById(R.id.etAppSearch);
        rv.setLayoutManager(new LinearLayoutManager(this));
        dialog.findViewById(R.id.btnCloseAppPicker).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.74f), (int) (getResources().getDisplayMetrics().heightPixels * 0.82f));
        }

        AppExecutors.runOnSingle(() -> {
            List<AppPickItem> items = new ArrayList<>();
            try {
                PackageManager pm = getPackageManager();
                List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                for (ApplicationInfo app : apps) {
                    if (app == null || app.packageName == null) continue;
                    Intent launchIntent = pm.getLaunchIntentForPackage(app.packageName);
                    if (launchIntent == null) continue;
                    String label;
                    try { label = String.valueOf(pm.getApplicationLabel(app)); }
                    catch (Throwable ignored) { label = app.packageName; }
                    Drawable icon = null;
                    try { icon = pm.getApplicationIcon(app); } catch (Throwable ignored) { }
                    items.add(new AppPickItem(label, app.packageName, icon));
                }
                items.sort((a, b) -> a.label.compareToIgnoreCase(b.label));
            } catch (Throwable t) {
                Log.w("KamiGAL", "load installed apps failed", t);
            }
            runOnUiThread(() -> {
                if (!dialog.isShowing()) return;
                loading.setVisibility(View.GONE);
                rv.setVisibility(View.VISIBLE);
                if (items.isEmpty()) {
                    hint.setText("没有找到可启动的应用");
                    return;
                }
                hint.setText("共 " + items.size() + " 个可启动应用，可搜索应用名或包名");
                final AppPickerAdapter[] adapterRef = new AppPickerAdapter[1];
                adapterRef[0] = new AppPickerAdapter(items, item -> {
                    target.setText(item.packageName);
                    dialog.dismiss();
                });
                rv.setAdapter(adapterRef[0]);
                search.addTextChangedListener(new TextWatcher() {
                    public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                    public void onTextChanged(CharSequence s, int st, int b, int c) {
                        if (adapterRef[0] == null) return;
                        adapterRef[0].filter(s == null ? "" : s.toString());
                        hint.setText("共 " + items.size() + " 个应用，当前显示 " + adapterRef[0].getItemCount() + " 个");
                    }
                    public void afterTextChanged(Editable e) {}
                });
            });
        });
    }

    private interface AppPickCallback { void onPick(AppPickItem item); }

    private static class AppPickItem {
        final String label;
        final String packageName;
        final Drawable icon;
        AppPickItem(String label, String packageName, Drawable icon) {
            this.label = label == null ? "" : label;
            this.packageName = packageName == null ? "" : packageName;
            this.icon = icon;
        }
    }

    private class AppPickerAdapter extends RecyclerView.Adapter<AppPickerAdapter.Holder> {
        private final List<AppPickItem> allItems;
        private final List<AppPickItem> items = new ArrayList<>();
        private final AppPickCallback callback;
        AppPickerAdapter(List<AppPickItem> items, AppPickCallback callback) {
            this.allItems = items == null ? new ArrayList<>() : new ArrayList<>(items);
            this.items.addAll(this.allItems);
            this.callback = callback;
        }
        void filter(String query) {
            String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
            items.clear();
            if (q.isEmpty()) {
                items.addAll(allItems);
            } else {
                for (AppPickItem item : allItems) {
                    String label = item.label == null ? "" : item.label.toLowerCase(Locale.ROOT);
                    String pkg = item.packageName == null ? "" : item.packageName.toLowerCase(Locale.ROOT);
                    if (label.contains(q) || pkg.contains(q)) items.add(item);
                }
            }
            notifyDataSetChanged();
        }
        @Override public Holder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_picker, parent, false);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, dp(76));
            lp.setMargins(0, 0, 0, dp(8));
            v.setLayoutParams(lp);
            return new Holder(v);
        }
        @Override public void onBindViewHolder(Holder h, int position) {
            AppPickItem item = items.get(position);
            h.label.setText(emptyText(item.label, item.packageName));
            h.pkg.setText(item.packageName);
            if (item.icon != null) h.icon.setImageDrawable(item.icon); else h.icon.setImageResource(android.R.mipmap.sym_def_app_icon);
            h.itemView.setOnClickListener(v -> { if (callback != null) callback.onPick(item); });
        }
        @Override public int getItemCount() { return items.size(); }
        class Holder extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView label, pkg;
            Holder(View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.ivAppIcon);
                label = itemView.findViewById(R.id.tvAppLabel);
                pkg = itemView.findViewById(R.id.tvAppPackage);
            }
        }
    }

private String displayPath(String value) {
        if (value == null || value.trim().isEmpty()) return "未选择游戏目录";
        String s = value.trim();
        if (s.startsWith("file://")) {
            try {
                String path = Uri.parse(s).getPath();
                return path == null || path.isEmpty() ? s.substring("file://".length()) : path;
            } catch (Throwable ignored) {
                return s.substring("file://".length());
            }
        }
        if (s.startsWith("content://")) {
            String path = documentUriToPath(s);
            if (path != null && !path.isEmpty()) return path;
        }
        return s;
    }

    // 从包名获取应用显示名称
    private String getAppLabel(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return "未配置";
        String pkg = packageName.trim().toLowerCase(Locale.ROOT);
        // 处理内部包名
        if (pkg.startsWith("internal.krkr")) return "内置 KRKR";
        if (pkg.startsWith("internal.ons")) return "内置 ONS";
        if (pkg.startsWith("internal.tyrano")) return "内置 Tyrano";
        if (pkg.startsWith("internal.artemis.compat.v2")) return "内置 Artemis 兼容 V2";
        if (pkg.startsWith("internal.artemis.compat")) return "内置 Artemis 兼容";
        if (pkg.startsWith("internal.artemis")) return "内置 Artemis";
        if (pkg.startsWith("com.sakurajima.galsearch")) return "内置引擎";
        try {
            android.content.pm.ApplicationInfo ai = getPackageManager().getApplicationInfo(pkg, 0);
            CharSequence label = getPackageManager().getApplicationLabel(ai);
            return label != null ? label.toString() : pkg;
        } catch (Throwable e) {
            return pkg;
        }
    }

    private String documentUriToPath(String value) {
        try {
            Uri uri = Uri.parse(value);
            String docId = null;
            // DocumentFile.fromTreeUri(...).listFiles() 得到的子目录 URI 通常是：
            // content://.../tree/primary%3AGames/document/primary%3AGames%2FExample
            // 详情页要显示到真正的游戏子目录，所以优先取 documentId，而不是 treeId。
            try {
                docId = DocumentsContract.getDocumentId(uri);
            } catch (Throwable ignored) { }
            if (docId == null || docId.isEmpty()) {
                try {
                    docId = DocumentsContract.getTreeDocumentId(uri);
                } catch (Throwable ignored) { }
            }
            if (docId == null || docId.isEmpty()) {
                docId = uri.getLastPathSegment();
            }
            return documentIdToPath(docId);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private String documentIdToPath(String docId) {
        if (docId == null || docId.trim().isEmpty()) return null;
        String id = Uri.decode(docId.trim());
        // 有些 fallback 可能拿到带前缀的片段，先剥掉 URI 结构前缀；
        // 但不能按最后一个 / 截断，因为 primary:Games/Example 里的 / 是真实路径层级。
        int docPrefix = id.indexOf("/document/");
        if (docPrefix >= 0) id = id.substring(docPrefix + "/document/".length());
        if (id.startsWith("document/")) id = id.substring("document/".length());
        int treePrefix = id.indexOf("/tree/");
        if (treePrefix >= 0) id = id.substring(treePrefix + "/tree/".length());
        if (id.startsWith("tree/")) id = id.substring("tree/".length());
        int colon = id.indexOf(':');
        if (colon < 0) return null;
        String volume = id.substring(0, colon);
        String rel = id.substring(colon + 1);
        if (rel.startsWith("/")) rel = rel.substring(1);
        if ("primary".equalsIgnoreCase(volume)) {
            return rel.isEmpty() ? "/storage/emulated/0" : "/storage/emulated/0/" + rel;
        }
        return rel.isEmpty() ? "/storage/" + volume : "/storage/" + volume + "/" + rel;
    }

    private void showEditDialog(Game game) {
        pendingDirUri = game == null ? null : game.rootUri;
        pendingCoverUri = game == null ? null : game.coverUri;
        Dialog d = new Dialog(this); pendingEditDialog = d;
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(isPortrait ? R.layout.dialog_game_edit_portrait : R.layout.dialog_game_edit);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            // 使用实际屏幕方向计算宽度，避免异步方向未生效时尺寸不准
            DisplayMetrics dmEdit = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getRealMetrics(dmEdit);
            boolean actuallyPortrait = dmEdit.widthPixels < dmEdit.heightPixels;
            float wRatio = actuallyPortrait ? 0.95f : 0.82f;
            d.getWindow().setLayout((int) (dmEdit.widthPixels * wRatio), android.view.WindowManager.LayoutParams.WRAP_CONTENT);
        }
        ((TextView)d.findViewById(R.id.editDialogTitle)).setText(game == null ? "添加游戏" : "编辑游戏");
        EditText title = d.findViewById(R.id.etGameTitle), pkg = d.findViewById(R.id.etEmulatorPackage), desc = d.findViewById(R.id.etDescription);
        EditText gamehubLocalGameId = d.findViewById(R.id.etGameHubLocalGameId);
        Spinner sp = d.findViewById(R.id.spEngine);
        Spinner launchSp = d.findViewById(R.id.spLaunchTarget);
        Spinner winlatorModeSp = d.findViewById(R.id.spWinlatorLaunchMode);
        Spinner gamehubModeSp = d.findViewById(R.id.spGameHubLaunchMode);
        View winlatorAdvancedLayout = d.findViewById(R.id.layoutWinlatorLaunchMode);
        View gamehubLaunchLayout = d.findViewById(R.id.layoutGameHubLaunch);
        View artemisVersionLayout = d.findViewById(R.id.layoutArtemisVersion);
        Button btnArtemisAuto = d.findViewById(R.id.btnArtemisAuto);
        Button btnArtemisStd = d.findViewById(R.id.btnArtemisStd);
        Button btnArtemisCompat = d.findViewById(R.id.btnArtemisCompat);
        Button btnArtemisCompatV2 = d.findViewById(R.id.btnArtemisCompatV2);
        Button btnClearPlayTime = d.findViewById(R.id.btnClearPlayTime);
        TextView tvPlayTimeInfo = d.findViewById(R.id.tvPlayTimeInfo);
        View btnPickEmulatorApp = d.findViewById(R.id.btnPickEmulatorApp);
        View btnResetEmulatorPackage = d.findViewById(R.id.btnResetEmulatorPackage);
        View btnPickGameHubShortcut = d.findViewById(R.id.btnPickGameHubShortcut);
        btnPickGameHubShortcut.setOnClickListener(v -> showGameHubShortcutPicker(title, pkg, gamehubLocalGameId));
        btnPickEmulatorApp.setOnClickListener(v -> showInstalledAppPicker(pkg));
        pkg.setOnClickListener(v -> showInstalledAppPicker(pkg));
        Runnable updateWinlatorAdvanced = () -> {
            String engine = sp.getSelectedItem() == null ? "" : sp.getSelectedItem().toString();
            boolean isWinlator = "WINLATOR".equals(engine) || isWinlatorPackageName(pkg.getText() == null ? "" : pkg.getText().toString());
            winlatorAdvancedLayout.setVisibility(isWinlator ? View.VISIBLE : View.GONE);
            gamehubLaunchLayout.setVisibility("GAMEHUB".equals(engine) ? View.VISIBLE : View.GONE);
        };
        btnResetEmulatorPackage.setOnClickListener(v -> {
            String engine = sp.getSelectedItem() == null ? "" : sp.getSelectedItem().toString();
            String defaultPkg = defaultEmulatorPackageForEngine(engine);
            pkg.setText(defaultPkg);
            if ("GAMEHUB".equals(engine)) gamehubLocalGameId.setText("");
            if ("ARTEMIS".equals(engine)) updateArtemisVersionButtons(defaultPkg, btnArtemisAuto, btnArtemisStd, btnArtemisCompat, btnArtemisCompatV2);
            updateWinlatorAdvanced.run();
            Toast.makeText(this, defaultPkg.isEmpty() ? "已清空默认包名" : "已恢复默认包名：" + defaultPkg, Toast.LENGTH_SHORT).show();
        });

        pkg.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int st, int b, int c) { updateWinlatorAdvanced.run(); }
            public void afterTextChanged(Editable e) {}
        });
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_dark, new String[]{"AUTO", "KIRIKIRI", "ONS", "TYRANO", "ARTEMIS", "WINLATOR", "GAMEHUB", "UNKNOWN"});
        spAdapter.setDropDownViewResource(R.layout.spinner_dropdown_dark);
        sp.setAdapter(spAdapter);
        ArrayAdapter<String> winlatorModeAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_dark, new String[]{"启动到游戏", "启动到程序"});
        winlatorModeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_dark);
        winlatorModeSp.setAdapter(winlatorModeAdapter);
        ArrayAdapter<String> gamehubModeAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_dark, new String[]{"启动到游戏", "启动到程序"});
        gamehubModeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_dark);
        gamehubModeSp.setAdapter(gamehubModeAdapter);
        List<String> launchOptions = buildLaunchOptions(pendingDirUri);
        ArrayAdapter<String> launchAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_dark, launchOptions);
        launchAdapter.setDropDownViewResource(R.layout.spinner_dropdown_dark);
        launchSp.setAdapter(launchAdapter);
        if (game != null) {
            tvPlayTimeInfo.setVisibility(View.VISIBLE);
            tvPlayTimeInfo.setText("总时长：" + TimeFormatUtil.playTime(game.totalPlayTime) + " / 最近游玩：" + TimeFormatUtil.date(game.lastPlayedAt));
            btnClearPlayTime.setVisibility(View.VISIBLE);
            btnClearPlayTime.setOnClickListener(v -> confirmClearPlayTime(game, d));
        }
        btnArtemisAuto.setOnClickListener(v -> { pkg.setText(resolveArtemisPackageFromMarkers(pendingDirUri)); updateArtemisVersionButtons(pkg.getText().toString(), btnArtemisAuto, btnArtemisStd, btnArtemisCompat, btnArtemisCompatV2); });
        btnArtemisStd.setOnClickListener(v -> { pkg.setText("internal.artemis"); updateArtemisVersionButtons(pkg.getText().toString(), btnArtemisAuto, btnArtemisStd, btnArtemisCompat, btnArtemisCompatV2); });
        btnArtemisCompat.setOnClickListener(v -> { pkg.setText("internal.artemis.compat"); updateArtemisVersionButtons(pkg.getText().toString(), btnArtemisAuto, btnArtemisStd, btnArtemisCompat, btnArtemisCompatV2); });
        btnArtemisCompatV2.setOnClickListener(v -> { pkg.setText("internal.artemis.compat.v2"); updateArtemisVersionButtons(pkg.getText().toString(), btnArtemisAuto, btnArtemisStd, btnArtemisCompat, btnArtemisCompatV2); });
        if (game != null) {
            title.setText(game.title); pkg.setText(game.emulatorPackage); gamehubLocalGameId.setText(game.gamehubLocalGameId); updateWinlatorAdvanced.run(); desc.setText(game.description);
            winlatorModeSp.setSelection(winlatorModeIndex(game.winlatorLaunchMode));
            gamehubModeSp.setSelection(gamehubModeIndex(game.gamehubLaunchMode));
            sp.setSelection(engineIndex(game.engine));
            launchSp.setSelection(findLaunchSelection(launchOptions, game.launchTarget));
            ((TextView)d.findViewById(R.id.tvSelectedDir)).setText(emptyText(game.rootUri, "未选择游戏目录"));
            ((TextView)d.findViewById(R.id.tvSelectedCover)).setText(emptyText(game.coverUri, "未选择封面"));
            if (game.engine == EngineType.ARTEMIS) updateArtemisVersionButtons(pkg.getText().toString(), btnArtemisAuto, btnArtemisStd, btnArtemisCompat, btnArtemisCompatV2);
        } else if (pendingDirUri != null) {
            ((TextView)d.findViewById(R.id.tvSelectedDir)).setText(pendingDirUri);
        }
        sp.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String engine = (String) sp.getSelectedItem();
                boolean isArtemis = "ARTEMIS".equals(engine);
                boolean isGameHub = "GAMEHUB".equals(engine);
                artemisVersionLayout.setVisibility(isArtemis ? View.VISIBLE : View.GONE);
                pkg.setVisibility(isArtemis ? View.GONE : View.VISIBLE);
                if ((pkg.getText() == null || pkg.getText().toString().trim().isEmpty()) && "KIRIKIRI".equals(engine)) {
                    pkg.setText("internal.krkr");
                } else if ((pkg.getText() == null || pkg.getText().toString().trim().isEmpty()) && "TYRANO".equals(engine)) {
                    pkg.setText("internal.tyrano");
                } else if ((pkg.getText() == null || pkg.getText().toString().trim().isEmpty()) && "ONS".equals(engine)) {
                    pkg.setText("internal.ons");
                } else if ((pkg.getText() == null || pkg.getText().toString().trim().isEmpty()) && "ARTEMIS".equals(engine)) {
                    pkg.setText("internal.artemis");
                } else if ((pkg.getText() == null || pkg.getText().toString().trim().isEmpty()) && "WINLATOR".equals(engine)) {
                    pkg.setText(guessInstalledWinlatorPackage());
                } else if ((pkg.getText() == null || pkg.getText().toString().trim().isEmpty()) && isGameHub) {
                    pkg.setText(guessInstalledGameHubPackage());
                }
                updateWinlatorAdvanced.run();
                if (isArtemis) updateArtemisVersionButtons(pkg.getText().toString(), btnArtemisAuto, btnArtemisStd, btnArtemisCompat, btnArtemisCompatV2);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
        d.findViewById(R.id.btnPickDir).setOnClickListener(v -> editDirLauncher.launch(null));
        d.findViewById(R.id.btnPickCover).setOnClickListener(v -> coverLauncher.launch("image/*"));
        d.findViewById(R.id.btnCancel).setOnClickListener(v -> d.dismiss());
        d.findViewById(R.id.btnSave).setOnClickListener(v -> {
            if (title.getText().toString().trim().isEmpty()) { Toast.makeText(this, "请填写标题", Toast.LENGTH_SHORT).show(); return; }
            Game g = game == null ? new Game() : game;
            if ((pendingCoverUri == null || pendingCoverUri.isEmpty()) && pendingDirUri != null && !pendingDirUri.isEmpty()) {
                Uri autoCover = findFirstLevelImage(pendingDirUri);
                if (autoCover != null) pendingCoverUri = copyCoverToInternalStorage(autoCover);
            }
            g.title = title.getText().toString().trim(); g.rootUri = pendingDirUri == null ? "" : pendingDirUri; g.coverUri = pendingCoverUri; g.coverPersistUri = pendingCoverUri; g.coverSourceType = pendingCoverUri == null ? 0 : 1;
            g.engine = EngineType.fromString((String) sp.getSelectedItem()); if (g.engine == EngineType.AUTO) g.engine = EngineType.UNKNOWN;
            g.emulatorPackage = pkg.getText().toString().trim();
            g.gamehubLocalGameId = gamehubLocalGameId.getText().toString().trim();
            if (g.engine == EngineType.ARTEMIS) {
                g.emulatorPackage = normalizeArtemisPackage(g.emulatorPackage);
                if (!saveArtemisVersionMarker(g.rootUri, g.emulatorPackage)) {
                    Toast.makeText(this, "保存 Artemis 兼容标记失败", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            if (g.engine == EngineType.ONS && (g.emulatorPackage == null || g.emulatorPackage.trim().isEmpty())) g.emulatorPackage = "internal.ons";
            if (g.engine == EngineType.WINLATOR && (g.emulatorPackage == null || g.emulatorPackage.trim().isEmpty())) g.emulatorPackage = guessInstalledWinlatorPackage();
            if (g.engine == EngineType.GAMEHUB && (g.emulatorPackage == null || g.emulatorPackage.trim().isEmpty())) g.emulatorPackage = guessInstalledGameHubPackage();
            if (g.engine != EngineType.GAMEHUB) g.gamehubLocalGameId = "";
            g.winlatorLaunchMode = (g.engine == EngineType.WINLATOR || isWinlatorPackageName(g.emulatorPackage)) ? winlatorModeValue(winlatorModeSp.getSelectedItemPosition()) : "game";
            g.gamehubLaunchMode = g.engine == EngineType.GAMEHUB ? gamehubModeValue(gamehubModeSp.getSelectedItemPosition()) : "game";
            String selectedLaunchTarget = (String) launchSp.getSelectedItem();
            if (g.engine == EngineType.ARTEMIS || g.engine == EngineType.TYRANO) selectedLaunchTarget = "[游戏目录]";
            if (g.engine == EngineType.GAMEHUB) selectedLaunchTarget = "[GameHub]";
            g.launchTarget = selectedLaunchTarget;
            g.description = desc.getText().toString();
            if (game == null) repository.insert(g); else repository.update(g);
            d.dismiss(); loadGames();
        });
        d.setOnDismissListener(x -> pendingEditDialog = null);
        d.show();
        if (d.getWindow() != null) {
            DisplayMetrics dmAfter = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getRealMetrics(dmAfter);
            boolean portAfter = dmAfter.widthPixels < dmAfter.heightPixels;
            float r = portAfter ? 0.95f : 0.82f;
            d.getWindow().setLayout((int) (dmAfter.widthPixels * r), android.view.WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    private String defaultEmulatorPackageForEngine(String engine) {
        String e = engine == null ? "" : engine.trim().toUpperCase(Locale.ROOT);
        if ("KIRIKIRI".equals(e)) return "internal.krkr";
        if ("TYRANO".equals(e)) return "internal.tyrano";
        if ("ONS".equals(e)) return "internal.ons";
        if ("ARTEMIS".equals(e)) return "internal.artemis";
        if ("WINLATOR".equals(e)) return guessInstalledWinlatorPackage();
        if ("GAMEHUB".equals(e)) return guessInstalledGameHubPackage();
        return "";
    }

    private void updateArtemisVersionButtons(String value, Button auto, Button std, Button compat, Button compatV2) {
String pkg = normalizeArtemisPackage(value);
boolean isCompat = "internal.artemis.compat".equalsIgnoreCase(pkg);
boolean isV2 = "internal.artemis.compat.v2".equalsIgnoreCase(pkg);
boolean isStd = "internal.artemis".equalsIgnoreCase(pkg);
boolean autoMode = false;
auto.setSelected(autoMode);
std.setSelected(isStd);
compat.setSelected(isCompat);
compatV2.setSelected(isV2);
        auto.setAlpha(auto.isSelected() ? 1f : 0.55f);
        std.setAlpha(std.isSelected() ? 1f : 0.55f);
        compat.setAlpha(compat.isSelected() ? 1f : 0.55f);
        compatV2.setAlpha(compatV2.isSelected() ? 1f : 0.55f);
    }

    private String normalizeArtemisPackage(String value) {
String pkg = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
if (pkg.contains("compat.v2") || pkg.contains("compatible_v2") || pkg.endsWith(".2")) return "internal.artemis.compat.v2";
if (pkg.contains("compat")) return "internal.artemis.compat";
return "internal.artemis";
}

private String resolveArtemisPackageFromMarkers(String rootUri) {
try {
DocumentFile dir = gameDir(rootUri);
if (dir != null) {
if (dir.findFile(".compatible_v2") != null || dir.findFile("compatible_v2.ini") != null) return "internal.artemis.compat.v2";
if (dir.findFile(".compatible") != null || dir.findFile("compatible.ini") != null) return "internal.artemis.compat";
}
} catch (Throwable ignored) { }
try {
String path = displayPath(rootUri);
if (path != null && path.startsWith("/")) {
if (new File(path, ".compatible_v2").exists() || new File(path, "compatible_v2.ini").exists()) return "internal.artemis.compat.v2";
if (new File(path, ".compatible").exists() || new File(path, "compatible.ini").exists()) return "internal.artemis.compat";
}
} catch (Throwable ignored) { }
return "internal.artemis";
}

private boolean saveArtemisVersionMarker(String rootUri, String artemisPackage) {
String mode = normalizeArtemisPackage(artemisPackage);
try {
DocumentFile dir = gameDir(rootUri);
if (dir != null) {
DocumentFile c1 = dir.findFile(".compatible");
DocumentFile c2 = dir.findFile(".compatible_v2");
DocumentFile i1 = dir.findFile("compatible.ini");
DocumentFile i2 = dir.findFile("compatible_v2.ini");
if ("internal.artemis".equals(mode)) return true;
if (c1 != null) c1.delete();
if (c2 != null) c2.delete();
if (i1 != null) i1.delete();
if (i2 != null) i2.delete();
if ("internal.artemis.compat".equals(mode)) return dir.createFile("application/octet-stream", ".compatible") != null;
if ("internal.artemis.compat.v2".equals(mode)) return dir.createFile("application/octet-stream", ".compatible_v2") != null;
return true;
}
} catch (Throwable ignored) { }
try {
String path = displayPath(rootUri);
if (path == null || !path.startsWith("/")) return false;
File c1 = new File(path, ".compatible");
File c2 = new File(path, ".compatible_v2");
File i1 = new File(path, "compatible.ini");
File i2 = new File(path, "compatible_v2.ini");
if ("internal.artemis".equals(mode)) return true;
deleteFileQuietly(c1);
deleteFileQuietly(c2);
deleteFileQuietly(i1);
deleteFileQuietly(i2);
if ("internal.artemis.compat".equals(mode)) return c1.exists() || c1.createNewFile();
if ("internal.artemis.compat.v2".equals(mode)) return c2.exists() || c2.createNewFile();
return true;
} catch (Throwable ignored) {
return false;
}
}

private DocumentFile gameDir(String rootUri) {
if (rootUri == null || rootUri.trim().isEmpty()) return null;
if (rootUri.startsWith("/") || rootUri.startsWith("file://")) {
File file = new File(rootUri.startsWith("file://") ? Uri.parse(rootUri).getPath() : rootUri);
return DocumentFile.fromFile(file);
}
return DocumentFile.fromTreeUri(this, Uri.parse(rootUri));
}

private void deleteFileQuietly(File file) {
try {
if (file != null && file.exists()) file.delete();
} catch (Throwable ignored) { }
}

private void confirmClearPlayTime(Game game, Dialog editDialog) {
        if (game == null || game.id <= 0) return;
        new AlertDialog.Builder(this)
                .setTitle("清除游玩时长")
                .setMessage("确定要清除《" + emptyText(game.title, "未命名游戏") + "》的游玩时长吗？\n\n只会清除这个游戏的总时长、最近游玩时间和本地游玩记录，不会删除游戏或封面。同步时也会阻止旧游玩记录再次回流。")
                .setPositiveButton("清除", (dialog, which) -> {
                    repository.clearPlayTimeForGame(game.id);
                    Toast.makeText(this, "已清除该游戏游玩时长", Toast.LENGTH_SHORT).show();
                    if (editDialog != null) editDialog.dismiss();
                    loadGames();
                    updateProfilePanel();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showEditPlayTimeDialog(Game game) {
        if (game == null || game.id <= 0) return;
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.drawable.bg_dialog);
        int pad = dp(16);
        root.setPadding(pad, dp(12), pad, dp(10));

        TextView info = new TextView(this);
        info.setText("当前总时长：" + TimeFormatUtil.playTime(game.totalPlayTime) + "\n最近游玩：" + TimeFormatUtil.date(game.lastPlayedAt));
        info.setTextColor(getColorCompat(R.color.yh_text_muted));
        info.setTextSize(12);
        info.setLineSpacing(dp(2), 1.0f);
        root.addView(info);

        TextView totalLabel = new TextView(this);
        totalLabel.setText("\n设置新的总时长");
        totalLabel.setTextColor(getColorCompat(R.color.yh_text));
        totalLabel.setTextSize(14);
        totalLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(totalLabel);

        EditText totalInput = krEdit("例如 3h 20m / 200m / 7200s / 2.5h", TimeFormatUtil.playTime(game.totalPlayTime));
        totalInput.setText(parseDurationForEdit(game.totalPlayTime));
        root.addView(totalInput, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(42)));

        TextView addLabel = new TextView(this);
        addLabel.setText("\n追加游玩时长");
        addLabel.setTextColor(getColorCompat(R.color.yh_text));
        addLabel.setTextSize(14);
        addLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(addLabel);

        EditText addInput = krEdit("例如 30m / 1h30m / 0.5h", "");
        root.addView(addInput, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(42)));

        TextView hint = new TextView(this);
        hint.setText("说明：上面的“总时长”会直接覆盖该游戏的累计时长；“追加游玩时长”会在当前基础上增加。两者可以二选一，也可以都填。");
        hint.setTextColor(getColorCompat(R.color.yh_text_muted));
        hint.setTextSize(11);
        hint.setLineSpacing(dp(2), 1.0f);
        hint.setPadding(0, dp(8), 0, 0);
        root.addView(hint);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("修改游玩时长")
                .setView(root)
                .setPositiveButton("保存", null)
                .setNegativeButton("取消", null)
                .show();
        styleAlertDialogDark(dialog);
        if (dialog.getWindow() != null) {
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getRealMetrics(dm);
            boolean realPortrait = dm.widthPixels < dm.heightPixels;
            float wRatio = realPortrait ? 0.52f : 0.42f;
            dialog.getWindow().setLayout((int) (dm.widthPixels * wRatio), android.view.WindowManager.LayoutParams.WRAP_CONTENT);
        }
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            Long totalMinutes = parseDurationToMinutes(totalInput.getText() == null ? "" : totalInput.getText().toString().trim());
            Long addMinutes = parseDurationToMinutes(addInput.getText() == null ? "" : addInput.getText().toString().trim());
            if ((totalMinutes == null || totalMinutes < 0) && (addMinutes == null || addMinutes <= 0)) {
                Toast.makeText(this, "请填写有效的时长", Toast.LENGTH_SHORT).show();
                return;
            }
            long currentDuration = Math.max(0L, game.totalPlayTime);
            long finalDuration = currentDuration;
            if (totalMinutes != null && totalMinutes >= 0) {
                finalDuration = totalMinutes * 60_000L;
            }
            if (addMinutes != null && addMinutes > 0) {
                finalDuration += addMinutes * 60_000L;
            }
            repository.setManualPlayTimeForGame(game.id, finalDuration);
            Toast.makeText(this, "游玩时长已更新", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            loadGames();
            updateSideDetail(game);
            updateProfilePanel();
        });
    }

    private Long parseDurationToMinutes(String input) {
        if (input == null) return null;
        String s = input.trim().toLowerCase(Locale.ROOT);
        if (s.isEmpty()) return null;
        try {
            if (s.matches("^\\d+$")) return Long.parseLong(s);
            long totalMs = 0L;
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*([dhms])").matcher(s);
            boolean matched = false;
            while (m.find()) {
                matched = true;
                double value = Double.parseDouble(m.group(1));
                String unit = m.group(2);
                if ("d".equals(unit)) totalMs += (long) (value * 24d * 60d * 60d * 1000d);
                else if ("h".equals(unit)) totalMs += (long) (value * 60d * 60d * 1000d);
                else if ("m".equals(unit)) totalMs += (long) (value * 60d * 1000d);
                else if ("s".equals(unit)) totalMs += (long) (value * 1000d);
            }
            if (!matched) return null;
            return Math.max(0L, totalMs / 60_000L);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private String parseDurationForEdit(long durationMs) {
        if (durationMs <= 0) return "0m";
        long minutes = durationMs / 60_000L;
        long hours = minutes / 60L;
        long remain = minutes % 60L;
        if (hours <= 0) return remain + "m";
        if (remain <= 0) return hours + "h";
        return hours + "h" + remain + "m";
    }

    private int engineIndex(EngineType e) { if (e == EngineType.KIRIKIRI) return 1; if (e == EngineType.ONS) return 2; if (e == EngineType.TYRANO) return 3; if (e == EngineType.ARTEMIS) return 4; if (e == EngineType.WINLATOR) return 5; if (e == EngineType.GAMEHUB) return 6; if (e == EngineType.UNKNOWN) return 7; return 0; }

    private boolean isWinlatorPackageName(String pkg) {
        if (pkg == null) return false;
        String p = pkg.trim().toLowerCase(Locale.ROOT);
        return p.equals("com.winlator")
                || p.startsWith("com.winlator.")
                || p.contains("winlator")
                || p.contains("glibc")
                || p.contains("proot")
                || p.contains("mobox")
                || p.contains("winalator");
    }

    private int winlatorModeIndex(String mode) {
        String m = mode == null ? "game" : mode.trim().toLowerCase(Locale.ROOT);
        if ("program".equals(m) || "normal".equals(m)) return 1;
        return 0;
    }

    private String winlatorModeValue(int index) {
        if (index == 1) return "program";
        return "game";
    }

    private int gamehubModeIndex(String mode) {
        String m = mode == null ? "game" : mode.trim().toLowerCase(Locale.ROOT);
        if ("program".equals(m) || "normal".equals(m)) return 1;
        return 0;
    }

    private String gamehubModeValue(int index) {
        if (index == 1) return "program";
        return "game";
    }

    private void showKrSettingsDialog(Game game) {
        if (game == null || game.rootUri == null || game.rootUri.isEmpty()) {
            Toast.makeText(this, "请先选择游戏目录", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, String> prefs = loadKrPrefs(game.rootUri);
        Dialog dialog = new Dialog(this);
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setBackgroundColor(getColorCompat(com.sakurajima.galsearch.R.color.yh_card));
        int pad = (int) (18 * getResources().getDisplayMetrics().density);
        panel.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("KR 游戏设置");
        title.setTextColor(getColorCompat(com.sakurajima.galsearch.R.color.yh_text));
        title.setTextSize(22);
        title.setPadding(0, 0, 0, pad / 2);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(0, 0, 0, 0);

        CheckBox outputLog = krCheckBox("打印日志", "1".equals(pref(prefs, "outputlog", "1")));
        CheckBox showFps = krCheckBox("显示 FPS", "1".equals(pref(prefs, "showfps", "0")));
        CheckBox keepScreen = krCheckBox("保持屏幕常亮", "1".equals(pref(prefs, "keep_screen_alive", "1")));
        CheckBox forceFont = krCheckBox("强制使用默认字体", "1".equals(pref(prefs, "force_default_font", "0")));
        CheckBox textureCompress = krCheckBox("纹理压缩", "1".equals(pref(prefs, "texture_compress", "0")));
        Spinner renderer = krSpinner(new String[]{"软件渲染器", "OpenGL（试验性）"}, rendererToLabel(pref(prefs, "renderer", "software")));
        Spinner memusage = krSpinner(new String[]{"unlimited", "low", "medium", "high"}, pref(prefs, "memusage", "unlimited"));
        Spinner renderThread = krSpinner(new String[]{"auto", "1", "2", "3", "4", "6", "8"}, pref(prefs, "render_thread", "auto"));
        EditText fpsLimit = krEdit("FPS 限制，例如 60", pref(prefs, "fps_limit", "60"));
        fpsLimit.setInputType(InputType.TYPE_CLASS_NUMBER);
        EditText menuOpa = krEdit("手柄/菜单透明度，例如 0.15", pref(prefs, "menu_handler_opa", "0.15"));
        menuOpa.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText cursorScale = krEdit("虚拟光标缩放，例如 0.5", pref(prefs, "vcursor_scale", "0.5"));
        cursorScale.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText defaultFont = krEdit("默认字体路径，留空使用内置字体", pref(prefs, "default_font", ""));

        root.addView(krLabel("图形渲染器")); root.addView(renderer);
        root.addView(krLabel("内存用量")); root.addView(memusage);
        root.addView(krLabel("渲染线程数")); root.addView(renderThread);
        root.addView(krLabel("限制 FPS")); root.addView(fpsLimit);
        root.addView(krLabel("手柄/菜单透明度")); root.addView(menuOpa);
        root.addView(krLabel("虚拟光标缩放")); root.addView(cursorScale);
        root.addView(outputLog);
        root.addView(showFps);
        root.addView(keepScreen);
        root.addView(textureCompress);
        root.addView(forceFont);
        root.addView(krLabel("默认字体路径")); root.addView(defaultFont);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, pad / 2, 0, 0);
        Button cancel = krButton("取消");
        Button save = krButton("保存");
        actions.addView(cancel, new LinearLayout.LayoutParams(0, (int) (44 * getResources().getDisplayMetrics().density), 1));
        LinearLayout.LayoutParams saveLp = new LinearLayout.LayoutParams(0, (int) (44 * getResources().getDisplayMetrics().density), 1);
        saveLp.leftMargin = pad / 2;
        actions.addView(save, saveLp);

        scroll.addView(root);
        panel.addView(title);
        panel.addView(scroll, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        panel.addView(actions);
        dialog.setContentView(panel);
        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        cancel.setOnClickListener(v -> dialog.dismiss());
        save.setOnClickListener(v -> {
            prefs.put("menu_handler_opa", menuOpa.getText().toString().trim().isEmpty() ? "0.15" : menuOpa.getText().toString().trim());
            prefs.put("vcursor_scale", cursorScale.getText().toString().trim().isEmpty() ? "0.5" : cursorScale.getText().toString().trim());
            prefs.put("renderer", rendererFromLabel(String.valueOf(renderer.getSelectedItem())));
            prefs.put("memusage", String.valueOf(memusage.getSelectedItem()));
            prefs.put("render_thread", String.valueOf(renderThread.getSelectedItem()));
            prefs.put("fps_limit", fpsLimit.getText().toString().trim().isEmpty() ? "60" : fpsLimit.getText().toString().trim());
            prefs.put("outputlog", outputLog.isChecked() ? "1" : "0");
            prefs.put("showfps", showFps.isChecked() ? "1" : "0");
            prefs.put("keep_screen_alive", keepScreen.isChecked() ? "1" : "0");
            prefs.put("texture_compress", textureCompress.isChecked() ? "1" : "0");
            prefs.put("force_default_font", forceFont.isChecked() ? "1" : "0");
            prefs.put("default_font", defaultFont.getText().toString().trim());
            if (saveKrPrefs(game.rootUri, prefs)) {
                Toast.makeText(this, "KR 设置已保存", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "保存 KR 设置失败", Toast.LENGTH_LONG).show();
            }
        });
        dialog.show();
        Window shownWindow = dialog.getWindow();
        if (shownWindow != null) {
            shownWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getRealMetrics(dm);
            boolean realPortrait = dm.widthPixels < dm.heightPixels;
            float wRatio = realPortrait ? 0.72f : 0.48f;
            shownWindow.setLayout((int) (dm.widthPixels * wRatio), (int) (dm.heightPixels * 0.82f));
        }
    }

    private void showOnsSettingsDialog(Game game) {
        OnsSettings settings = OnsSettings.load(this);
        Dialog dialog = new Dialog(this);
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setBackgroundColor(getColorCompat(com.sakurajima.galsearch.R.color.yh_card));
        int pad = (int) (18 * getResources().getDisplayMetrics().density);
        panel.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("ONScripter 设置");
        title.setTextColor(getColorCompat(com.sakurajima.galsearch.R.color.yh_text));
        title.setTextSize(22);
        title.setPadding(0, 0, 0, pad / 2);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        CheckBox stretchFull = krCheckBox("拉伸全屏（--fullscreen2）", settings.stretchFull);
        CheckBox ignoreCutout = krCheckBox("忽略刘海/挖孔区域", settings.ignoreCutout);
        CheckBox disableVideo = krCheckBox("禁用视频播放（--no-video）", settings.disableVideo);
        CheckBox scopedSave = krCheckBox("使用独立存档目录", settings.scopedSaveDir);
        CheckBox allowEditArgs = krCheckBox("允许在详情中编辑启动参数", settings.allowEditArgs);
        CheckBox sharpness = krCheckBox("启用锐化（--sharpness）", settings.sharpness);
        EditText sharpnessValue = krEdit("锐化值，例如 2", settings.sharpnessValue);
        sharpnessValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        Spinner encoding = krSpinner(new String[]{"gbk", "sjis", "utf8"}, settings.encoding);

        root.addView(krLabel("文本编码")); root.addView(encoding);
        root.addView(stretchFull);
        root.addView(ignoreCutout);
        root.addView(disableVideo);
        root.addView(scopedSave);
        root.addView(allowEditArgs);
        root.addView(sharpness);
        root.addView(krLabel("锐化值")); root.addView(sharpnessValue);

        TextView tip = krLabel("说明：设置会生成 OnsYuri 原版参数：--root、--font、--fullscreen/--fullscreen2、--enc、--save-dir 等。修改后下次启动 ONS 游戏生效。");
        tip.setTextColor(getColorCompat(com.sakurajima.galsearch.R.color.yh_text_muted));
        root.addView(tip);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, pad / 2, 0, 0);
        Button cancel = krButton("取消");
        Button save = krButton("保存");
        actions.addView(cancel, new LinearLayout.LayoutParams(0, (int) (44 * getResources().getDisplayMetrics().density), 1));
        LinearLayout.LayoutParams saveLp = new LinearLayout.LayoutParams(0, (int) (44 * getResources().getDisplayMetrics().density), 1);
        saveLp.leftMargin = pad / 2;
        actions.addView(save, saveLp);

        scroll.addView(root);
        panel.addView(title);
        panel.addView(scroll, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        panel.addView(actions);
        dialog.setContentView(panel);
        cancel.setOnClickListener(v -> dialog.dismiss());
        save.setOnClickListener(v -> {
            settings.stretchFull = stretchFull.isChecked();
            settings.ignoreCutout = ignoreCutout.isChecked();
            settings.disableVideo = disableVideo.isChecked();
            settings.scopedSaveDir = scopedSave.isChecked();
            settings.allowEditArgs = allowEditArgs.isChecked();
            settings.sharpness = sharpness.isChecked();
            settings.sharpnessValue = sharpnessValue.getText().toString().trim().isEmpty() ? "2" : sharpnessValue.getText().toString().trim();
            settings.encoding = OnsSettings.normalizeEncoding(String.valueOf(encoding.getSelectedItem()));
            settings.save(this);
            if (game != null && (game.emulatorPackage == null || game.emulatorPackage.trim().isEmpty())) {
                game.emulatorPackage = "internal.ons";
                repository.update(game);
                loadGames();
            }
            Toast.makeText(this, "ONS 设置已保存", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        dialog.show();
        Window shownWindow = dialog.getWindow();
        if (shownWindow != null) {
            shownWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getRealMetrics(dm);
            boolean realPortrait = dm.widthPixels < dm.heightPixels;
            float wRatio = realPortrait ? 0.72f : 0.48f;
            shownWindow.setLayout((int) (dm.widthPixels * wRatio), (int) (dm.heightPixels * 0.82f));
        }
    }

    private TextView krLabel(String text) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextSize(13);
        v.setTextColor(getColorCompat(com.sakurajima.galsearch.R.color.yh_text));
        v.setPadding(0, 10, 0, 4);
        return v;
    }

    private CheckBox krCheckBox(String text, boolean checked) {
        CheckBox v = new CheckBox(this);
        v.setText(text);
        v.setChecked(checked);
        v.setTextColor(getColorCompat(com.sakurajima.galsearch.R.color.yh_text));
        v.setButtonTintList(android.content.res.ColorStateList.valueOf(getColorCompat(com.sakurajima.galsearch.R.color.yh_primary)));
        return v;
    }

    private Button krButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextColor(getColorCompat(com.sakurajima.galsearch.R.color.yh_text));
        b.setBackgroundColor(getColorCompat(com.sakurajima.galsearch.R.color.yh_card_2));
        return b;
    }

    private LinearLayout linkCardButton(String text, int iconResId) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), 0, dp(16), 0);
        row.setBackgroundResource(R.drawable.bg_auth_tab_inactive);
        row.setMinimumHeight(dp(48));
        ImageView icon = new ImageView(this);
        try {
            Drawable d = getDrawable(iconResId);
            if (d != null) {
                d = d.mutate();
                d.setTint(getColorCompat(com.sakurajima.galsearch.R.color.yh_primary));
                icon.setImageDrawable(d);
            }
        } catch (Throwable ignored) { }
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(24), dp(24));
        iconLp.rightMargin = dp(10);
        row.addView(icon, iconLp);
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextColor(getColorCompat(com.sakurajima.galsearch.R.color.yh_text));
        label.setTextSize(14);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        label.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        row.addView(label, labelLp);
        return row;
    }

    private EditText krEdit(String hint, String value) {
        EditText v = new EditText(this);
        v.setHint(hint);
        v.setSingleLine(true);
        v.setText(value);
        v.setTextColor(getColorCompat(com.sakurajima.galsearch.R.color.yh_text));
        v.setHintTextColor(getColorCompat(com.sakurajima.galsearch.R.color.yh_text_muted));
        v.setBackgroundColor(getColorCompat(com.sakurajima.galsearch.R.color.yh_card_2));
        v.setPadding(12, 0, 12, 0);
        return v;
    }

    private Spinner krSpinner(String[] values, String selected) {
        Spinner sp = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item_dark, values);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_dark);
        sp.setAdapter(adapter);
        for (int i = 0; i < values.length; i++) if (values[i].equalsIgnoreCase(selected)) { sp.setSelection(i); break; }
        return sp;
    }

    private int getColorCompat(int id) {
        if (Build.VERSION.SDK_INT >= 23) return getColor(id);
        return getResources().getColor(id);
    }

    private String rendererToLabel(String value) {
        if (value == null) return "软件渲染器";
        String v = value.trim().toLowerCase(Locale.ROOT);
        if ("opengl".equals(v) || "open_gl".equals(v) || "gl".equals(v) || "hardware".equals(v)) return "OpenGL（试验性）";
        return "软件渲染器";
    }

    private String rendererFromLabel(String label) {
if (label != null && label.toLowerCase(Locale.ROOT).contains("opengl")) return "opengl";
return "software";
}

private String krEngineVersionToLabel(String value) {
String mode = normalizeKrEngineVersion(value);
if ("1.3.4".equals(mode)) return "1.3.4";
if ("1.3.9".equals(mode)) return "1.3.9";
return "自动";
}

private String krEngineVersionFromLabel(String label) {
if (label == null) return "auto";
String v = label.trim();
if (v.contains("1.3.4")) return "1.3.4";
if (v.contains("1.3.9")) return "1.3.9";
return "auto";
}

private String normalizeKrEngineVersion(String value) {
String v = value == null ? "auto" : value.trim().toLowerCase(Locale.ROOT);
if ("134".equals(v) || "1.3.4".equals(v) || "kr134".equals(v)) return "1.3.4";
if ("139".equals(v) || "1.3.9".equals(v) || "kr139".equals(v)) return "1.3.9";
return "auto";
}

private String pref(Map<String, String> prefs, String key, String def) {
        String v = prefs.get(key);
        return v == null ? def : v;
    }

    private List<String> buildLaunchOptions(String rootUri) {
        List<String> options = new ArrayList<>();
        if (rootUri != null && !rootUri.isEmpty()) {
            String directPath = displayPath(rootUri);
            if (directPath != null && directPath.toLowerCase(Locale.ROOT).endsWith(".desktop")) {
                String name = directPath.substring(Math.max(directPath.lastIndexOf('/'), directPath.lastIndexOf('\\')) + 1);
                if (!name.isEmpty() && !options.contains(name)) options.add(name);
            }
            try {
                DocumentFile dir = DocumentFile.fromTreeUri(this, Uri.parse(rootUri));
                if (dir != null && dir.isDirectory()) {
                    DocumentFile[] files = dir.listFiles();
                    if (files != null) {
                        for (DocumentFile file : files) {
                            String name = file.getName();
                            if (name == null || !file.isFile()) continue;
                            String lower = name.toLowerCase(Locale.ROOT);
                            if (lower.endsWith(".xp3") || lower.endsWith(".tjs") || lower.endsWith(".ks") || lower.endsWith(".html") || lower.endsWith(".txt") || lower.endsWith(".dat") || lower.endsWith(".pfs") || lower.endsWith(".desktop")) {
                                if (!options.contains(name)) options.add(name);
                            }
                        }
                    }
                }
            } catch (Exception ignored) { }
        }
        if (options.contains("data.xp3")) {
            options.remove("data.xp3");
            options.add(0, "data.xp3");
        }
        if (options.contains("[游戏目录]")) options.remove("[游戏目录]");
        options.add("[游戏目录]");
        if (options.isEmpty()) options.add("未扫描到可启动文件，请先选择目录");
        return options;
    }

    private int findLaunchSelection(List<String> options, String target) {
        if (options == null || options.isEmpty()) return 0;
        if (target == null || target.trim().isEmpty()) target = "[游戏目录]";
        for (int i = 0; i < options.size(); i++) {
            if (target.equals(options.get(i))) return i;
        }
        int dirIndex = options.indexOf("[游戏目录]");
        return dirIndex >= 0 ? dirIndex : 0;
    }

    private Map<String, String> loadKrPrefs(String rootUri) {
        Map<String, String> prefs = defaultKrPrefs();
        try (InputStream in = openKrPrefsInput(rootUri)) {
            if (in == null) return prefs;
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
            NodeList items = doc.getElementsByTagName("Item");
            for (int i = 0; i < items.getLength(); i++) {
                if (!(items.item(i) instanceof Element)) continue;
                Element item = (Element) items.item(i);
                String key = item.getAttribute("key");
                if (key == null || key.isEmpty()) continue;
                prefs.put(key, item.getAttribute("value"));
            }
        } catch (Throwable ignored) { }
        return prefs;
    }

    private Map<String, String> defaultKrPrefs() {
        Map<String, String> prefs = new LinkedHashMap<>();
        prefs.put("menu_handler_opa", "0.15");
        prefs.put("vcursor_scale", "0.5");
        prefs.put("force_default_font", "0");
        prefs.put("default_font", "");
        prefs.put("renderer", "software");
        prefs.put("memusage", "unlimited");
        prefs.put("render_thread", "auto");
        prefs.put("texture_compress", "0");
        prefs.put("fps_limit", "60");
        prefs.put("keep_screen_alive", "1");
        prefs.put("showfps", "0");
        prefs.put("outputlog", "1");
        return prefs;
    }

    // KR 引擎版本只保留全局设置，不再通过单个游戏目录的 .1.3.4 标记读写，避免与右上角设置冲突。

    private DocumentFile krGameDir(String rootUri) {
        if (rootUri == null || rootUri.trim().isEmpty()) return null;
        if (rootUri.startsWith("/") || rootUri.startsWith("file://")) {
            File file = new File(rootUri.startsWith("file://") ? Uri.parse(rootUri).getPath() : rootUri);
            return DocumentFile.fromFile(file);
        }
        return DocumentFile.fromTreeUri(this, Uri.parse(rootUri));
    }

    private InputStream openKrPrefsInput(String rootUri) {
        try {
            if (rootUri == null || rootUri.isEmpty()) return null;
            if (rootUri.startsWith("/") || rootUri.startsWith("file://")) {
                File f = new File(rootUri.startsWith("file://") ? Uri.parse(rootUri).getPath() : rootUri, "Kirikiroid2Preference.xml");
                return f.exists() ? new FileInputStream(f) : null;
            }
            DocumentFile dir = DocumentFile.fromTreeUri(this, Uri.parse(rootUri));
            DocumentFile file = dir == null ? null : dir.findFile("Kirikiroid2Preference.xml");
            return file == null || !file.isFile() ? null : getContentResolver().openInputStream(file.getUri());
        } catch (Throwable ignored) { return null; }
    }

    private boolean saveKrPrefs(String rootUri, Map<String, String> prefs) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = doc.createElement("GlobalPreference");
            doc.appendChild(root);
            for (Map.Entry<String, String> e : prefs.entrySet()) {
                Element item = doc.createElement("Item");
                item.setAttribute("key", e.getKey());
                item.setAttribute("value", e.getValue() == null ? "" : e.getValue());
                root.appendChild(item);
            }
            try (OutputStream out = openKrPrefsOutput(rootUri)) {
                if (out == null) return false;
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.transform(new DOMSource(doc), new StreamResult(out));
            }
            return true;
        } catch (Throwable ignored) { return false; }
    }

    private OutputStream openKrPrefsOutput(String rootUri) {
        try {
            if (rootUri == null || rootUri.isEmpty()) return null;
            if (rootUri.startsWith("/") || rootUri.startsWith("file://")) {
                File dir = new File(rootUri.startsWith("file://") ? Uri.parse(rootUri).getPath() : rootUri);
                if (!dir.exists() && !dir.mkdirs()) return null;
                return new FileOutputStream(new File(dir, "Kirikiroid2Preference.xml"));
            }
            DocumentFile dir = DocumentFile.fromTreeUri(this, Uri.parse(rootUri));
            if (dir == null || !dir.isDirectory()) return null;
            DocumentFile file = dir.findFile("Kirikiroid2Preference.xml");
            if (file == null) file = dir.createFile("text/xml", "Kirikiroid2Preference.xml");
            return file == null ? null : getContentResolver().openOutputStream(file.getUri(), "wt");
        } catch (Throwable ignored) { return null; }
    }
private void showScanResults(List<ScanResult> results) {
        if (results.isEmpty()) { Toast.makeText(this, "未发现子目录候选游戏", Toast.LENGTH_LONG).show(); return; }
        Dialog d = new Dialog(this); d.requestWindowFeature(Window.FEATURE_NO_TITLE); d.setContentView(R.layout.dialog_scan_result);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            d.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.88f), android.view.WindowManager.LayoutParams.WRAP_CONTENT);
        }
        RecyclerView rv = d.findViewById(R.id.recyclerScanResults); rv.setLayoutManager(new LinearLayoutManager(this)); rv.setAdapter(new ScanResultAdapter(results));
        ((TextView)d.findViewById(R.id.tvScanTitle)).setText("扫描结果：" + results.size() + " 个候选游戏");
        d.findViewById(R.id.btnCancelScan).setOnClickListener(v -> d.dismiss());
        d.findViewById(R.id.btnImportScan).setOnClickListener(v -> {
            ScanImportStats stats = importScannedGames(results);
            if (stats.added > 0) AppExecutors.runOnIo(() -> autoMatchVndbForImportedGames(stats.importedGames));
            d.dismiss();
            loadGames();
            Toast.makeText(this, "新增 " + stats.added + " 个，已存在 " + stats.skipped + " 个" + (stats.added > 0 ? "，正在自动匹配 VNDB 封面" : ""), Toast.LENGTH_SHORT).show();
        });
        d.show();
    }

    private void runLibraryScan(Uri rootUri, boolean showToast) {
if (rootUri == null) return;
if (autoLibraryScanRunning) return;
autoLibraryScanRunning = true;
runOnUiThread(() -> setScanLoading(true));
if (showToast) Toast.makeText(this, "正在扫描，请稍候...", Toast.LENGTH_SHORT).show();
        int scanDepth = prefs == null ? DEFAULT_STARTUP_SCAN_DEPTH : prefs.getInt(KEY_STARTUP_SCAN_DEPTH, DEFAULT_STARTUP_SCAN_DEPTH);
        scanDepth = Math.max(1, Math.min(MAX_STARTUP_SCAN_DEPTH, scanDepth));
        final int finalScanDepth = scanDepth;
        AppExecutors.runOnSingle(() -> {
            List<ScanResult> results;
            try {
                results = GameScanner.scan(this, rootUri, finalScanDepth);
            } catch (Throwable t) {
                Log.w("KamiGAL", "library scan failed", t);
                results = new ArrayList<>();
            }
            ScanImportStats stats = importScannedGames(results);
            if (stats.added > 0) AppExecutors.runOnIo(() -> autoMatchVndbForImportedGames(stats.importedGames));
            runOnUiThread(() -> {
                autoLibraryScanRunning = false;
                setScanLoading(false);
                loadGames();
                if (showToast) Toast.makeText(this, "新增 " + stats.added + " 个，已存在 " + stats.skipped + " 个" + (stats.added > 0 ? "，正在自动匹配 VNDB 封面" : ""), Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void runLibraryScan(List<String> rootUris, boolean showToast) {
        if (rootUris == null || rootUris.isEmpty()) return;
        if (autoLibraryScanRunning) return;
        autoLibraryScanRunning = true;
        runOnUiThread(() -> setScanLoading(true));
        if (showToast) Toast.makeText(this, "正在扫描 " + rootUris.size() + " 个目录，请稍候...", Toast.LENGTH_SHORT).show();
        int scanDepth = prefs == null ? DEFAULT_STARTUP_SCAN_DEPTH : prefs.getInt(KEY_STARTUP_SCAN_DEPTH, DEFAULT_STARTUP_SCAN_DEPTH);
        scanDepth = Math.max(1, Math.min(MAX_STARTUP_SCAN_DEPTH, scanDepth));
        final int finalScanDepth = scanDepth;
        final List<String> scanRoots = new ArrayList<>(rootUris);
        AppExecutors.runOnSingle(() -> {
            List<ScanResult> results = new ArrayList<>();
            for (String root : scanRoots) {
                if (root == null || root.trim().isEmpty()) continue;
                try {
                    results.addAll(GameScanner.scan(this, Uri.parse(root), finalScanDepth));
                } catch (Throwable t) {
                    Log.w("KamiGAL", "library scan failed root=" + root, t);
                }
            }
            ScanImportStats stats = importScannedGames(results);
            if (stats.added > 0) AppExecutors.runOnIo(() -> autoMatchVndbForImportedGames(stats.importedGames));
            runOnUiThread(() -> {
                autoLibraryScanRunning = false;
                setScanLoading(false);
                loadGames();
                if (showToast) Toast.makeText(this, "扫描 " + scanRoots.size() + " 个目录：新增 " + stats.added + " 个，已存在 " + stats.skipped + " 个" + (stats.added > 0 ? "，正在自动匹配 VNDB 封面" : ""), Toast.LENGTH_SHORT).show();
            });
        });
    }

    private List<String> getScanRootUris() {
        List<String> roots = new ArrayList<>();
        if (prefs == null) return roots;
        String joined = prefs.getString(KEY_SCAN_ROOT_URIS, "");
        if (joined != null && !joined.trim().isEmpty()) {
            for (String part : joined.split("\\n")) {
                String s = part == null ? "" : part.trim();
                if (!s.isEmpty() && !roots.contains(s)) roots.add(s);
                if (roots.size() >= MAX_SCAN_ROOTS) break;
            }
        }
        String legacy = prefs.getString(KEY_LAST_SCAN_ROOT_URI, "");
        if (roots.isEmpty() && legacy != null && !legacy.trim().isEmpty()) roots.add(legacy.trim());
        return roots;
    }

    private void saveScanRootUris(List<String> roots) {
        if (prefs == null) return;
        List<String> cleaned = new ArrayList<>();
        if (roots != null) {
            for (String r : roots) {
                String s = r == null ? "" : r.trim();
                if (!s.isEmpty() && !cleaned.contains(s)) cleaned.add(s);
                if (cleaned.size() >= MAX_SCAN_ROOTS) break;
            }
        }
        StringBuilder joined = new StringBuilder();
        for (String r : cleaned) {
            if (joined.length() > 0) joined.append('\n');
            joined.append(r);
        }
        SharedPreferences.Editor e = prefs.edit().putString(KEY_SCAN_ROOT_URIS, joined.toString());
        if (!cleaned.isEmpty()) e.putString(KEY_LAST_SCAN_ROOT_URI, cleaned.get(0)); else e.remove(KEY_LAST_SCAN_ROOT_URI);
        e.apply();
    }

    private boolean addOrReplaceScanRoot(String uri, int replaceIndex) {
        if (uri == null || uri.trim().isEmpty()) return false;
        List<String> roots = getScanRootUris();
        String value = uri.trim();
        roots.remove(value);
        if (replaceIndex >= 0 && replaceIndex < roots.size()) roots.set(replaceIndex, value);
        else if (roots.size() < MAX_SCAN_ROOTS) roots.add(value);
        else {
            Toast.makeText(this, "最多绑定 " + MAX_SCAN_ROOTS + " 个扫描目录", Toast.LENGTH_SHORT).show();
            return false;
        }
        saveScanRootUris(roots);
        return true;
    }

    private void removeScanRootAt(int index) {
        List<String> roots = getScanRootUris();
        if (index < 0 || index >= roots.size()) return;
        roots.remove(index);
        saveScanRootUris(roots);
    }

    private String scanRootsSummary() {
        List<String> roots = getScanRootUris();
        if (roots.isEmpty()) return "未绑定";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < roots.size(); i++) {
            if (i > 0) sb.append('\n');
            sb.append(i + 1).append(". ").append(roots.get(i));
        }
        return sb.toString();
    }

    private String compactUriLabel(String uri) {
        if (uri == null || uri.trim().isEmpty()) return "未绑定";
        String s = uri.trim();
        try {
            Uri u = Uri.parse(s);
            String last = u.getLastPathSegment();
            if (last != null && !last.isEmpty()) return java.net.URLDecoder.decode(last, "UTF-8").replace("primary:", "/storage/emulated/0/");
        } catch (Throwable ignored) { }
        return s.length() > 72 ? "..." + s.substring(s.length() - 72) : s;
    }

    private void launchScanRootPicker(int replaceIndex) {
        pendingScanRootReplaceIndex = replaceIndex;
        scanDirLauncher.launch(null);
    }

    private LinearLayout scanRootCard(String uri, int index, Runnable refresh) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(android.view.Gravity.CENTER_VERTICAL);
        card.setBackgroundResource(R.drawable.bg_input);
        card.setPadding(dp(10), dp(8), dp(8), dp(8));
        TextView text = new TextView(this);
        text.setText((index + 1) + ". " + compactUriLabel(uri));
        text.setTextColor(getColorCompat(R.color.yh_text));
        text.setTextSize(11);
        text.setSingleLine(false);
        card.addView(text, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        TextView change = new TextView(this);
        change.setText("更换");
        change.setTextColor(getColorCompat(R.color.yh_primary));
        change.setTextSize(12);
        change.setTypeface(null, android.graphics.Typeface.BOLD);
        change.setPadding(dp(10), 0, dp(8), 0);
        change.setOnClickListener(v -> launchScanRootPicker(index));
        card.addView(change);
        TextView remove = new TextView(this);
        remove.setText("移除");
        remove.setTextColor(getColorCompat(R.color.yh_warning));
        remove.setTextSize(12);
        remove.setTypeface(null, android.graphics.Typeface.BOLD);
        remove.setPadding(dp(8), 0, 0, 0);
        remove.setOnClickListener(v -> {
            removeScanRootAt(index);
            if (refresh != null) refresh.run();
        });
        card.addView(remove);
        return card;
    }

    private void refreshActiveScanRootListUi() {
        if (activeScanRootList != null) refreshScanRootListUi(activeScanRootList, activeScanRootInfo);
    }

    private void refreshScanRootListUi(LinearLayout container, TextView info) {
        if (container == null) return;
        container.removeAllViews();
        List<String> roots = getScanRootUris();
        if (info != null) info.setText("已绑定 " + roots.size() + "/" + MAX_SCAN_ROOTS + " 个目录，扫描时会合并扫描。" + (roots.isEmpty() ? "\n请先添加扫描目录。" : ""));
        if (roots.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("暂无扫描目录");
            empty.setTextColor(getColorCompat(R.color.yh_text_muted));
            empty.setTextSize(12);
            empty.setGravity(android.view.Gravity.CENTER);
            empty.setBackgroundResource(R.drawable.bg_input);
            container.addView(empty, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(44)));
            return;
        }
        for (int i = 0; i < roots.size(); i++) {
            final int index = i;
            LinearLayout card = scanRootCard(roots.get(i), index, () -> refreshScanRootListUi(container, info));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, dp(6));
            container.addView(card, lp);
        }
    }

    private void scanLastRootOrChoose() {
        List<String> roots = getScanRootUris();
        if (roots.isEmpty()) {
            scanDirLauncher.launch(null);
            return;
        }
        runLibraryScan(roots, true);
    }

    private void autoScanLastRootIfAvailable() {
        String last = prefs.getString(KEY_LAST_SCAN_ROOT_URI, null);
        if (last == null || last.isEmpty()) return;
        runLibraryScan(Uri.parse(last), false);
    }

    private void setScanButtonLoadingState(boolean loading) {
        setScanLoading(loading);
    }

    private String defaultLaunchTargetForEngine(EngineType engine) {
        if (engine == EngineType.TYRANO || engine == EngineType.ARTEMIS || engine == EngineType.KIRIKIRI) return "[游戏目录]";
        if (engine == EngineType.GAMEHUB) return "[GameHub]";
        return "[游戏目录]";
    }

    private void autoMatchVndbForImportedGames(List<Game> games) {
        if (games == null || games.isEmpty()) return;
        int changed = 0;
        for (Game g : games) {
            if (g == null || g.id <= 0 || g.title == null || g.title.trim().isEmpty()) continue;
            try {
                List<VnMetadata> candidates = VndbClient.searchCandidates(g.title, 1);
                if (candidates == null || candidates.isEmpty()) continue;
                VnMetadata meta = candidates.get(0);
                if (metadataRepository != null) metadataRepository.saveVndb(g.id, meta);
                boolean updated = false;
                if (!hasCover(g) && meta.coverUrl != null && !meta.coverUrl.isEmpty()) {
                    String cover = cacheRemoteImageSync(meta.coverUrl, "scan_cover_" + emptyText(meta.id, String.valueOf(g.id)));
                    if (cover != null && !cover.isEmpty()) {
                        g.coverUri = cover;
                        g.coverPersistUri = cover;
                        g.coverSourceType = 1;
                        updated = true;
                    }
                }
                if (updated) {
                    repository.update(g);
                    changed++;
                }
            } catch (Throwable t) {
                Log.w("KamiGAL", "auto VNDB match failed: " + g.title, t);
            }
        }
        int finalChanged = changed;
        if (finalChanged > 0) runOnUiThread(() -> {
            loadGames();
            Toast.makeText(this, "已自动补全 " + finalChanged + " 个 VNDB 封面", Toast.LENGTH_SHORT).show();
        });
    }

    private boolean isDesktopLaunchTarget(String target) {
        return target != null && target.trim().toLowerCase(Locale.ROOT).endsWith(".desktop");
    }

    private String guessInstalledGameHubPackage() {
        try {
            PackageManager pm = getPackageManager();
            if (pm.getLaunchIntentForPackage("com.xiaoji.egggamz") != null) return "com.xiaoji.egggamz";
            if (pm.getLaunchIntentForPackage("com.xiaoji.egggame") != null) return "com.xiaoji.egggame";
        } catch (Throwable ignored) { }
        return "com.xiaoji.egggamz";
    }

private String guessInstalledWinlatorPackage() {
try {
            PackageManager pm = getPackageManager();
            List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            String fallback = "";
            for (ApplicationInfo app : apps) {
                if (app == null || app.packageName == null) continue;
                String pkg = app.packageName.toLowerCase(Locale.ROOT);
                String label = "";
                try { label = String.valueOf(pm.getApplicationLabel(app)).toLowerCase(Locale.ROOT); } catch (Throwable ignored) { }
                boolean hit = pkg.contains("winlator") || label.contains("winlator") || pkg.contains("glibc") || pkg.contains("proot");
                if (!hit) continue;
                if (pm.getLaunchIntentForPackage(app.packageName) == null) continue;
                if (pkg.contains("cmod")) return app.packageName;
                if (fallback.isEmpty()) fallback = app.packageName;
            }
            return fallback;
        } catch (Throwable ignored) {
            return "";
        }
    }

    private ScanImportStats importScannedGames(List<ScanResult> results) {
        ScanImportStats stats = new ScanImportStats();
        if (results == null || results.isEmpty()) return stats;
        Set<String> existing = repository.getRootUriSet();
        for (ScanResult r : results) {
            if (r == null || r.uri == null || r.uri.trim().isEmpty()) continue;
            if (existing.contains(r.uri)) {
                stats.skipped++;
                continue;
            }
            Game g = new Game();
            g.title = r.title;
            g.rootUri = r.uri;
            g.engine = r.engine;
            g.launchTarget = (r.launchTarget == null || r.launchTarget.trim().isEmpty()) ? defaultLaunchTargetForEngine(r.engine) : r.launchTarget;
            String cover = null;
            if (r.coverUri != null && !r.coverUri.trim().isEmpty()) {
                cover = copyCoverToInternalStorage(Uri.parse(r.coverUri));
            }
            if (cover == null || cover.isEmpty()) {
                Uri autoCover = findFirstLevelImage(r.uri);
                if (autoCover != null) cover = copyCoverToInternalStorage(autoCover);
            }
            if (cover != null) {
                g.coverUri = cover;
                g.coverPersistUri = cover;
                g.coverSourceType = 1;
            }
            if (r.engine == EngineType.KIRIKIRI) g.emulatorPackage = "internal.krkr";
            if (r.engine == EngineType.ONS) g.emulatorPackage = "internal.ons";
            if (r.engine == EngineType.TYRANO) g.emulatorPackage = "internal.tyrano";
            if (r.engine == EngineType.ARTEMIS) g.emulatorPackage = resolveArtemisPackageFromMarkers(g.rootUri);
            if (isDesktopLaunchTarget(g.launchTarget)) g.emulatorPackage = guessInstalledWinlatorPackage();
            long newId = repository.insertIfNotExists(g);
            if (newId > 0) {
                g.id = newId;
                existing.add(r.uri);
                stats.added++;
                stats.importedGames.add(g);
            } else {
                stats.skipped++;
            }
        }
        return stats;
    }

    private static class ScanImportStats {
        int added;
        int skipped;
        final List<Game> importedGames = new ArrayList<>();
    }

    private void launchGame(Game game) {
        lastStorageProbeResult = null;
        lastStorageProbeAt = 0L;
        if (shouldProbeStorageBeforeLaunch(game)) {
            launchGameWithStorageProbe(game);
            return;
        }
        doLaunchGame(game);
    }

    private void doLaunchGame(Game game) {
        String emulatorPackage = game.emulatorPackage == null ? "" : game.emulatorPackage.trim();
        if (emulatorPackage.isEmpty() && game.engine == EngineType.KIRIKIRI) emulatorPackage = "internal.krkr";
        if (emulatorPackage.isEmpty() && game.engine == EngineType.ONS) emulatorPackage = "internal.ons";
        if (emulatorPackage.isEmpty() && game.engine == EngineType.TYRANO) emulatorPackage = "internal.tyrano";
        if (emulatorPackage.isEmpty() && game.engine == EngineType.WINLATOR) emulatorPackage = guessInstalledWinlatorPackage();
        if (emulatorPackage.isEmpty() && game.engine == EngineType.GAMEHUB) emulatorPackage = guessInstalledGameHubPackage();
        if (game.engine == EngineType.ARTEMIS) {
            emulatorPackage = normalizeArtemisPackage(emulatorPackage);
        }
        String launchTarget = game.launchTarget;
        if (game.engine == EngineType.ARTEMIS || game.engine == EngineType.TYRANO) launchTarget = "[游戏目录]";
        if (game.engine == EngineType.GAMEHUB) {
            String ghMode = game.gamehubLaunchMode == null ? "game" : game.gamehubLaunchMode.trim().toLowerCase(Locale.ROOT);
            if (!("program".equals(ghMode) || "normal".equals(ghMode)) && (game.gamehubLocalGameId == null || game.gamehubLocalGameId.trim().isEmpty())) { Toast.makeText(this, "请先编辑游戏，通过Shizuku导入GameHub localGameId。", Toast.LENGTH_LONG).show(); return; }
            launchTarget = game.title;
        }
        if (emulatorPackage.isEmpty()) { Toast.makeText(this, "请先编辑游戏，填写模拟器包名。", Toast.LENGTH_LONG).show(); return; }
        runningGameId = game.id;
        sessionStart = System.currentTimeMillis();
        String launchType = resolveLaunchType(emulatorPackage);
        runningSessionId = repository.startPlaySession(game.id, sessionStart, launchType);
        launchedExternal = true;
        if (!launchGameInternal(game, emulatorPackage, launchTarget)) {
            repository.cancelPlaySession(runningSessionId);
            launchedExternal = false;
            runningGameId = -1;
            runningSessionId = -1;
            sessionStart = 0;
            Toast.makeText(this, "启动失败：未找到该模拟器，或该模拟器不接受当前启动目标", Toast.LENGTH_LONG).show();
        }
    }

    private boolean launchGameInternal(Game game, String emulatorPackage, String launchTarget) {
        if (game == null || emulatorPackage == null || emulatorPackage.trim().isEmpty()) return false;
        String pkg = emulatorPackage.trim();
        if (pkg.startsWith("internal.krkr") || pkg.equals("org.tvp.kirikiri2.internal")) {
boolean compatMode = prefs != null && prefs.getBoolean(KEY_KR_COMPAT_MODE, false);
String krEngineVersion = prefs == null ? "auto" : prefs.getString(KEY_KR_ENGINE_VERSION, "auto");
return startActivitySafely(EmulatorLauncher.buildInternalKrkrIntent(this, game.rootUri, launchTarget, false, compatMode, krEngineVersion));
}
        if (pkg.startsWith("internal.tyrano") || pkg.equals("com.sakurajima.galsearch.tyrano")) {
            return startActivitySafely(EmulatorLauncher.buildInternalTyranoIntent(this, game.rootUri, launchTarget));
        }
        if (pkg.startsWith("internal.ons") || pkg.equals("com.sakurajima.galsearch.ons")) {
            return startActivitySafely(EmulatorLauncher.buildInternalOnsIntent(this, game.rootUri, launchTarget));
        }
        if (pkg.startsWith("internal.artemis")) {
            return startActivitySafely(EmulatorLauncher.buildInternalArtemisIntent(this, pkg, game.rootUri, launchTarget));
        }
        return EmulatorLauncher.launchGame(this, emulatorPackage, game.rootUri, launchTarget, game.winlatorLaunchMode, game.gamehubLaunchMode, game.gamehubLocalGameId);
    }

    private boolean startActivitySafely(android.content.Intent intent) {
        if (intent == null) return false;
        try {
            startActivity(intent);
            return true;
        } catch (Throwable t) {
            Log.w("KamiGAL", "startActivitySafely failed", t);
            return false;
        }
    }

    private String resolveLaunchType(String emulatorPackage) {
        String pkg = emulatorPackage == null ? "" : emulatorPackage.trim().toLowerCase(Locale.ROOT);
        if (pkg.startsWith("internal.krkr") || pkg.equals("org.tvp.kirikiri2.internal")) return "internal.krkr";
        if (pkg.startsWith("internal.ons") || pkg.equals("com.sakurajima.galsearch.ons")) return "internal.ons";
        if (pkg.startsWith("internal.tyrano") || pkg.equals("com.sakurajima.galsearch.tyrano")) return "internal.tyrano";
        if (pkg.startsWith("internal.artemis")) return pkg;
        return "external";
    }

    private boolean shouldProbeStorageBeforeLaunch(Game game) {
        if (game == null || game.rootUri == null || game.rootUri.trim().isEmpty()) return false;
        if (isScopedSaveEnabledFor(game.engine)) return false;
        if (game.engine == EngineType.KIRIKIRI) return true;
        if (game.engine == EngineType.ARTEMIS) return true;
        return false;
    }

    private void launchGameWithStorageProbe(Game game) {
        final Game target = game;
        final java.util.concurrent.atomic.AtomicBoolean launched = new java.util.concurrent.atomic.AtomicBoolean(false);
        java.util.concurrent.Future<?> future = AppExecutors.io().submit(() -> {
            StorageProbeResult result = probeGameStorage(target);
            Log.i("StorageProbe", result.toLogLine());
            runOnUiThread(() -> {
                if (!launched.compareAndSet(false, true)) return;
                handleStorageProbeResultBeforeLaunch(target, result);
            });
        });
        AppExecutors.schedule(() -> runOnUiThread(() -> {
            if (!launched.compareAndSet(false, true)) return;
            Log.w("StorageProbe", "probe timeout after " + STORAGE_PROBE_TIMEOUT_MS + "ms root=" + (target == null ? null : target.rootUri));
            try { future.cancel(true); } catch (Throwable ignored) { }
            doLaunchGame(target);
        }), STORAGE_PROBE_TIMEOUT_MS);
    }

    private void handleStorageProbeResultBeforeLaunch(Game game, StorageProbeResult result) {
        lastStorageProbeResult = result;
        lastStorageProbeAt = System.currentTimeMillis();
        if (game == null) return;
        if (result != null && result.rawResolved && !result.rawWriteOk) {
            boolean scopedEnabled = isScopedSaveEnabledFor(game.engine);
            boolean safCanHandle = canUseKrSafFileFallback(game, result);
            String engine = game.engine == null ? "引擎" : game.engine.getDisplayName();
            Log.w("StorageProbe", "raw write unavailable for " + engine + ", scopedSaveEnabled=" + scopedEnabled + ", safCanHandle=" + safCanHandle + ", rawPath=" + result.rawPath + ", err=" + result.writeError + ", safErr=" + result.safError);
            if (!scopedEnabled && !safCanHandle) {
                Toast.makeText(this, "检测到游戏目录可能无法写入，若闪退或无法存档，请在设置中开启" + engine + "独立存档目录。", Toast.LENGTH_LONG).show();
            }
        }
        if (result != null && result.rawResolved && !result.rawReadOk) {
            Log.w("StorageProbe", "raw read unavailable rawPath=" + result.rawPath + ", err=" + result.readError + ", safErr=" + result.safError);
        }
        doLaunchGame(game);
    }

    private boolean isScopedSaveEnabledFor(EngineType engine) {
        if (prefs == null || engine == null) return false;
        if (engine == EngineType.KIRIKIRI) return prefs.getBoolean(KEY_KR_SCOPED_SAVE_DIR, false);
        if (engine == EngineType.ARTEMIS) return prefs.getBoolean(KEY_ARTEMIS_SCOPED_SAVE_DIR, false);
        return false;
    }

    @SuppressWarnings("unused")
    private boolean shouldUseKrSafFileFallback(Game game) {
        return canUseKrSafFileFallback(game, lastStorageProbeResult)
                && System.currentTimeMillis() - lastStorageProbeAt <= 5000L;
    }

    private boolean canUseKrSafFileFallback(Game game, StorageProbeResult r) {
        if (game == null || game.engine != EngineType.KIRIKIRI) return false;
        if (isScopedSaveEnabledFor(game.engine)) return false;
        if (r == null || !r.rawResolved || !r.safTreeCoversPath || !r.safWriteOk) return false;
        return r.rawReadOk || r.safReadOk;
    }

    private StorageProbeResult probeGameStorage(Game game) {
        long start = System.currentTimeMillis();
        StorageProbeResult result = new StorageProbeResult();
        result.engine = game == null || game.engine == null ? "unknown" : game.engine.name();
        result.rootUri = game == null ? null : game.rootUri;
        result.rawPath = fastRawPathFromUri(result.rootUri);
        result.rawResolved = result.rawPath != null && result.rawPath.startsWith("/");
        try {
            java.io.File appExternal = getExternalFilesDir(null);
            result.appPrivateWriteOk = quickWriteProbe(appExternal, ".saku_probe");
        } catch (Throwable t) {
            result.appPrivateError = shortError(t);
        }
        if (!result.rawResolved) {
            result.elapsedMs = System.currentTimeMillis() - start;
            result.readError = "raw path unavailable";
            return result;
        }
        java.io.File root = new java.io.File(result.rawPath);
        try {
            result.rawExists = root.exists();
            result.rawIsDirectory = root.isDirectory();
            if (result.rawIsDirectory) {
                String[] names = root.list();
                result.rawReadOk = names != null;
            } else {
                result.rawReadOk = root.isFile() && root.canRead();
            }
        } catch (Throwable t) {
            result.readError = shortError(t);
        }
        if (!result.rawReadOk && result.readError == null) result.readError = "list/canRead failed";
        try {
            java.io.File writeDir = root.isDirectory() ? root : root.getParentFile();
            result.rawWriteOk = quickWriteProbe(writeDir, ".saku_write_probe");
        } catch (Throwable t) {
            result.writeError = shortError(t);
        }
        if (!result.rawWriteOk && result.writeError == null) result.writeError = "create/write/delete failed";
        if (game != null && game.engine == EngineType.KIRIKIRI) {
            probeSafWriteFallback(result);
        } else if (!result.rawReadOk || !result.rawWriteOk) {
            probeSafWriteFallback(result);
        }
        result.elapsedMs = System.currentTimeMillis() - start;
        return result;
    }

    private void probeSafWriteFallback(StorageProbeResult result) {
        if (result == null || !result.rawResolved || result.rawPath == null || result.rawPath.trim().isEmpty()) return;
        result.safCandidate = result.rawPath.startsWith("/storage/") || result.rawPath.startsWith("/sdcard");
        if (!result.safCandidate) return;
        try {
            SafPath safPath = toSafPath(result.rawPath);
            if (safPath == null || safPath.volume == null || safPath.rel == null) {
                result.safError = "raw path cannot map to SAF doc id";
                return;
            }
            android.content.ContentResolver resolver = getContentResolver();
            if (resolver == null) {
                result.safError = "content resolver unavailable";
                return;
            }
            for (android.content.UriPermission perm : resolver.getPersistedUriPermissions()) {
                if (perm == null || perm.getUri() == null) continue;
                String treeId;
                try { treeId = android.provider.DocumentsContract.getTreeDocumentId(perm.getUri()); } catch (Throwable ignored) { continue; }
                if (treeId == null) continue;
                String decodedTreeId = android.net.Uri.decode(treeId);
                if (decodedTreeId == null || !decodedTreeId.startsWith(safPath.volume + ":")) continue;
                String treeRel = decodedTreeId.substring((safPath.volume + ":").length());
                if (!treeRel.isEmpty() && !safPath.rel.equals(treeRel) && !safPath.rel.startsWith(treeRel + "/")) continue;
                result.safTreeCoversPath = true;
                result.safReadOk = perm.isReadPermission();
                boolean safTargetIsDirectory = result.rawIsDirectory || isSafTargetDirectory(perm.getUri(), decodedTreeId, safPath);
                if (!perm.isWritePermission()) {
                    result.safError = "persisted SAF tree is read-only";
                    return;
                }
                android.net.Uri probeUri = createSafProbeDocument(resolver, perm.getUri(), decodedTreeId, safPath, safTargetIsDirectory, ".saku_saf_probe_" + android.os.Process.myPid() + "_" + System.nanoTime() + ".tmp");
                if (probeUri == null) {
                    result.safError = "create SAF probe failed";
                    return;
                }
                try (java.io.OutputStream out = resolver.openOutputStream(probeUri, "wt")) {
                    if (out == null) {
                        result.safError = "open SAF probe output failed";
                        return;
                    }
                    out.write(new byte[]{'S', 'G'});
                    out.flush();
                } finally {
                    try { android.provider.DocumentsContract.deleteDocument(resolver, probeUri); } catch (Throwable ignored) { }
                }
                result.safWriteOk = true;
                result.safError = null;
                return;
            }
            result.safError = "no persisted SAF tree covers raw path";
        } catch (Throwable t) {
            result.safError = shortError(t);
        }
    }

    private SafPath toSafPath(String path) {
        if (path == null) return null;
        String p = path.trim();
        if (p.startsWith("file://")) p = p.substring("file://".length());
        while (p.contains("//")) p = p.replace("//", "/");
        String volume;
        String rel;
        if (p.startsWith("/storage/emulated/0/")) {
            volume = "primary";
            rel = p.substring("/storage/emulated/0/".length());
        } else if ("/storage/emulated/0".equals(p)) {
            volume = "primary";
            rel = "";
        } else if (p.startsWith("/sdcard/")) {
            volume = "primary";
            rel = p.substring("/sdcard/".length());
        } else if ("/sdcard".equals(p)) {
            volume = "primary";
            rel = "";
        } else if (p.startsWith("/storage/")) {
            String rest = p.substring("/storage/".length());
            int slash = rest.indexOf('/');
            if (slash <= 0) return null;
            volume = rest.substring(0, slash);
            rel = rest.substring(slash + 1);
        } else {
            return null;
        }
        if (volume == null || volume.isEmpty() || rel == null) return null;
        return new SafPath(volume, rel);
    }

    private boolean isSafTargetDirectory(android.net.Uri tree, String decodedTreeId, SafPath safPath) {
        try {
            if (tree == null || safPath == null) return false;
            android.provider.DocumentsContract dc = null;
            androidx.documentfile.provider.DocumentFile current = androidx.documentfile.provider.DocumentFile.fromTreeUri(this, tree);
            if (current == null) return false;
            String treePrefix = safPath.volume + ":";
            String localRel = safPath.rel;
            String treeRel = decodedTreeId != null && decodedTreeId.startsWith(treePrefix) ? decodedTreeId.substring(treePrefix.length()) : "";
            if (!treeRel.isEmpty()) {
                if (localRel.equals(treeRel)) localRel = "";
                else if (localRel.startsWith(treeRel + "/")) localRel = localRel.substring(treeRel.length() + 1);
            }
            if (localRel == null || localRel.isEmpty()) return current.isDirectory();
            String[] parts = localRel.split("/");
            for (String part : parts) {
                if (part == null || part.isEmpty() || ".".equals(part)) continue;
                current = current.findFile(part);
                if (current == null) return false;
            }
            return current.isDirectory();
        } catch (Throwable ignored) {
            return false;
        }
    }

    private android.net.Uri createSafProbeDocument(android.content.ContentResolver resolver, android.net.Uri tree, String decodedTreeId, SafPath safPath, boolean rawIsDirectory, String probeName) {
        try {
            if (resolver == null || tree == null || safPath == null || probeName == null || probeName.trim().isEmpty()) return null;
            androidx.documentfile.provider.DocumentFile dir = androidx.documentfile.provider.DocumentFile.fromTreeUri(this, tree);
            if (dir == null) return null;
            String treePrefix = safPath.volume + ":";
            String localRel = safPath.rel;
            String treeRel = decodedTreeId != null && decodedTreeId.startsWith(treePrefix) ? decodedTreeId.substring(treePrefix.length()) : "";
            if (!treeRel.isEmpty()) {
                if (localRel.equals(treeRel)) localRel = "";
                else if (localRel.startsWith(treeRel + "/")) localRel = localRel.substring(treeRel.length() + 1);
            }
            String[] parts = localRel.split("/");
            androidx.documentfile.provider.DocumentFile current = dir;
            int end = rawIsDirectory ? parts.length : Math.max(0, parts.length - 1);
            for (int i = 0; i < end; i++) {
                String part = parts[i];
                if (part == null || part.isEmpty() || ".".equals(part)) continue;
                androidx.documentfile.provider.DocumentFile child = current.findFile(part);
                if (child == null) child = current.createDirectory(part);
                if (child == null || !child.isDirectory()) return null;
                current = child;
            }
            androidx.documentfile.provider.DocumentFile existing = current.findFile(probeName);
            if (existing != null) {
                try { existing.delete(); } catch (Throwable ignored) { }
            }
            androidx.documentfile.provider.DocumentFile probe = current.createFile("application/octet-stream", probeName);
            return probe == null ? null : probe.getUri();
        } catch (Throwable t) {
            Log.w("StorageProbe", "create SAF probe failed", t);
            return null;
        }
    }

    private boolean quickWriteProbe(java.io.File dir, String prefix) throws Exception {
        if (dir == null || !dir.isDirectory()) return false;
        java.io.File probe = new java.io.File(dir, prefix + "_" + android.os.Process.myPid() + "_" + System.nanoTime() + ".tmp");
        boolean ok = false;
        try (java.io.FileOutputStream out = new java.io.FileOutputStream(probe, false)) {
            out.write(new byte[]{'S', 'G'});
            out.flush();
            ok = probe.isFile() && probe.length() >= 2;
        } finally {
            if (probe.exists() && !probe.delete()) Log.w("StorageProbe", "probe delete failed " + probe.getAbsolutePath());
        }
        return ok;
    }

    private String fastRawPathFromUri(String value) {
        if (value == null || value.trim().isEmpty()) return value;
        String s = value.trim();
        if (s.startsWith("file://")) {
            try { return android.net.Uri.parse(s).getPath(); } catch (Throwable ignored) { return s.substring("file://".length()); }
        }
        if (s.startsWith("/")) return s;
        try {
            android.net.Uri uri = android.net.Uri.parse(s);
            String docId = null;
            String path = uri.getPath();
            if (path != null && path.contains("/document/")) {
                try { docId = android.provider.DocumentsContract.getDocumentId(uri); } catch (Throwable ignored) { }
            }
            if (docId == null || docId.isEmpty()) {
                try { docId = android.provider.DocumentsContract.getTreeDocumentId(uri); } catch (Throwable ignored) { }
            }
            if (docId == null || docId.isEmpty()) {
                try { docId = android.provider.DocumentsContract.getDocumentId(uri); } catch (Throwable ignored) { }
            }
            if (docId != null && !docId.isEmpty()) {
                int colon = docId.indexOf(':');
                String volume = colon >= 0 ? docId.substring(0, colon) : docId;
                String rel = colon >= 0 ? docId.substring(colon + 1) : "";
                if ("primary".equalsIgnoreCase(volume)) return rel.isEmpty() ? "/storage/emulated/0" : "/storage/emulated/0/" + rel;
                if (volume != null && !volume.isEmpty()) return rel.isEmpty() ? "/storage/" + volume : "/storage/" + volume + "/" + rel;
            }
            String p = uri.getPath();
            return p == null ? s : p;
        } catch (Throwable t) {
            return s;
        }
    }

    private String shortError(Throwable t) {
        if (t == null) return null;
        String msg = t.getMessage();
        String name = t.getClass().getSimpleName();
        return msg == null || msg.trim().isEmpty() ? name : name + ": " + msg;
    }

    private static class SafPath {
        final String volume;
        final String rel;
        SafPath(String volume, String rel) {
            this.volume = volume;
            this.rel = rel;
        }
    }

    private static class StorageProbeResult {
        String engine;
        String rootUri;
        String rawPath;
        boolean rawResolved;
        boolean rawExists;
        boolean rawIsDirectory;
        boolean rawReadOk;
        boolean rawWriteOk;
        boolean safCandidate;
        boolean safTreeCoversPath;
        boolean safReadOk;
        boolean safWriteOk;
        boolean appPrivateWriteOk;
        String readError;
        String writeError;
        String safError;
        String appPrivateError;
        long elapsedMs;

        String toLogLine() {
            return "engine=" + engine
                    + " rawResolved=" + rawResolved
                    + " rawExists=" + rawExists
                    + " rawDir=" + rawIsDirectory
                    + " rawReadOk=" + rawReadOk
                    + " rawWriteOk=" + rawWriteOk
                    + " safCandidate=" + safCandidate
                    + " safCovers=" + safTreeCoversPath
                    + " safReadOk=" + safReadOk
                    + " safWriteOk=" + safWriteOk
                    + " appPrivateWriteOk=" + appPrivateWriteOk
                    + " elapsedMs=" + elapsedMs
                    + " rawPath=" + rawPath
                    + " readErr=" + readError
                    + " writeErr=" + writeError
                    + " safErr=" + safError
                    + " appErr=" + appPrivateError;
        }
    }

    private void finishCurrentPlaySessionIfAny() {
        if (launchedExternal && runningGameId > 0 && runningSessionId > 0 && sessionStart > 0) {
            repository.finishPlaySession(runningSessionId, System.currentTimeMillis(), MIN_PLAY_SESSION_MS, MAX_PLAY_SESSION_MS);
            launchedExternal = false;
            runningGameId = -1;
            runningSessionId = -1;
            sessionStart = 0;
            loadGames();
        }
    }

    private void finishStalePlaySessionsIfAny() {
        if (repository == null) return;
        PlayActivity open = repository.findLatestOpenPlaySession();
        if (open == null) return;
        long now = System.currentTimeMillis();
        long rawDuration = Math.max(0L, now - open.startTime);
        long duration = Math.min(rawDuration, MAX_PLAY_SESSION_MS);
        String message = "检测到最近一次游玩未正常结束。\n\n"
                + "游戏：" + emptyText(open.gameTitle, "未命名游戏") + "\n"
                + "开始时间：" + TimeFormatUtil.date(open.startTime) + "\n"
                + "可补记时长：" + TimeFormatUtil.playTime(duration) + "\n\n"
                + "如果这段时间确实在游玩，可选择补记；如果只是测试启动、闪退或误操作，请选择忽略。\n\n"
                + "本操作仅处理这一条未完成记录。";
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("发现未完成的游玩记录")
                .setMessage(message)
                .setPositiveButton("补记", (d, w) -> {
                    repository.finishPlaySession(open.sessionId, System.currentTimeMillis(), MIN_PLAY_SESSION_MS, MAX_PLAY_SESSION_MS);
                    loadGames();
                    updateProfilePanel();
                    Toast.makeText(this, "已补记上次游玩时长", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("忽略", (d, w) -> {
                    repository.deleteOpenPlaySession(open.sessionId);
                    loadGames();
                    updateProfilePanel();
                    Toast.makeText(this, "已忽略上次未完成记录", Toast.LENGTH_SHORT).show();
                })
                .setCancelable(false)
                .show();
        styleAlertDialogDark(dialog);
    }

@Override protected void onPause() {
    pauseBackgroundVideoIfNeeded();
    super.onPause();
}

    @Override protected void onDestroy() {
        stopHeartbeat();
        stopStatsPolling();
        reportOffline();
        releaseBackgroundMediaPlayer();
        super.onDestroy();
    }

    // ========== 统计系统 ==========

    private String getDeviceId() {
        // 使用 Android 设备固定标识（ANDROID_ID），卸载重装也不变
        String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (id == null || id.isEmpty()) {
            // 极少数设备可能取不到，fallback 到 UUID
            id = prefs.getString(KEY_DEVICE_ID, "");
            if (id.isEmpty()) {
                id = java.util.UUID.randomUUID().toString();
                prefs.edit().putString(KEY_DEVICE_ID, id).apply();
            }
        }
        return id;
    }

    private void sendStatsRequest(String endpoint) {
        String deviceId = getDeviceId();
        if (deviceId.isEmpty()) return;
        AppExecutors.runOnIo(() -> {
            try {
                java.net.URL url = new java.net.URL(STATS_BASE_URL + endpoint);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                String json = "{\"json\":{\"deviceId\":\"" + deviceId + "\"}}";
                java.io.OutputStream os = conn.getOutputStream();
                os.write(json.getBytes("UTF-8"));
                os.flush();
                os.close();
                int code = conn.getResponseCode();
                conn.disconnect();
            } catch (Throwable ignored) { }
        });
    }

    private void reportLaunch() {
        sendStatsRequest("/api/trpc/stats.launch");
    }

    private void startHeartbeat() {
        stopHeartbeat();
        statsHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        heartbeatTask = new Runnable() {
            @Override
            public void run() {
                sendStatsRequest("/api/trpc/stats.heartbeat");
                if (statsHandler != null) {
                    statsHandler.postDelayed(this, 60 * 1000);
                }
            }
        };
        // 首次立即发送，不用等60秒
        heartbeatTask.run();
    }

    private void stopHeartbeat() {
        if (statsHandler != null && heartbeatTask != null) {
            statsHandler.removeCallbacks(heartbeatTask);
        }
        statsHandler = null;
        heartbeatTask = null;
    }

    private void reportOffline() {
        sendStatsRequest("/api/trpc/stats.offline");
    }
private void startStatsPolling() {
        stopStatsPolling();
        statsPollHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        statsPollTask = new Runnable() {
            @Override
            public void run() {
                fetchStatsOverview();
                if (statsPollHandler != null) {
                    int interval = isMobileData() ? 60 * 1000 : 30 * 1000;
                    statsPollHandler.postDelayed(this, interval);
                }
            }
        };
        statsPollTask.run();
    }

    private boolean isMobileData() {
        try {
            android.net.ConnectivityManager cm = (android.net.ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            android.net.NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.getType() == android.net.ConnectivityManager.TYPE_MOBILE;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private void stopStatsPolling() {
        if (statsPollHandler != null && statsPollTask != null) {
            statsPollHandler.removeCallbacks(statsPollTask);
        }
        statsPollHandler = null;
        statsPollTask = null;
    }

    private void fetchStatsOverview() {
        AppExecutors.runOnIo(() -> {
            try {
                URL url = new URL(STATS_BASE_URL + "/api/trpc/stats.overview");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                int code = conn.getResponseCode();
                if (code == 200) {
                    java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    br.close();
                    org.json.JSONObject root = new org.json.JSONObject(sb.toString());
                    org.json.JSONObject data = root.optJSONObject("result");
                    if (data != null) data = data.optJSONObject("data");
                    if (data != null) data = data.optJSONObject("json");
                    final String onlineStr = String.valueOf(data != null ? data.optInt("onlineNow", 0) : 0);
                    final String todayStr = String.valueOf(data != null ? data.optInt("todayLaunches", 0) : 0);
                    runOnUiThread(() -> {
                        if (statsOvOnline != null) statsOvOnline.setText("在线\n" + onlineStr);
                        if (statsOvToday != null) statsOvToday.setText("今日使用\n" + todayStr);
                    });
                }
            } catch (Throwable ignored) {}
        });
    }

    private void resumeBackgroundVideoIfNeeded() {
    if (prefs == null || !"video".equals(prefs.getString(KEY_CUSTOM_BACKGROUND_TYPE, "image"))) return;
    if (backgroundMediaPlayer != null) {
        try { if (!backgroundMediaPlayer.isPlaying()) backgroundMediaPlayer.start(); } catch (Throwable ignored) { }
    } else if (pendingBackgroundVideoUri != null) {
        TextureView textureView = findViewById(R.id.customBackgroundVideo);
        if (textureView != null && textureView.getVisibility() == View.VISIBLE) playBackgroundVideo(textureView, pendingBackgroundVideoUri, false);
    }
}

private void pauseBackgroundVideoIfNeeded() {
    if (prefs == null || !"video".equals(prefs.getString(KEY_CUSTOM_BACKGROUND_TYPE, "image"))) return;
    try { if (backgroundMediaPlayer != null && backgroundMediaPlayer.isPlaying()) backgroundMediaPlayer.pause(); } catch (Throwable ignored) { }
}

    // ========== 图片识别Gal（AnimeTrace） ==========
    private static final int IMGSEARCH_PICK_IMAGE = 9101;
    private String imgSearchPendingPath = null;

    private void showPortraitImageSearch() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(16), dp(16), dp(16));

        // 标题
        TextView title = new TextView(this);
        title.setText("识图识别 Galgame");
        title.setTextColor(getColorCompat(R.color.yh_text));
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 0, 0, dp(16));
        root.addView(title);

        // 选择图片按钮
        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        TextView pickBtn = new TextView(this);
        pickBtn.setLayoutParams(new LinearLayout.LayoutParams(0, dp(44), 1));
        pickBtn.setGravity(android.view.Gravity.CENTER);
        pickBtn.setBackgroundDrawable(getDrawable(R.drawable.bg_yuki_button));
        pickBtn.setText("🖼 选择图片");
        pickBtn.setTextColor(0xFF071221);
        pickBtn.setTextSize(14);
        pickBtn.setTypeface(null, android.graphics.Typeface.BOLD);
        btnRow.addView(pickBtn);
        root.addView(btnRow);

        // 提示文字
        TextView hint = new TextView(this);
        hint.setText("上传 Galgame 截图，自动识别游戏名称和角色信息");
        hint.setTextColor(getColorCompat(R.color.yh_text_muted));
        hint.setTextSize(11);
        hint.setPadding(dp(4), dp(10), dp(4), dp(4));
        root.addView(hint);

        // 图片预览区域
        FrameLayout previewArea = new FrameLayout(this);
        previewArea.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(320)));
        previewArea.setBackgroundDrawable(getDrawable(R.drawable.bg_input));
        previewArea.setPadding(dp(8), dp(8), dp(8), dp(8));

        ImageView preview = new ImageView(this);
        preview.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        preview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        preview.setVisibility(View.GONE);
        int previewId = View.generateViewId();
        imgSearchPreviewId = previewId;
        preview.setId(previewId);
        previewArea.addView(preview);

        TextView placeholder = new TextView(this);
        placeholder.setLayoutParams(new FrameLayout.LayoutParams(-2, -2, android.view.Gravity.CENTER));
        placeholder.setText("点击上方按钮选择图片");
        placeholder.setTextColor(getColorCompat(R.color.yh_text_muted));
        placeholder.setTextSize(12);
        previewArea.addView(placeholder);
        root.addView(previewArea);

        // 状态提示
        TextView statusText = new TextView(this);
        statusText.setVisibility(View.GONE);
        statusText.setTextColor(getColorCompat(R.color.yh_text_muted));
        statusText.setTextSize(12);
        statusText.setPadding(dp(4), dp(10), dp(4), dp(4));
        int statusId = View.generateViewId();
        imgSearchStatusId = statusId;
        statusText.setId(statusId);
        root.addView(statusText);

        // 结果容器（ScrollView）
        ScrollView resultScroll = new ScrollView(this);
        resultScroll.setVisibility(View.GONE);
        int scrollId = View.generateViewId();
        imgSearchScrollId = scrollId;
        resultScroll.setId(scrollId);
        LinearLayout resultArea = new LinearLayout(this);
        resultArea.setOrientation(LinearLayout.VERTICAL);
        int areaId = View.generateViewId();
        imgSearchResultAreaId = areaId;
        resultArea.setId(areaId);
        resultScroll.addView(resultArea, new ScrollView.LayoutParams(-1, -2));
        root.addView(resultScroll, new LinearLayout.LayoutParams(-1, 0, 1));

        // 图片选择点击
        pickBtn.setOnClickListener(v -> {
            clickFeedback(v);
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            imgSearchLauncher.launch(intent);
        });

        pageContent.addView(root);
    }

    private void showImageSearchDialog() {
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this).create();
        dialog.setTitle("📷 识图识别Galgame");
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(16), dp(16), dp(16));
        imgSearchDialogRoot = root;
        buildImageSearchPage(root);
        scroll.addView(root);
        dialog.setView(scroll);
        dialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, "关闭", (d, w) -> { imgSearchDialogRoot = null; d.dismiss(); });
        dialog.setOnDismissListener(d -> imgSearchDialogRoot = null);
        dialog.show();
        styleAlertDialogDark(dialog);
    }

    private void buildImageSearchPage(LinearLayout root) {
        TextView hint = new TextView(this);
        hint.setText("上传Galgame截图，识别游戏名称和角色信息");
        hint.setTextColor(getColorCompat(R.color.yh_text_muted));
        hint.setTextSize(13);
        root.addView(hint);

        root.addView(new View(this) {{ setLayoutParams(new LinearLayout.LayoutParams(-1, dp(12))); }});

        // 图片预览区域
        FrameLayout previewArea = new FrameLayout(this);
        previewArea.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(320)));
        previewArea.setBackgroundDrawable(getDrawable(R.drawable.bg_input));
        previewArea.setPadding(dp(8), dp(8), dp(8), dp(8));

        ImageView preview = new ImageView(this);
        preview.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        preview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        preview.setVisibility(View.GONE);
        int previewId = View.generateViewId();
        imgSearchPreviewId = previewId;
        preview.setId(previewId);
        previewArea.addView(preview);

        TextView placeholder = new TextView(this);
        placeholder.setLayoutParams(new FrameLayout.LayoutParams(-2, -2, android.view.Gravity.CENTER));
        placeholder.setText("点击下方按钮选择图片");
        placeholder.setTextColor(getColorCompat(R.color.yh_text_muted));
        placeholder.setTextSize(12);
        previewArea.addView(placeholder);
        root.addView(previewArea);

        root.addView(new View(this) {{ setLayoutParams(new LinearLayout.LayoutParams(-1, dp(12))); }});

        // 按钮行
        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));

        TextView pickBtn = new TextView(this);
        pickBtn.setLayoutParams(new LinearLayout.LayoutParams(0, dp(36), 1));
        pickBtn.setGravity(android.view.Gravity.CENTER);
        pickBtn.setBackgroundDrawable(getDrawable(R.drawable.bg_yuki_button));
        pickBtn.setText("🖼 选择图片");
        pickBtn.setTextColor(0xFF071221);
        pickBtn.setTextSize(12);
        pickBtn.setTypeface(null, android.graphics.Typeface.BOLD);
        btnRow.addView(pickBtn);

        root.addView(btnRow);

        // 结果显示区域
        root.addView(new View(this) {{ setLayoutParams(new LinearLayout.LayoutParams(-1, dp(12))); }});
        LinearLayout resultArea = new LinearLayout(this);
        resultArea.setOrientation(LinearLayout.VERTICAL);
        resultArea.setId(View.generateViewId());
        root.addView(resultArea);

        // 图片选择点击
        pickBtn.setOnClickListener(v -> {
            clickFeedback(v);
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            imgSearchLauncher.launch(intent);
        });
    }

    private final ActivityResultLauncher<Intent> imgSearchLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != RESULT_OK || result.getData() == null) return;
            Uri uri = result.getData().getData();
            if (uri == null) return;
            doImageSearch(uri);
        });

    private void doImageSearch(Uri imageUri) {
        try {
            // 读取图片并压缩（API限制最大4MB）
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
            if (bitmap == null) {
                showToast("无法读取图片");
                return;
            }
            // 压缩到最大1200px宽/高
            int maxDim = 1200;
            if (bitmap.getWidth() > maxDim || bitmap.getHeight() > maxDim) {
                float scale = Math.min((float) maxDim / bitmap.getWidth(), (float) maxDim / bitmap.getHeight());
                int newW = Math.round(bitmap.getWidth() * scale);
                int newH = Math.round(bitmap.getHeight() * scale);
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, newW, newH, true);
                bitmap.recycle();
                bitmap = scaled;
            }

            // 保存到缓存文件用于multipart上传
            java.io.File cacheDir = getCacheDir();
            java.io.File tmpFile = new java.io.File(cacheDir, "imgsearch_temp.jpg");
            java.io.FileOutputStream fos = new java.io.FileOutputStream(tmpFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            fos.close();
            bitmap.recycle();

            // 立刻显示预览
            updateImagePreview(imageUri);

            runOnUiThread(() -> showToast("正在识别中，请稍候…"));

            // 调用API（用multipart上传文件）
            new Thread(() -> {
                try {
                    String result = callAnimeTraceApiWithMultipart(tmpFile);
                    android.util.Log.i("KamiGAL", "API响应: " + (result != null ? result.substring(0, Math.min(result.length(), 200)) : "null"));
                    runOnUiThread(() -> showImageSearchResult(result, imageUri));
                } catch (Exception e) {
                    android.util.Log.e("KamiGAL", "API调用失败", e);
                    runOnUiThread(() -> showToast("识别失败: " + e.toString()));
                } finally {
                    tmpFile.delete();
                }
            }).start();

        } catch (Exception e) {
            android.util.Log.e("KamiGAL", "图片处理失败", e);
            showToast("图片处理失败: " + e.toString());
        }
    }

    private String callAnimeTraceApiWithMultipart(java.io.File imageFile) throws Exception {
        String boundary = "----KamiGAL" + System.currentTimeMillis();
        String lineEnd = "\r\n";
        java.net.URL url = new java.net.URL("https://api.animetrace.com/v1/search");
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(120000);
        conn.setUseCaches(false);

        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();

        // 工具函数：写入文本字段
        byte[] boundaryBytes = ("--" + boundary + lineEnd).getBytes("UTF-8");
        byte[] lineEndBytes = lineEnd.getBytes("UTF-8");

        // file字段
        bos.write(boundaryBytes);
        bos.write(("Content-Disposition: form-data; name=\"file\"; filename=\"image.jpg\"" + lineEnd).getBytes("UTF-8"));
        bos.write(("Content-Type: image/jpeg" + lineEnd).getBytes("UTF-8"));
        bos.write(lineEndBytes);
        java.io.FileInputStream fis = new java.io.FileInputStream(imageFile);
        byte[] buf = new byte[8192];
        int n;
        while ((n = fis.read(buf)) != -1) bos.write(buf, 0, n);
        fis.close();
        bos.write(lineEndBytes);

        // model字段
        bos.write(boundaryBytes);
        bos.write(("Content-Disposition: form-data; name=\"model\"" + lineEnd).getBytes("UTF-8"));
        bos.write(lineEndBytes);
        bos.write("animetrace_high_beta".getBytes("UTF-8"));
        bos.write(lineEndBytes);

        // is_multi字段
        bos.write(boundaryBytes);
        bos.write(("Content-Disposition: form-data; name=\"is_multi\"" + lineEnd).getBytes("UTF-8"));
        bos.write(lineEndBytes);
        bos.write("1".getBytes("UTF-8"));
        bos.write(lineEndBytes);

        // 结束boundary
        bos.write(("--" + boundary + "--" + lineEnd).getBytes("UTF-8"));
        bos.flush();

        // 写入输出流
        byte[] requestData = bos.toByteArray();
        bos.close();
        conn.setRequestProperty("Content-Length", String.valueOf(requestData.length));
        java.io.OutputStream os = conn.getOutputStream();
        os.write(requestData);
        os.flush();
        os.close();

        int httpCode = conn.getResponseCode();
        java.io.InputStream is = (httpCode == 200) ? conn.getInputStream() : conn.getErrorStream();
        String resp = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A").next();
        is.close();
        conn.disconnect();
        return resp;
    }

    private void showImageSearchResult(String json, Uri imageUri) {
        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            com.google.gson.JsonObject obj = gson.fromJson(json, com.google.gson.JsonObject.class);
            if (obj == null) { showToast("返回数据为空"); return; }

            int code = obj.has("code") ? obj.get("code").getAsInt() : -1;

            if (code != 0 && code != 17720) {
                String msg = obj.has("zh_message") ? obj.get("zh_message").getAsString() : "识别失败(代码:" + code + ")";
                showToast(msg);
                return;
            }

            // data可能是数组也可能是空对象，安全处理
            com.google.gson.JsonArray data = null;
            if (obj.has("data") && obj.get("data").isJsonArray()) {
                data = obj.getAsJsonArray("data");
            }
            if (data == null || data.size() == 0) {
                showToast("未识别到游戏信息，换张清晰点的截图试试～");
                return;
            }

            // 构建结果卡片列表
            java.util.List<LinearLayout> cards = new java.util.ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                com.google.gson.JsonElement elem = data.get(i);
                if (elem == null || !elem.isJsonObject()) continue;
                com.google.gson.JsonObject item = elem.getAsJsonObject();
                boolean notConfident = item.has("not_confident") && item.get("not_confident").getAsBoolean();

                com.google.gson.JsonArray chars = null;
                if (item.has("character") && item.get("character").isJsonArray()) {
                    chars = item.getAsJsonArray("character");
                }
                if (chars == null || chars.size() == 0) continue;

                for (int j = 0; j < chars.size(); j++) {
                    com.google.gson.JsonElement chElem = chars.get(j);
                    if (chElem == null || !chElem.isJsonObject()) continue;
                    com.google.gson.JsonObject ch = chElem.getAsJsonObject();
                    String work = getJsonStr(ch, "work");
                    String character = getJsonStr(ch, "character");

                    LinearLayout card = new LinearLayout(this);
                    card.setOrientation(LinearLayout.VERTICAL);
                    card.setBackgroundDrawable(getDrawable(R.drawable.bg_game_card));
                    card.setPadding(dp(12), dp(12), dp(12), dp(12));
                    LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(-1, -2);
                    cardLp.setMargins(0, 0, 0, dp(8));
                    card.setLayoutParams(cardLp);

                    final String copyWork = work != null ? work : "";
                    final String copyChar = character != null ? character : "";

                    if (work != null) {
                        TextView tvWork = new TextView(this);
                        tvWork.setText("🎮 " + work);
                        tvWork.setTextColor(getColorCompat(R.color.yh_text));
                        tvWork.setTextSize(16);
                        tvWork.setTypeface(null, android.graphics.Typeface.BOLD);
                        card.addView(tvWork);
                    }
                    if (character != null) {
                        TextView tvChar = new TextView(this);
                        tvChar.setText("👤 角色: " + character);
                        tvChar.setTextColor(getColorCompat(R.color.yh_primary));
                        tvChar.setTextSize(14);
                        card.addView(tvChar);
                    }
                    if (notConfident) {
                        TextView tvHint = new TextView(this);
                        tvHint.setText("⚠️ 置信度较低，仅供参考");
                        tvHint.setTextColor(getColorCompat(R.color.yh_text_muted));
                        tvHint.setTextSize(10);
                        card.addView(tvHint);
                    }

                    // 复制按钮行
                    LinearLayout copyRow = new LinearLayout(this);
                    copyRow.setOrientation(LinearLayout.HORIZONTAL);
                    copyRow.setPadding(0, dp(8), 0, 0);

                    if (!copyWork.isEmpty() || !copyChar.isEmpty()) {
                        if (!copyWork.isEmpty()) {
                            TextView copyWorkBtn = new TextView(this);
                            copyWorkBtn.setText("📋 作品名");
                            copyWorkBtn.setTextColor(getColorCompat(R.color.yh_primary));
                            copyWorkBtn.setTextSize(11);
                            copyWorkBtn.setTypeface(null, android.graphics.Typeface.BOLD);
                            copyWorkBtn.setPadding(dp(8), dp(4), dp(8), dp(4));
                            copyWorkBtn.setBackgroundDrawable(getDrawable(R.drawable.bg_input));
                            copyWorkBtn.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
                            copyWorkBtn.setOnClickListener(v -> {
                                android.content.ClipboardManager cm = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                cm.setPrimaryClip(android.content.ClipData.newPlainText("KamiGAL识图", copyWork));
                                showToast("已复制作品名");
                            });
                            copyRow.addView(copyWorkBtn);
                        }
                        if (!copyChar.isEmpty()) {
                            if (!copyWork.isEmpty()) {
                                View spacer = new View(this);
                                spacer.setLayoutParams(new LinearLayout.LayoutParams(dp(8), -1));
                                copyRow.addView(spacer);
                            }
                            TextView copyCharBtn = new TextView(this);
                            copyCharBtn.setText("📋 角色名");
                            copyCharBtn.setTextColor(getColorCompat(R.color.yh_primary));
                            copyCharBtn.setTextSize(11);
                            copyCharBtn.setTypeface(null, android.graphics.Typeface.BOLD);
                            copyCharBtn.setPadding(dp(8), dp(4), dp(8), dp(4));
                            copyCharBtn.setBackgroundDrawable(getDrawable(R.drawable.bg_input));
                            copyCharBtn.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
                            copyCharBtn.setOnClickListener(v -> {
                                android.content.ClipboardManager cm = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                cm.setPrimaryClip(android.content.ClipData.newPlainText("KamiGAL识图", copyChar));
                                showToast("已复制角色名");
                            });
                            copyRow.addView(copyCharBtn);
                        }
                    }

                    card.addView(copyRow);
                    cards.add(card);
                }
            }

            if (cards.isEmpty()) {
                showToast("未识别到游戏信息，换张清晰点的截图试试～");
                return;
            }

            // 更新预览
            updateImagePreview(imageUri);

            if (isSearchDialogShowing) {
                // 横屏对话框模式 → 直接在对话框中显示结果
                if (imgSearchDialogRoot != null) {
                    imgSearchDialogRoot.removeAllViews();
                    TextView title = new TextView(this);
                    title.setText("🎯 识别结果");
                    title.setTextColor(getColorCompat(R.color.yh_text));
                    title.setTextSize(18);
                    title.setTypeface(null, android.graphics.Typeface.BOLD);
                    title.setPadding(0, 0, 0, dp(12));
                    imgSearchDialogRoot.addView(title);
                    for (LinearLayout card : cards) imgSearchDialogRoot.addView(card);
                }
            } else if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                // 横屏但对话框已被系统关闭 → 重新弹结果对话框
                android.app.AlertDialog resultDialog = new android.app.AlertDialog.Builder(this).create();
                resultDialog.setTitle("🎯 识别结果");
                ScrollView scroll = new ScrollView(this);
                LinearLayout root = new LinearLayout(this);
                root.setOrientation(LinearLayout.VERTICAL);
                root.setPadding(dp(12), dp(12), dp(12), dp(12));
                for (LinearLayout card : cards) root.addView(card);
                scroll.addView(root);
                resultDialog.setView(scroll);
                resultDialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, "关闭", (d, w) -> d.dismiss());
                resultDialog.show();
                styleAlertDialogDark(resultDialog);
            } else {
                // 竖屏模式 → 直接在页面内显示
                ScrollView resultScroll = findViewById(imgSearchScrollId);
                LinearLayout resultArea = findViewById(imgSearchResultAreaId);
                TextView statusText = findViewById(imgSearchStatusId);
                if (resultArea != null && resultScroll != null) {
                    resultScroll.setVisibility(View.VISIBLE);
                    resultArea.removeAllViews();
                    for (LinearLayout card : cards) resultArea.addView(card);
                }
                if (statusText != null) {
                    statusText.setVisibility(View.VISIBLE);
                    statusText.setText("找到 " + cards.size() + " 个结果");
                }
            }

        } catch (Exception e) {
            String errMsg = "解析失败: " + e.toString();
            if (json != null && json.length() > 0) {
                errMsg += " | 响应: " + (json.length() > 150 ? json.substring(0, 150) : json);
            }
            // 把堆栈写入日志
            try {
                java.io.StringWriter sw = new java.io.StringWriter();
                java.io.PrintWriter pw = new java.io.PrintWriter(sw);
                e.printStackTrace(pw);
                android.util.Log.e("KamiGAL", "解析异常堆栈:\n" + sw.toString());
            } catch (Throwable ignored) {}
            showToast(errMsg);
        }
    }

    private String getJsonStr(com.google.gson.JsonObject obj, String key) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            String val = obj.get(key).getAsString();
            return val.trim().isEmpty() ? null : val;
        }
        return null;
    }

    private void updateImagePreview(Uri uri) {
        if (imgSearchPreviewId <= 0) return;
        try {
            Bitmap bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
            if (bmp == null) return;
            int maxDim = 1200;
            if (bmp.getWidth() > maxDim || bmp.getHeight() > maxDim) {
                float scale = Math.min((float) maxDim / bmp.getWidth(), (float) maxDim / bmp.getHeight());
                Bitmap scaled = Bitmap.createScaledBitmap(bmp, Math.round(bmp.getWidth() * scale), Math.round(bmp.getHeight() * scale), true);
                bmp.recycle();
                bmp = scaled;
            }
            final Bitmap finalBmp = bmp;
            runOnUiThread(() -> {
                // 优先在对话框根视图中查找（横屏），否则在Activity视图树中查找（竖屏）
                ImageView iv = null;
                if (imgSearchDialogRoot != null) {
                    iv = imgSearchDialogRoot.findViewById(imgSearchPreviewId);
                }
                if (iv == null) {
                    iv = findViewById(imgSearchPreviewId);
                }
                if (iv != null) {
                    iv.setImageBitmap(finalBmp);
                    iv.setVisibility(View.VISIBLE);
                    ViewGroup parent = (ViewGroup) iv.getParent();
                    if (parent != null && parent.getChildCount() > 1) {
                        parent.getChildAt(1).setVisibility(View.GONE);
                    }
                }
            });
        } catch (Exception ignored) {}
    }

    private void showToast(String msg) {
        runOnUiThread(() -> android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show());
    }
    private String emptyText(String s, String fallback) { return s == null || s.trim().isEmpty() ? fallback : s; }
}