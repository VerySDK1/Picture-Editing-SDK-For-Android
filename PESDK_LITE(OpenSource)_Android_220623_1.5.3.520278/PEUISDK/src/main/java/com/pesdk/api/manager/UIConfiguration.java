package com.pesdk.api.manager;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;

/**
 * 参数配置
 */
@Keep
public class UIConfiguration implements Parcelable {
    public String baseUrl;

    private UIConfiguration(Builder builder) {
        this.baseUrl = builder.baseUrl;
    }

    /**
     * Builder class for {@link UIConfiguration} objects.
     */
    public static class Builder {

        private String baseUrl;

        /**
         * 素材系统baseUrl
         */
        public Builder setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public UIConfiguration get() {
            return new UIConfiguration(this);
        }

    }

    protected UIConfiguration(Parcel in) {
        baseUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(baseUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UIConfiguration> CREATOR = new Creator<UIConfiguration>() {
        @Override
        public UIConfiguration createFromParcel(Parcel in) {
            return new UIConfiguration(in);
        }

        @Override
        public UIConfiguration[] newArray(int size) {
            return new UIConfiguration[size];
        }
    };
}
