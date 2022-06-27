package android.support.v4.app;

import android.content.Context;

public class ActivityCompat {
    public static int checkSelfPermission(Context context, String permission) {
        return androidx.core.app.ActivityCompat.checkSelfPermission(context, permission);
    }
}
