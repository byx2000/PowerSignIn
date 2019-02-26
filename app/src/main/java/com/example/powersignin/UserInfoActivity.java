package com.example.powersignin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.QueryListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.powersignin.Util.FileUtil;
import com.example.powersignin.Util.WifiUtil;
import com.example.powersignin.bean.Student;
import com.example.powersignin.bean.Teacher;

import java.io.File;

public class UserInfoActivity extends BaseActivity implements View.OnClickListener
{
    private static final String EXTRA_USERNAME = "username";
    private static final String EXTRA_OBJECTID = "objectid";
    private static final String EXTRA_NICKNAME = "nickname";
    private static final String EXTRA_IDENTITY = "identity";

    private Toolbar teacherInfoActivityToolbar;
    private TextView mIdentityTextView;
    private TextView mUsernameTextView;
    private TextView mNicknameTextView;
    //private TextView mObjectIdTextView;
    private Button mLogoutButton;
    private ImageView mFaceImage;

    private String mUsername;
    private String mObjectId;
    private String mNickname;
    private String mIdentity;

    public static Intent newIntent(Context context, String username, String objectId, String nickname, String identity)
    {
        Intent intent = new Intent(context, UserInfoActivity.class);
        intent.putExtra(EXTRA_USERNAME, username);
        intent.putExtra(EXTRA_OBJECTID, objectId);
        intent.putExtra(EXTRA_NICKNAME, nickname);
        intent.putExtra(EXTRA_IDENTITY, identity);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setContentView()
    {
        setContentView(R.layout.activity_user_info);
    }

    @Override
    protected void initViews()
    {
        teacherInfoActivityToolbar = (Toolbar)findViewById(R.id.toolbar_teacher_info_activity);
        mIdentityTextView = (TextView)findViewById(R.id.text_identity);
        mUsernameTextView = (TextView)findViewById(R.id.text_username);
        mNicknameTextView = (TextView)findViewById(R.id.text_nickname);
        //mObjectIdTextView = (TextView)findViewById(R.id.text_teacher_objectid);
        mLogoutButton = (Button)findViewById(R.id.btn_logout);
        mFaceImage = (ImageView)findViewById(R.id.img_face);
    }

    @Override
    protected void initListeners()
    {
        mLogoutButton.setOnClickListener(this);
        mFaceImage.setOnClickListener(this);
    }

    @Override
    protected void initData()
    {
        //设置Toolbar
        setSupportActionBar(teacherInfoActivityToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //setToolbarTitle("我的信息");

        mUsername = getIntent().getStringExtra(EXTRA_USERNAME);
        mUsernameTextView.setText(mUsernameTextView.getText() + mUsername);

        mObjectId = getIntent().getStringExtra(EXTRA_OBJECTID);
        //mObjectIdTextView.setText(mObjectId);

        mNickname = getIntent().getStringExtra(EXTRA_NICKNAME);
        mNicknameTextView.setText(mNicknameTextView.getText() + mNickname);

        mIdentity = getIntent().getStringExtra(EXTRA_IDENTITY);
        if (mIdentity.equals("teacher"))
        {
            mIdentityTextView.setText(mIdentityTextView.getText() + "教师");
        }
        else
        {
            mIdentityTextView.setText(mIdentityTextView.getText() + "学生");
        }

        //下载面部图片
        final File file = new File(getExternalCacheDir() + "face.jpg");
        if (file.isFile() && file.exists())
        {
            file.delete();
        }

        if (mIdentity.equals("teacher"))
        {
            findTeacherByTeacherObjectId(mObjectId, new QueryListener<Teacher>()
            {
                @Override
                public void done(Teacher teacher, BmobException e)
                {
                    if (e == null)
                    {
                        downloadFile(file, teacher.getFaceImageUrl(), new DownloadFileListener()
                        {
                            @Override
                            public void done(String s, BmobException e)
                            {
                                if (e == null)
                                {
                                    Glide.with(UserInfoActivity.this).load(s).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(mFaceImage);
                                }
                                else
                                {
                                    toast("下载图片失败");
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
                        toast("查找教师失败");
                    }
                }
            });
        }
        else
        {
            findStudentByStudentObjectId(mObjectId, new QueryListener<Student>()
            {
                @Override
                public void done(Student student, BmobException e)
                {
                    if (e == null)
                    {
                        downloadFile(file, student.getFaceImageUrl(), new DownloadFileListener()
                        {
                            @Override
                            public void done(String s, BmobException e)
                            {
                                if (e == null)
                                {
                                    Glide.with(UserInfoActivity.this).load(s).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(mFaceImage);
                                }
                                else
                                {
                                    toast("下载图片失败");
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
                        toast("查找学生失败: " + e.getMessage());
                    }
                }
            });
        }
    }

    @Override
    public void onClick(View v)
    {
        //退出登陆按钮
        if (v == mLogoutButton)
        {
            if (!new WifiUtil(this).isNetworkConnected())
            {
                toast("请检查网络连接!");
                return;
            }

            //弹出对话框
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(R.drawable.warning);
            builder.setTitle("退出登陆");
            builder.setMessage("确定要退出登陆吗?\n退出登陆将会清空您的登陆信息");
            builder.setPositiveButton("是", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    //退出登陆
                    FileUtil.save(UserInfoActivity.this, "", "login", new FileUtil.SaveFileListener()
                    {
                        @Override
                        public void succeed()
                        {
                            logout();
                            ActivityCollector.finishAll();
                            startLoginActivity();
                        }

                        @Override
                        public void failed(String info)
                        {
                            toast("退出登陆失败，请重试");
                        }
                    });
                }
            });
            builder.setNegativeButton("否", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {

                }
            });
            builder.show();
        }
        //查看面部照片
        else if (v == mFaceImage)
        {
            startImageActivity(new File(getExternalCacheDir() + "face.jpg").getPath());
        }
    }
}
