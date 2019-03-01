package me.jiahuan.plugin;

import me.jiahuan.plugin.lint.IncrementLintTask;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class PluginEntry implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        System.out.println("内建插件配置启动");
        project.getTasks().create("incrementLint", IncrementLintTask.class);
        copyPreCommit();
        System.out.println("内建插件配置结束");
    }


    private void copyPreCommit() {
        File outputFile = new File(".git/hooks/pre-commit");
        if (outputFile.exists()) {
            return;
        }
        outputFile.getParentFile().mkdirs();
        File inputFile = new File("buildSrc/src/main/resources/pre-commit");
        try {
            Files.copy(inputFile.toPath(), outputFile.toPath());
            outputFile.setExecutable(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
