package com.example.eric.photopicker.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.eric.photopicker.R;
import com.example.eric.photopicker.base.BaseListAdapter;
import com.example.eric.photopicker.module.Folder;

import java.io.File;

public class FolderAdapter extends BaseListAdapter<Folder> {

    int mImageSize;
    int lastSelected = 0;

    public FolderAdapter(Context context) {
        super(context, null);
        this.context = context;
        mImageSize = context.getResources().getDimensionPixelOffset(R.dimen.folder_cover_size);
    }

    @Override
    public int getCount() {
        return list == null ? 1 : list.size() + 1;
    }

    @Override
    public Folder getItem(int i) {
        if (i == 0) return null;
        return list.get(i - 1);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.list_item_folder;
    }

    @Override
    protected void setView(int position, View convertView, ViewGroup parent, boolean isNew) {
        ImageView cover = getViewById(R.id.cover);
        TextView name = getViewById(R.id.name);
        TextView size = getViewById(R.id.size);
        ImageView indicator = getViewById(R.id.indicator);
        Folder f = null;
        if (position == 0) {
            if (list != null) {
                f = list.get(0);
            }
            name.setText(context.getString(R.string.all_image));//image_number
            size.setText(context.getString(R.string.image_number, getTotalImageSize()));
        } else {
            f = getItem(position);
            name.setText(f.name);
            size.setText(context.getString(R.string.image_number, f.images.size()));
        }
        if ( f!= null) {
            Glide.with(context)
                    .load(new File(f.cover.path))
                    .error(R.mipmap.default_error)
                    .override(mImageSize, mImageSize)
                    .centerCrop()
                    .into(cover);
        }

        if (lastSelected == position) {
            indicator.setVisibility(View.VISIBLE);
        } else {
            indicator.setVisibility(View.INVISIBLE);
        }
    }

    private int getTotalImageSize() {
        int result = 0;
        if (list != null && !list.isEmpty()) {
            for (Folder f : list) {
                result += f.images.size();
            }
        }
        return result;
    }

    public void setSelectIndex(int i) {
        if (lastSelected == i) return;
        lastSelected = i;
        notifyDataSetChanged();
    }

    public int getSelectIndex() {
        return lastSelected;
    }

}
