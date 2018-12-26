package com.example.powersignin;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import cn.bmob.v3.*;
import cn.bmob.v3.datatype.BatchResult;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.*;
import com.example.powersignin.bean.*;
import com.example.powersignin.config.Constant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseActivity extends AppCompatActivity
{
    //Activity请求码
    protected static final int REQUEST_NEW_CLASS = 1001;
    protected static final int REQUEST_JOIN_CLASS = 1002;
    protected static final int REQUEST_TEACHER_CLASS_INFO = 1003;
    protected static final int REQUEST_STUDENT_CLASS_INFO = 1004;
    protected static final int REQUEST_TAKE_PHOTO = 1005;
    protected static final int REQUEST_SIGNIN = 1006;
    protected static final int REQUEST_LOCATION_PERMISSION = 1007;
    protected static final int REQUEST_WRITE_SETTING_PERMISSION = 1008;
    protected static final int REQUEST_SYSTEM_WIFIAP_SETTING = 1009;
    protected static final int REQUEST_START_SIGNIN = 1010;

    //权限标志变量
    protected boolean hasGetLocationPermission;
    protected boolean hasWriteSettingPermission;

    //@RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        ActivityCollector.addActivity(this);

        //初始化 Bmob SDK
        Bmob.initialize(this, Constant.applicationID);

        setContentView();
        initViews();
        initListeners();
        initData();

        //获取权限
        getPermissions();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        ActivityCollector.removeActivity(this);
    }

    //获取权限
    //@RequiresApi(api = Build.VERSION_CODES.M)
    private void getPermissions()
    {
        //获取定位权限 Android8.0及以上
        //没有定位权限
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_PERMISSION);
        }
        //有定位权限
        else
        {
            hasGetLocationPermission = true;
        }

        //获取修改系统设置权限 Android6.0及以上、Android7.1以下
        //else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1)
        //else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            //没有修改系统设置权限
            if (!Settings.System.canWrite(this))
            {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, REQUEST_WRITE_SETTING_PERMISSION);
            }
            else
            {
                hasWriteSettingPermission = true;
            }
        }
        else
        {
            hasWriteSettingPermission = true;
        }*/
    }

    //位置权限授权界面返回
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_LOCATION_PERMISSION:
                //用户同意权限
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    hasGetLocationPermission = true;
                }
                //用户拒绝授权
                else
                {
                    hasGetLocationPermission = false;
                }
                break;
            default:
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        //修改系统设置权限返回
        if (requestCode == REQUEST_WRITE_SETTING_PERMISSION)
        {
            // 判断是否有WRITE_SETTINGS权限
            if (Settings.System.canWrite(this))
            {
                //有权限
                hasWriteSettingPermission = true;
            } else
            {
                //没有权限
                hasWriteSettingPermission = false;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    protected void setToolbarTitle(String title)
    {
        getSupportActionBar().setTitle(title);
    }

    protected void toast(String info)
    {
        Toast.makeText(getApplicationContext(), info, Toast.LENGTH_SHORT).show();
    }

    protected abstract void setContentView();
    protected abstract void initViews();
    protected abstract void initListeners();
    protected abstract void initData();

    //启动Activity
    protected void startSignupActivity()
    {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }

    protected void startLoginActivity()
    {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    protected void startTeacherMainActivity(String teacherUsername, String teacherObjectId, String teacherNickname)
    {
        Intent intent = TeacherMainActivity.newIntent(this, teacherUsername, teacherObjectId, teacherNickname);
        startActivity(intent);
    }

    protected void startTeacherClassInfoActivity(String classroomName, String classroomObjectId)
    {
        Intent intent = TeacherClassInfoActivity.newIntent(this, classroomName, classroomObjectId);
        startActivityForResult(intent, REQUEST_TEACHER_CLASS_INFO);
    }

    protected void startUserInfoActivity(String username, String objectId, String nickname, String identity)
    {
        Intent intent = UserInfoActivity.newIntent(this, username, objectId, nickname, identity);
        startActivity(intent);
    }

    protected void startNewClassActivity(String teacherObjectId)
    {
        Intent intent = NewClassActivity.newIntent(this, teacherObjectId);
        //startActivity(intent);
        startActivityForResult(intent, REQUEST_NEW_CLASS);
    }

    protected void startStudentMainActivity(String studentUsername, String studentObjectId, String studentNickname)
    {
        Intent intent = StudentMainActivity.newIntent(this, studentUsername, studentObjectId, studentNickname);
        startActivity(intent);
    }

    protected void startJoinClassActivity(String studentObjectId)
    {
        Intent intent = JoinClassActivity.newIntent(this, studentObjectId);
        startActivityForResult(intent, REQUEST_JOIN_CLASS);
    }

    protected void startStudentClassInfoActivity(String classroomObjectId, String studentObjectId)
    {
        Intent intent = StudentClassInfoActivity.newIntent(this, classroomObjectId, studentObjectId);
        startActivityForResult(intent, REQUEST_STUDENT_CLASS_INFO);
    }

    protected void startStartSigninActivity(String classroomName, String classroomObjectId, String signinEventObjectId)
    {
        Intent intent = StartSigninActivity.newIntent(this, classroomName, classroomObjectId, signinEventObjectId);
        //startActivity(intent);
        startActivityForResult(intent, REQUEST_START_SIGNIN);
    }

    protected void startCameraActivity(Uri imageUri)
    {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }

    protected void startSigninActivity(String classroomObjectId, String studentObjectId)
    {
        Intent intent = SigninActivity.newIntent(this, classroomObjectId, studentObjectId);
        startActivityForResult(intent, REQUEST_SIGNIN);
    }

    protected void startSigninHistoryActivity(String classroomObjectId)
    {
        Intent intent = SigninHistoryActivity.newIntent(this, classroomObjectId);
        startActivity(intent);
    }

    protected void startSigninEventDetailActivity(String signinEventObjectId)
    {
        Intent intent = SigninEventDetailActivity.newIntent(this, signinEventObjectId);
        startActivity(intent);
    }

    //打开系统GPS设置页面
    protected void startGPSSettingActivity()
    {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    //打开系统流量设置页面
    protected void startNetWorkSettingActivity()
    {
        Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
        startActivity(intent);
    }

    //打开系统热点设置页面
    protected void startWifiApSettingActivity()
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        ComponentName com = new ComponentName("com.android.settings", "com.android.settings.TetherSettings");
        intent.setComponent(com);
        startActivityForResult(intent, REQUEST_SYSTEM_WIFIAP_SETTING);
    }

    //后端数据操作

    protected interface SignupListener
    {
        void succeed(User user);
        void failed(String info);
    }

    protected interface LogoutListener
    {
        void succeed();
        void failed();
    }

    protected interface LoginListener
    {
        void succeed(User user);
        void failed(BmobException e);
    }

    protected interface SaveClassroomListener
    {
        void succeed(String classroomObjectId);
        void failed(String info);
    }

    protected interface JoinClassroomListener
    {
        void succeed();
        void failed(String info);
    }

    protected interface QuitClassroomListener
    {
        void succeed();
        void failed(String info);
    }

    protected interface UploadFileDoneListener
    {
        void done(BmobFile bmobFile, BmobException e);
    }

    protected interface SaveSigninEventListener
    {
        void succeed(String signinEventObjectId);
        void failed(String info);
    }

    protected interface StudentSigninListener
    {
        void succeed();
        void failed(String info);
    }

    protected interface FindAbsentStudentListener
    {
        void succeed(List<Student> students);
        void failed(String info);
    }

    protected interface DeleteClassroomListener
    {
        void succeed();
        void failed(String info);
    }

    //用户注册
    protected void signup(String username, String password, final String nickname, final String type, File faceImage, final SignupListener signupListener)
    {
        final User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setType(type);
        user.setNickname(nickname);

        //上传面部图片
        uploadFile(faceImage, new UploadFileDoneListener()
        {
            @Override
            public void done(final BmobFile bmobFile, BmobException e)
            {
                //上传成功
                if (e == null)
                {
                    user.setFaceImageUrl(bmobFile.getUrl());
                    user.signUp(new SaveListener<User>()
                    {
                        @Override
                        public void done(final User user, BmobException e)
                        {
                            if (e == null)
                            {
                                //教师类型用户
                                if (type.equals("teacher"))
                                {
                                    saveTeacher(user.getObjectId(), nickname, bmobFile.getUrl(), new SaveListener<String>()
                                    {
                                        @Override
                                        public void done(String s, BmobException e)
                                        {
                                            if (e == null)
                                            {
                                                signupListener.succeed(user);
                                            }
                                            else
                                            {
                                                signupListener.failed("cannot save as teacher");
                                            }
                                        }
                                    });
                                }
                                //学生类型用户
                                else
                                {
                                    saveStudent(user.getObjectId(), nickname, bmobFile.getUrl(), new SaveListener<String>()
                                    {
                                        @Override
                                        public void done(String s, BmobException e)
                                        {
                                            if (e == null)
                                            {
                                                signupListener.succeed(user);
                                            }
                                            else
                                            {
                                                signupListener.failed("cannot save as student");
                                            }
                                        }
                                    });
                                }
                            }
                            else
                            {
                                signupListener.failed(e.getMessage());
                            }
                        }
                    });
                }
                //上传失败
                else
                {
                    signupListener.failed("face image upload failed");
                }
            }
        });
    }

    //用户登陆
    protected void login(String username, String password, SaveListener<User> saveListener)
    {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.login(saveListener);
    }

    //判断当前是否有用户登陆
    protected boolean isLogin()
    {
        return BmobUser.isLogin();
    }

    //注销
    protected void logout()
    {
        BmobUser.logOut();
    }

    //保存学生
    protected void saveStudent(String userObjectId, String nickname, String faceImageUrl, SaveListener<String> saveListener)
    {
        Student student = new Student();
        User user = new User();
        user.setObjectId(userObjectId);
        student.setUser(user);
        student.setNickname(nickname);
        student.setFaceImageUrl(faceImageUrl);
        student.save(saveListener);
    }

    //保存教师
    protected void saveTeacher(String userObjectId, String nickname, String faceImageUrl, SaveListener<String> saveListener)
    {
        Teacher teacher = new Teacher();
        User user = new User();
        user.setObjectId(userObjectId);
        teacher.setUser(user);
        teacher.setNickname(nickname);
        teacher.setFaceImageUrl(faceImageUrl);
        teacher.save(saveListener);
    }

    //保存班级
    protected void saveClassroom(String description, String teacherObjectId, final SaveClassroomListener saveClassroomListener)
    {
        Classroom classroom = new Classroom();
        final Teacher teacher = new Teacher();
        teacher.setObjectId(teacherObjectId);
        classroom.setDescription(description);
        classroom.setTeacher(teacher);
        classroom.setSignin(false);
        //classroom.save(saveListener);
        classroom.save(new SaveListener<String>()
        {
            @Override
            public void done(final String s, BmobException e)
            {
                //班级保存成功
                if (e == null)
                {
                    BmobRelation relation = new BmobRelation();
                    Classroom c = new Classroom();
                    c.setObjectId(s);
                    relation.add(c);
                    teacher.setClassrooms(relation);
                    teacher.update(new UpdateListener()
                    {
                        @Override
                        public void done(BmobException e)
                        {
                            //教师更新成功
                            if (e == null)
                            {
                                saveClassroomListener.succeed(s);
                            }
                            //教师更新失败
                            else
                            {
                                saveClassroomListener.failed("teacher update failed");
                            }
                        }
                    });
                }
                //班级保存失败
                else
                {
                    saveClassroomListener.failed("classroom save failed");
                }
            }
        });
    }

    //学生加入班级
    protected void joinClassroom(final String studentObjectId, String classroomObjectId, final JoinClassroomListener joinClassroomListener)
    {
        BmobQuery<Classroom> query = new BmobQuery<>();
        query.addWhereEqualTo("objectId", classroomObjectId);
        query.findObjects(new FindListener<Classroom>()
        {
            @Override
            public void done(final List<Classroom> list, BmobException e)
            {
                //找到对应班级
                if (e == null)
                {
                    final Student student = new Student();
                    student.setObjectId(studentObjectId);
                    BmobRelation r1 = new BmobRelation();
                    r1.add(list.get(0));
                    student.setClassrooms(r1);

                    //更新学生数据
                    student.update(new UpdateListener()
                    {
                        @Override
                        public void done(BmobException e)
                        {
                            //学生数据更新成功
                            if (e == null)
                            {
                                //更新班级数据
                                BmobRelation r2 = new BmobRelation();
                                r2.add(student);
                                list.get(0).setStudents(r2);
                                list.get(0).update(new UpdateListener()
                                {
                                    @Override
                                    public void done(BmobException e)
                                    {
                                        //班级数据更新成功
                                        if (e == null)
                                        {
                                            joinClassroomListener.succeed();
                                        }
                                        //班级数据更新失败
                                        else
                                        {
                                            joinClassroomListener.failed("classroom data update failed: " + e.getMessage());
                                        }
                                    }
                                });
                            }
                            //学生数据更新失败
                            else
                            {
                                joinClassroomListener.failed("student data update failed: " + e.getMessage());
                            }
                        }
                    });
                }
                //没找到对应班级
                else
                {
                    joinClassroomListener.failed("class is not exist");
                }
            }
        });
    }

    //根据用户id查找教师
    protected void findTeacherByUserObjectId(String userObjectId, FindListener<Teacher> findListener)
    {
        BmobQuery<Teacher> query = new BmobQuery<>();
        User user = new User();
        user.setObjectId(userObjectId);
        query.addWhereEqualTo("user", user);
        query.findObjects(findListener);
    }

    //查找教师管理的所有班级
    protected void findTeacherClassrooms(String teacherObjectId, FindListener<Classroom> findListener)
    {
        BmobQuery<Classroom> query = new BmobQuery<>();
        Teacher teacher = new Teacher();
        teacher.setObjectId(teacherObjectId);
        query.order("-createdAt");
        query.addWhereRelatedTo("classrooms", new BmobPointer(teacher));
        query.findObjects(findListener);
    }

    //查找学生
    protected void findStudentByUserObjectId(String userObjectId, FindListener<Student> findListener)
    {
        BmobQuery<Student> query = new BmobQuery<>();
        User user = new User();
        user.setObjectId(userObjectId);
        query.addWhereEqualTo("user", user);
        query.findObjects(findListener);
    }

    //查找学生加入的所有班级
    protected void findStudentClassrooms(String studentObjectId, FindListener<Classroom> findListener)
    {
        BmobQuery<Classroom> query = new BmobQuery<>();
        Student student = new Student();
        student.setObjectId(studentObjectId);
        query.order("-updatedAt");
        query.addWhereRelatedTo("classrooms", new BmobPointer(student));
        query.findObjects(findListener);
    }

    //根据班级id查找班级
    protected void findClassroom(String classroomObjectId, QueryListener<Classroom> queryListener)
    {
        BmobQuery<Classroom> query = new BmobQuery<>();
        query.getObject(classroomObjectId, queryListener);
    }

    //根据用户id查找用户
    protected void findUser(String userObjectId, QueryListener<User> queryListener)
    {
        BmobQuery<User> query = new BmobQuery<>();
        query.getObject(userObjectId, queryListener);
    }

    //根据教师id查找教师
    protected void findTeacherByTeacherObjectId(String teacherObjectId, QueryListener<Teacher> queryListener)
    {
        BmobQuery<Teacher> query = new BmobQuery<>();
        query.getObject(teacherObjectId, queryListener);
    }

    //根据学生id查找学生
    protected void findStudentByStudentObjectId(String studentObjectId, QueryListener<Student> queryListener)
    {
        BmobQuery<Student> query = new BmobQuery<>();
        query.getObject(studentObjectId, queryListener);
    }

    //教师删除班级
    /*protected void deleteClassroom(String classroomObjectId, UpdateListener updateListener)
    {
        //查找该班级的所有签到事件
        findClassroomSigninEvents(classroomObjectId, new FindListener<SigninEvent>()
        {
            @Override
            public void done(List<SigninEvent> list, BmobException e)
            {
                if (e == null)
                {
                    //批量删除签到事件
                    List<BmobObject> objects = new ArrayList<>();
                    for (SigninEvent event : list)
                    {
                        objects.add(event);
                    }
                    new BmobBatch().deleteBatch(objects).doBatch(new QueryListListener<BatchResult>()
                    {
                        @Override
                        public void done(List<BatchResult> list, BmobException e)
                        {
                            if (e == null)
                            {

                            }
                            else
                            {

                            }
                        }
                    });
                }
                else
                {

                }
            }
        });

        Classroom classroom = new Classroom();
        classroom.setObjectId(classroomObjectId);
        classroom.delete(updateListener);
    }*/

    protected void deleteClassroom(final String classroomObjectId, final DeleteClassroomListener deleteClassroomListener)
    {
        //查找该班级的所有签到事件
        findClassroomSigninEvents(classroomObjectId, new FindListener<SigninEvent>()
        {
            @Override
            public void done(List<SigninEvent> list, BmobException e)
            {
                if (e == null)
                {
                    //批量删除签到事件
                    List<BmobObject> objects = new ArrayList<>();
                    for (SigninEvent event : list)
                    {
                        objects.add(event);
                    }
                    new BmobBatch().deleteBatch(objects).doBatch(new QueryListListener<BatchResult>()
                    {
                        @Override
                        public void done(List<BatchResult> list, BmobException e)
                        {
                            if (e == null)
                            {
                                //删除班级
                                Classroom classroom = new Classroom();
                                classroom.setObjectId(classroomObjectId);
                                classroom.delete(new UpdateListener()
                                {
                                    @Override
                                    public void done(BmobException e)
                                    {
                                        if (e == null)
                                        {
                                            deleteClassroomListener.succeed();
                                        }
                                        else
                                        {
                                            deleteClassroomListener.failed("delete classroom failed: " + e.getMessage());
                                        }
                                    }
                                });
                            }
                            else
                            {
                                deleteClassroomListener.failed("delete signin events failed: " + e.getMessage());
                            }
                        }
                    });
                }
                else
                {
                    deleteClassroomListener.failed("find signin events failed: " + e.getMessage());
                }
            }
        });
    }

    //学生退出班级
    protected void quitClassroom(final String studentObjectId, final String classroomObjectId, final QuitClassroomListener quitClassroomListener)
    {
        //查找学生
        findStudentByStudentObjectId(studentObjectId, new QueryListener<Student>()
        {
            @Override
            public void done(Student student, BmobException e)
            {
                //学生查找成功
                if (e == null)
                {
                    //将该学生从该班级的学生中删除
                    BmobRelation relation = new BmobRelation();
                    relation.remove(student);
                    //更新班级信息
                    Classroom classroom = new Classroom();
                    classroom.setObjectId(classroomObjectId);
                    classroom.setStudents(relation);
                    classroom.update(new UpdateListener()
                    {
                        @Override
                        public void done(BmobException e)
                        {
                            //班级更新成功
                            if (e == null)
                            {
                                //查找班级
                                findClassroom(classroomObjectId, new QueryListener<Classroom>()
                                {
                                    @Override
                                    public void done(Classroom classroom, BmobException e)
                                    {
                                        //班级查找成功
                                        if (e == null)
                                        {
                                            //将该班级从学生加入的班级中删除
                                            BmobRelation relation = new BmobRelation();
                                            relation.remove(classroom);
                                            //更新学生信息
                                            Student student1 = new Student();
                                            student1.setObjectId(studentObjectId);
                                            student1.setClassrooms(relation);
                                            student1.update(new UpdateListener()
                                            {
                                                @Override
                                                public void done(BmobException e)
                                                {
                                                    //学生更新成功
                                                    if (e == null)
                                                    {
                                                        quitClassroomListener.succeed();
                                                    }
                                                    //学生更新失败
                                                    else
                                                    {
                                                        quitClassroomListener.failed("student update failed: " + e.getMessage());
                                                    }
                                                }
                                            });
                                        }
                                        //班级查找失败
                                        else
                                        {
                                            quitClassroomListener.failed("classroom find failed: " + e.getMessage());
                                        }
                                    }
                                });
                            }
                            //班级更新失败
                            else
                            {
                                quitClassroomListener.failed("classroom update failed: " + e.getMessage());
                            }
                        }
                    });
                }
                //学生查找失败
                else
                {
                    quitClassroomListener.failed("student find failed: " + e.getMessage());
                }
            }
        });
    }

    //查找某班级的所有学生
    protected void findClassroomStudents(String classroomObjectId, FindListener<Student> findListener)
    {
        BmobQuery<Student> query = new BmobQuery<>();
        Classroom classroom = new Classroom();
        classroom.setObjectId(classroomObjectId);
        query.order("-updatedAt");
        query.addWhereRelatedTo("students", new BmobPointer(classroom));
        query.findObjects(findListener);
    }

    //上传文件
    protected void uploadFile(File file, final UploadFileDoneListener uploadFileDoneListener)
    {
        final BmobFile bmobFile = new BmobFile(file);
        bmobFile.uploadblock(new UploadFileListener()
        {
            @Override
            public void done(BmobException e)
            {
                uploadFileDoneListener.done(bmobFile, e);
            }
        });
    }

    //下载文件
    protected void downloadFile(File file, String url, DownloadFileListener downloadFileListener)
    {
        BmobFile bmobFile = new BmobFile();
        bmobFile.setUrl(url);
        bmobFile.download(file, downloadFileListener);
    }

    //保存签到事件
    protected void saveSigninEvent(final String classroomObjectId, String bssid, final SaveSigninEventListener saveSigninEventListener)
    {
        final Classroom classroom = new Classroom();
        classroom.setObjectId(classroomObjectId);
        SigninEvent signinEvent = new SigninEvent();
        signinEvent.setClassroom(classroom);
        signinEvent.setBssid(bssid);
        //保存签到事件
        signinEvent.save(new SaveListener<String>()
        {
            @Override
            public void done(final String s, BmobException e)
            {
                if (e == null)
                {
                    //更新班级信息
                    SigninEvent t = new SigninEvent();
                    t.setObjectId(s);
                    BmobRelation relation = new BmobRelation();
                    relation.add(t);
                    classroom.setSigninEvents(relation);
                    classroom.update(new UpdateListener()
                    {
                        @Override
                        public void done(BmobException e)
                        {
                            if (e == null)
                            {
                                saveSigninEventListener.succeed(s);
                            }
                            else
                            {
                                saveSigninEventListener.failed(e.getMessage());
                            }
                        }
                    });
                }
                else
                {
                    saveSigninEventListener.failed(e.getMessage());
                }
            }
        });
    }

    //查找签到事件
    protected void findSigninEvent(String signinEventObjectId, QueryListener<SigninEvent> queryListener)
    {
        BmobQuery<SigninEvent> query = new BmobQuery<>();
        query.getObject(signinEventObjectId, queryListener);
    }

    //设置班级状态为正在签到
    protected void setClassroomSignin(String classroomObjectId, String signinEventObjectId, UpdateListener updateListener)
    {
        Classroom classroom = new Classroom();
        classroom.setObjectId(classroomObjectId);
        classroom.setSignin(true);
        classroom.setCurrentSigninEvent(signinEventObjectId);
        classroom.update(updateListener);
    }

    //设置班级状态为不在签到
    protected void setClassroomFinishSignin(String classroomObjectId, UpdateListener updateListener)
    {
        Classroom classroom = new Classroom();
        classroom.setObjectId(classroomObjectId);
        classroom.setSignin(false);
        classroom.update(updateListener);
    }

    //学生签到
    protected void studentSignin(final String studentObjectId, String classroomObjectId, final StudentSigninListener studentSigninListener)
    {
        //查找班级
        BmobQuery<Classroom> queryClassroom = new BmobQuery<>();
        queryClassroom.getObject(classroomObjectId, new QueryListener<Classroom>()
        {
            @Override
            public void done(Classroom classroom, BmobException e)
            {
                if (e == null)
                {
                    String signinEventObjectId = classroom.getCurrentSigninEvent();
                    //查找签到事件
                    BmobQuery<SigninEvent> querySigninEvent = new BmobQuery<>();
                    querySigninEvent.getObject(signinEventObjectId, new QueryListener<SigninEvent>()
                    {
                        @Override
                        public void done(SigninEvent signinEvent, BmobException e)
                        {
                            if (e == null)
                            {
                                //将学生添加到已签到学生中
                                Student student = new Student();
                                student.setObjectId(studentObjectId);
                                BmobRelation relation = new BmobRelation();
                                relation.add(student);
                                signinEvent.setSignedStudents(relation);
                                //更新签到事件
                                signinEvent.update(new UpdateListener()
                                {
                                    @Override
                                    public void done(BmobException e)
                                    {
                                        if (e == null)
                                        {
                                            studentSigninListener.succeed();
                                        }
                                        else
                                        {
                                            studentSigninListener.failed("签到事件更新失败: " + e.getMessage());
                                        }
                                    }
                                });
                            }
                            else
                            {
                                studentSigninListener.failed("签到事件查找失败: " + e.getMessage());
                            }
                        }
                    });
                }
                else
                {
                    studentSigninListener.failed("班级查找失败: " + e.getMessage());
                }
            }
        });
    }

    //查找已签到学生
    protected void findSigninedStudents(String signinEventObjectId, FindListener<Student> findListener)
    {
        BmobQuery<Student> query = new BmobQuery<>();
        SigninEvent signinEvent = new SigninEvent();
        signinEvent.setObjectId(signinEventObjectId);
        query.order("-updatedAt");
        query.addWhereRelatedTo("signedStudents", new BmobPointer(signinEvent));
        query.findObjects(findListener);
    }

    //查找某班级的所有签到记录
    protected void findClassroomSigninEvents(String classroomObjectId, FindListener<SigninEvent> findListener)
    {
        BmobQuery<SigninEvent> query = new BmobQuery<>();
        Classroom classroom = new Classroom();
        classroom.setObjectId(classroomObjectId);
        query.order("-createdAt");
        query.addWhereRelatedTo("signinEvents", new BmobPointer(classroom));
        query.findObjects(findListener);
    }

    //查找缺席的学生
    protected void findAbsentStudents(final String signEventObjectId, final FindAbsentStudentListener findAbsentStudentListener)
    {
        //查找签到事件
        findSigninEvent(signEventObjectId, new QueryListener<SigninEvent>()
        {
            @Override
            public void done(SigninEvent signinEvent, BmobException e)
            {
                if (e == null)
                {
                    //查找签到事件所属的班级的所有学生
                    findClassroomStudents(signinEvent.getClassroom().getObjectId(), new FindListener<Student>()
                    {
                        @Override
                        public void done(final List<Student> allStudents, BmobException e)
                        {
                            if (e == null)
                            {
                                //查找已签到的学生
                                findSigninedStudents(signEventObjectId, new FindListener<Student>()
                                {
                                    @Override
                                    public void done(List<Student> signinedStudents, BmobException e)
                                    {
                                        if (e == null)
                                        {
                                            for (Student student : signinedStudents)
                                            {
                                                for (int i = 0; i < allStudents.size(); ++i)
                                                {
                                                    if (allStudents.get(i).getObjectId().equals(student.getObjectId()))
                                                    {
                                                        allStudents.remove(i);
                                                    }
                                                }
                                            }

                                            findAbsentStudentListener.succeed(allStudents);
                                        }
                                        else
                                        {
                                            findAbsentStudentListener.failed("find signined students failed: " + e.getMessage());
                                        }
                                    }
                                });
                            }
                            else
                            {
                                findAbsentStudentListener.failed("find classroom students failed: " + e.getMessage());
                            }
                        }
                    });
                }
                else
                {
                    findAbsentStudentListener.failed("find signin event failed: " + e.getMessage());
                }
            }
        });
    }
}
