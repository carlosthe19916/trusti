package org.trusti.importer;

import java.util.HashMap;
import java.util.Map;

public class ImporterCamelHeaders {
    public static final String IMPORTER_TYPE_HEADER = "ImporterType";

    public static final String IMPORTER_TASK_ID = "ImporterOutputBaseUrl";
    public static final String REMOTE_IMPORTER_TRUSTI_SERVER_URL = "RemoteImporterTrustiServerUrl";

    // HTTP
    public static final String IMPORTER_HTTP_SERVER_URL = "ImporterHttpServerUrl";

    // GIT
    public static final String IMPORTER_GIT_WORKSPACE = "ImporterGitWorkspace";
    public static final String IMPORTER_GIT_REPOSITORY = "ImporterGitRepository";
    public static final String IMPORTER_GIT_REF = "ImporterGitRef";
    public static final String IMPORTER_GIT_WORKING_DIRECTORY = "ImporterGitWorkingDirectory";

    public static Map<String, Object> http(Long taskId, String serverUrl) {
        Map<String, Object> headers = new HashMap<>();

        headers.put(IMPORTER_TYPE_HEADER, "http");
        headers.put(IMPORTER_TASK_ID, taskId);
        headers.put(IMPORTER_HTTP_SERVER_URL, serverUrl);

        return headers;
    }

    public static Map<String, Object> git(Long taskId, String workspace, String repository, String ref, String workingDirectory) {
        Map<String, Object> headers = new HashMap<>();

        headers.put(IMPORTER_TYPE_HEADER, "git");
        headers.put(IMPORTER_TASK_ID, taskId);
        headers.put(IMPORTER_GIT_WORKSPACE, workspace);

        headers.put(IMPORTER_GIT_REPOSITORY, repository);
        headers.put(IMPORTER_GIT_REF, ref);
        headers.put(IMPORTER_GIT_WORKING_DIRECTORY, workingDirectory);

        return headers;
    }
}
