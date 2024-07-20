package io.jenkins.plugins.step;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.Run;
import hudson.util.ArgumentListBuilder;
import java.util.Set;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeleteFirewallRuleStepTest {
    private static final String NAME = "test";

    private final StepContext contextMock = mock(StepContext.class);
    private final EnvVars envVarsMock = mock(EnvVars.class);
    private final Launcher launcherMock = mock(Launcher.class, RETURNS_DEEP_STUBS);

    private final DeleteFirewallRuleStep step = new DeleteFirewallRuleStep(NAME);

    @BeforeEach
    void before() throws Exception {
        when(contextMock.get(Launcher.class)).thenReturn(launcherMock);
        when(launcherMock
                        .launch()
                        .cmds(any(ArgumentListBuilder.class))
                        .quiet(true)
                        .join())
                .thenReturn(0);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Test
    void testDescriptor() {
        final var descriptor = new DeleteFirewallRuleStep.Descriptor();
        assertEquals("Delete a firewall rule", descriptor.getDisplayName());
        assertEquals("deleteFirewallRule", descriptor.getFunctionName());
        assertTrue(descriptor.getRequiredContext().containsAll(Set.of(Run.class, Launcher.class, EnvVars.class)));
    }

    @Test
    void testName() {
        assertEquals(NAME, step.getName());
    }

    @Test
    void testRunLauncherCommandWithEnvVars() throws Exception {
        when(contextMock.get(EnvVars.class)).thenReturn(envVarsMock);
        final var execution = step.start(contextMock);

        when(launcherMock
                        .launch()
                        .cmds(any(ArgumentListBuilder.class))
                        .envs(envVarsMock)
                        .quiet(true)
                        .join())
                .thenReturn(0);

        when(launcherMock
                        .launch()
                        .cmds(any(ArgumentListBuilder.class))
                        .envs(envVarsMock)
                        .quiet(true)
                        .join())
                .thenReturn(0);

        assertDoesNotThrow(execution::start);
    }

    @Test
    void testRunAndLauncherCommandNonZeroResult() throws Exception {
        when(launcherMock
                        .launch()
                        .cmds(any(ArgumentListBuilder.class))
                        .quiet(true)
                        .join())
                .thenReturn(1);

        final var execution = new DeleteFirewallRuleStep.DeleteFirewallRuleStepExecution(contextMock, NAME);

        assertThrows(IllegalArgumentException.class, execution::run);
    }

    @Test
    void testStart() {
        final var execution = step.start(contextMock);

        assertDoesNotThrow(execution::start);
    }
}
