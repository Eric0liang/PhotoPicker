package com.example.eric.photopicker.base;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.example.eric.photopicker.R;

import java.util.List;


/**
 * 基础列表adapter
 * Created on 2016/6/14.
 */
public abstract class BaseListAdapter<T> extends BaseAdapter implements View.OnClickListener {

    protected List<T> list;
    protected Context context;
    protected OnListItemClickListener listener;
    private View mConvertView;

    public BaseListAdapter(Context context, OnListItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setList(List<T> list) {
        if (list != null) {
            this.list = list;
        }
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return list == null ? null : list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        boolean isNew = false;
        if (convertView == null) {
            isNew = true;
            convertView = LayoutInflater.from(context).inflate(getLayoutId(), null);
        }
        mConvertView = convertView;

        setView(position, convertView, parent, isNew);

        return convertView;
    }

    /**
     * 设置点击事件
     */
    protected void setClickEvent(boolean isNew, int position, View... vs) {
        for (View v : vs) {
            v.setTag(R.id.tag_position, position);
            if (isNew) {
                v.setOnClickListener(this);
            }
        }
    }

    @Override
    public void onClick(View view) {
        final Object object = view.getTag(R.id.tag_position);
        if (object != null && object instanceof Integer) {
            final int position = (Integer) object;
            if (position < getCount()) {
                if (listener != null) {
                    listener.onItemClick(getItem(position), view, position);
                }
            }
        }
    }

    /**
     * ViewHolder 复用控件
     */
    @SuppressWarnings("unchecked")
    public <V extends View> V getViewById(int id) {
        SparseArray<View> viewHolder = (SparseArray<View>) mConvertView.getTag();
        if (viewHolder == null) {
            viewHolder = new SparseArray<>();
            mConvertView.setTag(viewHolder);
        }
        View childView = viewHolder.get(id);
        if (childView == null) {
            childView = mConvertView.findViewById(id);
            viewHolder.put(id, childView);
        }
        return (V) childView;
    }

    /**
     * 获取布局文件
     *
     * @return R.layout.*
     */
    protected abstract int getLayoutId();

    /**
     * getView的剩余操作
     *
     * @param isNew       是否新建的convertView，即非复用控件
     */
    protected abstract void setView(int position, View convertView, ViewGroup parent, boolean
            isNew);
}

