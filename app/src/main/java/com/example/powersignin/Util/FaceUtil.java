package com.example.powersignin.Util;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FaceUtil
{
    private static final String APIKey = "MGZfDrHCC94OqMUMoOqcG84B";
    private static final String SecretKey = "Nv55tz5ZEeTFgZtBimdDxQQsPSpgfCww";

    //人脸检测结果JSON对应的java bean
    public static class FaceDetectJsonBean
    {
        public String error_code;
        public String error_msg;
        public Result result;

        public static class Result
        {
            public int face_num;
        }
    }

    //人脸对比结果JSON对应的java bean
    public static class FaceMatchJsonBean
    {
        public String error_code;
        public String error_msg;
        public Result result;

        public static class Result
        {
            public double score;
        }
    }

    public interface GetTokenListener
    {
        void succeed(String token);
        void failed(String info);
    }

    public interface FaceDetectListener
    {
        void succeed(boolean hasFace);
        void failed(String info);
    }

    public interface FaceMatchListener
    {
        void succeed(double similarity);
        void failed(String info);
    }

    //获取token 用于调用api
    public static void getToken(final GetTokenListener getTokenListener)
    {
        final int MSG_GET_TOKEN_SUCCEED = 1001;
        final int MSG_GET_TOKEN_FAILED = 1002;
        final Message message = new Message();
        final Bundle bundle = new Bundle();

        //处理消息
        @SuppressLint("HandlerLeak") final Handler handler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                switch (msg.what)
                {
                    case MSG_GET_TOKEN_SUCCEED:
                        getTokenListener.succeed(msg.getData().getString("token"));
                        break;
                    case MSG_GET_TOKEN_FAILED:
                        getTokenListener.failed(msg.getData().getString("error"));
                        break;
                }
            }
        };

        //开启新线程
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                // 获取token地址
                String authHost = "https://aip.baidubce.com/oauth/2.0/token?";
                String getAccessTokenUrl = authHost
                        // 1. grant_type为固定参数
                        + "grant_type=client_credentials"
                        // 2. 官网获取的 API Key
                        + "&client_id=" + APIKey
                        // 3. 官网获取的 Secret Key
                        + "&client_secret=" + SecretKey;
                try
                {
                    URL realUrl = new URL(getAccessTokenUrl);
                    // 打开和URL之间的连接
                    HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();
                    // 获取所有响应头字段
                    Map<String, List<String>> map = connection.getHeaderFields();
                    // 定义 BufferedReader输入流来读取URL的响应
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String result = "";
                    String line;
                    while ((line = in.readLine()) != null)
                    {
                        result += line;
                    }
                    /**
                     * 返回结果示例
                     */
                    JSONObject jsonObject = new JSONObject(result);
                    String access_token = jsonObject.getString("access_token");

                    bundle.putString("token", access_token);
                    message.what = MSG_GET_TOKEN_SUCCEED;
                    message.setData(bundle);
                    handler.sendMessage(message);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    bundle.putString("error", "获取token失败!");
                    message.what = MSG_GET_TOKEN_FAILED;
                    message.setData(bundle);
                    handler.sendMessage(message);
                }
            }
        }).start();
    }

    //人脸检测
    public static void faceDetect(final String path, final FaceDetectListener faceDetectListener)
    {
        final int MSG_FACE_DETECT_SUCCEED = 1001;
        final int MSG_FACE_DETECT_FAILED = 1002;
        final Message message = new Message();
        final Bundle bundle = new Bundle();

        //处理消息
        @SuppressLint("HandlerLeak") final Handler handler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                switch (msg.what)
                {
                    //检测成功
                    case MSG_FACE_DETECT_SUCCEED:
                        //将JSON转换成java bean
                        String json = bundle.getString("result");
                        FaceDetectJsonBean bean = GsonUtils.fromJson(json, FaceDetectJsonBean.class);

                        //有人脸
                        if (bean.error_code.equals("0"))
                        {
                            faceDetectListener.succeed(true);
                        }
                        //无人脸
                        else
                        {
                            faceDetectListener.succeed(false);
                        }
                        break;
                    //检测失败
                    case MSG_FACE_DETECT_FAILED:
                        String info = message.getData().getString("error");
                        faceDetectListener.failed(info);
                        break;
                }
            }
        };

        //获取token
        getToken(new GetTokenListener()
        {
            @Override
            public void succeed(final String token)
            {
                //开启新线程
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // 请求url
                        String url = "https://aip.baidubce.com/rest/2.0/face/v3/detect";
                        try
                        {
                            byte[] bytes = FileUtil.readFileByBytes(path);
                            String image = Base64Util.encode(bytes);

                            Map<String, Object> map = new HashMap<>();
                            map.put("image", image);
                            map.put("face_field", "faceshape,facetype");
                            map.put("image_type", "BASE64");

                            String param = GsonUtils.toJson(map);

                            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
                            String result = HttpUtil.post(url, token, "application/json", param);

                            bundle.putString("result", result);
                            message.what = MSG_FACE_DETECT_SUCCEED;
                            message.setData(bundle);
                            handler.sendMessage(message);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            bundle.putString("error", "人脸检测失败!");
                            message.what = MSG_FACE_DETECT_FAILED;
                            message.setData(bundle);
                            handler.sendMessage(message);
                        }
                    }
                }).start();
            }

            @Override
            public void failed(String info)
            {
                faceDetectListener.failed(info);
            }
        });
    }

    //人脸对比
    public static void faceMatch(final String path1, final String path2, final FaceMatchListener faceMatchListener)
    {
        final int MSG_FACE_MATCH_SUCCEED = 1001;
        final int MSG_FACE_MATCH_FAILED = 1002;
        final Message message = new Message();
        final Bundle bundle = new Bundle();

        //处理消息
        @SuppressLint("HandlerLeak") final Handler handler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                switch (msg.what)
                {
                    //检测成功
                    case MSG_FACE_MATCH_SUCCEED:
                        //将JSON转换成java bean
                        String json = message.getData().getString("result");
                        FaceMatchJsonBean bean = GsonUtils.fromJson(json, FaceMatchJsonBean.class);

                        if (bean.error_code.equals("0"))
                        {
                            faceMatchListener.succeed(bean.result.score);
                        }
                        else
                        {
                            faceMatchListener.failed(bean.error_msg);
                        }
                        break;
                    //检测失败
                    case MSG_FACE_MATCH_FAILED:
                        String info = message.getData().getString("error");
                        faceMatchListener.failed(info);
                        break;
                }
            }
        };

        //获取token
        getToken(new GetTokenListener()
        {
            @Override
            public void succeed(final String token)
            {
                //开启新线程
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // 请求url
                        String url = "https://aip.baidubce.com/rest/2.0/face/v3/match";
                        try
                        {
                            byte[] bytes1 = FileUtil.readFileByBytes(path1);
                            byte[] bytes2 = FileUtil.readFileByBytes(path2);
                            String image1 = Base64Util.encode(bytes1);
                            String image2 = Base64Util.encode(bytes2);

                            List<Map<String, Object>> images = new ArrayList<>();

                            Map<String, Object> map1 = new HashMap<>();
                            map1.put("image", image1);
                            map1.put("image_type", "BASE64");
                            map1.put("face_type", "LIVE");
                            map1.put("quality_control", "LOW");
                            map1.put("liveness_control", "NORMAL");

                            Map<String, Object> map2 = new HashMap<>();
                            map2.put("image", image2);
                            map2.put("image_type", "BASE64");
                            map2.put("face_type", "LIVE");
                            map2.put("quality_control", "LOW");
                            map2.put("liveness_control", "NORMAL");

                            images.add(map1);
                            images.add(map2);

                            String param = GsonUtils.toJson(images);

                            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。

                            String result = HttpUtil.post(url, token, "application/json", param);

                            bundle.putString("result", result);
                            message.what = MSG_FACE_MATCH_SUCCEED;
                            message.setData(bundle);
                            handler.sendMessage(message);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            bundle.putString("error", "人脸对比失败!");
                            message.what = MSG_FACE_MATCH_FAILED;
                            message.setData(bundle);
                            handler.sendMessage(message);
                        }
                    }
                }).start();
            }

            @Override
            public void failed(String info)
            {
                faceMatchListener.failed(info);
            }
        });
    }
}


