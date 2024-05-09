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
import org.kohsuke.stapler.DataBoundSetter;

public class CreateFirewallRuleStep extends Step {

    private final String name;

    // exactly one of those is required
    private String action;
    private String allow;

    // optional
    private String description;
    private String destinationRanges;
    private String direction;
    private boolean disabled;
    private boolean enableLogging;
    private String loggingMetadata;
    private String network;
    private int priority;
    private String rules;
    private String sourceRanges;
    private String sourceServiceAccounts;
    private String sourceTags;
    private String targetServiceAccounts;
    private String targetTags;

    @DataBoundConstructor
    public CreateFirewallRuleStep(final String name) {
        this.name = name;
    }

    @DataBoundSetter
    public void setAction(final String action) {
        this.action = action;
    }

    @DataBoundSetter
    public void setAllow(final String allow) {
        this.allow = allow;
    }

    @DataBoundSetter
    public void setDescription(final String description) {
        this.description = description;
    }

    @DataBoundSetter
    public void setDestinationRanges(final String destinationRanges) {
        this.destinationRanges = destinationRanges;
    }

    @DataBoundSetter
    public void setDirection(final String direction) {
        this.direction = direction;
    }

    @DataBoundSetter
    public void setDisabled(final boolean disabled) {
        this.disabled = disabled;
    }

    @DataBoundSetter
    public void setEnableLogging(final boolean enableLogging) {
        this.enableLogging = enableLogging;
    }

    @DataBoundSetter
    public void setLoggingMetadata(final String loggingMetadata) {
        this.loggingMetadata = loggingMetadata;
    }

    @DataBoundSetter
    public void setNetwork(final String network) {
        this.network = network;
    }

    @DataBoundSetter
    public void setPriority(final int priority) {
        this.priority = priority;
    }

    @DataBoundSetter
    public void setRules(final String rules) {
        this.rules = rules;
    }

    @DataBoundSetter
    public void setSourceRanges(final String sourceRanges) {
        this.sourceRanges = sourceRanges;
    }

    @DataBoundSetter
    public void setSourceServiceAccounts(final String sourceServiceAccounts) {
        this.sourceServiceAccounts = sourceServiceAccounts;
    }

    @DataBoundSetter
    public void setSourceTags(final String sourceTags) {
        this.sourceTags = sourceTags;
    }

    @DataBoundSetter
    public void setTargetServiceAccounts(final String targetServiceAccounts) {
        this.targetServiceAccounts = targetServiceAccounts;
    }

    @DataBoundSetter
    public void setTargetTags(final String targetTags) {
        this.targetTags = targetTags;
    }

    public String getName() {
        return name;
    }

    public String getAction() {
        return action;
    }

    public String getAllow() {
        return allow;
    }

    public String getDescription() {
        return description;
    }

    public String getDestinationRanges() {
        return destinationRanges;
    }

    public String getDirection() {
        return direction;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public boolean isEnableLogging() {
        return enableLogging;
    }

    public String getLoggingMetadata() {
        return loggingMetadata;
    }

    public String getNetwork() {
        return network;
    }

    public int getPriority() {
        return priority;
    }

    public String getRules() {
        return rules;
    }

    public String getSourceRanges() {
        return sourceRanges;
    }

    public String getSourceServiceAccounts() {
        return sourceServiceAccounts;
    }

    public String getSourceTags() {
        return sourceTags;
    }

    public String getTargetServiceAccounts() {
        return targetServiceAccounts;
    }

    public String getTargetTags() {
        return targetTags;
    }

    @Extension
    public static class Descriptor extends StepDescriptor {

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Set.of(Run.class, Launcher.class, EnvVars.class);
        }

        @Override
        public String getFunctionName() {
            return "createFirewallRule";
        }

        @Override
        @NonNull
        public String getDisplayName() {
            return "Create a firewall rule";
        }
    }

    @Override
    public StepExecution start(final StepContext context) {
        return new CreateFirewallRuleStepExecution(context, this);
    }

    static final class CreateFirewallRuleStepExecution extends SynchronousStepExecution<Void> {

        private static final long serialVersionUID = 1L;
        private final transient CreateFirewallRuleStep step;

        CreateFirewallRuleStepExecution(final StepContext context, final CreateFirewallRuleStep step) {
            super(context);
            this.step = step;
        }

        @Override
        protected Void run() throws Exception {
            final var context = getContext();

            final var action = step.getAction();
            final var allow = step.getAllow();

            if (action == null && allow == null) {
                throw new IllegalArgumentException("Either 'action' or 'allow' should be specified!");
            }

            final var launcher = context.get(Launcher.class);
            final var cmd = new ArgumentListBuilder();
            cmd.add("gcloud", "compute", "firewall-rules", "create", step.getName());

            if (action == null) {
                cmd.add("--allow=" + allow);
            } else {
                cmd.add("--action=" + action);
            }

            if (step.getDescription() != null) {
                cmd.add("--description=" + step.getDescription());
            }

            if (step.getDestinationRanges() != null) {
                cmd.add("--destination-ranges=" + step.getDestinationRanges());
            }

            if (step.getDirection() != null) {
                cmd.add("--direction=" + step.getDirection());
            }

            if (step.isDisabled()) {
                cmd.add("--disabled");
            }

            if (step.isEnableLogging()) {
                cmd.add("--enable-logging");
                if (step.getLoggingMetadata() != null) {
                    cmd.add("--logging-metadata=" + step.getLoggingMetadata());
                }
            }

            if (step.getNetwork() != null) {
                cmd.add("--network=" + step.getNetwork());
            }

            if (step.getPriority() >= 0 && step.getPriority() <= 65535) {
                cmd.add("--priority=" + step.getPriority());
            }

            if (step.getRules() != null) {
                cmd.add("--rules=" + step.getRules());
            }

            if (step.getSourceRanges() != null) {
                cmd.add("--source-ranges=" + step.getSourceRanges());
            }

            if (step.getSourceServiceAccounts() != null) {
                cmd.add("--source-service-accounts=" + step.getSourceServiceAccounts());
            }

            if (step.getSourceTags() != null) {
                cmd.add("--source-tags=" + step.getSourceTags());
            }

            if (step.getTargetServiceAccounts() != null) {
                cmd.add("--target-service-accounts=" + step.getTargetServiceAccounts());
            }

            if (step.getTargetTags() != null) {
                cmd.add("--target-tags=" + step.getTargetTags());
            }

            final var envVars = context.get(EnvVars.class);
            final var starter = launcher.launch().cmds(cmd).quiet(true);
            if (envVars != null) {
                starter.envs(envVars);
            }
            final var result = starter.join();

            if (result != 0) {
                throw new IllegalArgumentException("Failed to create a firewall rule with this command: " + cmd);
            }

            return null;
        }
    }
}
