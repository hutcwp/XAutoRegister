# XAutoRegister

### 功能介绍：
通过注解自动注入类到集合，场景：将不同类型的策略类注册到Manager类中.

省去手动注册的代码。

#### 限制：
目前只支持单个moudle中使用，多moudle项目暂不支持

#### 接入方式：
1. 在project下的build.gradle加入：
```
allprojects {
    repositories {
        google()
        jcenter()
        maven {url 'https://dl.bintray.com/jfrogcwp/maven'}  //这一行代码
    }
}
```

2. 然后在moudle的build.gradle加入下面的依赖：
### Jcenter地址：
```
    compile 'hutcwp:xregister-api:0.0.1'
    annotationProcessor  'hutcwp:xregister-compiler:0.0.1'
```

### 使用用法

1. 创建Manager类(类名随意)继承BaseManager
```
import me.hutcwp.api.BaseManager;

public class Manager extends BaseManager {
}
```

2. 创建要被注入到Manager中的数据基类InjectData，用@AutoRegister注解,targetClass为要被注入到的Manager类
```
import me.hutcwp.api.AutoRegister;
import me.hutcwp.api.IRegister;

@AutoRegister(targetClass = Manager.class)
public abstract class InjectData implements IRegister {
    public abstract String name();
}

//具体实现类A
public class InjectDataSubA extends InjectData {
    @Override
    public String name() {
        return "InjectDataSubA";
    }
}

//具体实现类B
public class InjectDataSubB extends InjectData {
    @Override
    public String name() {
        return "InjectDataSubB";
    }
}
```

3. 业务调用
```

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Manager manager = new Manager();
        StringBuilder sb = new StringBuilder();
        sb.append("当前size = ").append(manager.getData().size());
        for (int i = 0; i < manager.getData().size(); i++) {
            sb.append("item").append(i).append(" is ").append(manager.getData().get(i));
        }
        Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();
    }
}


```

4. 效果

正确的话，打印log:"当前size = 2item0 is me.hutcwp.test.InjectDataSubA@cec17f8item1 is me.hutcwp.test.InjectDataSubB@1a3f4d1",且弹出toast
