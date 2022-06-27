package com.pesdk.uisdk.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.widget.AppConfig;
import com.pesdk.uisdk.widget.ColorView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 背景纯色
 */
public class CanvasColorAdapter extends BaseRVAdapter<CanvasColorAdapter.ColorViewHolder> {


    private int[] colorData;

    public CanvasColorAdapter(int color) {
        colorData = AppConfig.colors;
        lastCheck = getIndex(color);
    }

    private int getIndex(int color) {
        int len = colorData.length;
        for (int i = 0; i < len; i++) {
            if (colorData[i] == color) {
                return i;
            }
        }
        return BaseRVAdapter.UN_CHECK;
    }

    /**
     * 切换选中的颜色
     *
     * @param color
     */
    public void updateCheck(int color) {
        if (lastCheck >= 0) {
            notifyItemChanged(lastCheck);
        }
        lastCheck = getIndex(color);
        if (lastCheck >= 0) {
            notifyItemChanged(lastCheck);
        }
    }


    public int getItem(int poistion) {
        return poistion >= 0 ? colorData[poistion] : 0;
    }

    public class ColorViewHolder extends RecyclerView.ViewHolder {
        ColorView mColorView;

        public ColorViewHolder(@NonNull View itemView) {
            super(itemView);
            mColorView = itemView.findViewById(R.id.ivColor);
        }
    }

    @NonNull
    @Override
    public CanvasColorAdapter.ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pesdk_item_cv_color_layout, parent, false);
        ColorViewHolder holder = new ColorViewHolder(v);
        ItemClickListener listener = new ItemClickListener();
        holder.mColorView.setMode(ColorView.MODE_RECT);
        holder.mColorView.setOnClickListener(listener);
        holder.mColorView.setTag(listener);
        return holder;
    }


    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            updateItemHolder(holder, position);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
        ItemClickListener listener = (ItemClickListener) holder.mColorView.getTag();
        listener.setPosition(position);
        updateItemHolder(holder, position);
    }

    private void updateItemHolder(ColorViewHolder holder, int position) {
        holder.mColorView.setColor(colorData[position]);
        holder.mColorView.setChecked(lastCheck == position);
    }


    class ItemClickListener extends BaseItemClickListener {

        @Override
        protected void onSingleClick(View view) {
            int tmp = lastCheck;
            lastCheck = position;
            if (tmp >= 0) {
                notifyItemChanged(tmp, tmp + "");
            }
            notifyItemChanged(position, position + "");
            mOnItemClickListener.onItemClick(position, colorData[position]);
        }
    }


    @Override
    public int getItemCount() {
        return colorData.length;
    }

}
