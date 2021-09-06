package me.jiahuan.plugin;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;

import me.jiahuan.plugin.lint.IncrementLintTask;

public class PluginEntry implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getTasks().create("incrementLint", IncrementLintTask.class, task -> {
            task.setGroup(JavaBasePlugin.VERIFICATION_GROUP);
            task.setDescription("Run Lint on Changed Files.");
        });
    }
}
