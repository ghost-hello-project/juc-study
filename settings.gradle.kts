pluginManagement {
    repositories {
        // 插件使用阿里云 maven 源
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        gradlePluginPortal()
    }
}

rootProject.name = "juc-study"

include(":juc-hello")
