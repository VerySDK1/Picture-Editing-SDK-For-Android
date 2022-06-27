package com.pesdk.uisdk.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.widget.AppConfig;
import com.pesdk.uisdk.widget.ColorView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class ColorListAdapter extends BaseRVAdapter<ColorListAdapter.ColorViewHolder> {


    private int[] colorData;
    private int itemPx = -1;

    public ColorListAdapter(int size) {
        colorData = AppConfig.colors;
        lastCheck = 0;
        itemPx = size;
    }

    /**
     * 选中的颜色
     *
     * @param color
     */
    public void setCheckedColor(int color) {
        int index = -1;
        int len = colorData.length;
        for (int i = 0; i < len; i++) {
            if (colorData[i] == color) {
                index = i;
                break;
            }
        }
        lastCheck = index;
        notifyDataSetChanged();
    }


    public int getItem(int poistion) {
        return poistion >= 0 ? colorData[poistion] : 0;
    }

    public class ColorViewHolder extends RecyclerView.ViewHolder {
        ColorView colorPanelView;

        public ColorViewHolder(@NonNull View itemView, int size) {
            super(itemView);
            colorPanelView = itemView.findViewById(R.id.ivColor);
            ViewGroup.LayoutParams lp = colorPanelView.getLayoutParams();
            if (size > 0) {
                lp.width = size;
                lp.height = size;
                colorPanelView.setLayoutParams(lp);
            }
        }
    }

    @NonNull
    @Override
    public ColorListAdapter.ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pesdk_view_color_panel, parent, false);
        ColorViewHolder holder = new ColorViewHolder(v, itemPx);
        ItemClickListener listener = new ItemClickListener();
        holder.colorPanelView.setOnClickListener(listener);
        holder.colorPanelView.setTag(listener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ColorListAdapter.ColorViewHolder holder, int position) {
        holder.colorPanelView.setColor(colorData[position]);
        holder.colorPanelView.setChecked(lastCheck == position);
        ItemClickListener listener = (ItemClickListener) holder.colorPanelView.getTag();
        listener.setPosition(position);
    }

    class ItemClickListener extends BaseItemClickListener {

        @Override
        protected void onSingleClick(View view) {
            lastCheck = position;
            notifyDataSetChanged();
            mOnItemClickListener.onItemClick(position, colorData[position]);
        }
    }


    @Override
    public int getItemCount() {
        return colorData.length;
    }

}
