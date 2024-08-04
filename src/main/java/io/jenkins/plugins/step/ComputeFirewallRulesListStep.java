package io.jenkins.plugins.step;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class ComputeFirewallRulesListStep extends Step {

    private String name;
    private String regexp;
    private String filter;
    private String limit;
    private int pageSize;
    private String sortBy;
    private boolean uri;
    private String format;
    private boolean printOutput = true;

    @DataBoundConstructor
    public ComputeFirewallRulesListStep() {}

    @DataBoundSetter
    public void setName(final String name) {
        this.name = name;
    }

    @DataBoundSetter
    public void setRegexp(final String regexp) {
        this.regexp = regexp;
    }

    @DataBoundSetter
    public void setFilter(final String filter) {
        this.filter = filter;
    }

    @DataBoundSetter
    public void setLimit(final String limit) {
        this.limit = limit;
    }

    @DataBoundSetter
    public void setPageSize(final int pageSize) {
        this.pageSize = pageSize;
    }

    @DataBoundSetter
    public void setSortBy(final String sortBy) {
        this.sortBy = sortBy;
    }

    @DataBoundSetter
    public void setUri(final boolean uri) {
        this.uri = uri;
    }

    @DataBoundSetter
    public void setFormat(final String format) {
        this.format = format;
    }

    @DataBoundSetter
    public void setPrintOutput(final boolean printOutput) {
        this.printOutput = printOutput;
    }

    public String getName() {
        return name;
    }

    public String getRegexp() {
        return regexp;
    }

    public String getFilter() {
        return filter;
    }

    public String getLimit() {
        return limit;
    }

    public int getPageSize() {
        return pageSize;
    }

    public String getSortBy() {
        return sortBy;
    }

    public boolean isUri() {
        return uri;
    }

    public String getFormat() {
        return format;
    }

    public boolean isPrintOutput() {
        return printOutput;
    }

    @Extension
    public static class Descriptor extends StepDescriptor {

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Set.of(Run.class, Launcher.class, EnvVars.class, TaskListener.class);
        }

        @Override
        public String getFunctionName() {
            return "computeFirewallRulesList";
        }

        @Override
        @NonNull
        public String getDisplayName() {
            return "List firewall rules";
        }
    }

    @Override
    public StepExecution start(final StepContext context) {
        return new ComputeFirewallRulesListRuleStepExecution(context, this);
    }

    static final class ComputeFirewallRulesListRuleStepExecution extends SynchronousStepExecution<String> {

        private static final long serialVersionUID = 1L;
        private final transient ComputeFirewallRulesListStep step;

        ComputeFirewallRulesListRuleStepExecution(final StepContext context, final ComputeFirewallRulesListStep step) {
            super(context);
            this.step = step;
        }

        @Override
        protected String run() throws Exception {
            final var context = getContext();
            final var listener = context.get(TaskListener.class);
            final var launcher = context.get(Launcher.class);
            final var cmd = new ArgumentListBuilder();
            cmd.add("gcloud", "compute", "firewall-rules", "list");

            if (step.getName() != null) {
                cmd.add("--filter=name=(" + step.getName() + ")");
            }

            if (step.getRegexp() != null) {
                cmd.add("--filter=name~" + step.getRegexp());
            }

            if (step.getFilter() != null) {
                cmd.add("--filter=" + step.getFilter());
            }

            if (step.getLimit() != null) {
                cmd.add("--limit=" + step.getLimit());
            }

            if (step.getPageSize() != 0) {
                cmd.add("--page-size=" + step.getPageSize());
            }

            if (step.getSortBy() != null) {
                cmd.add("--sort-by=" + step.getSortBy());
            }

            if (step.isUri()) {
                cmd.add("--uri");
            }

            if (step.getFormat() != null) {
                cmd.add("--format=" + step.getFormat());
            }

            final var envVars = context.get(EnvVars.class);
            final var starter = launcher.launch().cmds(cmd).quiet(true);
            if (envVars != null) {
                starter.envs(envVars);
            }
            final var outputStream = new ByteArrayOutputStream();
            starter.stdout(outputStream);
            final var result = starter.join();

            if (result != 0) {
                throw new IllegalArgumentException("Failed to create a firewall rule with this command: " + cmd);
            }

            final var output = outputStream.toString(StandardCharsets.UTF_8);
            if (step.isPrintOutput()) {
                listener.getLogger().println(output);
            }
            return output;
        }
    }
}
