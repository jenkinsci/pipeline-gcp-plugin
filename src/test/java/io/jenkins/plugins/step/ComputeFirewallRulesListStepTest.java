package io.jenkins.plugins.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import java.util.Set;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ComputeFirewallRulesListStepTest {
    private final StepContext contextMock = mock(StepContext.class);
    private final TaskListener taskListenerMock = mock(TaskListener.class, RETURNS_DEEP_STUBS);
    private final EnvVars envVarsMock = mock(EnvVars.class);
    private final Launcher launcherMock = mock(Launcher.class, RETURNS_DEEP_STUBS);

    private final ComputeFirewallRulesListStep step = new ComputeFirewallRulesListStep();

    @BeforeEach
    void before() throws Exception {
        when(contextMock.get(Launcher.class)).thenReturn(launcherMock);
        when(contextMock.get(TaskListener.class)).thenReturn(taskListenerMock);
        when(launcherMock
                        .launch()
                        .cmds(any(ArgumentListBuilder.class))
                        .quiet(true)
                        .join())
                .thenReturn(0);
    }

    @Test
    void testDescriptor() {
        final var descriptor = new ComputeFirewallRulesListStep.Descriptor();
        assertThat(descriptor.getDisplayName()).isEqualTo("List firewall rules");
        assertThat(descriptor.getFunctionName()).isEqualTo("computeFirewallRulesList");
        assertThat(descriptor.getRequiredContext())
                .isEqualTo(Set.of(Run.class, Launcher.class, EnvVars.class, TaskListener.class));
    }

    @Test
    void testRunLauncherCommandNonZeroResult() throws Exception {
        final var execution =
                new ComputeFirewallRulesListStep.ComputeFirewallRulesListRuleStepExecution(contextMock, step);

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
        when(contextMock.get(EnvVars.class)).thenReturn(envVarsMock);
        final var execution =
                new ComputeFirewallRulesListStep.ComputeFirewallRulesListRuleStepExecution(contextMock, step);

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
    void testPrintOutputIsFalse() {
        step.setPrintOutput(false);
        final var execution =
                new ComputeFirewallRulesListStep.ComputeFirewallRulesListRuleStepExecution(contextMock, step);

        assertThatCode(execution::run).doesNotThrowAnyException();
        verify(taskListenerMock, never()).getLogger();
    }

    @Test
    void testRunAllParamsNotNull() {
        step.setName("name");
        step.setRegexp("regexp");
        step.setFilter("filter");
        step.setLimit("limit");
        step.setPageSize(10);
        step.setSortBy("sort-by");
        step.setUri(true);
        step.setFormat("format");
        step.setPrintOutput(true);
        final var execution =
                new ComputeFirewallRulesListStep.ComputeFirewallRulesListRuleStepExecution(contextMock, step);

        assertThatCode(execution::run).doesNotThrowAnyException();
        final var cmd = ArgumentCaptor.forClass(ArgumentListBuilder.class);
        verify(launcherMock.launch(), atLeastOnce()).cmds(cmd.capture());
        verify(taskListenerMock).getLogger();
        assertThat(cmd.getValue().toList())
                .containsExactly(
                        "gcloud",
                        "compute",
                        "firewall-rules",
                        "list",
                        "--filter=name=(name)",
                        "--filter=name~regexp",
                        "--filter=filter",
                        "--limit=limit",
                        "--page-size=10",
                        "--sort-by=sort-by",
                        "--uri",
                        "--format=format");
    }
}
