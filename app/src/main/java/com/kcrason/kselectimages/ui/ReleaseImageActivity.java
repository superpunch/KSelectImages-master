package com.kcrason.kselectimages.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kcrason.kselectimages.JBitmapUtils;
import com.kcrason.kselectimages.R;
import com.kcrason.kselectimages.adapter.ImagePublishAdapter;
import com.kcrason.kselectimages.event.RemoveImageEvent;
import com.kcrason.kselectimages.utils.ActivityManager;
import com.kcrason.kselectimages.utils.Constants;
import com.kcrason.kselectimages.utils.KUtils;
import com.kcrason.kselectimages.utils.ShowUtils;
import com.kcrason.kselectimages.utils.SnackBarUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by KCrason on 2016/6/7.
 */
public class ReleaseImageActivity extends Activity
        implements AdapterView.OnItemClickListener, DialogInterface.OnClickListener {

    private static final int MSG_PB = 0x01;

    private final static int REQUEST_IMAGE = 3;

    private ImagePublishAdapter mAdapter;

    private ProgressDialog progressDialog;

    private ArrayList<String> mSelectPath = new ArrayList<>();
    private ArrayList<String> mYSelectPath = new ArrayList<>();

    private MessageHandler mMessageHandler;

    @BindView(R.id.rootView)
    LinearLayout mRootView;

    @BindView(R.id.txt_send)
    TextView mSendImage;

    @BindView(R.id.edt_content)
    EditText mInputContent;

    @BindView(R.id.gv_images)
    GridView mGridView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_relese_image);
        ButterKnife.bind(this);
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    @OnClick({R.id.back, R.id.txt_send})
    void onClicks(View view) {
        switch (view.getId()) {
            case R.id.back:
                if (mSelectPath.size() != 0) {
                    ShowUtils.showDialog(this, getString(R.string.exit_edit), this);
                } else {
                    recoveryActivitys();
                }
                break;
            case R.id.txt_send:
                sendSelectedImages();
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();


    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        super.onDestroy();
    }

    private void selectAblum() {
        Intent intent = new Intent(ReleaseImageActivity.this, KSelectImagesActivity.class);
        // 是否显示拍摄图片
        intent.putExtra(Constants.EXTRA_SHOW_CAMERA, true);
        // 最大可选择图片数量
        intent.putExtra(Constants.EXTRA_SELECT_COUNT, 9);
        // 选择模式
        intent.putExtra(Constants.EXTRA_SELECT_MODE, KSelectImagesActivity.MODE_MULTI);
        // 默认选择
        intent.putExtra(Constants.EXTRA_DEFAULT_SELECTED_LIST, mSelectPath);

        startActivityForResult(intent, REQUEST_IMAGE);
        overridePendingTransition(R.anim.selecter_image_alpha_enter, R.anim.selecter_image_alpha_exit);
    }


    public void initViews() {
        ButterKnife.bind(this);
        ActivityManager.getInstance().addActivity(this);
        mMessageHandler = new MessageHandler(this);

        Bundle bundle = getIntent().getBundleExtra(Constants.KEY);
        if (bundle != null) {
            mSelectPath = bundle.getStringArrayList(Constants.EXTRA_RESULT);
        }

        mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mAdapter = new ImagePublishAdapter(this, mSelectPath);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);
    }


    private int getDataSize() {
        return mSelectPath == null ? 0 : mSelectPath.size();
    }

    private void recoveryActivitys() {
        if (mSelectPath != null) {
            mSelectPath.clear();
        }
        ActivityManager.getInstance().finishActivitys();
    }


    private void sendSelectedImages() {
        if (mSelectPath.size() != 0) {
            if (true) {
                KUtils.putAwaySoftKeyboard(ReleaseImageActivity.this, mInputContent);
                /**
                 * 遍历图片地址，如果发现有空值的则无法上传
                 */
                for (String fp : mSelectPath) {
                    if (TextUtils.isEmpty(fp)) {
                        SnackBarUtils.showSnackBar(mRootView, getString(R.string.image_error));
                        return;
                    }
                }

                Log.e("options","mSelectPath:"+mSelectPath);
                Log.e("options","文件路径:"+ Environment.DIRECTORY_PICTURES);

            int i=0;
                for( String path: mSelectPath){
                try {
                  Bitmap bitmap= JBitmapUtils.revitionImageSize(path);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        Log.e("options","bitmap19 大小:"+bitmap.getAllocationByteCount());
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1){//API 12
                        Log.e("options","bitmap12 大小:"+   bitmap.getByteCount());
                    }
                    Log.e("options","bitmap :"+bitmap.getWidth()+"------"+bitmap.getHeight());
                    String path0=   path.substring(0, path.lastIndexOf("/")+1);
                    String path1=   path.substring( path.lastIndexOf("."));
                    String  path2=   path.substring( path.lastIndexOf("/")+1 ,path.lastIndexOf("."))+"ys";
                    String  filepath=path0+path2+path1;

                    String str=    JBitmapUtils.saveBitmap2File(bitmap,filepath);

                    mYSelectPath.add(str);
                 //   File f = new File( "/storage/emulated/0/DCIM/"+bitName + ".jpg");
              //      JBitmapUtils.saveMyBitmap(bitmap,"ceshi"+i++);
                    Log.e("options","生成的文件路径：:"+ str);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                }
                progressDialog = ShowUtils.showProgressDialog(this, getString(R.string.loading));

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(10000);
                            mMessageHandler.sendEmptyMessage(MSG_PB);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            } else {
                SnackBarUtils.showSnackBar(mRootView, getString(R.string.not_network));
            }
        } else {
            SnackBarUtils.showSnackBar(mRootView, getString(R.string.at_least_one_photo));
        }

        for(String path:mYSelectPath){
            File deleteFile = new File(path);
            deleteFile.delete();
            Log.e("options","删除成功！");
        }


    }


    static class MessageHandler extends Handler {

        WeakReference<Activity> mWeakReference;

        public MessageHandler(ReleaseImageActivity releaseImageActivity) {
            this.mWeakReference = new WeakReference<Activity>(releaseImageActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            ReleaseImageActivity releaseImageActivity = (ReleaseImageActivity) this.mWeakReference.get();
            switch (msg.what) {
                case MSG_PB:
                    SnackBarUtils.showSnackBar(releaseImageActivity.mRootView, releaseImageActivity.getString(R.string.upload_sucess));
                    releaseImageActivity.progressDialog.dismiss();
                    break;
            }
            super.handleMessage(msg);
        }
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == getDataSize()) {
            KUtils.putAwaySoftKeyboard(ReleaseImageActivity.this, mInputContent);
            selectAblum();
        } else {
            Intent intent = new Intent(ReleaseImageActivity.this, ImageZoomActivity.class);
            intent.putExtra(Constants.EXTRA_RESULT, (Serializable) mSelectPath);
            intent.putExtra(Constants.EXTRA_CURRENT_IMG_POSITION, position);
            startActivity(intent);
            overridePendingTransition(R.anim.selecter_image_alpha_enter, R.anim.selecter_image_alpha_exit);
        }
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (mSelectPath.size() != 0) {
                ShowUtils.showDialog(this, getString(R.string.exit_edit), this);
            } else {
                recoveryActivitys();
            }
        }
        return true;
    }

    @Subscribe (sticky = true, threadMode = ThreadMode.MAIN)
    public void onImageChangeListener(RemoveImageEvent event) {
        boolean isRevoke = event.getIsRevoke();
        int index = event.getIndex();
        if (isRevoke) {
            String path = event.getPath();
            if (!TextUtils.isEmpty(path)) {
                mSelectPath.add(index, event.getPath());
            }
        } else {
            mSelectPath.remove(index);
        }
        mAdapter.notifyDataSetChanged();
        Log.e("Fragment", "onImageChangeListener: " + mSelectPath);
        Log.e("Fragment", "onImageChangeListener_RemoveImageEvent index: " + index+"  isRevoke:"+isRevoke);


    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        recoveryActivitys();
    }
}
