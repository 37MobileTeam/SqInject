## SqInject
### 目录
##### 一、功能说明
##### 二、接入说明
##### 三、使用示例
##### 四、工程模块说明
##### 五、版本说明


### 一、功能说明

1、使用BindView将属性和id绑定（绑定采用的是getIdentifier的方式绑定的，非R.id)

2、BindId将id的值注入到属性中（采用的是getIdentifier)

3、SqR类的生成使用gradle插件实现，在gradle列表中，sqinject组的SqRGenerator，该task可生成SqR类

4、其他注解：BindString, BindColor, BindInt，分别对应R.string获取字符串、R.color获取颜色、R.integer获取int值

5、OnClick注解支持事件绑定

```
public class MainActivity extends AppCompatActivity {

    @BindView(SqR.id.hello)
    TextView mHelloView;

    @BindId(value = SqR.layout.activity_main, type = IdType.LAYOUT)
    int layoutId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //绑定除BindView以外的值
        SqInject.bindIds(this);
        setContentView(layoutId);
        //绑定View
        SqInject.bindView(this);
        Toast.makeText(this, mHelloView.getText().toString(), Toast.LENGTH_SHORT).show();
    }

    @OnClick(SqR.id.hello)
    public void click(View v) {
        Toast.makeText(this, mHelloView.getText().toString(), Toast.LENGTH_SHORT).show();
    }
}
```

### 二、接入说明

#### 1、根目录build.gradle配置

```
buildscript {
    repositories {
        google()
        jcenter()
        //配置仓库地址
        maven { url "http://www.azact.com/artifactory/sy_dev_libs/" }
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.4.1'
        //gradle插件
        classpath 'com.37sy.android:sqinject-gradle-plugin:1.0.0'
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        //仓库地址
        maven { url "http://www.azact.com/artifactory/sy_dev_libs/" }
    }
}
```

#### 2、模块build.gradle配置

```
//1、只用gradle插件
apply plugin: 'com.sq.sqinject'
//2、android标签内defaultConfig标签处理
defaultConfig {
	//...
    //增加如下配置,其中com.sq.sqinjectdemo换成对应包名，如果是lib工程，对应成lib工程R文件的包名
    javaCompileOptions {
        annotationProcessorOptions {
            includeCompileClasspath = true
            argument "applicationId", "com.sq.sqinjectdemo"
        }
    }
}
//3、增加依赖
annotationProcessor 'com.37sy.android:sqinject-compile:1.0.0'
implementation 'com.37sy.android:sqinject:1.0.0'
```

### 三、使用示例
（1）绑定View类型对象
```
    @BindView(SqR.id.dialog)
    Button mDialogTest;

    @BindView(SqR.id.fragment)
    Button mFragmentTest;

    @BindId(value = SqR.id.container, type = IdType.ID)
    int containerId;

    //使用示例1：自定义Dialog弹窗显示
    mDialogTest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    processMyDialog();
                }
            });

    //使用示例2：自添加fragment页面
        mFragmentTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processMyFragment();
            }
        });
```

（2）绑定基本类型数据
```
    @BindColor(SqR.color.white)
    int whiteColor;

    @BindString(SqR.string.app_name)
    String mAppName;

    @BindInt(SqR.integer.appId)
    int mAppId;

    //获取基本数据
    LogUtils.d("mAppName: " + mAppName + ", mAppId: " + mAppId + ", whiteColor: " + whiteColor);

```

（3）绑定图片
```
    @BindView(SqR.id.imgIcon)
    ImageView mImageView;

    @BindId(value = SqR.drawable.icon, type = IdType.DRAWABLE)
    int iconId;

    //imageView控件显示
    mImageView.setImageResource(iconId);

```

（4）绑定活动布局
```
    @BindId(value = SqR.layout.activity_test, type = IdType.LAYOUT)
    int layoutId;

    //设定布局资源
    setContentView(layoutId);
```


### 四、工程模块说明
##### 1、annotation module
注解绑定对象的接口，包括color、id、View，int、String，onclick事件等

##### 2、compile module
注解处理器，获取注解信息并解析，包括控件类注解解析和id类注解解析

##### 3、library module
注解绑定的实现，包括绑定view（activity、dialog、fragment等）和绑定控件id

##### 4、sqinject_gradle_plugin module
注解绑定的资源id文件生成，利用gradle脚本在编译过程根据R.java或R.txt的文件内容，构建SqR.java文件。

eg：
(1)如果是application类型，编译资源时根据R.java生成SqR.java

R.java：../SqInject/app/build/generated/not_namespaced_r_class_sources/debug/processDebugResources/r/com/sqinject/demo/R.java

SqR.java：../SqInject/app/build/generated/source/sqr/debug/com/sqinject/demo/SqR.java

(2)如果是library类型，编译资源时根据R.txt生成SqR.java


### 五、版本说明
当前适配的gradle插件版本范围是v2.3.0--v4.1.1，建议使用较新的gradle版本。

已知的兼容性问题有：

（1）v3.2.1 - ，processResourcesProvider方法缺失；

（2）v3.6.0 + ，Task ProcessAndroidResources无法直接获取getSourceOutputDir()

更多参考：Android Gradle 插件版本说明
https://developer.android.com/studio/releases/gradle-plugin?hl=zh-cn

如果您对我们感兴趣，欢迎加群和我们交流

![12711615026029_.pic](./12711615026029_.pic.jpg)

群二维码失效，可以加个人微信，再加进群哈

![12891615258362_.pic](./12891615258362_.pic.jpg)
