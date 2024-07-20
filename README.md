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
* [createFirewallRule](#createFirewallRule)
* [deleteFirewallRule](#deleteFirewallRule)
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

### createFirewallRule
This step will create a firewall rule with the given configuration.

Please refer to the [CLI command documentation](https://cloud.google.com/sdk/gcloud/reference/compute/firewall-rules/create) for more information on the parameters.

Either `allow` or `action` must be provided:
```groovy
createFirewallRule(name: "firewallRuleName", allow: "tcp:22")
```
or
```groovy
createFirewallRule(name: "firewallRuleName", action: "DENY", rules: "tcp:22")
```

### deleteFirewallRule
This step will delete firewall rules with the given names.
Names should be separated by a whitespace.

Please refer to the [CLI command documentation](https://cloud.google.com/sdk/gcloud/reference/compute/firewall-rules/delete) for more information.

```groovy
deleteFirewallRule(name: "firewallRuleName anotherFirewallRuleName")
```

## Contributing

Refer to our [contribution guidelines](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md)

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)

