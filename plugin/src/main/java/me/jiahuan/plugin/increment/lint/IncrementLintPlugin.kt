package me.jiahuan.plugin.increment.lint

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin

class IncrementLintPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.tasks.create("incrementLint", IncrementLintTask::class.java) { task ->
            task.group = JavaBasePlugin.VERIFICATION_GROUP
            task.description = " Runs lint on Changed Files. "
            task.outputs.upToDateWhen { false }
        }
    }
}
