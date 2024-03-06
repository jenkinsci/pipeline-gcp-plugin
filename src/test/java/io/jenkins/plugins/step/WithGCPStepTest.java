package io.jenkins.plugins.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import java.util.List;
import java.util.Set;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.plaincredentials.FileCredentials;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WithGCPStepTest {
    private static final String CREDENTIALS_ID = "id";
    private static final String GOOGLE_APPLICATION_CREDENTIALS = "GOOGLE_APPLICATION_CREDENTIALS";

    private final StepContext stepContextMock = mock(StepContext.class, RETURNS_DEEP_STUBS);
    private final TaskListener listenerMock = mock(TaskListener.class, RETURNS_DEEP_STUBS);
    private final Launcher launcherMock = mock(Launcher.class);
    private final FilePath workspaceMock = mock(FilePath.class);
    private final FilePath tempFileMock = mock(FilePath.class);
    private final EnvVars envVarsMock = mock(EnvVars.class);
    private final FileCredentials credentialsMock = mock(FileCredentials.class);
    private final Launcher.ProcStarter procStarterMock = mock(Launcher.ProcStarter.class);

    @BeforeEach
    void setUp() throws Exception {
        when(stepContextMock.get(TaskListener.class)).thenReturn(listenerMock);
        when(stepContextMock.get(Launcher.class)).thenReturn(launcherMock);
        when(stepContextMock.get(FilePath.class)).thenReturn(workspaceMock);
        when(stepContextMock.get(EnvVars.class)).thenReturn(envVarsMock);

        when(launcherMock.launch()).thenReturn(procStarterMock);
        when(procStarterMock.cmds(any(ArgumentListBuilder.class))).thenReturn(procStarterMock);
        when(procStarterMock.pwd(workspaceMock)).thenReturn(procStarterMock);
        when(procStarterMock.quiet(true)).thenReturn(procStarterMock);
        when(workspaceMock.createTempFile(anyString(), anyString())).thenReturn(tempFileMock);
        when(tempFileMock.readToString()).thenReturn("{project_id: \"id\"}");
        when(credentialsMock.getId()).thenReturn(CREDENTIALS_ID);
    }

    @Test
    void testCredentialsMissing() throws Exception {
        try (final var credentialsProviderMock = mockStatic(CredentialsProvider.class);
                final var jenkinsMock = mockStatic(Jenkins.class)) {
            jenkinsMock.when(Jenkins::get).thenReturn(mock(Jenkins.class));
            credentialsProviderMock
                    .when(() -> CredentialsProvider.lookupCredentialsInItemGroup(any(), any(), any(), any()))
                    .thenReturn(List.of(credentialsMock));
            when(credentialsMock.getId()).thenReturn(null);

            final var execution = new WithGCPStep(CREDENTIALS_ID);
            final var result = execution.start(stepContextMock);

            assertNotNull(result);

            final var executionResult = result.start();

            assertFalse(executionResult);
            verify(listenerMock).getLogger();
            verifyNoInteractions(envVarsMock);
        }
    }

    @Test
    void testGcloudAuthenticationSuccess() throws Exception {
        try (final var credentialsProviderMock = mockStatic(CredentialsProvider.class);
                final var jenkinsMock = mockStatic(Jenkins.class)) {
            jenkinsMock.when(Jenkins::get).thenReturn(mock(Jenkins.class));
            credentialsProviderMock
                    .when(() -> CredentialsProvider.lookupCredentialsInItemGroup(any(), any(), any(), any()))
                    .thenReturn(List.of(credentialsMock));
            when(procStarterMock.join()).thenReturn(0);

            final var execution = new WithGCPStep(CREDENTIALS_ID);
            final var result = execution.start(stepContextMock);

            assertNotNull(result);

            final var executionResult = result.start();

            assertFalse(executionResult);
            verify(envVarsMock).put(GOOGLE_APPLICATION_CREDENTIALS, CREDENTIALS_ID);
        }
    }

    @Test
    void testGcloudAuthenticationFailure() throws Exception {
        try (final var credentialsProviderMock = mockStatic(CredentialsProvider.class);
                final var jenkinsMock = mockStatic(Jenkins.class)) {
            jenkinsMock.when(Jenkins::get).thenReturn(mock(Jenkins.class));
            credentialsProviderMock
                    .when(() -> CredentialsProvider.lookupCredentialsInItemGroup(any(), any(), any(), any()))
                    .thenReturn(List.of(credentialsMock));
            when(procStarterMock.join()).thenReturn(1);

            final var execution = new WithGCPStep(CREDENTIALS_ID);
            final var result = execution.start(stepContextMock);

            assertNotNull(result);

            final var executionResult = result.start();

            assertFalse(executionResult);
            verify(envVarsMock, never()).put(GOOGLE_APPLICATION_CREDENTIALS, CREDENTIALS_ID);
        }
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Test
    void testDescriptor() {
        final var descriptor = new WithGCPStep.Descriptor();
        assertEquals("Sets GCP credentials for nested block", descriptor.getDisplayName());
        assertEquals("withGCP", descriptor.getFunctionName());
        assertTrue(descriptor.takesImplicitBlockArgument());
        assertTrue(descriptor
                .getRequiredContext()
                .containsAll(Set.of(TaskListener.class, EnvVars.class, Run.class, Launcher.class, FilePath.class)));
    }
}
