plugins {
    `kotlin-dsl`
}

repositories {
    maven {
        setUrl("https://maven.aliyun.com/repository/public/")
    }
    maven {
        setUrl("https://maven.aliyun.com/repository/spring/")
    }
    gradlePluginPortal()
    mavenLocal()
    mavenCentral()
}

gradlePlugin{
    plugins {
        create("my-plugin"){
            id = "my-plugin"
            implementationClass = "com.laolang.gradle.plugin.MyPlugin"
        }
    }
}

