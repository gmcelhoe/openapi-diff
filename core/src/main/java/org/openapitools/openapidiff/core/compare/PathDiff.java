package org.openapitools.openapidiff.core.compare;

import static org.openapitools.openapidiff.core.utils.ChangedUtils.isChanged;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.openapitools.openapidiff.core.model.ChangedPath;
import org.openapitools.openapidiff.core.model.DiffContext;

public class PathDiff {
  private final OpenApiDiff openApiDiff;

  public PathDiff(OpenApiDiff openApiDiff) {
    this.openApiDiff = openApiDiff;
  }

  public Optional<ChangedPath> diff(PathItem left, PathItem right, DiffContext context) {
    Map<PathItem.HttpMethod, Operation> oldOperationMap = left.readOperationsMap();
    Map<PathItem.HttpMethod, Operation> newOperationMap = right.readOperationsMap();
    MapKeyDiff<PathItem.HttpMethod, Operation> operationsDiff =
        MapKeyDiff.diff(oldOperationMap, newOperationMap);
    List<PathItem.HttpMethod> sharedMethods = operationsDiff.getSharedKey();
    ChangedPath changedPath =
        new ChangedPath(context.getUrl(), left, right, context)
            .setIncreased(operationsDiff.getIncreased())
            .setMissing(operationsDiff.getMissing());
    for (PathItem.HttpMethod method : sharedMethods) {
      Operation oldOperation = oldOperationMap.get(method);
      Operation newOperation = newOperationMap.get(method);
      openApiDiff
          .getOperationDiff()
          .diff(oldOperation, newOperation, context.copyWithMethod(method))
          .ifPresent(changedPath.getChanged()::add);
    }
    openApiDiff
        .getExtensionsDiff()
        .diff(left.getExtensions(), right.getExtensions(), context)
        .ifPresent(changedPath::setExtensions);
    return isChanged(changedPath);
  }
}
