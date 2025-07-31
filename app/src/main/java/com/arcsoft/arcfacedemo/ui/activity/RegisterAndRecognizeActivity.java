package com.arcsoft.arcfacedemo.ui.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.common.MatiposRequestServer;
import com.arcsoft.arcfacedemo.common.MatiposResponseServer;
import com.arcsoft.arcfacedemo.databinding.ActivityRegisterAndRecognizeBinding;
import com.arcsoft.arcfacedemo.facedb.entity.MovementEntity;
import com.arcsoft.arcfacedemo.matiposserver.MatiposServer;
import com.arcsoft.arcfacedemo.matiposserver.WebSocketCallback;
import com.arcsoft.arcfacedemo.matiposserver.WebSocketManager;
import com.arcsoft.arcfacedemo.ui.model.PreviewConfig;
import com.arcsoft.arcfacedemo.ui.viewmodel.MatiposViewModel;
import com.arcsoft.arcfacedemo.ui.viewmodel.RecognizeViewModel;
import com.arcsoft.arcfacedemo.util.ConfigUtil;
import com.arcsoft.arcfacedemo.util.ErrorCodeUtil;
import com.arcsoft.arcfacedemo.util.FaceRectTransformer;
import com.arcsoft.arcfacedemo.util.camera.CameraListener;
import com.arcsoft.arcfacedemo.util.camera.DualCameraHelper;
import com.arcsoft.arcfacedemo.util.face.constants.LivenessType;
import com.arcsoft.arcfacedemo.util.face.model.FacePreviewInfo;
import com.arcsoft.arcfacedemo.util.gpios.Gpio;
import com.arcsoft.arcfacedemo.widget.FaceRectView;
import com.arcsoft.arcfacedemo.widget.RecognizeAreaView;
import com.arcsoft.face.ErrorInfo;
import com.common.CommonConstants;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class RegisterAndRecognizeActivity extends BaseActivity implements ViewTreeObserver.OnGlobalLayoutListener, WebSocketCallback {
    private static final String TAG = "RegisterAndRecognize";

    private DualCameraHelper rgbCameraHelper;
    private DualCameraHelper irCameraHelper;
    private FaceRectTransformer rgbFaceRectTransformer;
    private FaceRectTransformer irFaceRectTransformer;

    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;

    int actionAfterFinish = 0;
    private static final int NAVIGATE_TO_RECOGNIZE_SETTINGS_ACTIVITY = 1;
    private static final int NAVIGATE_TO_RECOGNIZE_DEBUG_ACTIVITY = 2;
    private static final int NAVIGATE_TO_HOME_ACTIVITY = 3;

    /**
     * 所需的所有权限信息
     */
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE
    };
    private ActivityRegisterAndRecognizeBinding binding;
    private RecognizeViewModel recognizeViewModel;
    private LivenessType livenessType;
    private boolean enableLivenessDetect = false;
    private RecognizeAreaView recognizeAreaView;
    private TextView textViewRgb;
    private TextView textViewIr;
    private boolean openRectInfoDraw;
    private int touchCounter = 0;
    private MediaPlayer mediaPlayerInfoMessage;
    private AlertDialog dialog;
    private MatiposViewModel matiposViewModel;
    private MatiposResponseServer matiposResponseServer;
    private boolean inProgres = false;
    private Gpio gpio;
    private long currentFaceID;

    // Variables to websocket
    private WebSocketManager socketManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register_and_recognize);
        gpio = new Gpio();

        //保持亮屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        getWindow().setAttributes(attributes);

        // Activity启动后就锁定为启动时的方向
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        initData();
        initViewModel();
        initView();
        openRectInfoDraw = false;
        recognizeViewModel.setDrawRectInfoTextValue(true);
        matiposViewModel.stopReaders(RegisterAndRecognizeActivity.this);

        // TODO: Verificar
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciJ9.CVqQYFCwzzRmpR-gf-fSwMSZQHMmGccslAWPj7X7LfM";
        String url = "ws://192.168.1.76:8000/telpo/ws";

        socketManager = new WebSocketManager(url, token, this);
        socketManager.connect();

    }

    private void initReaders() {
        matiposViewModel.startReaders(RegisterAndRecognizeActivity.this, ConfigUtil.isMatiposIsQrReaderEnable(getApplicationContext()), ConfigUtil.isMatiposIsNfcReaderEnable(getApplicationContext()));

        if (ConfigUtil.isMatiposIsQrReaderEnable(getApplicationContext()))
            matiposViewModel.getQrValidationCode().observe(this, this::stopReaders);

        if (ConfigUtil.isMatiposIsNfcReaderEnable(getApplicationContext()))
            matiposViewModel.getNfcValidationCode().observe(this, this::stopReaders);
    }

    private void stopReaders(String data) {
        if (data.length() > 4 && touchCounter <= 2) {

            // Update dialog with message validate code with server
            if (dialog != null) {
                dialog.dismiss();
            }

            // Build dialog info
            AlertDialog.Builder builder = new AlertDialog.Builder(RegisterAndRecognizeActivity.this);
            LayoutInflater inflater = RegisterAndRecognizeActivity.this.getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_validate_code, null);
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            builder.setView(view);
            builder.setCancelable(false);
            LinearLayout linearLayout = view.findViewById(R.id.linea_layout);
            TextView title = view.findViewById(R.id.title);
            TextView message = view.findViewById(R.id.message);
            title.setText("Validando codigo");
            message.setText(data);
            linearLayout.setBackgroundResource(R.drawable.rounded);
            dialog = builder.create();
            dialog.show();

            // Disable qrReader
            matiposViewModel.stopReaders(RegisterAndRecognizeActivity.this);

            inProgres = true;
            if (currentFaceID == 0)
                validateCode(RegisterAndRecognizeActivity.this, data);
            else
                validateCode(RegisterAndRecognizeActivity.this, data, currentFaceID);

        }
    }

    private void initData() {
        String livenessTypeStr = ConfigUtil.getLivenessDetectType(this);
        if (livenessTypeStr.equals((getString(R.string.value_liveness_type_rgb)))) {
            livenessType = LivenessType.RGB;
        } else if (livenessTypeStr.equals(getString(R.string.value_liveness_type_ir))) {
            livenessType = LivenessType.IR;
        } else {
            livenessType = null;
        }
        enableLivenessDetect = !ConfigUtil.getLivenessDetectType(this).equals(getString(R.string.value_liveness_type_disable));
    }

    private void initViewModel() {
        recognizeViewModel = new ViewModelProvider(
                getViewModelStore(),
                new ViewModelProvider.AndroidViewModelFactory(getApplication())
        ).get(RecognizeViewModel.class);

        recognizeViewModel.setLiveType(livenessType);
        recognizeViewModel.getFtInitCode().observe(this, ftInitCode -> {
            if (ftInitCode != ErrorInfo.MOK) {
                String error = getString(R.string.specific_engine_init_failed, "ftEngine",
                        ftInitCode, ErrorCodeUtil.arcFaceErrorCodeToFieldName(ftInitCode));
                Log.i(TAG, "initEngine: " + error);
                showToast(error);
            }
        });
        recognizeViewModel.getFrInitCode().observe(this, frInitCode -> {
            if (frInitCode != ErrorInfo.MOK) {
                String error = getString(R.string.specific_engine_init_failed, "frEngine",
                        frInitCode, ErrorCodeUtil.arcFaceErrorCodeToFieldName(frInitCode));
                Log.i(TAG, "initEngine: " + error);
                showToast(error);
            }
        });
        recognizeViewModel.getFlInitCode().observe(this, flInitCode -> {
            if (flInitCode != ErrorInfo.MOK) {
                String error = getString(R.string.specific_engine_init_failed, "flEngine",
                        flInitCode, ErrorCodeUtil.arcFaceErrorCodeToFieldName(flInitCode));
                Log.i(TAG, "initEngine: " + error);
                showToast(error);
            }
        });
        recognizeViewModel.getFaceItemEventMutableLiveData().observe(this, faceItemEvent -> {
            RecyclerView.Adapter adapter = binding.dualCameraRecyclerViewPerson.getAdapter();
            switch (faceItemEvent.getEventType()) {
                case REMOVED:
                    try {
                        if (adapter != null) {
                            adapter.notifyItemRemoved(faceItemEvent.getIndex());
                        }
                    } catch (Exception ignored) {

                    }
                    break;
                case INSERTED:
                    try {
                        if (adapter != null && !inProgres) {
                            if (touchCounter <= 2) {
                                boolean isInputDevice = ConfigUtil.isInputDevice(getApplicationContext());
                                if (!isInputDevice) {
                                    if (!ConfigUtil.isMatiposIsQrReaderEnable(getApplicationContext()) && !ConfigUtil.isMatiposIsNfcReaderEnable(getApplicationContext())) {

                                        MatiposRequestServer requestServer = new MatiposRequestServer(
                                                "FACE_DETECTION",
                                                "",
                                                ""
                                        );

                                        MatiposResponseServer responseServer = new MatiposResponseServer(
                                                Boolean.TRUE,
                                                "FaceRecognigtion",
                                                LocalDateTime.now().toString(),
                                                "Validacion rostro exitosa"
                                        );

                                        MovementEntity movementEntity = new MovementEntity();
                                        movementEntity.operationType = "VALIDATE";
                                        movementEntity.requestData = requestServer.toString();
                                        movementEntity.requestDatetime = LocalDateTime.now().toString();
                                        movementEntity.faceId = (int) faceItemEvent.getFaceEntity().getFaceId();
                                        movementEntity.responseData = responseServer.toString();
                                        movementEntity.responseDatetime = LocalDateTime.now().toString();

                                        new MatiposServer().insertLogMovement(getApplicationContext(), movementEntity);

                                        inProgres = Boolean.TRUE;
                                        showDialog(responseServer);

                                    } else {

                                        if (dialog != null) {
                                            dialog.dismiss();
                                        }

                                        // Build dialog of register user
                                        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterAndRecognizeActivity.this);
                                        LayoutInflater inflater = getLayoutInflater();
                                        View dialogView = inflater.inflate(R.layout.custom_dialog, null);
                                        dialogView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                                        builder.setView(dialogView);

                                        builder.setTitle("Rostro Identificado, por favor leer el codigo");
                                        builder.setCancelable(false);

                                        // Enable QrReader
                                        matiposViewModel.stopReaders(getApplicationContext());

                                        dialog = builder.create();
                                        dialog.show();

                                        currentFaceID = faceItemEvent.getFaceEntity().getFaceId();

                                        initReaders();
                                    }
                                } else {
                                    if (dialog != null) {
                                        dialog.dismiss();
                                    }

                                    if (!ConfigUtil.isMatiposIsQrReaderEnable(getApplicationContext()) && !ConfigUtil.isMatiposIsNfcReaderEnable(getApplicationContext())) {
                                        MatiposRequestServer requestServer = new MatiposRequestServer(
                                                "FACE_DETECTION",
                                                "",
                                                ""
                                        );

                                        MatiposResponseServer responseServer = new MatiposResponseServer(
                                                Boolean.TRUE,
                                                "FaceRecognigtion",
                                                LocalDateTime.now().toString(),
                                                "Validacion rostro exitosa"
                                        );

                                        MovementEntity movementEntity = new MovementEntity();
                                        movementEntity.operationType = "VALIDATE";
                                        movementEntity.requestData = requestServer.toString();
                                        movementEntity.requestDatetime = LocalDateTime.now().toString();
                                        movementEntity.faceId = (int) faceItemEvent.getFaceEntity().getFaceId();
                                        movementEntity.responseData = responseServer.toString();
                                        movementEntity.responseDatetime = LocalDateTime.now().toString();

                                        new MatiposServer().insertLogMovement(getApplicationContext(), movementEntity);

                                        inProgres = Boolean.TRUE;
                                        showDialog(responseServer);
                                    } else {
                                        // Build dialog of register user
                                        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterAndRecognizeActivity.this);
                                        LayoutInflater inflater = getLayoutInflater();
                                        View dialogView = inflater.inflate(R.layout.custom_dialog, null);
                                        dialogView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                                        builder.setView(dialogView);

                                        builder.setTitle("Rostro ya registrado");
                                        builder.setCancelable(false);

                                        dialog = builder.create();
                                        dialog.show();
                                    }

                                    currentFaceID = 0;

                                    // Enable QrReader
                                    matiposViewModel.stopReaders(getApplicationContext());

                                }
                            }

                            adapter.notifyItemInserted(faceItemEvent.getIndex());
                        }
                    } catch (Exception ignored) {

                    }
                    break;
                default:
                    break;
            }
        });
        recognizeViewModel.getRecognizeConfiguration().observe(this, recognizeConfiguration -> {
            Log.i(TAG, "initViewModel recognizeConfiguration: " + recognizeConfiguration.toString());
        });
        recognizeViewModel.setOnRegisterFinishedCallback((facePreviewInfo, success) -> {
            showToast(success ? "register success" : "register failed");
            if (success) {
                matiposViewModel.updateFaceIdInMovement(RegisterAndRecognizeActivity.this, (int) matiposResponseServer.getIdMovement());
            }
        });
        recognizeViewModel.getRecognizeNotice().observe(this, notice -> binding.setRecognizeNotice(notice));
        recognizeViewModel.getDrawRectInfoText().observe(this, drawRectInfoText -> {
            binding.setDrawRectInfoText(drawRectInfoText);
        });

        // Matipos viewModel
        matiposViewModel = new ViewModelProvider(
                getViewModelStore(),
                new ViewModelProvider.AndroidViewModelFactory(getApplication())
        ).get(MatiposViewModel.class);


        matiposViewModel.getMatiposResponse().observe(this, this::showDialog);

        matiposViewModel.IsProcessEnding().observe(this, isEnd -> {
            if (isEnd) {
                if (dialog != null)
                    dialog.dismiss();

                inProgres = false;
                dialog = null;
            }
        });

    }

    private void showDialog(MatiposResponseServer response) {
        if (!inProgres)
            return;

        if (dialog != null)
            dialog.dismiss();

        // Star led module
        int ledColor = CommonConstants.LedColor.WHITE_LED;
        int ledSecondsInOn = ConfigUtil.getMatiposSecondsEnableLed(RegisterAndRecognizeActivity.this) * 1000;

        // Show dialog with information
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterAndRecognizeActivity.this);
        LayoutInflater inflater = RegisterAndRecognizeActivity.this.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_matipos_server_response, null);
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        builder.setView(view);
        builder.setCancelable(false);
        LinearLayout linearLayout = view.findViewById(R.id.validate_code_container);

        if (mediaPlayerInfoMessage != null) {
            mediaPlayerInfoMessage.stop();
            mediaPlayerInfoMessage.release();
            mediaPlayerInfoMessage = null;
        }

        // Message information
        if (response.getStatus() != null) {
            TextView textView = linearLayout.findViewById(R.id.title);
            textView.setText(response.getAns());
            ledColor = response.getStatus() ? CommonConstants.LedColor.GREEN_LED : CommonConstants.LedColor.RED_LED;
            linearLayout.setBackgroundResource(response.getStatus() ? R.drawable.ok : R.drawable.no);

            int audioId = ConfigUtil.isInputDevice(RegisterAndRecognizeActivity.this) ? R.raw.ok : R.raw.ok_salida;
            mediaPlayerInfoMessage = MediaPlayer.create(RegisterAndRecognizeActivity.this, response.getStatus() ? audioId : R.raw.no);

        } else {
            linearLayout.setBackgroundResource(R.drawable.warning);
            TextView textView = linearLayout.findViewById(R.id.title);
            textView.setText(response.getAns());
            mediaPlayerInfoMessage = MediaPlayer.create(RegisterAndRecognizeActivity.this, R.raw.warning);
        }

        // Turn ON Led
        mediaPlayerInfoMessage.start();
        gpio.toggle(RegisterAndRecognizeActivity.this, CommonConstants.LedType.FILL_LIGHT_1, ledColor, ledSecondsInOn);

        dialog = builder.create();
        dialog.show();

        matiposResponseServer = response;

        if (ConfigUtil.isInputDevice(getApplicationContext())) {
            try {
                // if (response.getStatus())
                if (ConfigUtil.isMatiposIsQrReaderEnable(getApplicationContext()) || ConfigUtil.isMatiposIsNfcReaderEnable(getApplicationContext()))
                    recognizeViewModel.prepareRegister();
            } catch (Exception e) {
                showToast(e.getMessage());
            }
        }
        matiposViewModel.startProcessToEndingValidateCode(mediaPlayerInfoMessage, ledSecondsInOn);
    }

    private void initView() {

        findViewById(R.id.frame_camera).setOnClickListener(v -> {
            if (touchCounter > 2) {

                // TODO: Back to menu using password
                this.actionAfterFinish = NAVIGATE_TO_HOME_ACTIVITY;

                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterAndRecognizeActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View view = inflater.inflate(R.layout.dialog_custom_login, null);
                view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                builder.setView(view);

                builder.setTitle("Ir a menu aplicación");

                // Boton OK
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        TextInputEditText username = view.findViewById(R.id.username);
                        TextInputEditText password = view.findViewById(R.id.password);

                        if (Objects.requireNonNull(username.getText()).toString().equals(ConfigUtil.getAdminUsername(getApplicationContext()))
                                && Objects.requireNonNull(password.getText()).toString().equals(ConfigUtil.getAdminPassword(getApplicationContext()))) {
                            if (gpio != null)
                                gpio.off();

                            finish();
                        } else
                            showToast("Usuario o Contraseña incorrecto");

                        touchCounter = 0;
                    }
                });

                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                dialog = builder.create();
                dialog.show();

            }
            touchCounter++;
        });

        if (!DualCameraHelper.hasDualCamera() || livenessType != LivenessType.IR) {
            binding.flRecognizeIr.setVisibility(View.GONE);
        }
        //在布局结束后才做初始化操作
        binding.dualCameraTexturePreviewRgb.getViewTreeObserver().addOnGlobalLayoutListener(this);
        binding.setCompareResultList(recognizeViewModel.getCompareResultList().getValue());

        if (ConfigUtil.isWhiteLightEnable(RegisterAndRecognizeActivity.this)) {
            gpio.write(RegisterAndRecognizeActivity.this, CommonConstants.LedType.FILL_LIGHT_1, CommonConstants.LedColor.WHITE_LED, 255);
        } else {
            gpio.write(RegisterAndRecognizeActivity.this, CommonConstants.LedType.FILL_LIGHT_1, CommonConstants.LedColor.WHITE_LED, 0);
            if (gpio != null)
                gpio.off();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (matiposViewModel != null) {
            matiposViewModel.stopReaders(RegisterAndRecognizeActivity.this);
        }

        if (irCameraHelper != null) {
            irCameraHelper.release();
            irCameraHelper = null;
        }

        if (rgbCameraHelper != null) {
            rgbCameraHelper.release();
            rgbCameraHelper = null;
        }

        recognizeViewModel.destroy();
        switch (actionAfterFinish) {
            case NAVIGATE_TO_RECOGNIZE_DEBUG_ACTIVITY:
                navigateToNewPage(RecognizeDebugActivity.class);
                break;
            case NAVIGATE_TO_RECOGNIZE_SETTINGS_ACTIVITY:
                navigateToNewPage(RecognizeSettingsActivity.class);
                break;
            default:
                navigateToNewPage(HomeActivity.class);
                break;
        }

        if (socketManager != null) {
            socketManager.close();
            socketManager = null;
        }
    }


    /**
     * 调整View的宽高，使2个预览同时显示
     *
     * @param previewView        显示预览数据的view
     * @param faceRectView       画框的view
     * @param previewSize        预览大小
     * @param displayOrientation 相机旋转角度
     * @return 调整后的LayoutParams
     */
    private ViewGroup.LayoutParams adjustPreviewViewSize(View rgbPreview, View previewView, FaceRectView faceRectView, Camera.Size previewSize, int displayOrientation, float scale) {
        ViewGroup.LayoutParams layoutParams = previewView.getLayoutParams();
        int measuredWidth = previewView.getMeasuredWidth();
        int measuredHeight = previewView.getMeasuredHeight();
        float ratio = ((float) previewSize.height) / (float) previewSize.width;
        if (ratio > 1) {
            ratio = 1 / ratio;
        }
        if (displayOrientation % 180 == 0) {
            layoutParams.width = measuredWidth;
            layoutParams.height = (int) (measuredWidth * ratio);
        } else {
            layoutParams.height = measuredHeight;
            layoutParams.width = (int) (measuredHeight * ratio);
        }
        if (scale < 1f) {
            ViewGroup.LayoutParams rgbParam = rgbPreview.getLayoutParams();
            layoutParams.width = (int) (rgbParam.width * scale);
            layoutParams.height = (int) (rgbParam.height * scale);
        } else {
            layoutParams.width *= scale;
            layoutParams.height *= scale;
        }

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        if (layoutParams.width >= metrics.widthPixels) {
            float viewRatio = layoutParams.width / ((float) metrics.widthPixels);
            layoutParams.width /= viewRatio;
            layoutParams.height /= viewRatio;
        }
        if (layoutParams.height >= metrics.heightPixels) {
            float viewRatio = layoutParams.height / ((float) metrics.heightPixels);
            layoutParams.width /= viewRatio;
            layoutParams.height /= viewRatio;
        }

        previewView.setLayoutParams(layoutParams);
        faceRectView.setLayoutParams(layoutParams);
        return layoutParams;
    }

    private void initRgbCamera() {
        CameraListener cameraListener = new CameraListener() {
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                runOnUiThread(() -> {
                    Camera.Size previewSizeRgb = camera.getParameters().getPreviewSize();
                    ViewGroup.LayoutParams layoutParams = adjustPreviewViewSize(binding.dualCameraTexturePreviewRgb,
                            binding.dualCameraTexturePreviewRgb, binding.dualCameraFaceRectView,
                            previewSizeRgb, displayOrientation, 1.0f);
                    rgbFaceRectTransformer = new FaceRectTransformer(previewSizeRgb.width, previewSizeRgb.height,
                            layoutParams.width, layoutParams.height, displayOrientation, cameraId, isMirror,
                            ConfigUtil.isDrawRgbRectHorizontalMirror(RegisterAndRecognizeActivity.this),
                            ConfigUtil.isDrawRgbRectVerticalMirror(RegisterAndRecognizeActivity.this));

                    FrameLayout parentView = ((FrameLayout) binding.dualCameraTexturePreviewRgb.getParent());
                    if (textViewRgb == null) {
                        textViewRgb = new TextView(RegisterAndRecognizeActivity.this, null);
                    } else {
                        parentView.removeView(textViewRgb);
                    }
                    textViewRgb.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    // textViewRgb.setText(getString(R.string.camera_rgb_preview_size, previewSizeRgb.width, previewSizeRgb.height));
                    textViewRgb.setTextColor(Color.WHITE);
                    textViewRgb.setBackgroundColor(getResources().getColor(R.color.color_bg_notification));
                    parentView.addView(textViewRgb);

                    // 父View宽度和子View一致，保持居中
                    ViewGroup.LayoutParams parentLayoutParams = parentView.getLayoutParams();
                    parentLayoutParams.width = layoutParams.width;
                    parentView.setLayoutParams(parentLayoutParams);

                    // 添加recognizeAreaView，在识别区域发生变更时，更新数据给FaceHelper
                    if (ConfigUtil.isRecognizeAreaLimited(RegisterAndRecognizeActivity.this)) {
                        if (recognizeAreaView == null) {
                            recognizeAreaView = new RecognizeAreaView(RegisterAndRecognizeActivity.this);
                            recognizeAreaView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                        } else {
                            parentView.removeView(recognizeAreaView);
                        }
                        recognizeAreaView.setOnRecognizeAreaChangedListener(recognizeArea -> recognizeViewModel.setRecognizeArea(recognizeArea));
                        parentView.addView(recognizeAreaView);
                    }

                    recognizeViewModel.onRgbCameraOpened(camera);
                    recognizeViewModel.setRgbFaceRectTransformer(rgbFaceRectTransformer);
                });
            }

            @Override
            public void onPreview(final byte[] nv21, Camera camera) {
                binding.dualCameraFaceRectView.clearFaceInfo();
                List<FacePreviewInfo> facePreviewInfoList = recognizeViewModel.onPreviewFrame(nv21, true);
                if (facePreviewInfoList != null && rgbFaceRectTransformer != null) {
                    drawPreviewInfo(facePreviewInfoList);

                    if (facePreviewInfoList.size() != 0) {
                        if (dialog == null) {
                            if (ConfigUtil.isMatiposIsQrReaderEnable(getApplicationContext()) || ConfigUtil.isMatiposIsNfcReaderEnable(getApplicationContext())) {

                                boolean isInputDevice = ConfigUtil.isInputDevice(getApplicationContext());

                                // TODO: Enable to production
                                if (isInputDevice) {
                                    if (mediaPlayerInfoMessage == null) {
                                        mediaPlayerInfoMessage = MediaPlayer.create(RegisterAndRecognizeActivity.this, R.raw.info);
                                        mediaPlayerInfoMessage.start();
                                    }
                                }

                                // Build dialog of register user
                                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterAndRecognizeActivity.this);
                                LayoutInflater inflater = getLayoutInflater();
                                View dialogView = inflater.inflate(R.layout.custom_dialog, null);
                                dialogView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                                builder.setView(dialogView);

                                builder.setTitle(isInputDevice ? "Rostro Identificado, por favor leer codigo para ingreso" : "Rostro Identificado, validando....");
                                builder.setCancelable(false);

                                // Enable QrReader
                                if (isInputDevice)
                                    initReaders();

                                dialog = builder.create();
                                dialog.show();
                            }
                        }

                    } else {
                        try {
                            if (!inProgres) {
                                if (dialog != null && touchCounter <= 2) {
                                    dialog.dismiss();
                                    dialog = null;
                                }

                                if (mediaPlayerInfoMessage != null) {
                                    mediaPlayerInfoMessage.stop();
                                    mediaPlayerInfoMessage.release();
                                    mediaPlayerInfoMessage = null;
                                }

                                if (matiposViewModel != null)
                                    matiposViewModel.stopReaders(RegisterAndRecognizeActivity.this);
                            }
                        } catch (Exception ignored) {
                            Log.i(TAG, "onPreview: ");
                        }
                    }
                }
                recognizeViewModel.clearLeftFace(facePreviewInfoList);
            }

            @Override
            public void onCameraClosed() {
                Log.i(TAG, "onCameraClosed: ");
            }

            @Override
            public void onCameraError(Exception e) {
                Log.i(TAG, "onCameraError: " + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
                Log.i(TAG, "onCameraConfigurationChanged:" + Thread.currentThread().getName());
                if (rgbFaceRectTransformer != null) {
                    rgbFaceRectTransformer.setCameraDisplayOrientation(displayOrientation);
                }
                Log.i(TAG, "onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
            }
        };

        PreviewConfig previewConfig = recognizeViewModel.getPreviewConfig();
        rgbCameraHelper = new DualCameraHelper.Builder()
                .previewViewSize(new Point(binding.dualCameraTexturePreviewRgb.getMeasuredWidth(), binding.dualCameraTexturePreviewRgb.getMeasuredHeight()))
                .rotation(getWindowManager().getDefaultDisplay().getRotation())
                .additionalRotation(previewConfig.getRgbAdditionalDisplayOrientation())
                .previewSize(recognizeViewModel.loadPreviewSize())
                .specificCameraId(previewConfig.getRgbCameraId())
                .isMirror(ConfigUtil.isDrawRgbPreviewHorizontalMirror(this))
                .previewOn(binding.dualCameraTexturePreviewRgb)
                .cameraListener(cameraListener)
                .build();
        rgbCameraHelper.init();
        rgbCameraHelper.start();
    }

    /**
     * 初始化红外相机，若活体检测类型是可见光活体检测或不启用活体，则不需要启用
     */
    private void initIrCamera() {
        if (livenessType == LivenessType.RGB || !enableLivenessDetect) {
            return;
        }
        CameraListener irCameraListener = new CameraListener() {
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                Camera.Size previewSizeIr = camera.getParameters().getPreviewSize();
                ViewGroup.LayoutParams layoutParams = adjustPreviewViewSize(binding.dualCameraTexturePreviewRgb,
                        binding.dualCameraTexturePreviewIr, binding.dualCameraFaceRectViewIr,
                        previewSizeIr, displayOrientation, 0.25f);

                irFaceRectTransformer = new FaceRectTransformer(previewSizeIr.width, previewSizeIr.height,
                        layoutParams.width, layoutParams.height, displayOrientation, cameraId, isMirror,
                        ConfigUtil.isDrawIrRectHorizontalMirror(RegisterAndRecognizeActivity.this),
                        ConfigUtil.isDrawIrRectVerticalMirror(RegisterAndRecognizeActivity.this));

                FrameLayout parentView = ((FrameLayout) binding.dualCameraTexturePreviewIr.getParent());
                if (textViewIr == null) {
                    textViewIr = new TextView(RegisterAndRecognizeActivity.this, null);
                } else {
                    parentView.removeView(textViewIr);
                }
                textViewIr.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                textViewIr.setText(getString(R.string.camera_ir_preview_size, previewSizeIr.width, previewSizeIr.height));
                textViewIr.setTextColor(Color.WHITE);
                textViewIr.setBackgroundColor(getResources().getColor(R.color.color_bg_notification));
                parentView.addView(textViewIr);

                recognizeViewModel.onIrCameraOpened(camera);
                recognizeViewModel.setIrFaceRectTransformer(irFaceRectTransformer);
            }


            @Override
            public void onPreview(final byte[] nv21, Camera camera) {
                recognizeViewModel.refreshIrPreviewData(nv21);
            }

            @Override
            public void onCameraClosed() {
                Log.i(TAG, "onCameraClosed: ");
            }

            @Override
            public void onCameraError(Exception e) {
                Log.i(TAG, "onCameraError: " + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
                if (irFaceRectTransformer != null) {
                    irFaceRectTransformer.setCameraDisplayOrientation(displayOrientation);
                }
                Log.i(TAG, "onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
            }
        };

        PreviewConfig previewConfig = recognizeViewModel.getPreviewConfig();
        irCameraHelper = new DualCameraHelper.Builder()
                .previewViewSize(new Point(binding.dualCameraTexturePreviewIr.getMeasuredWidth(), binding.dualCameraTexturePreviewIr.getMeasuredHeight()))
                .rotation(getWindowManager().getDefaultDisplay().getRotation())
                .specificCameraId(previewConfig.getIrCameraId())
                .previewOn(binding.dualCameraTexturePreviewIr)
                .cameraListener(irCameraListener)
                .isMirror(ConfigUtil.isDrawIrPreviewHorizontalMirror(this))
                .previewSize(recognizeViewModel.loadPreviewSize()) //相机预览大小设置，RGB与IR需使用相同大小
                .additionalRotation(previewConfig.getIrAdditionalDisplayOrientation()) //额外旋转角度
                .build();
        irCameraHelper.init();
        try {
            irCameraHelper.start();
        } catch (RuntimeException e) {
            showToast(e.getMessage() + getString(R.string.camera_error_notice));
        }
    }


    /**
     * 绘制RGB、IR画面的实时人脸信息
     *
     * @param facePreviewInfoList RGB画面的实时人脸信息
     */
    private void drawPreviewInfo(List<FacePreviewInfo> facePreviewInfoList) {
        if (rgbFaceRectTransformer != null) {
            List<FaceRectView.DrawInfo> rgbDrawInfoList = recognizeViewModel.getDrawInfo(facePreviewInfoList, LivenessType.RGB, openRectInfoDraw);
            binding.dualCameraFaceRectView.drawRealtimeFaceInfo(rgbDrawInfoList);
        }
        if (irFaceRectTransformer != null) {
            List<FaceRectView.DrawInfo> irDrawInfoList = recognizeViewModel.getDrawInfo(facePreviewInfoList, LivenessType.IR, openRectInfoDraw);
            binding.dualCameraFaceRectViewIr.drawRealtimeFaceInfo(irDrawInfoList);
        }
    }

    @Override
    protected void afterRequestPermission(int requestCode, boolean isAllGranted) {
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            if (isAllGranted) {
                recognizeViewModel.init();
                initRgbCamera();
                if (DualCameraHelper.hasDualCamera() && livenessType == LivenessType.IR) {
                    initIrCamera();
                }
            } else {
                showToast(getString(R.string.permission_denied));
            }
        }
    }

    public void openRectInfoDraw(View view) {
        openRectInfoDraw = !openRectInfoDraw;
        recognizeViewModel.setDrawRectInfoTextValue(openRectInfoDraw);
    }

    /**
     * 将准备注册的状态置为待注册
     *
     * @param view 注册按钮
     */
    public void register(View view) {
        recognizeViewModel.prepareRegister();
    }

    /**
     * 参数配置
     *
     * @param view
     */
    public void setting(View view) {
        this.actionAfterFinish = NAVIGATE_TO_RECOGNIZE_SETTINGS_ACTIVITY;
        showLongToast(getString(R.string.please_wait));
        finish();
    }

    /**
     * 识别分析界面
     *
     * @param view 注册按钮
     */
    public void recognizeDebug(View view) {
        this.actionAfterFinish = NAVIGATE_TO_RECOGNIZE_DEBUG_ACTIVITY;
        showLongToast(getString(R.string.please_wait));
        finish();
    }

    /**
     * 在{@link ActivityRegisterAndRecognizeBinding#dualCameraTexturePreviewRgb}第一次布局完成后，去除该监听，并且进行引擎和相机的初始化
     */
    @Override
    public void onGlobalLayout() {
        binding.dualCameraTexturePreviewRgb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        } else {
            recognizeViewModel.init();
            initRgbCamera();
            if (DualCameraHelper.hasDualCamera() && livenessType == LivenessType.IR) {
                initIrCamera();
            }
        }
    }

    // Matipos Server
    public void validateCode(Context context, String code) {
        boolean isInputDevice = ConfigUtil.isInputDevice(getApplicationContext());
        if (!isInputDevice)
            matiposViewModel.postValidationCode(context, code, ConfigUtil.getMatiposDeviceCode(context), -1);
        else
            matiposViewModel.postValidationCode(context, code, ConfigUtil.getMatiposDeviceCode(context), 0);
    }

    public void validateCode(Context context, String code, long faceId) {
        matiposViewModel.postValidationCode(context, code, ConfigUtil.getMatiposDeviceCode(context), faceId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeCamera();
    }

    private void resumeCamera() {
        if (rgbCameraHelper != null) {
            rgbCameraHelper.start();
        }
        if (irCameraHelper != null) {
            irCameraHelper.start();
        }
    }

    @Override
    protected void onPause() {
        pauseCamera();
        super.onPause();
    }

    private void pauseCamera() {
        if (rgbCameraHelper != null) {
            rgbCameraHelper.stop();
        }
        if (irCameraHelper != null) {
            irCameraHelper.stop();
        }
    }

    @Override
    public void onMessageReceived(String message) {
        try {
            recognizeViewModel.validateMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onConnectionOpened() {

    }

    @Override
    public void onConnectionClosed() {
        if (socketManager != null)
            socketManager.reconnect();
    }

    @Override
    public void onError(Throwable t) {

    }
}
