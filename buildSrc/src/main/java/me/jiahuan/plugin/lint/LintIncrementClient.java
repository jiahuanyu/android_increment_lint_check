package me.jiahuan.plugin.lint;

import com.android.annotations.NonNull;
import com.android.tools.lint.LintCliClient;
import com.android.tools.lint.LintCliFlags;
import com.android.tools.lint.Warning;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class LintIncrementClient extends LintCliClient {
    
    public LintIncrementClient(@NonNull LintCliFlags flags, @NonNull String clientName) {
        super(flags, clientName);
    }

    public List<Warning> getWarnings() {
        return warnings;
    }
}
