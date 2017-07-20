package fingerprint.com.fingerprintrecognition;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import fingerprint.com.fingerprintrecognition.core.FingerprintCore;

/**
 * @author：KevinLiu
 * @E-mail:KevinLiu9527@163.com
 * @time 2017/7/20 12:39
 * 备注：指纹识别
 */
public class FingerprintMainActivity extends Activity implements View.OnClickListener {

    private FingerprintCore mFingerprintCore;
    private KeyguardLockScreenManager mKeyguardLockScreenManager;

    private Toast mToast;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private ImageView mFingerGuideImg;
    private TextView mFingerGuideTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint_main);
        initViews();
        initViewListeners();
        initFingerprintCore();
    }

    /**
     * 初始化指纹相关
     */
    private void initFingerprintCore() {
        mFingerprintCore = new FingerprintCore(this);
        mFingerprintCore.setFingerprintManager(mResultListener);
        mKeyguardLockScreenManager = new KeyguardLockScreenManager(this);
    }

    /**
     * 初始化
     */
    private void initViews() {
        mFingerGuideImg = (ImageView) findViewById(R.id.fingerprint_guide);
        mFingerGuideTxt = (TextView) findViewById(R.id.fingerprint_guide_tip);
    }

    /**
     * 初始化监听
     */
    private void initViewListeners() {
        findViewById(R.id.fingerprint_recognition_start).setOnClickListener(this);
        findViewById(R.id.fingerprint_recognition_cancel).setOnClickListener(this);
        findViewById(R.id.fingerprint_recognition_sys_unlock).setOnClickListener(this);
        findViewById(R.id.fingerprint_recognition_sys_setting).setOnClickListener(this);
    }

    /**
     * 点击监听
     */
    @Override
    public void onClick(View v) {
        final int viewId = v.getId();
        switch (viewId) {
            //开始指纹识别
            case R.id.fingerprint_recognition_start:
                startFingerprintRecognition();
                break;
            //取消指纹识别
            case R.id.fingerprint_recognition_cancel:
                cancelFingerprintRecognition();
                break;
            //测试密码解锁屏幕
            case R.id.fingerprint_recognition_sys_unlock:
                startFingerprintRecognitionUnlockScreen();
                break;
            //进入系统指纹设置界面
            case R.id.fingerprint_recognition_sys_setting:
                enterSysFingerprintSettingPage();
                break;
        }
    }

    private void enterSysFingerprintSettingPage() {
        FingerprintUtil.openFingerPrintSettingPage(this);
    }

    private void cancelFingerprintRecognition() {
        if (mFingerprintCore.isAuthenticating()) {
            mFingerprintCore.cancelAuthenticate();
            resetGuideViewState();
        }
    }

    private void startFingerprintRecognitionUnlockScreen() {
        if (mKeyguardLockScreenManager == null) {
            return;
        }
        if (!mKeyguardLockScreenManager.isOpenLockScreenPwd()) {
            toastTipMsg(R.string.fingerprint_not_set_unlock_screen_pws);
            FingerprintUtil.openFingerPrintSettingPage(this);
            return;
        }
        mKeyguardLockScreenManager.showAuthenticationScreen(this);
    }

    /**
     * 开始指纹识别
     */
    private void startFingerprintRecognition() {
        if (mFingerprintCore.isSupport()) {
            if (!mFingerprintCore.isHasEnrolledFingerprints()) {
                toastTipMsg(R.string.fingerprint_recognition_not_enrolled);
                FingerprintUtil.openFingerPrintSettingPage(this);
                return;
            }
            toastTipMsg(R.string.fingerprint_recognition_tip);
            mFingerGuideTxt.setText(R.string.fingerprint_recognition_tip);
            mFingerGuideImg.setBackgroundResource(R.drawable.fingerprint_guide);
            if (mFingerprintCore.isAuthenticating()) {
                toastTipMsg(R.string.fingerprint_recognition_authenticating);
            } else {
                mFingerprintCore.startAuthenticate();
            }
        } else {
            toastTipMsg(R.string.fingerprint_recognition_not_support);
            mFingerGuideTxt.setText(R.string.fingerprint_recognition_tip);
        }
    }

    private void resetGuideViewState() {
        mFingerGuideTxt.setText(R.string.fingerprint_recognition_guide_tip);
        mFingerGuideImg.setBackgroundResource(R.drawable.fingerprint_normal);
    }

    /*指纹识别回调*/
    private FingerprintCore.IFingerprintResultListener mResultListener = new FingerprintCore.IFingerprintResultListener() {
        @Override
        public void onAuthenticateSuccess() {
            toastTipMsg(R.string.fingerprint_recognition_success);
            resetGuideViewState();
        }

        @Override
        public void onAuthenticateFailed(int helpId) {
            toastTipMsg(R.string.fingerprint_recognition_failed);
            mFingerGuideTxt.setText(R.string.fingerprint_recognition_failed);
        }

        @Override
        public void onAuthenticateError(int errMsgId) {
            resetGuideViewState();
            toastTipMsg(R.string.fingerprint_recognition_error);
        }

        @Override
        public void onStartAuthenticateResult(boolean isSuccess) {

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == KeyguardLockScreenManager.REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS) {
            // Challenge completed, proceed with using cipher
            if (resultCode == RESULT_OK) {
                toastTipMsg(R.string.sys_pwd_recognition_success);
            } else {
                toastTipMsg(R.string.sys_pwd_recognition_failed);
            }
        }
    }

    private void toastTipMsg(int messageId) {
        if (mToast == null) {
            mToast = Toast.makeText(this, messageId, Toast.LENGTH_SHORT);
        }
        mToast.setText(messageId);
        mToast.cancel();
        mHandler.removeCallbacks(mShowToastRunnable);
        mHandler.postDelayed(mShowToastRunnable, 0);
    }

    private void toastTipMsg(String message) {
        if (mToast == null) {
            mToast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        }
        mToast.setText(message);
        mToast.cancel();
        mHandler.removeCallbacks(mShowToastRunnable);
        mHandler.postDelayed(mShowToastRunnable, 200);
    }

    private Runnable mShowToastRunnable = new Runnable() {
        @Override
        public void run() {
            mToast.show();
        }
    };

    @Override
    protected void onDestroy() {
        if (mFingerprintCore != null) {
            mFingerprintCore.onDestroy();
            mFingerprintCore = null;
        }
        if (mKeyguardLockScreenManager != null) {
            mKeyguardLockScreenManager.onDestroy();
            mKeyguardLockScreenManager = null;
        }
        mResultListener = null;
        mShowToastRunnable = null;
        mToast = null;
        super.onDestroy();
    }
}
