# XAutoRegister

### 功能介绍：
通过注解自动注入类到集合

#### 使用方式：
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



