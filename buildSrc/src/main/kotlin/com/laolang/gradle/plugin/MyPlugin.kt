package com.laolang.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 自定义插件
 */
class MyPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.tasks.register("jxHello"){
            doLast{
                println("jx plugin do last")
            }
        }
    }
}