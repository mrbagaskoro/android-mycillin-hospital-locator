package com.mycillin.hospitallocator.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mycillin.hospitallocator.R;

/**
 * Created by mrbagaskoro on 11-Mar-18.
 */

public class DialogImagePickerAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;

    public DialogImagePickerAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        View view = convertView;

        if (view == null) {
            view = layoutInflater.inflate(R.layout.dialog_image_picker_content, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.textView = view.findViewById(R.id.dialogImagePickerContent_tv_textView);
            viewHolder.imageView = view.findViewById(R.id.dialogImagePickerContent_iv_imageView);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        Context context = parent.getContext();
        switch (position) {
            case 0:
                viewHolder.textView.setText("Gallery");
                viewHolder.imageView.setImageResource(R.mipmap.ic_launcher);
                break;
            case 1:
                viewHolder.textView.setText("Camera");
                viewHolder.imageView.setImageResource(R.mipmap.ic_launcher);
                break;
        }

        return view;
    }

    static class ViewHolder {
        TextView textView;
        ImageView imageView;
    }
}
