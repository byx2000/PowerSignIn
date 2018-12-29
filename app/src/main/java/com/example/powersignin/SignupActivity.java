package com.example.powersignin;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.powersignin.Util.FaceUtil;
import com.example.powersignin.bean.User;
import id.zelory.compressor.Compressor;

import java.io.File;
import java.io.IOException;

public class SignupActivity extends BaseActivity implements View.OnClickListener
{
    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private EditText mNicknameEditText;
    private RadioButton mTeacherRadioButton;
    private RadioButton mStudentRadioButton;
    private Button mSignupButton;
    private ImageView mFaceImage;

    private File mImageFile;
    private Uri mImageUri;

    private boolean hasFacePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setContentView()
    {
        setContentView(R.layout.activity_signup);
    }

    @Override
    protected void initViews()
    {
        mUsernameEditText = (EditText)findViewById(R.id.edit_username);
        mPasswordEditText = (EditText)findViewById(R.id.edit_password);
        mNicknameEditText = (EditText)findViewById(R.id.edit_nickname);
        mTeacherRadioButton = (RadioButton)findViewById(R.id.radio_teacher);
        mStudentRadioButton = (RadioButton)findViewById(R.id.radio_student);
        mSignupButton = (Button)findViewById(R.id.btn_signup);
        mFaceImage = (ImageView)findViewById(R.id.img_face_photo);
    }

    @Override
    protected void initListeners()
    {
        mSignupButton.setOnClickListener(this);
        mFaceImage.setOnClickListener(this);
    }

    @Override
    protected void initData()
    {
        mStudentRadioButton.setChecked(true);
        hasFacePhoto = false;

        //开启摄像机
        mImageFile = new File(getExternalCacheDir(), "face.jpg");
        if (mImageFile.exists())
        {
            mImageFile.delete();
        }
        try
        {
            mImageFile.createNewFile();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= 24)
        {
            mImageUri = FileProvider.getUriForFile(SignupActivity.this, "com.example.powersignin.fileprovider", mImageFile);
        }
        else
        {
            mImageUri = Uri.fromFile(mImageFile);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v)
    {
        //注册用户
        if (v == mSignupButton)
        {
            if (!hasFacePhoto)
            {
                toast("请拍摄面部照片");
            }
            else
            {
                disableSignupButton();

                final String username = mUsernameEditText.getText().toString();
                final String password = mPasswordEditText.getText().toString();
                final String nickname = mNicknameEditText.getText().toString();

                if (username.equals(""))
                {
                    toast("请填写手机号");
                    enableSignupButton();
                    return;
                }
                else if (password.equals(""))
                {
                    toast("请填写密码");
                    enableSignupButton();
                    return;
                }
                else if (nickname.equals(""))
                {
                    toast("请填写昵称");
                    enableSignupButton();
                    return;
                }

                String type = "student";
                if (mTeacherRadioButton.isChecked())
                {
                    type = "teacher";
                }

                File compressFile = null;

                //压缩图片
                try
                {
                    compressFile = new Compressor(this)
                            .setDestinationDirectoryPath(new File(getExternalCacheDir(), "face").getPath())
                            .compressToFile(mImageFile);
                }
                catch (IOException e)
                {
                    toast("压缩图片失败!");
                    e.printStackTrace();
                }

                if (compressFile == null)
                {
                    compressFile = mImageFile;
                }

                //检测人脸
                final String finalType = type;
                final File finalCompressFile = compressFile;
                FaceUtil.faceDetect(compressFile.getPath(), new FaceUtil.FaceDetectListener()
                {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void succeed(boolean hasFace)
                    {
                        //有人脸
                        if (hasFace)
                        {
                            //注册
                            signup(username, password, nickname, finalType, finalCompressFile, new SignupListener()
                            {
                                @Override
                                public void succeed(User user)
                                {
                                    toast("注册成功: " + user.getUsername() + " " + user.getType());
                                    finish();
                                }

                                @Override
                                public void failed(String info)
                                {
                                    toast("注册失败: " + info);
                                    mSignupButton.setEnabled(true);
                                    mSignupButton.setText("注册");
                                }
                            });
                        }
                        //无人脸
                        else
                        {
                            toast("照片无人脸，请重新拍摄");
                            enableSignupButton();
                        }
                    }

                    //检测失败
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void failed(String info)
                    {
                        toast("人脸检测失败，请重试");
                        enableSignupButton();
                    }
                });
            }
        }
        //拍摄脸部照片
        else if (v == mFaceImage)
        {
            //打开相机
            startCameraActivity(mImageUri);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_TAKE_PHOTO)
        {
            if (resultCode == RESULT_OK)
            {
                hasFacePhoto = true;
                Glide.with(this).load(mImageFile).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(mFaceImage);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void enableSignupButton()
    {
        mSignupButton.setEnabled(true);
        mSignupButton.setTextColor(getColor(R.color.white));
        mSignupButton.setText("注册");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void disableSignupButton()
    {
        mSignupButton.setEnabled(false);
        mSignupButton.setTextColor(getColor(R.color.grey));
        mSignupButton.setText("正在注册...");
    }
}
