package me.jiahuan.plugin.lint;

import com.android.tools.lint.LintCliFlags;
import com.android.tools.lint.Reporter;
import com.android.tools.lint.Warning;
import com.android.tools.lint.checks.BuiltinIssueRegistry;
import com.android.tools.lint.client.api.LintClient;

import org.gradle.api.GradleException;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ALL")
public class IncrementLintExecution {

    private final Project mProject;

    public IncrementLintExecution(Project project) {
        this.mProject = project;
    }

    public void runLint() throws IOException {
        LintCliFlags flags = new LintCliFlags();
        LintIncrementClient client = new LintIncrementClient(flags, LintClient.CLIENT_GRADLE);

        // 添加控制台的报告输出
        flags.getReporters().add(
                Reporter.createTextReporter(
                        client,
                        flags,
                        null,
                        new PrintWriter(System.out, true),
                        true
                )
        );

        // 是否是致命错误，包括 warnings
        flags.setFatalOnly(false);

        List<Warning> warnings = client.runLint(new BuiltinIssueRegistry(), getIncrementFiles());

        if (!warnings.isEmpty()) {
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
