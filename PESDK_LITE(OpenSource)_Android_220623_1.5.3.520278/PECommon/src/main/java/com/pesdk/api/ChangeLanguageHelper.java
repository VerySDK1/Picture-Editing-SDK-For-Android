package com.pesdk.api;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.util.DisplayMetrics;


import java.util.Locale;

/**
 * 切换语言辅助
 */
public class ChangeLanguageHelper {
    public static final int LANGUAGE_SYSTEM = 0;
    public static final int LANGUAGE_CHINESE = 1;
    public static final int LANGUAGE_ENGLISH = 2;


    private static final String APP_LANGUAGE = "custom_language_pref_key";
    private static final String SP_NAME = "peLanguageConfig";
    private static SharedPreferences mSharedPreferences;
    private static LocaleList sLocaleList = null;

    //https://www.jianshu.com/p/9a304c2047ff  解决跟随系统异常
    static {
        //由于API仅支持7.0，需要判断，否则程序会crash
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sLocaleList = LocaleList.getDefault();
        }
    }


    public static void init(Context context, int defaultLanguage) {
        mSharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        mLanguage = mSharedPreferences.getInt(APP_LANGUAGE, defaultLanguage);
    }

    /**
     * 是否是中文
     *
     * @return true 中文；false 其他
     */
    public static boolean isZh(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.contains("zh"))
            return true;
        else
            return false;
    }

    public static int getAppLanguage(Context context) {
        if (null == mSharedPreferences) {
            init(context, LANGUAGE_SYSTEM);
        }
        return mLanguage;
    }

    private static int mLanguage = LANGUAGE_SYSTEM;

    /**
     * 确保每次打开apk重新加载语言环境
     *
     * @return
     */
    public static int getCurrentLanguage() {
        return mLanguage;
    }


    /**
     * 更改语言环境
     *
     * @param context
     * @param newLanguage
     */
    @TargetApi(Build.VERSION_CODES.N)
    public static void changeAppLanguage(Context context, int newLanguage) {
        mLanguage = newLanguage;
        mSharedPreferences.edit().putInt(APP_LANGUAGE, newLanguage).commit();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
//            <7.0
            updateConfiguration(context, newLanguage);
        }
    }


    /**
     * @param language
     * @return
     */
    @TargetApi(Build.VERSION_CODES.N)
    private static Locale getLocaleByLanguage(int language) {
        Locale locale = null;
        if (language == LANGUAGE_CHINESE) {
            locale = Locale.CHINESE;
        } else if (language == LANGUAGE_ENGLISH) {
            locale = Locale.ENGLISH;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locale = (null != sLocaleList && sLocaleList.size() >= 1) ? sLocaleList.get(0) : Locale.getDefault();
            } else {
                locale = Locale.getDefault();
            }
            if (!locale.getLanguage().contains("zh")) { //非中文时，强制切为英文
                locale = Locale.ENGLISH;
            }
        }
        return locale;
    }

    /**
     * @param context
     * @param language
     * @return
     */
    public static Context attachBaseContext(Context context, int language) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context, language);
        } else {
            updateConfiguration(context, language);
            return context;
        }
    }

    private static final String TAG = "language";


    /**
     * 7.0及以上的修改app语言的方法
     *
     * @param context  context
     * @param language language
     * @return context
     */
    @TargetApi(Build.VERSION_CODES.N)
    private static Context updateResources(Context context, int language) {
        Resources resources = context.getResources();
        Locale locale = getLocaleByLanguage(language);
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        configuration.setLocales(new LocaleList(locale));
        return context.createConfigurationContext(configuration);
    }

    /**
     * 7.0以下的修改app语言的方法
     *
     * @param context  context
     * @param language language
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static void updateConfiguration(Context context, int language) {
        Locale locale = getLocaleByLanguage(language);
        Resources resources = context.getResources();
        Locale.setDefault(locale);
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        resources.updateConfiguration(configuration, displayMetrics);
    }

    /**
     * 是否是英文 （默认英文|强制英文）
     *
     * @param context
     * @return
     */
    public static boolean isEn(Context context) {
        return (getCurrentLanguage() == LANGUAGE_ENGLISH || !isZh(context));
    }

}
