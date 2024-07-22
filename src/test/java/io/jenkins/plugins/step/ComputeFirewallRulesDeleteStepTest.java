package io.jenkins.plugins.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
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

class ComputeFirewallRulesDeleteStepTest {
    private static final String NAME = "test";

    private final StepContext contextMock = mock(StepContext.class);
    private final EnvVars envVarsMock = mock(EnvVars.class);
    private final Launcher launcherMock = mock(Launcher.class, RETURNS_DEEP_STUBS);

    private final ComputeFirewallRulesDeleteStep step = new ComputeFirewallRulesDeleteStep(NAME);

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
        final var descriptor = new ComputeFirewallRulesDeleteStep.Descriptor();
        assertThat(descriptor.getDisplayName()).isEqualTo("Delete a firewall rule");
        assertThat(descriptor.getFunctionName()).isEqualTo("computeFirewallRulesDelete");
        assertThat(descriptor.getRequiredContext()).isEqualTo(Set.of(Run.class, Launcher.class, EnvVars.class));
    }

    @Test
    void testName() {
        assertThat(step.getName()).isEqualTo(NAME);
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

        assertThatCode(execution::start).doesNotThrowAnyException();
    }

    @Test
    void testRunAndLauncherCommandNonZeroResult() throws Exception {
        when(launcherMock
                        .launch()
                        .cmds(any(ArgumentListBuilder.class))
                        .quiet(true)
                        .join())
                .thenReturn(1);

        final var execution =
                new ComputeFirewallRulesDeleteStep.ComputeFirewallRulesDeleteRuleStepExecution(contextMock, NAME);

        assertThatCode(execution::run).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testStart() {
        final var execution = step.start(contextMock);

        assertThatCode(execution::start).doesNotThrowAnyException();
    }
}
