package com.qdesrame.openapi.diff.compare;

import com.qdesrame.openapi.diff.model.ChangedRequestBody;
import com.qdesrame.openapi.diff.utils.RefPointer;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.Objects;
import java.util.Optional;

/**
 * Created by adarsh.sharma on 28/12/17.
 */
public class RequestBodyDiff {
    private OpenApiDiff openApiDiff;
    private ReferenceDiffCache<ChangedRequestBody> requestBodyDiffCache;

    public RequestBodyDiff(OpenApiDiff openApiDiff) {
        this.openApiDiff = openApiDiff;
        this.requestBodyDiffCache = new ReferenceDiffCache<>();
    }

    public Optional<ChangedRequestBody> diff(RequestBody left, RequestBody right) {
        String leftRef = left.get$ref();
        String rightRef = right.get$ref();
        boolean areBothRefs = leftRef != null && rightRef != null;
        if (areBothRefs) {
            Optional<ChangedRequestBody> changedRequestBodyCache = requestBodyDiffCache.getFromCache(leftRef, rightRef);
            if (changedRequestBodyCache.isPresent()) {
                return changedRequestBodyCache;
            }
        }

        Content oldRequestContent = new Content();
        Content newRequestContent = new Content();
        RequestBody oldRequestBody = null;
        RequestBody newRequestBody = null;
        if (left != null) {
            oldRequestBody = RefPointer.Replace.requestBody(openApiDiff.getOldSpecOpenApi().getComponents(), left);
            if (oldRequestBody.getContent() != null) {
                oldRequestContent = oldRequestBody.getContent();
            }
        }
        if (right != null) {
            newRequestBody = RefPointer.Replace.requestBody(openApiDiff.getNewSpecOpenApi().getComponents(), right);
            if (newRequestBody.getContent() != null) {
                newRequestContent = newRequestBody.getContent();
            }
        }

        ChangedRequestBody changedRequestBody = new ChangedRequestBody(oldRequestBody, newRequestBody);

        boolean leftRequired = oldRequestBody != null && Boolean.TRUE.equals(oldRequestBody.getRequired());
        boolean rightRequired = newRequestBody != null && Boolean.TRUE.equals(newRequestBody.getRequired());
        changedRequestBody.setChangeRequired(leftRequired != rightRequired);

        String leftDescription = oldRequestBody != null ? oldRequestBody.getDescription() : null;
        String rightDescription = newRequestBody != null ? newRequestBody.getDescription() : null;
        changedRequestBody.setChangeDescription(!Objects.equals(leftDescription, rightDescription));

        openApiDiff.getContentDiff().diff(oldRequestContent, newRequestContent).ifPresent(changedRequestBody::setChangedContent);

        if (areBothRefs) {
            requestBodyDiffCache.addToCache(leftRef, rightRef, changedRequestBody);
        }

        return changedRequestBody.isDiff() ? Optional.of(changedRequestBody) : Optional.empty();
    }
}