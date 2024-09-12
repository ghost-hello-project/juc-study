import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.laolang.gradle.JxBuildConfig
import com.laolang.gradle.Version
import com.laolang.gradle.mavenAlibaba
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

/**
 * 使用插件之前需要先声明
 */
plugins {
    application
    id("maven-publish")
    id("com.dorongold.task-tree") version "3.0.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    kotlin("jvm") version "1.9.0"
    id("idea")
}

idea{
    module{
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}


/**
 * 所有工程的配置, 包含根项目
 */
allprojects {
    repositories {
        mavenAlibaba()
        mavenLocal()
        mavenCentral()
    }
}

/**
 * 所有子工程的配置, 不包含根项目
 */
subprojects {
    // 不处理主工程, 主工程单独写构建文件
    apply(plugin = "application")
    apply(plugin = "maven-publish")
    apply(plugin = "com.dorongold.task-tree")
    apply(plugin = "com.github.johnrengelman.shadow")

    group = "com.laolang.jx"
    version = "0.1"

    application {
        mainClass.set(projectMainClass(project))
    }

    dependencies {
        compileOnly(rootProject.libs.lombok)
        annotationProcessor(rootProject.libs.lombok)
        implementation(rootProject.libs.hutool.all)
        implementation(rootProject.libs.vavr)
        implementation(rootProject.libs.bundles.mapstruct)
        implementation(rootProject.libs.guava)
        implementation(rootProject.libs.commons.lang3)

        implementation(rootProject.libs.bundles.logback)

        testImplementation(rootProject.libs.testng)
        testCompileOnly(rootProject.libs.lombok)
        testAnnotationProcessor(rootProject.libs.lombok)
    }


    // 打包时生成 source.jar 和 javadoc.jar
    configure<JavaPluginExtension> {
        withSourcesJar()
        withJavadocJar()
    }

    /**
     * java 编译配置
     */
    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        sourceCompatibility = Version.sourceCompatibility.toString()
        targetCompatibility = Version.targetCompatibility.toString()
    }

    /**
     * javadoc
     */
    tasks.withType<Javadoc> {
        options {
            encoding = Charsets.UTF_8.name()
            charset(Charsets.UTF_8.name())
        }
        // 忽略 javadoc 报错
        isFailOnError = false
    }

    /**
     * 打包可执行 jar
     */
    tasks.named<ShadowJar>("shadowJar") {
        archiveBaseName.set(project.name)
        archiveVersion.set(project.version.toString())
        archiveFileName.set(project.name + ".jar")

        destinationDirectory.set(layout.buildDirectory.dir("shaded"))
    }

    tasks.named<Test>("test") {
//            useTestNG {
//                suites("testng.xml")
//            }
        useTestNG()
        // 输出详细日志
        testLogging {
            // 记录日志的事件类型
            events("FAILED", "PASSED", "SKIPPED", "STANDARD_ERROR", "STANDARD_OUT", "STARTED")
            // 记录测试异常的格式
            // FULL: 完整显示异常
            // SHORT: 异常的简短显示
            exceptionFormat = TestExceptionFormat.FULL
            // 是否记录标准输出和标准错误的输出
            showStandardStreams = true
        }
    }

    /**
     * 发布到本地
     */
    publishing {
        repositories {
            mavenLocal()
        }
        publications {
            create<MavenPublication>("maven") {
                groupId = project.group.toString()
                artifactId = project.name
                version = project.version.toString()

                from(components["java"])
            }
        }
    }
}

/**
 * 获取子项目启动类
 *
 * 规则:
 * 启动类所在包固定为 com.laolang.jx
 * 然后拼接上项目名的大驼峰形式
 */
fun projectMainClass(project: Project): String {
    return JxBuildConfig.basePackageName + "." + projectNameToUpperCamelCase(project)
}

/**
 * 将子项目名转为大驼峰命名
 */
fun projectNameToUpperCamelCase(project: Project): String {
    val arr = project.name.split("-")
    val parts = arr.map { part -> part.toLowerCase().capitalize() }
    return parts.joinToString("")
}

/**
 * 根工程不需要打包
 */
tasks.withType<ShadowJar> {
    enabled = false
}

