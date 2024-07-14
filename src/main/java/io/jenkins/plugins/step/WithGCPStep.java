package io.jenkins.plugins.step;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.security.ACL;
import hudson.util.ArgumentListBuilder;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.plaincredentials.FileCredentials;
import org.jenkinsci.plugins.workflow.steps.BodyExecutionCallback;
import org.jenkinsci.plugins.workflow.steps.EnvironmentExpander;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;

public class WithGCPStep extends Step {

    private final String credentialsId;

    @DataBoundConstructor
    public WithGCPStep(final String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    @Extension
    public static class Descriptor extends StepDescriptor {

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Set.of(EnvVars.class, Run.class, Launcher.class, FilePath.class);
        }

        @Override
        public String getFunctionName() {
            return "withGCP";
        }

        @Override
        @NonNull
        public String getDisplayName() {
            return "Set GCP credentials for nested block";
        }

        @Override
        public boolean takesImplicitBlockArgument() {
            return true;
        }
    }

    @Override
    public StepExecution start(final StepContext context) {
        return new WithGCPExecution(context, credentialsId);
    }

    private static class WithGCPExecution extends StepExecution {
        private final String credentialsId;

        WithGCPExecution(final StepContext context, final String credentialsId) {
            super(context);
            this.credentialsId = credentialsId;
        }

        @Override
        public boolean start() throws Exception {
            final var context = getContext();
            final var fileCreds = getFileCredentials();

            if (fileCreds == null) {
                throw new IllegalArgumentException("Couldn't find credentials file with id " + credentialsId);
            }

            final var launcher = context.get(Launcher.class);
            final var workspace = context.get(FilePath.class);
            workspace.mkdirs();

            final var tempFile = getTempFile(fileCreds, workspace);

            final var cmd = new ArgumentListBuilder();
            cmd.add("gcloud", "auth", "activate-service-account", "--key-file=" + tempFile);
            final var result =
                    launcher.launch().cmds(cmd).pwd(workspace).quiet(true).join();

            if (result != 0) {
                tempFile.delete();
                throw new IllegalArgumentException(
                        "Failed to authenticate to GCP using credentials file with id " + credentialsId);
            }

            final var envVars = context.get(EnvVars.class);
            envVars.put("GOOGLE_APPLICATION_CREDENTIALS", credentialsId);
            final var projectId = extractProjectId(tempFile);
            if (projectId != null) {
                envVars.put("CLOUDSDK_CORE_PROJECT", projectId);
            }
            context.newBodyInvoker()
                    .withContext(EnvironmentExpander.merge(
                            context.get(EnvironmentExpander.class), new ExpanderImpl(envVars)))
                    .withCallback(BodyExecutionCallback.wrap(context))
                    .start();

            tempFile.delete();
            return false;
        }

        private static FilePath getTempFile(final FileCredentials fileCreds, final FilePath workspace)
                throws IOException {
            try {
                final var fileName = UUID.randomUUID().toString();
                final var extension = ".json";
                final var tempFile = workspace.createTempFile(fileName, extension);
                tempFile.copyFrom(fileCreds.getContent());
                return tempFile;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        private FileCredentials getFileCredentials() {
            final var creds = CredentialsProvider.lookupCredentialsInItemGroup(
                    FileCredentials.class, Jenkins.get(), ACL.SYSTEM2, List.of());

            return creds.stream()
                    .filter(cred -> credentialsId.equals(cred.getId()))
                    .findFirst()
                    .orElse(null);
        }

        private static String extractProjectId(final FilePath fileCreds) {
            try {
                final var jsonObject = new JSONObject(fileCreds.readToString());
                return jsonObject.getString("project_id");
            } catch (final InterruptedException | IOException e) {
                return null;
            }
        }

        private static class ExpanderImpl extends EnvironmentExpander {
            private final Map<String, String> envVars;

            private ExpanderImpl(final Map<String, String> envVars) {
                this.envVars = envVars;
            }

            @Override
            public void expand(@NonNull final EnvVars env) {
                env.overrideAll(envVars);
            }
        }
    }
}
