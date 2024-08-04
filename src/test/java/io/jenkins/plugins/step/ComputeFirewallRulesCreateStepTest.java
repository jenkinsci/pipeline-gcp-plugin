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

class ComputeFirewallRulesCreateStepTest {

    private final StepContext contextMock = mock(StepContext.class);
    private final EnvVars envVarsMock = mock(EnvVars.class);
    private final Launcher launcherMock = mock(Launcher.class, RETURNS_DEEP_STUBS);

    private final ComputeFirewallRulesCreateStep step = new ComputeFirewallRulesCreateStep("test");

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
        final var descriptor = new ComputeFirewallRulesCreateStep.Descriptor();
        assertThat(descriptor.getDisplayName()).isEqualTo("Create a firewall rule");
        assertThat(descriptor.getFunctionName()).isEqualTo("computeFirewallRulesCreate");
        assertThat(descriptor.getRequiredContext()).isEqualTo(Set.of(Run.class, Launcher.class, EnvVars.class));
    }

    @Test
    void testRunBothActionAndAllowNull() {
        final var execution =
                new ComputeFirewallRulesCreateStep.ComputeFirewallRulesCreateRuleStepExecution(contextMock, step);

        assertThatCode(execution::run).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testRunActionNullAllowNotNull() {
        step.setAllow("allow");
        final var execution =
                new ComputeFirewallRulesCreateStep.ComputeFirewallRulesCreateRuleStepExecution(contextMock, step);

        assertThatCode(execution::run).doesNotThrowAnyException();
    }

    @Test
    void testRunActionNotNullAllowNull() {
        step.setAction("action");
        final var execution =
                new ComputeFirewallRulesCreateStep.ComputeFirewallRulesCreateRuleStepExecution(contextMock, step);

        assertThatCode(execution::run).doesNotThrowAnyException();
    }

    @Test
    void testRunLauncherCommandNonZeroResult() throws Exception {
        step.setAction("action");
        final var execution =
                new ComputeFirewallRulesCreateStep.ComputeFirewallRulesCreateRuleStepExecution(contextMock, step);

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
        final var execution =
                new ComputeFirewallRulesCreateStep.ComputeFirewallRulesCreateRuleStepExecution(contextMock, step);

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

        final var execution =
                new ComputeFirewallRulesCreateStep.ComputeFirewallRulesCreateRuleStepExecution(contextMock, step);

        assertThatCode(execution::run).doesNotThrowAnyException();

        final var cmd = ArgumentCaptor.forClass(ArgumentListBuilder.class);
        verify(launcherMock.launch(), atLeastOnce()).cmds(cmd.capture());
        assertThat(cmd.getValue().toList())
                .containsExactly(
                        "gcloud",
                        "compute",
                        "firewall-rules",
                        "create",
                        "test",
                        "--action=action",
                        "--description=description",
                        "--destination-ranges=destinationRanges",
                        "--direction=direction",
                        "--disabled",
                        "--enable-logging",
                        "--logging-metadata=loggingMetadata",
                        "--network=network",
                        "--priority=1000",
                        "--rules=rules",
                        "--source-ranges=sourceRanges",
                        "--source-service-accounts=sourceServiceAccounts",
                        "--source-tags=sourceTags",
                        "--target-service-accounts=targetServiceAccounts",
                        "--target-tags=targetTags");
    }
}
