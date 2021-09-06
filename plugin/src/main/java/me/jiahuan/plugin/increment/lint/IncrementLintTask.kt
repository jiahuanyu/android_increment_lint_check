package me.jiahuan.plugin.increment.lint

import com.android.tools.lint.LintCliClient
import com.android.tools.lint.LintCliFlags
import com.android.tools.lint.Reporter
import com.android.tools.lint.checks.BuiltinIssueRegistry
import com.android.tools.lint.client.api.LintClient
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.PrintWriter

@Suppress("UnstableApiUsage")
open class IncrementLintTask : DefaultTask() {

    @TaskAction
    fun lint() {
        runLint()
    }

    private fun runLint() {
        val registry = BuiltinIssueRegistry()
        val flags = LintCliFlags()
        val client = LintCliClient(flags = flags, clientName = LintClient.CLIENT_GRADLE)
        flags.reporters.add(
            Reporter.createTextReporter(
                client, flags, null, PrintWriter(System.out, true),
                close = false
            )
        )
        client.run(registry, emptyList())
    }
}
