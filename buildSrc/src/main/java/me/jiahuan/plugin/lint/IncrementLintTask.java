package me.jiahuan.plugin.lint;

import com.android.tools.lint.LintCliFlags;
import com.android.tools.lint.Reporter;
import com.android.tools.lint.checks.BuiltinIssueRegistry;
import com.android.tools.lint.client.api.LintClient;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * 通过git 工具判断增量更新的文件，并进行lint检查
 */
@SuppressWarnings("UnstableApiUsage")
public class IncrementLintTask extends DefaultTask {
    @TaskAction
    public void lint() throws IOException {
        LintCliFlags flags = new LintCliFlags();
        LintIncrementClient client = new LintIncrementClient(flags, LintClient.CLIENT_GRADLE);
        flags.getReporters().add(
                Reporter.createTextReporter(
                        client,
                        flags,
                        null,
                        new PrintWriter(System.out, true),
                        true
                )
        );

        int exitCode = client.run(new BuiltinIssueRegistry(), getIncrementFiles());
        if (exitCode == LintCliFlags.ERRNO_CREATED_BASELINE) {
            throw new GradleException("Aborting build since new baseline file was created");
        }

        if (exitCode == LintCliFlags.ERRNO_APPLIED_SUGGESTIONS) {
            throw new GradleException(
                    "Aborting build since sources were modified to apply quickfixes after compilation");
        }
        if(!client.getWarnings().isEmpty()) {
            throw new GradleException("See Above Warnings");
        }
    }

    /**
     * 通过 git 命令，获取修改的文件
     */
    private List<File> getIncrementFiles() {
        return new ArrayList<>();
    }
}
