package me.jiahuan.plugin.lint;

import com.android.builder.model.Variant;
import com.android.sdklib.BuildToolInfo;
import com.android.tools.lint.LintCliFlags;
import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.client.api.LintRequest;
import com.android.tools.lint.gradle.LintGradleClient;
import com.android.tools.lint.gradle.api.VariantInputs;

import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 增量LintGradleClient
 */
public class IncrementLintGradleClient extends LintGradleClient {
    public IncrementLintGradleClient(String version, IssueRegistry registry, LintCliFlags flags, Project gradleProject, File sdkHome, Variant variant, VariantInputs variantInputs, BuildToolInfo buildToolInfo, boolean isAndroid, String baselineVariantName) {
        super(version, registry, flags, gradleProject, sdkHome, variant, variantInputs, buildToolInfo, isAndroid, baselineVariantName);
    }

    @Override
    protected LintRequest createLintRequest(List<File> files) {
        LintRequest lintRequest = super.createLintRequest(files);
        for (com.android.tools.lint.detector.api.Project project : lintRequest.getProjects()) {
            addFiles(project);
        }
        return lintRequest;
    }


    private void addFiles(com.android.tools.lint.detector.api.Project project) {
        List<File> incrementFiles = getIncrementFiles();
        if (incrementFiles != null) {
            for (File file : incrementFiles) {
                System.out.println("添加文件" + file.getAbsolutePath() + "到lint检查");
                project.addFile(file);
            }
        }
    }

    private List<File> getIncrementFiles() {
        InputStreamReader inputStreamReader = null;
        LineNumberReader lineNumberReader = null;
        try {
            List<File> goalFiles = new ArrayList();
            Process process = Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", "git diff --cached --name-only --diff-filter=ACM  -- '*.java' '*.kt' '*.xml' | grep -v -E 'buildSrc'"});
            inputStreamReader = new InputStreamReader(process.getInputStream());
            lineNumberReader = new LineNumberReader(inputStreamReader);
            String line;
            while ((line = lineNumberReader.readLine()) != null) {     //按行打印输出内容
                System.out.println("控制台输出 = " + line);
                File file = new File(line);
                System.out.println("文件绝对路径 = " + file.getAbsolutePath());
                goalFiles.add(file);
            }
            return goalFiles;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStreamReader == null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (lineNumberReader == null) {
                try {
                    lineNumberReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    private void recursiveTraversal(List<File> fileList, File file) {
        if (!file.isDirectory()) {
            fileList.add(file);
        } else {
            File[] innerFiles = file.listFiles();
            if (innerFiles != null && innerFiles.length > 0) {
                for (File innerFile : innerFiles) {
                    recursiveTraversal(fileList, innerFile);
                }
            }
        }
    }
}
