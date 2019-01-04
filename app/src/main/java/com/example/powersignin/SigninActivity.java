package com.example.powersignin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.QueryListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.powersignin.Util.FaceUtil;
import com.example.powersignin.Util.WifiUtil;
import com.example.powersignin.bean.Classroom;
import com.example.powersignin.bean.SigninEvent;
import com.example.powersignin.bean.Student;
import id.zelory.compressor.Compressor;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SigninActivity extends BaseActivity implements View.OnClickListener
{
    private static final String EXTRA_CLASSROOM_OBJECTID = "classroom_objectid";
    private static final String EXTRA_STUDENT_OBJECTID = "student_objectid";

    private Toolbar mToolbar;
    private Button mFaceVerify;
    private Button mWifiVerify;

    private String mClassroomObjectId;
    private String mStudentObjectId;
    private String mBssid;

    private boolean isFaceVerifyPass;
    private boolean isWifiVerifyPass;

    private WifiUtil mWifiUtil;

    private WifiBroadcastReceiver mWifiBroadcastReceiver;

    private File mCurrentImageFile;
    private Uri mCurrentImageUri;

    public static Intent newIntent(Context context, String classroomObjectId, String studentObjectId)
    {
        Intent intent = new Intent(context, SigninActivity.class);
        intent.putExtra(EXTRA_CLASSROOM_OBJECTID, classroomObjectId);
        intent.putExtra(EXTRA_STUDENT_OBJECTID, studentObjectId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //注册广播接收器
        IntentFilter intentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mWifiBroadcastReceiver = new WifiBroadcastReceiver();
        registerReceiver(mWifiBroadcastReceiver, intentFilter);
    }

    @Override
    protected void setContentView()
    {
        setContentView(R.layout.activity_signin);
    }

    @Override
    protected void initViews()
    {
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mFaceVerify = (Button)findViewById(R.id.btn_face_verify);
        mWifiVerify = (Button)findViewById(R.id.btn_wifi_verify);
    }

    @Override
    protected void initListeners()
    {
        mFaceVerify.setOnClickListener(this);
        mWifiVerify.setOnClickListener(this);
    }

    @Override
    protected void initData()
    {
        mClassroomObjectId = getIntent().getStringExtra(EXTRA_CLASSROOM_OBJECTID);
        mStudentObjectId = getIntent().getStringExtra(EXTRA_STUDENT_OBJECTID);
        isFaceVerifyPass = false;
        isWifiVerifyPass = false;

        mWifiUtil = new WifiUtil(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v)
    {
        //人脸识别验证
        if (v == mFaceVerify)
        {
            if (!mWifiUtil.isNetworkConnected())
            {
                toast("请检查网络连接!");
                return;
            }

            pauseFaceVerifyButton();

            isFaceVerifyPass = false;

            //开启相机
            mCurrentImageFile = new File(getExternalCacheDir(), "current_face.jpg");
            if (mCurrentImageFile.exists())
            {
                mCurrentImageFile.delete();
            }
            try
            {
                mCurrentImageFile.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            if (Build.VERSION.SDK_INT >= 24)
            {
                mCurrentImageUri = FileProvider.getUriForFile(SigninActivity.this, "com.example.powersignin.fileprovider", mCurrentImageFile);
            }
            else
            {
                mCurrentImageUri = Uri.fromFile(mCurrentImageFile);
            }

            startCameraActivity(mCurrentImageUri);
        }
        //wifi验证
        else if (v == mWifiVerify)
        {
            if (!mWifiUtil.isNetworkConnected())
            {
                toast("请检查网络连接!");
                return;
            }

            if (!mWifiUtil.isGpsOpen())
            {
                toast("请打开GPS");
                return;
            }

            pauseWifiVerifyButton();

            findClassroom(mClassroomObjectId, new QueryListener<Classroom>()
            {
                @Override
                public void done(Classroom classroom, BmobException e)
                {
                    if (e == null)
                    {
                        findSigninEvent(classroom.getCurrentSigninEvent(), new QueryListener<SigninEvent>()
                        {
                            @RequiresApi(api = Build.VERSION_CODES.M)
                            @Override
                            public void done(SigninEvent signinEvent, BmobException e)
                            {
                                if (e == null)
                                {
                                    mBssid = signinEvent.getBssid();
                                    pauseWifiVerifyButton();
                                    mWifiUtil.startScanWifi();
                                }
                            }
                        });
                    }
                }
            });
        }

        //检查签到结果
        checkSigninResult();
    }

    //wifi扫描结束的广播接收器
    private class WifiBroadcastReceiver extends BroadcastReceiver
    {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
            {
                if (!isWifiVerifyPass)
                {
                    List<ScanResult> results = mWifiUtil.getScanResults();
                    for (ScanResult result : results)
                    {
                        if (result.BSSID.toLowerCase().equals(mBssid.toLowerCase()))
                        {
                            disableWifiVerifyButton();
                            isWifiVerifyPass = true;
                            //取消注册广播
                            unregisterReceiver(mWifiBroadcastReceiver);
                            //检查签到结果
                            checkSigninResult();
                            return;
                        }
                    }

                    toast("wifi验证失败");
                    enableWifiVerifyButton();
                }
            }
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (!isWifiVerifyPass)
        {
            //取消注册广播
            unregisterReceiver(mWifiBroadcastReceiver);
        }
    }

    private void checkSigninResult()
    {
        if (isFaceVerifyPass && isWifiVerifyPass)
        {
            studentSignin(mStudentObjectId, mClassroomObjectId, new StudentSigninListener()
            {
                @Override
                public void succeed()
                {
                    setResult(RESULT_OK);
                    finish();
                }

                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void failed(String info)
                {
                    toast("签到失败: " + info);
                    isFaceVerifyPass = false;
                    isWifiVerifyPass = false;
                    enableFaceVerifyButton();
                    enableWifiVerifyButton();
                }
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_TAKE_PHOTO)
        {
            if (resultCode == RESULT_OK)
            {
                pauseFaceVerifyButton();

                File mCurrentFace = null;

                //压缩图片
                try
                {
                    mCurrentFace = new Compressor(this)
                            .setDestinationDirectoryPath(new File(getExternalCacheDir(), "face").getPath())
                            .compressToFile(mCurrentImageFile);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                //下载图片
                final File finalMCurrentFace = mCurrentFace;
                findStudentByStudentObjectId(mStudentObjectId, new QueryListener<Student>()
                {
                    @Override
                    public void done(Student student, BmobException e)
                    {
                        if (e == null)
                        {
                            downloadFile(new File(getExternalCacheDir(), "signined_face.jpg"), student.getFaceImageUrl(), new DownloadFileListener()
                            {
                                @RequiresApi(api = Build.VERSION_CODES.M)
                                @Override
                                public void done(String s, BmobException e)
                                {
                                    if (e == null)
                                    {
                                        //人脸对比
                                        FaceUtil.faceMatch(s, finalMCurrentFace.getPath(), new FaceUtil.FaceMatchListener()
                                        {
                                            @RequiresApi(api = Build.VERSION_CODES.M)
                                            @Override
                                            public void succeed(double similarity)
                                            {
                                                disableFaceVerifyButton();
                                                isFaceVerifyPass = true;
                                                checkSigninResult();
                                            }

                                            @RequiresApi(api = Build.VERSION_CODES.M)
                                            @Override
                                            public void failed(String info)
                                            {
                                                toast("人脸对比失败: " + info);
                                                enableFaceVerifyButton();
                                            }
                                        });
                                    }
                                    else
                                    {
                                        toast("人脸验证失败: " + e.getMessage());
                                        enableFaceVerifyButton();
                                    }
                                }

                                @Override
                                public void onProgress(Integer integer, long l)
                                {

                                }
                            });
                        }
                        else
                        {
                            toast("人脸验证失败: " + e.getMessage());
                            enableFaceVerifyButton();
                        }
                    }
                });
            }
            else
            {
                toast("人脸验证失败");
                enableFaceVerifyButton();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void pauseFaceVerifyButton()
    {
        mFaceVerify.setEnabled(false);
        mFaceVerify.setTextColor(getResources().getColor(R.color.grey));
        mFaceVerify.setText("正在进行人脸验证...");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void disableFaceVerifyButton()
    {
        mFaceVerify.setEnabled(false);
        mFaceVerify.setTextColor(getResources().getColor(R.color.white));
        mFaceVerify.setBackground(getDrawable(R.drawable.button_ok));
        mFaceVerify.setText("人脸验证通过");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void enableFaceVerifyButton()
    {
        mFaceVerify.setEnabled(true);
        mFaceVerify.setTextColor(getResources().getColor(R.color.white));
        mFaceVerify.setText("人脸验证");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void pauseWifiVerifyButton()
    {
        mWifiVerify.setEnabled(false);
        mWifiVerify.setTextColor(getResources().getColor(R.color.grey));
        mWifiVerify.setText("正在进行wifi验证...");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void disableWifiVerifyButton()
    {
        mWifiVerify.setEnabled(false);
        mWifiVerify.setTextColor(getResources().getColor(R.color.white));
        mWifiVerify.setBackground(getDrawable(R.drawable.button_ok));
        mWifiVerify.setText("wifi验证通过");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void enableWifiVerifyButton()
    {
        mWifiVerify.setEnabled(true);
        mWifiVerify.setTextColor(getResources().getColor(R.color.white));
        mWifiVerify.setText("wifi验证");
    }
}
