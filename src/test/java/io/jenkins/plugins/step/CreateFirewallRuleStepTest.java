package io.jenkins.plugins.step;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.Run;
import hudson.util.ArgumentListBuilder;
import java.util.Set;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CreateFirewallRuleStepTest {

    private final StepContext contextMock = mock(StepContext.class);
    private final EnvVars envVarsMock = mock(EnvVars.class);
    private final Launcher launcherMock = mock(Launcher.class, RETURNS_DEEP_STUBS);

    private final CreateFirewallRuleStep step = new CreateFirewallRuleStep("test");

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
        final var descriptor = new CreateFirewallRuleStep.Descriptor();
        assertEquals("Create a firewall rule", descriptor.getDisplayName());
        assertEquals("createFirewallRule", descriptor.getFunctionName());
        assertTrue(descriptor.getRequiredContext().containsAll(Set.of(Run.class, Launcher.class)));
    }

    @Test
    void testRunBothActionAndAllowNull() {
        final var execution = new CreateFirewallRuleStep.CreateFirewallRuleStepExecution(contextMock, step);

        assertThrows(IllegalArgumentException.class, execution::run);
    }

    @Test
    void testRunActionNullAllowNotNull() {
        step.setAllow("allow");
        final var execution = new CreateFirewallRuleStep.CreateFirewallRuleStepExecution(contextMock, step);

        assertDoesNotThrow(execution::run);
    }

    @Test
    void testRunActionNotNullAllowNull() {
        step.setAction("action");
        final var execution = new CreateFirewallRuleStep.CreateFirewallRuleStepExecution(contextMock, step);

        assertDoesNotThrow(execution::run);
    }

    @Test
    void testRunLauncherCommandNonZeroResult() throws Exception {
        step.setAction("action");
        final var execution = new CreateFirewallRuleStep.CreateFirewallRuleStepExecution(contextMock, step);

        when(launcherMock
                        .launch()
                        .cmds(any(ArgumentListBuilder.class))
                        .quiet(true)
                        .join())
                .thenReturn(1);

        assertThrows(IllegalArgumentException.class, execution::run);
    }

    @Test
    void testRunLauncherCommandWithEnvVars() throws Exception {
        step.setAction("action");
        when(contextMock.get(EnvVars.class)).thenReturn(envVarsMock);
        final var execution = new CreateFirewallRuleStep.CreateFirewallRuleStepExecution(contextMock, step);

        when(launcherMock
                        .launch()
                        .cmds(any(ArgumentListBuilder.class))
                        .envs(envVarsMock)
                        .quiet(true)
                        .join())
                .thenReturn(0);

        assertDoesNotThrow(execution::run);
    }

    @Test
    void testRunAllParamsNotNull() {
        step.setAction("action");
        step.setAllow("allow");
        step.setDescription("description");
        step.setDestinationRanges("destinationRanges");
        step.setDirection("direction");
        step.setDisabled(true);
        step.setEnableLogging(true);
        step.setLoggingMetadata("loggingMetadata");
        step.setNetwork("network");
        step.setPriority(1000);
        step.setRules("rules");
        step.setSourceRanges("sourceRanges");
        step.setSourceServiceAccounts("sourceServiceAccounts");
        step.setSourceTags("sourceTags");
        step.setTargetServiceAccounts("targetServiceAccounts");
        step.setTargetTags("targetTags");

        final var execution = new CreateFirewallRuleStep.CreateFirewallRuleStepExecution(contextMock, step);

        assertDoesNotThrow(execution::run);

        final var cmd = ArgumentCaptor.forClass(ArgumentListBuilder.class);
        verify(launcherMock.launch(), atLeastOnce()).cmds(cmd.capture());
        assertTrue(cmd.getValue().toString().contains("--action=action"));
        assertFalse(cmd.getValue().toString().contains("--allow=allow"));
        assertTrue(cmd.getValue().toString().contains("--description=description"));
        assertTrue(cmd.getValue().toString().contains("--destination-ranges=destinationRanges"));
        assertTrue(cmd.getValue().toString().contains("--disabled"));
        assertTrue(cmd.getValue().toString().contains("--direction=direction"));
        assertTrue(cmd.getValue().toString().contains("--enable-logging"));
        assertTrue(cmd.getValue().toString().contains("--logging-metadata=loggingMetadata"));
        assertTrue(cmd.getValue().toString().contains("--network=network"));
        assertTrue(cmd.getValue().toString().contains("--priority=1000"));
        assertTrue(cmd.getValue().toString().contains("--rules=rules"));
        assertTrue(cmd.getValue().toString().contains("--source-ranges=sourceRanges"));
        assertTrue(cmd.getValue().toString().contains("--source-service-accounts=sourceServiceAccounts"));
        assertTrue(cmd.getValue().toString().contains("--source-tags=sourceTags"));
        assertTrue(cmd.getValue().toString().contains("--target-service-accounts=targetServiceAccounts"));
        assertTrue(cmd.getValue().toString().contains("--target-tags=targetTags"));
    }
}
