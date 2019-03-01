package me.jiahuan.plugin.lint;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.AppPlugin;
import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.LibraryPlugin;
import com.android.build.gradle.internal.api.BaseVariantImpl;
import com.android.build.gradle.internal.scope.GlobalScope;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.build.gradle.tasks.LintBaseTask;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.Variant;
import com.android.tools.lint.LintCliFlags;
import com.android.tools.lint.Warning;
import com.android.tools.lint.checks.BuiltinIssueRegistry;
import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.client.api.LintBaseline;
import com.android.tools.lint.gradle.api.VariantInputs;
import com.android.utils.Pair;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.api.tasks.TaskAction;
import org.gradle.tooling.provider.model.ToolingModelBuilder;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

/**
 * 通过git 工具判断增量更新的文件，并进行lint检查
 */
public class IncrementLintTask extends DefaultTask {

    private static final String VERSION = "1.0.0";

    private IssueRegistry mIssueRegistry;
    private LintCliFlags mLintCliFlags;
    private Project mProject;
    private GlobalScope mGlobalScope;
    private BaseVariantData mVariantData;
    private Variant mVariant;
    private IncrementLintGradleClient mIncrementLintGradleClient;
    private File mSdkHome;

    /**
     * 提前准备工作
     */
    private boolean preAction() {
        // 获取 gradleProject
        mProject = getProject();

        // 变种列表
        Object[] variantImplList = null;

        if (mProject.getPlugins().hasPlugin(AppPlugin.class)) {
            AppExtension appExtension = mProject.getExtensions().findByType(AppExtension.class);
            if (appExtension != null) {
                mSdkHome = appExtension.getSdkDirectory();
                variantImplList = appExtension.getApplicationVariants().toArray();
            }
        } else if (mProject.getPlugins().hasPlugin(LibraryPlugin.class)) {
            LibraryExtension libraryExtension = mProject.getExtensions().findByType(LibraryExtension.class);
            if (libraryExtension != null) {
                mSdkHome = libraryExtension.getSdkDirectory();
                variantImplList = libraryExtension.getLibraryVariants().toArray();
            }
        }

        if (variantImplList == null || mSdkHome == null) {
            return false;
        }

        // 选择一个变种
        Object variantImpl = null;

        for (Object object : variantImplList) {
            String variantName = ((BaseVariantImpl) object).getName();
            if ("debug".equals(variantName)) {
                variantImpl = object;
                break;
            }
        }

        if (variantImpl == null) {
            variantImpl = variantImplList[0];
        }

        if (variantImpl == null) {
            return false;
        }

        try {
            Class baseVariantImplClazz = Class.forName("com.android.build.gradle.internal.api.BaseVariantImpl");
            Method method = baseVariantImplClazz.getDeclaredMethod("getVariantData");
            if (method != null) {
                method.setAccessible(true);
                mVariantData = (BaseVariantData) method.invoke(variantImpl);
            }
        } catch (Exception e) {
        }

        if (mVariantData == null) {
            return false;
        }


        // GlobalScope
        mGlobalScope = mVariantData.getScope().getGlobalScope();

        if (mGlobalScope == null) {
            return false;
        }


        // 创建AndroidProject
        String modelName = AndroidProject.class.getName();
        ToolingModelBuilder modelBuilder = mGlobalScope.getToolingRegistry().getBuilder(modelName);

        ExtraPropertiesExtension ext = mProject.getExtensions().getExtraProperties();
        ext.set("android.injected.build.model.only.versioned", Integer.toString(3));
        ext.set("android.injected.build.model.disable.src.download", Boolean.TRUE);

        AndroidProject var5;
        try {
            var5 = (AndroidProject) modelBuilder.buildAll(modelName, mProject);
        } finally {
            ext.set("android.injected.build.model.only.versioned", (Object) null);
            ext.set("android.injected.build.model.disable.src.download", (Object) null);
        }

        Collection<Variant> variantList = var5.getVariants();
        if (variantList != null && !variantList.isEmpty()) {
            for (Variant variant : variantList) {
                if (variant.getName().equals("debug")) {
                    mVariant = variant;
                    break;
                }
            }
        }

        if (mVariant == null) {
            return false;
        }

        return true;
    }

    @TaskAction
    public void action() {
        System.out.println("IncrementLintTask action start");
        if (preAction()) {
            System.out.println("pre action success");

            VariantInputs variantInputs = new LintBaseTask.VariantInputs(mVariantData.getScope());

            mIssueRegistry = new BuiltinIssueRegistry();
            mLintCliFlags = new LintCliFlags();

            mIncrementLintGradleClient = new IncrementLintGradleClient(
                    VERSION,
                    mIssueRegistry,
                    mLintCliFlags,
                    mProject,
                    null,
                    mVariant,
                    variantInputs,
                    null,
                    true,
                    null);

            // 错误标识位
            boolean hasError = false;
            try {
                Pair<List<Warning>, LintBaseline> warnings = mIncrementLintGradleClient.run(mIssueRegistry);
                for (Warning w : warnings.getFirst()) {
                    if (w.severity.isError()) {
                        hasError = true;
                    }
                    System.out.println(w.message + "       " + w.path);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            if (hasError) {
                throw new RuntimeException("Lint检查未通过");
            }
        }
        System.out.println("IncrementLintTask action end");
    }
}
