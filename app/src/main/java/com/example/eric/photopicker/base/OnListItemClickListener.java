package com.example.eric.photopicker.base;

import android.view.View;

/**
 * {@link BaseListAdapter}项点击事件回调
 * Created by on 2016/6/14.
 */
public interface OnListItemClickListener {
    void onItemClick(Object item, View view, int position);
}
