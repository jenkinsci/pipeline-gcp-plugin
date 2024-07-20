package io.jenkins.plugins.step;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.Run;
import hudson.util.ArgumentListBuilder;
import java.util.Set;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

public class DeleteFirewallRuleStep extends Step {

    private final String name;

    @DataBoundConstructor
    public DeleteFirewallRuleStep(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Extension
    public static class Descriptor extends StepDescriptor {

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Set.of(Run.class, Launcher.class, EnvVars.class);
        }

        @Override
        public String getFunctionName() {
            return "deleteFirewallRule";
        }

        @Override
        @NonNull
        public String getDisplayName() {
            return "Delete a firewall rule";
        }
    }

    @Override
    public StepExecution start(final StepContext context) {
        return new DeleteFirewallRuleStepExecution(context, name);
    }

    static final class DeleteFirewallRuleStepExecution extends SynchronousStepExecution<Void> {

        private static final long serialVersionUID = 1L;
        private final String name;

        DeleteFirewallRuleStepExecution(final StepContext context, final String name) {
            super(context);
            this.name = name;
        }

        @Override
        protected Void run() throws Exception {
            final var context = getContext();
            final var launcher = context.get(Launcher.class);
            final var cmd = new ArgumentListBuilder();
            cmd.add("gcloud", "compute", "firewall-rules", "delete").addTokenized(name);
            final var envVars = context.get(EnvVars.class);
            final var starter = launcher.launch().cmds(cmd).quiet(true);
            if (envVars != null) {
                starter.envs(envVars);
            }
            final var result = starter.join();

            if (result != 0) {
                throw new IllegalArgumentException("Failed to delete a firewall rule with this command: " + cmd);
            }

            return null;
        }
    }
}
