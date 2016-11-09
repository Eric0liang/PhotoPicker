package com.example.eric.photopicker.module;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.eric.photopicker.Constants;
import com.example.eric.photopicker.R;
import com.example.eric.photopicker.adapter.PhotoPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eric on 2016/10/09
 */
public class PhotoPreviewActivity extends AppCompatActivity implements PhotoPagerAdapter.PhotoViewClickListener{

    /** 选择结果，返回为 ArrayList&lt;String&gt; 图片路径集合  */
    public static final String EXTRA_RESULT = "preview_result";

    /** 预览请求状态码 */
    public static final int REQUEST_PREVIEW = 99;

    private ArrayList<String> paths;
    private ViewPagerFixed mViewPager;
    private PhotoPagerAdapter mPagerAdapter;
    private int currentItem = 0;
    private final static String EXTRA_PHOTOS = "extra_photos";
    private final static String EXTRA_CURRENT_ITEM = "extra_current_item";
    private final static String IS_DELETE = "is_delete";


    /**
     * 相册预览操作
     * @param context
     * @param paths 图片地址
     * @param currentItem 当前点击图片
     */
    public static void actStart(Activity context, ArrayList<String> paths, int currentItem, int requestCode) {
        Intent intent = new Intent(context, PhotoPreviewActivity.class);
        intent.putStringArrayListExtra(EXTRA_PHOTOS, paths);
        intent.putExtra(EXTRA_CURRENT_ITEM, currentItem);
        intent.putExtra(IS_DELETE, true);
        context.startActivityForResult(intent, requestCode);
    }

    /**
     * 图片预览
     * @param context
     * @param paths 图片地址
     * @param currentItem 当前点击图片
     */
    public static void actStart(Context context, List<String> paths, int currentItem) {
        Intent intent = new Intent(context, PhotoPreviewActivity.class);
        intent.putStringArrayListExtra(EXTRA_PHOTOS, (ArrayList<String>) paths);
        intent.putExtra(EXTRA_CURRENT_ITEM, currentItem);
        intent.putExtra(IS_DELETE, false);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_preview);

        initViews();

        paths = new ArrayList<>();
        ArrayList<String> pathArr = getIntent().getStringArrayListExtra(EXTRA_PHOTOS);
        if(pathArr != null){
            if (pathArr != null && pathArr.contains(Constants.ICON_ADD)){
                pathArr.remove(Constants.ICON_ADD);
            }
            paths.addAll(pathArr);
        }

        currentItem = getIntent().getIntExtra(EXTRA_CURRENT_ITEM, 0);

        mPagerAdapter = new PhotoPagerAdapter(this, paths);
        mPagerAdapter.setPhotoViewClickListener(this);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(currentItem);
        mViewPager.setOffscreenPageLimit(5);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                updateActionBarTitle();
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        updateActionBarTitle();
    }

    private void initViews(){
        mViewPager = (ViewPagerFixed) findViewById(R.id.vp_photos);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.pickerToolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void OnPhotoTapListener(View view, float v, float v1) {
        onBackPressed();
    }

    public void updateActionBarTitle() {
        getSupportActionBar().setTitle(
                getString(R.string.image_index, mViewPager.getCurrentItem() + 1, paths.size()));
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_RESULT, paths);
        setResult(RESULT_OK, intent);
        finish();
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean isDel = getIntent().getBooleanExtra(IS_DELETE, false);
        if (isDel) {
            getMenuInflater().inflate(R.menu.menu_preview, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }

        // 删除当前照片
        if(item.getItemId() == R.id.action_discard){
            final int index = mViewPager.getCurrentItem();
            final String deletedPath =  paths.get(index);
            if(paths.size() <= 1){
                paths.remove(index);
                onBackPressed();
            }else{
                paths.remove(index);
                mPagerAdapter.notifyDataSetChanged();
            }

        }
        return super.onOptionsItemSelected(item);
    }
}
