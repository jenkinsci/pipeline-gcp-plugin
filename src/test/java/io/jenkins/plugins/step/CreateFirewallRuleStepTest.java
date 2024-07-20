package io.jenkins.plugins.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
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

    @Test
    void testDescriptor() {
        final var descriptor = new CreateFirewallRuleStep.Descriptor();
        assertThat(descriptor.getDisplayName()).isEqualTo("Create a firewall rule");
        assertThat(descriptor.getFunctionName()).isEqualTo("createFirewallRule");
        assertThat(descriptor.getRequiredContext()).isEqualTo(Set.of(Run.class, Launcher.class, EnvVars.class));
    }

    @Test
    void testRunBothActionAndAllowNull() {
        final var execution = new CreateFirewallRuleStep.CreateFirewallRuleStepExecution(contextMock, step);

        assertThatCode(execution::run).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testRunActionNullAllowNotNull() {
        step.setAllow("allow");
        final var execution = new CreateFirewallRuleStep.CreateFirewallRuleStepExecution(contextMock, step);

        assertThatCode(execution::run).doesNotThrowAnyException();
    }

    @Test
    void testRunActionNotNullAllowNull() {
        step.setAction("action");
        final var execution = new CreateFirewallRuleStep.CreateFirewallRuleStepExecution(contextMock, step);

        assertThatCode(execution::run).doesNotThrowAnyException();
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

        assertThatCode(execution::run).isInstanceOf(IllegalArgumentException.class);
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

        assertThatCode(execution::run).doesNotThrowAnyException();
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

        assertThatCode(execution::run).doesNotThrowAnyException();

        final var cmd = ArgumentCaptor.forClass(ArgumentListBuilder.class);
        verify(launcherMock.launch(), atLeastOnce()).cmds(cmd.capture());
        assertThat(cmd.getValue().toString()).contains("--action=action");
        assertThat(cmd.getValue().toString()).doesNotContain("--allow=allow");
        assertThat(cmd.getValue().toString()).contains("--description=description");
        assertThat(cmd.getValue().toString()).contains("--destination-ranges=destinationRanges");
        assertThat(cmd.getValue().toString()).contains("--disabled");
        assertThat(cmd.getValue().toString()).contains("--direction=direction");
        assertThat(cmd.getValue().toString()).contains("--enable-logging");
        assertThat(cmd.getValue().toString()).contains("--logging-metadata=loggingMetadata");
        assertThat(cmd.getValue().toString()).contains("--network=network");
        assertThat(cmd.getValue().toString()).contains("--priority=1000");
        assertThat(cmd.getValue().toString()).contains("--rules=rules");
        assertThat(cmd.getValue().toString()).contains("--source-ranges=sourceRanges");
        assertThat(cmd.getValue().toString()).contains("--source-service-accounts=sourceServiceAccounts");
        assertThat(cmd.getValue().toString()).contains("--source-tags=sourceTags");
        assertThat(cmd.getValue().toString()).contains("--target-service-accounts=targetServiceAccounts");
        assertThat(cmd.getValue().toString()).contains("--target-tags=targetTags");
    }
}
