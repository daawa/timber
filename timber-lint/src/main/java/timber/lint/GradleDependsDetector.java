package timber.lint;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.GradleContext;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;

import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


/**
 * 模块间互相依赖检查, 基本原则如下
 * 1. business只能引用common | common_business
 * 2. common_business只能引common
 * 3. common只能引common
 * 4. dynamic_feature在1的基础上可以多引用一个:shell
 */
public class GradleDependsDetector extends Detector implements Detector.GradleScanner {

    private final static String DEPENDENCIES = "dependencies";
    private final static String PREFIX = "project(";


    private final static String PREFIX_BUSINESS = "business";
    private final static String PREFIX_COMMON_BUSINESS = "common_business";
    private final static String PREFIX_COMMON = "common";
    private final static String PKG_DYNAMIC = "com.legend.business.dynamic";

    private final static String PREFIX_SHELL = "shell";

    private final static HashMap<String, List<String>> dependsMap = new HashMap();

    static{
        dependsMap.put(PREFIX_BUSINESS, Arrays.asList(PREFIX_COMMON_BUSINESS, PREFIX_COMMON));
        dependsMap.put(PREFIX_COMMON_BUSINESS, Arrays.asList(PREFIX_COMMON));
        dependsMap.put(PREFIX_COMMON, Arrays.asList(PREFIX_COMMON));
    }

    @Override
    public void checkDslPropertyAssignment(@NotNull GradleContext context, @NotNull String property, @NotNull String value, @NotNull String parent, @Nullable String parentParent, @NotNull Object valueCookie, @NotNull Object statementCookie) {
        super.checkDslPropertyAssignment(context, property, value, parent, parentParent, valueCookie, statementCookie);
        //if(!parent.equals(DEPENDENCIES) || !value.startsWith(PREFIX)) return;
        String pkg = context.getProject().getPackage();
        String name = context.getProject().getName();
        String module = getModuleName(pkg);

//        String pkg = context.getProject().getPackage();
//        String name = context.getProject().getName();
        String gradlePath = context.file.getAbsolutePath();
        println("===========pkg:    " + pkg + "===========");
        println("===========project_name:   " + name + "===========");
        println("===========gradlePath:    " + gradlePath + "===========");

        println("===========property:   " + property + "===========");
        println("===========value:  " + value + "===========");

        println("===========parent:  " + parent + "===========");
        println("===========parentParent:  " + parentParent + "===========");

        //println("===========propertyCookie:  " + propertyCookie + "===========");
        println("===========valueCookie:  " + valueCookie.getClass().getName() + "===========");

        println("===========statementCookie:  " + statementCookie.getClass().getName() + "===========");
        if(TextUtils.isEmpty(module)) return;
        String dependency = getDependencies(value);
        //output: ===========property:implementation, value:project(':timber')===========
        if(TextUtils.isEmpty(dependency)) return;
        if(!dependsMap.containsKey(module)) return;
        if(!dependsMap.get(module).contains(dependency) && !inWhiteList(pkg, dependency)) {
            context.report(ISSUE, valueCookie, context.getLocation(valueCookie), "模块依赖不规范，请参考<E-H-IN Android开发规范>或<E-H-IN lint指南>", null);
        }
    }

    /*
    @Override
    public void checkDslPropertyAssignment(@NotNull GradleContext context, @NotNull String property, @NotNull String value, @NotNull String parent, @Nullable String parentParent, @NotNull Object propertyCookie, @NotNull Object valueCookie, @NotNull Object statementCookie) {
        super.checkDslPropertyAssignment(context, property, value, parent, parentParent, propertyCookie, valueCookie, statementCookie);
        String pkg = context.getProject().getPackage();
        String name = context.getProject().getName();
        String gradlePath = context.file.getAbsolutePath();
        println("===========pkg:    " + pkg + "===========");
        println("===========project_name:   " + name + "===========");
        println("===========gradlePath:    " + gradlePath + "===========");

        println("===========property:   " + property + "===========");
        println("===========value:  " + value + "===========");

        println("===========parent:  " + parent + "===========");
        println("===========parentParent:  " + parentParent + "===========");

        println("===========propertyCookie:  " + propertyCookie + "===========");
        println("===========valueCookie:  " + valueCookie + "===========");

        println("===========statementCookie:  " + statementCookie + "===========");

    }*/

    public static final Issue ISSUE = Issue.create(
            "LegendGradleDependsCheck",
            "模块依赖不规范",
            "模块依赖不规范，请参考<E-H-IN Android开发规范>或<E-H-IN lint指南>",
            Category.CORRECTNESS, 5, Severity.ERROR,
            new Implementation(GradleDependsDetector.class, Scope.GRADLE_SCOPE));

    private Boolean inWhiteList(String pkg, String dependency){
        if(pkg.startsWith(PKG_DYNAMIC) && dependency.equals(PREFIX_SHELL)){
            return true;
        }
        return false;
    }

    private String getModuleName(String pkg){
        if(pkg.split("\\.").length <= 2) return "";
        return pkg.split("\\.")[2];
    }

    private String getDependencies(String value){
        if(value.split(":").length <= 1) return "";
        return value.split(":")[1];
    }


    private void println(String log) {
        System.out.println(log);
    }

}
