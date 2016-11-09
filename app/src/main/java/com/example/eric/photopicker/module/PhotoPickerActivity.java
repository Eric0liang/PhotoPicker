package com.example.eric.photopicker.module;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.eric.photopicker.Constants;
import com.example.eric.photopicker.R;
import com.example.eric.photopicker.adapter.FolderAdapter;
import com.example.eric.photopicker.adapter.ImageGridAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class PhotoPickerActivity extends AppCompatActivity {

    public static final String TAG = PhotoPickerActivity.class.getName();

    private Context mCxt;

    /** 图片选择模式，int类型 */
    public static final String EXTRA_SELECT_MODE = "select_count_mode";
    /** 单选 */
    public static final int MODE_SINGLE = 0;
    /** 多选 */
    public static final int MODE_MULTI = 1;
    /** 最大图片选择次数，int类型 */
    public static final String EXTRA_SELECT_COUNT = "max_select_count";
    /** 默认最大照片数量 */
    public static final int DEFAULT_MAX_TOTAL= 9;
    /** 是否显示相机，boolean类型 */
    public static final String EXTRA_SHOW_CAMERA = "show_camera";
    /** 默认选择的数据集 */
    public static final String EXTRA_DEFAULT_SELECTED_LIST = "default_result";
    /** 筛选照片配置信息 */
    public static final String EXTRA_IMAGE_CONFIG = "image_config";
    /** 选择结果，返回为 ArrayList&lt;String&gt; 图片路径集合  */
    public static final String EXTRA_RESULT = "select_result";

    // 结果数据
    private ArrayList<String> resultList = new ArrayList<>();
    // 文件夹数据
    private ArrayList<Folder> mResultFolder = new ArrayList<>();

    // 不同loader定义
    private static final int LOADER_ALL = 0;
    private static final int LOADER_CATEGORY = 1;

    private MenuItem menuDoneItem;
    private GridView mGridView;
    private View mPopupAnchorView;
    private Button btnAlbum;
    private Button btnPreview;
    private LinearLayout layoutFolder;
    private ListView folderListView;

    // 最大照片数量
    private ImageCaptureManager captureManager;
    private int mDesireImageCount;
    private ImageConfig imageConfig; // 照片配置

    private ImageGridAdapter mImageAdapter;
    private FolderAdapter mFolderAdapter;

    private boolean hasFolderGened = false;
    private boolean mIsShowCamera = false;

    /**
     * 图片选择调用方法
     * @param context
     * @param model MODE_SINGLE 单选，MODE_MULTI 多选
     * @param total 最多可选图片数量
     * @param showCamera 是否显示照相机
     */
    public static void actStart(Activity context, SelectModel model, int total, boolean showCamera, int requestCode) {
        Intent intent = new Intent(context, PhotoPickerActivity.class);
        intent.putExtra(EXTRA_SELECT_COUNT, total);
        intent.putExtra(EXTRA_SELECT_MODE, Integer.parseInt(model.toString()));
        intent.putExtra(EXTRA_SHOW_CAMERA, showCamera);
        context.startActivityForResult(intent, requestCode);
    }

    /**
     * 图片选择调用方法
     * @param context
     * @param model MODE_SINGLE 单选，MODE_MULTI 多选
     * @param total 最多可选图片数量
     * @param showCamera 是否显示照相机
     */
    public static void actStart(Fragment context, SelectModel model, int total, boolean showCamera, int requestCode) {
        Intent intent = new Intent(context.getActivity(), PhotoPickerActivity.class);
        intent.putExtra(EXTRA_SELECT_COUNT, total);
        intent.putExtra(EXTRA_SELECT_MODE, Integer.parseInt(model.toString()));
        intent.putExtra(EXTRA_SHOW_CAMERA, showCamera);
        context.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photopicker);

        initViews();

        // 照片属性
        imageConfig = getIntent().getParcelableExtra(EXTRA_IMAGE_CONFIG);

        // 首次加载所有图片
        getSupportLoaderManager().initLoader(LOADER_ALL, null, mLoaderCallback);
        //getSupportLoaderManager()

        // 选择图片数量
        mDesireImageCount = getIntent().getIntExtra(EXTRA_SELECT_COUNT, DEFAULT_MAX_TOTAL);

        // 图片选择模式
        final int mode = getIntent().getExtras().getInt(EXTRA_SELECT_MODE, MODE_SINGLE);

        // 默认选择
        if(mode == MODE_MULTI) {
            ArrayList<String> tmp = getIntent().getStringArrayListExtra(EXTRA_DEFAULT_SELECTED_LIST);
            if(tmp != null && tmp.size() > 0) {
                resultList.addAll(tmp);
            }
        }

        // 是否显示照相机
        mIsShowCamera = getIntent().getBooleanExtra(EXTRA_SHOW_CAMERA, false);
        mImageAdapter = new ImageGridAdapter(mCxt, mIsShowCamera, getItemImageWidth());
        // 是否显示选择指示器
        mImageAdapter.showSelectIndicator(mode == MODE_MULTI);
        mGridView.setAdapter(mImageAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mImageAdapter.isShowCamera()) {
                    // 如果显示照相机，则第一个Grid显示为照相机，处理特殊逻辑
                    if (i == 0) {
                        if(mode == MODE_MULTI){
                            // 判断选择数量问题
                            if(mDesireImageCount == resultList.size()-1){
                                Toast.makeText(mCxt, "你最多只能选择"+mDesireImageCount+"张图片", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                        showCameraAction();
                    } else {
                        // 正常操作
                        Image image = (Image) adapterView.getAdapter().getItem(i);
                        selectImageFromGrid(image, mode);
                    }
                } else {
                    // 正常操作
                    Image image = (Image) adapterView.getAdapter().getItem(i);
                    selectImageFromGrid(image, mode);
                }
            }
        });

        mFolderAdapter = new FolderAdapter(mCxt);

        // 打开相册列表
        btnAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutFolder == null) {
                    createPopupFolderList();
                }

                if (layoutFolder.getVisibility() == View.VISIBLE) {
                    dimissFolderList();
                } else {
                    showFolderList();
                }
            }
        });
        //TODO
        // 预览
        btnPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoPreviewActivity.actStart(PhotoPickerActivity.this, resultList,
                        0, PhotoPreviewActivity.REQUEST_PREVIEW);
            }
        });
    }

    private void initViews() {
        mCxt = this;
        captureManager = new ImageCaptureManager(mCxt);

        mGridView = (GridView) findViewById(R.id.grid);
        mGridView.setNumColumns(getNumColnums());

        mPopupAnchorView = findViewById(R.id.photo_picker_footer);
        btnAlbum = (Button) findViewById(R.id.btnAlbum);
        btnPreview = (Button) findViewById(R.id.btnPreview);

        Toolbar toolbar = (Toolbar) findViewById(R.id.pickerToolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            return;
        }
        actionBar.setTitle(getString(R.string.image));
        actionBar.setDisplayHomeAsUpEnabled(true);

    }
    private void createPopupFolderList(){
        layoutFolder = (LinearLayout) findViewById(R.id.layout_folder);
        folderListView = (ListView) findViewById(R.id.list_folder);
        folderListView.setAdapter(mFolderAdapter);
        folderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                mFolderAdapter.setSelectIndex(position);

                final int index = position;
                final AdapterView v = parent;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dimissFolderList();
                        if (index == 0) {
                            getSupportLoaderManager().restartLoader(LOADER_ALL, null, mLoaderCallback);
                            btnAlbum.setText(getString(R.string.all_image));
                            mImageAdapter.setShowCamera(mIsShowCamera);
                        } else {
                            Folder folder = (Folder) v.getAdapter().getItem(index);
                            if (null != folder) {
                                mImageAdapter.setList(folder.images);
                                btnAlbum.setText(folder.name);
                                // 设定默认选择
                                if (resultList != null && resultList.size() > 0) {
                                    mImageAdapter.setDefaultSelected(resultList);
                                }
                            }
                            mImageAdapter.setShowCamera(false);
                        }
                        mImageAdapter.notifyDataSetChanged();

                        // 滑动到最初始位置
                        mGridView.smoothScrollToPosition(0);
                    }
                }, 100);
            }
        });

        layoutFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dimissFolderList();
            }
        });
    }

    public void onSingleImageSelected(String path) {
        Intent data = new Intent();
        resultList.add(path);
        data.putStringArrayListExtra(EXTRA_RESULT, resultList);
        setResult(RESULT_OK, data);
        finish();
    }

    public void onImageSelected(String path) {
        if(!resultList.contains(path)) {
            resultList.add(path);
        }
        refreshActionStatus();
    }

    public void onImageUnselected(String path) {
        if(resultList.contains(path)){
            resultList.remove(path);
        }
        refreshActionStatus();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            switch (requestCode){
                // 相机拍照完成后，返回图片路径
                case ImageCaptureManager.REQUEST_TAKE_PHOTO:
                    if(captureManager.getCurrentPhotoPath() != null) {
                        captureManager.galleryAddPic();
                        resultList.add(captureManager.getCurrentPhotoPath());
                    }
                    complete();
                    break;
                // 预览照片
                case PhotoPreviewActivity.REQUEST_PREVIEW:
                    ArrayList<String> pathArr = data.getStringArrayListExtra(PhotoPreviewActivity.EXTRA_RESULT);
                    // 刷新页面
                    if(pathArr != null && pathArr.size() != resultList.size()){
                        resultList = pathArr;
                        refreshActionStatus();
                        mImageAdapter.setDefaultSelected(resultList);
                    }
                    break;
            }
        }
    }

    private void showFolderList() {
        if (layoutFolder != null && layoutFolder.getVisibility() == View.GONE) {
            //int fromXType, float fromXValue, int toXType, float toXValue, int fromYType, float fromYValue, int toYType, float toYValue
            TranslateAnimation mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                    0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF,
                    0.0f);
            mHiddenAction.setDuration(300);
            folderListView.startAnimation(mHiddenAction);
            layoutFolder.setBackgroundColor(Color.parseColor("#cc000000"));
            layoutFolder.setVisibility(View.VISIBLE);
            folderListView.setVisibility(View.VISIBLE);
            mGridView.setEnabled(false);
        }
    }

    private void dimissFolderList() {
        if (layoutFolder != null) {
            if (layoutFolder.getVisibility() == View.VISIBLE) {
                TranslateAnimation mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                        0.0f, Animation.RELATIVE_TO_SELF, 1.0f);
                mShowAction.setDuration(300);
                layoutFolder.startAnimation(mShowAction);
                layoutFolder.setBackgroundResource(0);
                layoutFolder.setVisibility(View.GONE);
                mGridView.setEnabled(true);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "on change");

        // 重置列数
        mGridView.setNumColumns(getNumColnums());
        // 重置Item宽度
        mImageAdapter.setItemSize(getItemImageWidth());
        dimissFolderList();
        super.onConfigurationChanged(newConfig);
    }

    /**
     * 选择相机
     */
    private void showCameraAction() {
        try {
            Intent intent = captureManager.dispatchTakePictureIntent();
            startActivityForResult(intent, ImageCaptureManager.REQUEST_TAKE_PHOTO);
        } catch (IOException e) {
            Toast.makeText(mCxt, getString(R.string.msg_no_camera), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    /**
     * 选择图片操作
     * @param image
     */
    private void selectImageFromGrid(Image image, int mode) {
        if(image != null) {
            // 多选模式
            if(mode == MODE_MULTI) {
                if (resultList.contains(image.path)) {
                    resultList.remove(image.path);
                    onImageUnselected(image.path);
                } else {
                    // 判断选择数量问题
                    if(mDesireImageCount == resultList.size()){
                        Toast.makeText(mCxt, getString(R.string.image_max, mDesireImageCount), Toast.LENGTH_LONG).show();
                        return;
                    }
                    resultList.add(image.path);
                    onImageSelected(image.path);
                }
                mImageAdapter.select(image);
            }else if(mode == MODE_SINGLE){
                // 单选模式
                onSingleImageSelected(image.path);
            }
        }
    }

    /**
     * 刷新操作按钮状态
     */
    private void refreshActionStatus(){
        if(resultList.contains(Constants.ICON_ADD)){
            resultList.remove(Constants.ICON_ADD);
        }
        String text = getString(R.string.done_with_count, resultList.size(), mDesireImageCount);
        menuDoneItem.setTitle(text);
        boolean hasSelected = resultList.size() > 0;
        menuDoneItem.setEnabled(hasSelected);
        btnPreview.setEnabled(hasSelected);
        //TODO
        if(hasSelected){
            btnPreview.setText(getResources().getString(R.string.preview) + "(" + (resultList.size()) + ")");
        } else {
            btnPreview.setText(getResources().getString(R.string.preview));
        }
    }

    private LoaderManager.LoaderCallbacks
            <Cursor> mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        private final String[] IMAGE_PROJECTION = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media._ID };

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {

            // 根据图片设置参数新增验证条件
            StringBuilder selectionArgs = new StringBuilder();

            if(imageConfig != null){
                if(imageConfig.minWidth != 0){
                    selectionArgs.append(MediaStore.Images.Media.WIDTH + " >= " + imageConfig.minWidth);
                }

                if(imageConfig.minHeight != 0){
                    selectionArgs.append("".equals(selectionArgs.toString()) ? "" : " and ");
                    selectionArgs.append(MediaStore.Images.Media.HEIGHT + " >= " + imageConfig.minHeight);
                }

                if(imageConfig.minSize != 0f){
                    selectionArgs.append("".equals(selectionArgs.toString()) ? "" : " and ");
                    selectionArgs.append(MediaStore.Images.Media.SIZE + " >= " + imageConfig.minSize);
                }

                if(imageConfig.mimeType != null){
                    selectionArgs.append(" and (");
                    for(int i = 0, len = imageConfig.mimeType.length; i < len; i++){
                        if(i != 0){
                            selectionArgs.append(" or ");
                        }
                        selectionArgs.append(MediaStore.Images.Media.MIME_TYPE + " = '" + imageConfig.mimeType[i] + "'");
                    }
                    selectionArgs.append(")");
                }
            }

            if(id == LOADER_ALL) {
                CursorLoader cursorLoader = new CursorLoader(mCxt,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                        selectionArgs.toString(), null, IMAGE_PROJECTION[2] + " DESC");
                return cursorLoader;
            }else if(id == LOADER_CATEGORY){
                String selectionStr = selectionArgs.toString();
                if(!"".equals(selectionStr)){
                    selectionStr += " and" + selectionStr;
                }
                CursorLoader cursorLoader = new CursorLoader(mCxt,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                        IMAGE_PROJECTION[0] + " like '%" + args.getString("path") + "%'" + selectionStr, null,
                        IMAGE_PROJECTION[2] + " DESC");
                return cursorLoader;
            }

            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {
                List<Image> images = new ArrayList<>();
                int count = data.getCount();
                if (count > 0) {
                    data.moveToFirst();
                    do{
                        String path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                        String name = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                        long dateTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));

                        Image image = new Image(path, name, dateTime);
                        images.add(image);
                        if( !hasFolderGened ) {
                            // 获取文件夹名称
                            File imageFile = new File(path);
                            File folderFile = imageFile.getParentFile();
                            Folder folder = new Folder();
                            folder.name = folderFile.getName();
                            folder.path = folderFile.getAbsolutePath();
                            folder.cover = image;
                            if (!mResultFolder.contains(folder)) {
                                List<Image> imageList = new ArrayList<>();
                                imageList.add(image);
                                folder.images = imageList;
                                mResultFolder.add(folder);
                            } else {
                                // 更新
                                Folder f = mResultFolder.get(mResultFolder.indexOf(folder));
                                f.images.add(image);
                            }
                        }

                    }while(data.moveToNext());

                    mImageAdapter.setList(images);

                    // 设定默认选择
                    if(resultList != null && resultList.size()>0){
                        mImageAdapter.setDefaultSelected(resultList);
                    }

                    mFolderAdapter.setList(mResultFolder);
                    hasFolderGened = true;

                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    /**
     * 获取GridView Item宽度
     * @return
     */
    private int getItemImageWidth(){
        int cols = getNumColnums();
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int columnSpace = getResources().getDimensionPixelOffset(R.dimen.space_size);
        return (screenWidth - columnSpace * (cols-1)) / cols;
    }

    /**
     * 根据屏幕宽度与密度计算GridView显示的列数， 最少为三列
     * @return
     */
    private int getNumColnums(){
        int cols = getResources().getDisplayMetrics().widthPixels / getResources().getDisplayMetrics().densityDpi;
        return cols < 3 ? 3 : cols;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_picker, menu);
        menuDoneItem = menu.findItem(R.id.action_picker_done);
        menuDoneItem.setEnabled(false);
        refreshActionStatus();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }

        if(item.getItemId() == R.id.action_picker_done){
            complete();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // 返回已选择的图片数据
    private void complete(){
        Intent data = new Intent();
        data.putStringArrayListExtra(EXTRA_RESULT, resultList);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        captureManager.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        captureManager.onRestoreInstanceState(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }
}
