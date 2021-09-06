package me.jiahuan.plugin.lint;

import com.android.tools.lint.LintCliClient;
import com.android.tools.lint.LintCliFlags;
import com.android.tools.lint.checks.BuiltinIssueRegistry;
import com.android.tools.lint.client.api.LintClient;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 通过git 工具判断增量更新的文件，并进行lint检查
 */
@SuppressWarnings("UnstableApiUsage")
public class IncrementLintTask extends DefaultTask {
    @TaskAction
    public void lint() {
        LintCliFlags flags = new LintCliFlags();
        LintCliClient client = new LintCliClient(flags, LintClient.CLIENT_GRADLE);
        try {
            client.run(new BuiltinIssueRegistry(), getIncrementFiles());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private List<File> getIncrementFiles() {
        InputStreamReader inputStreamReader = null;
        LineNumberReader lineNumberReader = null;
        List<File> incrementFiles = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", "git diff --cached --name-only --diff-filter=ACM  -- '*.java' '*.kt' '*.xml' | grep -v -E 'buildSrc'"});
            inputStreamReader = new InputStreamReader(process.getInputStream());
            lineNumberReader = new LineNumberReader(inputStreamReader);
            String line;
            while ((line = lineNumberReader.readLine()) != null) {
                System.out.println("控制台输出 = " + line);
                File file = new File(line);
                System.out.println("文件绝对路径 = " + file.getAbsolutePath());
                incrementFiles.add(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (lineNumberReader != null) {
                try {
                    lineNumberReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return incrementFiles;
    }
}
