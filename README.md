# Pipeline: GCP Steps

[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/pipeline-gcp-plugin/master)](https://ci.jenkins.io/job/Plugins/job/pipeline-gcp-plugin/job/master/)

## Introduction

This plugin adds Jenkins pipeline steps to interact with the GCP API.

## Getting started

The plugin assumes that you have a GCP account and a project.
You will need to create a service account, download the JSON key file locally and upload it to your Jenkins as Secret file.
The service account will need to have the necessary permissions to interact with the GCP services you want to use.

## Features

* [withGCP](#withGCP)
* [computeFirewallRulesCreate](#computeFirewallRulesCreate)
* [computeFirewallRulesDelete](#computeFirewallRulesDelete)
* [computeFirewallRulesList](#computeFirewallRulesList)
* _more features to come..._

### withGCP
This step will load the credentials file by the id and set the environment variables for the gcloud command to use.
In particular, it will set the `GOOGLE_APPLICATION_CREDENTIALS` environment variable to the path of the credentials file.
And will also attempt to extract the `project_id` from the file and set it as `CLOUDSDK_CORE_PROJECT` environment variable.
```groovy
withGCP(credentialsId: "credentials-id") {
    // run gcloud commands here
}
```

You can also combine other steps with it:
```groovy
withGCP(credentialsId: "credentials-id") {
    computeFirewallRulesCreate(name: "firewallRuleName", allow: "tcp:22")
}
```

### computeFirewallRulesCreate
This step will create a firewall rule with the given configuration.
Please refer to the [CLI command documentation](https://cloud.google.com/sdk/gcloud/reference/compute/firewall-rules/create) for more information on the parameters.

Either `allow` or `action` must be provided:
```groovy
computeFirewallRulesCreate(name: "firewallRuleName", allow: "tcp:22")
```
or
```groovy
computeFirewallRulesCreate(name: "firewallRuleName", action: "DENY", rules: "tcp:22")
```

### computeFirewallRulesDelete
This step will delete firewall rules with the given names.
Names should be separated by a whitespace.
Please refer to the [CLI command documentation](https://cloud.google.com/sdk/gcloud/reference/compute/firewall-rules/delete) for more information.

```groovy
computeFirewallRulesDelete(name: "firewallRuleName anotherFirewallRuleName")
```

### computeFirewallRulesList
This step will list firewall rules.
Please refer to the [CLI command documentation](https://cloud.google.com/sdk/gcloud/reference/compute/firewall-rules/list) for more information.

To print the list of firewall rules:
```groovy
computeFirewallRulesList()
```

To print the list of firewall rules in table format:
```groovy
computeFirewallRulesList(format: "table(name)")
```

To print the list of firewall rules applying a filter:
```groovy
computeFirewallRulesList(filter: "name~'^default-.*' AND network=default")
```

To store the result in a variable for further processing:
```groovy
def json = computeFirewallRulesList(format: "json")
def jqOutput = sh(script: "echo '${json}' | jq -r .[].id", returnStdout: true).trim()
echo "Filtered Output: ${jqOutput}"

``` 
To suppress the console output:
```groovy
def firewallRules = computeFirewallRulesList(printOutput: false)
echo "Firewall Rules: ${firewallRules}"
``` 

## Contributing

Refer to our [contribution guidelines](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md)

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)

