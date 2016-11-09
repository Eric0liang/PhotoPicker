package com.example.eric.photopicker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.eric.photopicker.module.PhotoPickerActivity;
import com.example.eric.photopicker.module.PhotoPreviewActivity;
import com.example.eric.photopicker.widget.ImageGridView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ImageGridView gridView;
    private List<String> paths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gridView = (ImageGridView) findViewById(R.id.gridview_img);
        paths = new ArrayList<>();
        gridView.loadAdpater(paths, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 图片控件
        if (requestCode == Constants.REQUEST_ADD_IMG && data != null) {
            List<String> list = data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT);
            paths.addAll(list);
            gridView.loadAdpater(paths, true);
        } else if (requestCode == Constants.REQUEST_PREVIEW_IMG && data != null) {
            paths = data.getStringArrayListExtra(PhotoPreviewActivity.EXTRA_RESULT);
            gridView.loadAdpater(paths, true);
        }
    }
}
