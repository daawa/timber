package timber.lint;

import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.ApiKt;
import com.android.tools.lint.detector.api.Issue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TimberIssueRegistry extends IssueRegistry {
  @Override public List<Issue> getIssues() {
    List<Issue> issues = new ArrayList<>();
    issues.addAll(Arrays.asList(WrongTimberUsageDetector.getIssues()));
    issues.add(GradleDependsDetector.ISSUE);
    return issues;
  }

  @Override public int getApi() {
    return ApiKt.CURRENT_API;
  }
}
