package com.pesdk.uisdk.ActivityResultCallback;

import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;

import com.pesdk.uisdk.ActivityResultCallback.callback.BeautyResultCallback;
import com.pesdk.uisdk.ActivityResultCallback.callback.EditResultCallback;
import com.pesdk.uisdk.ActivityResultCallback.callback.EraseResultCallback;
import com.pesdk.uisdk.ActivityResultCallback.callback.SegmentResultCallback;
import com.pesdk.uisdk.bean.FilterInfo;
import com.pesdk.uisdk.bean.code.Crop;
import com.pesdk.uisdk.beauty.BeautyActivity;
import com.pesdk.uisdk.internal.SdkEntryHandler;
import com.pesdk.uisdk.ui.home.CropActivity;
import com.pesdk.uisdk.ui.home.ErasePenActivity;
import com.pesdk.uisdk.ui.home.segment.SegmentActivity;
import com.vecore.models.PEImageObject;

import java.util.ArrayList;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;

/**
 * 统一管理主编辑调整
 */
public class EditRegisterManger {

    private final IResultCallback mCallback;

    private ActivityResultLauncher<Intent> mBeautyResultLauncher, mSegmentResultLauncher;
    private ActivityResultLauncher<Void> mLayerResultLauncher;
    private ActivityResultLauncher<Intent> mMediaResultLauncher;
    private ActivityResultLauncher<Intent> mEraseResultLauncher;

    public EditRegisterManger(IResultCallback callback) {
        mCallback = callback;
    }

    public void register(ComponentActivity activity) {
        ActivityResultContract<Void, ArrayList<String>> albumContract = SdkEntryHandler.getInstance().getAlbumContract();
        if (albumContract != null) {
            mLayerResultLauncher = activity.registerForActivityResult(albumContract, result -> {
                if (null != result && result.size() > 0) {
                    //增加单个layer
                    if (type == TYPE_LAYER) {
                        mCallback.addLayer(result.get(0));
                    } else if (type == TYPE_REPLACE_LAYER) {
                        mCallback.replaceLayer(result.get(0));
                    }
                }
            });
        }
        mMediaResultLauncher = activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new EditResultCallback() {
            @Override
            protected void onResult(RectF clipRect, @Crop.CropMode int mode) {
                mCallback.onEditResult(clipRect,mode);
            }

        });

        mBeautyResultLauncher = activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new BeautyResultCallback() {
            @Override
            protected void onResult(boolean isAdd, FilterInfo filterInfo, String hairMedia) {
                mCallback.onBeautyResult(isAdd, filterInfo, hairMedia);
            }
        });
        mSegmentResultLauncher = activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new SegmentResultCallback() {
            @Override
            protected void onResult(String maskPath) {
                mCallback.onSegmentResult(maskPath);
            }
        });

        mEraseResultLauncher = activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new EraseResultCallback() {
            @Override
            protected void onResult(PEImageObject imageObject) {
                mCallback.onEraseResult(imageObject);
            }
        });


    }

    private final int TYPE_LAYER = 1;
    private final int TYPE_REPLACE_LAYER = 2;
    private int type = 0;

    /**
     * 添加图层
     *
     * @param context
     */
    public void onAddLayer(Context context) {
        type = TYPE_LAYER;
        if (mLayerResultLauncher != null) {
            mLayerResultLauncher.launch(null);
        }
    }

    /**
     * 替换图层
     */
    public void replaceLayer() {
        type = TYPE_REPLACE_LAYER;
        if (mLayerResultLauncher != null) {
            mLayerResultLauncher.launch(null);
        }
    }


    public void onEdit(Context context, PEImageObject mediaObject, float previewAsp) {
        mMediaResultLauncher.launch(CropActivity.createIntent(context, mediaObject, previewAsp));
    }

    public void onBeauty(Context context, PEImageObject mediaObject, boolean isPip) {
        mBeautyResultLauncher.launch(BeautyActivity.createIntent(context, mediaObject, isPip));
    }

    public void onSegment(Context context, PEImageObject peImageObject, boolean isPip) {
        mSegmentResultLauncher.launch(SegmentActivity.createIntent(context, peImageObject, isPip));
    }

    public void onErase(Context context, PEImageObject imageObject) {
        mEraseResultLauncher.launch(ErasePenActivity.createIntent(context, imageObject));
    }

}
