package com.example.powersignin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class ImageActivity extends BaseActivity implements View.OnClickListener
{
    private static final String EXTRA_IMAGE_PATH = "image_path";

    private ImageView mImage;

    private String mImagePath;

    public static Intent newIntent(Context context, String imagePath)
    {
        Intent intent = new Intent(context, ImageActivity.class);
        intent.putExtra(EXTRA_IMAGE_PATH, imagePath);
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
        //全屏
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_image);
    }

    @Override
    protected void initViews()
    {
        mImage = (ImageView)findViewById(R.id.image);
    }

    @Override
    protected void initListeners()
    {
        mImage.setOnClickListener(this);
    }

    @Override
    protected void initData()
    {
        mImagePath = getIntent().getStringExtra(EXTRA_IMAGE_PATH);

        //加载图片
        Glide.with(this).load(mImagePath).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(mImage);
    }

    @Override
    public void onClick(View v)
    {
        finish();
    }
}
