package fingerprint.com.fingerprintrecognition;

import android.content.Context;
import android.content.Intent;

/**
 * @author：KevinLiu
 * @E-mail:KevinLiu9527@163.com
 * @time 2017/7/20 12:44
 * 备注：打开指纹开关
 */
public class FingerprintUtil {

    private static final String ACTION_SETTING = "android.settings.SETTINGS";

    public static void openFingerPrintSettingPage(Context context) {
        Intent intent = new Intent(ACTION_SETTING);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
        }
    }
}
