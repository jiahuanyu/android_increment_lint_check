package me.jiahuan.plugin.lint;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

/**
 * 通过git 工具判断增量更新的文件，并进行lint检查
 */
public class IncrementLintTask extends DefaultTask {
    @TaskAction
    public void lint() throws IOException {
        new IncrementLintExecution(getProject()).runLint();
    }
}
