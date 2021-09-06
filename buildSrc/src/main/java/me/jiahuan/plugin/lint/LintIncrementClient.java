package me.jiahuan.plugin.lint;

import com.android.annotations.NonNull;
import com.android.tools.lint.LintCliClient;
import com.android.tools.lint.LintCliFlags;
import com.android.tools.lint.Warning;
import com.android.tools.lint.client.api.IssueRegistry;

import org.gradle.api.GradleException;

import java.io.File;
import java.io.IOException;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class LintIncrementClient extends LintCliClient {
    
    public LintIncrementClient(@NonNull LintCliFlags flags, @NonNull String clientName) {
        super(flags, clientName);
    }

    public List<Warning> runLint(@NonNull IssueRegistry registry, @NonNull List<File> files) throws IOException {
        int exitCode = super.run(registry, files);
        if (exitCode == LintCliFlags.ERRNO_CREATED_BASELINE) {
            throw new GradleException("Aborting build since new baseline file was created");
        }

        if (exitCode == LintCliFlags.ERRNO_APPLIED_SUGGESTIONS) {
            throw new GradleException(
                    "Aborting build since sources were modified to apply quickfixes after compilation");
        }
        return warnings;
    }
}
