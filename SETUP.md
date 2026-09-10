# Setup

Java and Maven carry over from Labs 1 through 3. New here: Docker, the AWS CLI, and
credentials from the AWS Academy Learner Lab.

## 1. Java and Maven

- A JDK, version 21 or newer. Check with `java --version`.
- Maven 3.8 or newer. Check with `mvn --version`.

## 2. Docker

Install Docker Desktop from docker.com/products/docker-desktop (macOS, Windows) or
the Docker engine via your package manager (Linux). Ignore any instructions that
mention Docker Toolbox or docker-machine. Those are years stale. Start it, then
check:

```
docker info
```

If that prints server details, the daemon is running. If it errors, Docker is installed
but not started.

## 3. AWS CLI v2

Install AWS CLI version 2 from the official installer at
docs.aws.amazon.com/cli (macOS: `brew install awscli` also works). Then check:

```
aws --version
```

Version 1 will not do. It must report `aws-cli/2.x`.

Also install the **Session Manager plugin**, which milestone 2 uses to open a
shell on the instance. Installers for every OS are on the AWS docs page (search
"install the Session Manager plugin"). On macOS,
`brew install --cask session-manager-plugin` works. Check with
`session-manager-plugin` (it prints a success line). No plugin? The AWS console
offers the same shell with nothing installed (EC2 -> your instance -> Connect ->
Session Manager).

## 4. Learner Lab credentials

Start the lab from the AWS Academy Learner Lab course (AWS Academy runs its own
Canvas site, reached from the invitation email we sent you, not CMU's Canvas). Go to
**Modules**, then **Launch AWS Academy Learner Lab**. Agree to the Vocareum terms if
it asks (first time only), then click **Start Lab** at the top right. Starting can
take a few minutes. When the dot next to "AWS" turns green, the lab is
running. Clicking **AWS** (the label next to the dot) opens the AWS console in a
new tab. Keep it open for console views and evidence screenshots.

Click **AWS Details**, then **Show** next to "AWS CLI". You get a block with three
values: an access key id, a secret access key, and a session token.

Paste that block into `~/.aws/credentials` (on a fresh install that directory does
not exist yet, so `mkdir -p ~/.aws` first). It already comes formatted as a profile.
Do this yourself rather than handing the block to an agent. (This is good practice.) Anything you paste into
an agent conversation ends up in the transcript, and is a security risk. Then set your region:

```
aws configure set region us-east-1
```

Learner Lab sessions are short. When the session ends or expires, the credentials stop
working and you copy a fresh block from the same panel. Ending the lab also stops any
instance you left running (so does the template, after four hours). The stack stays,
and `delete-stack` still works on it, but the service is gone and its URL times out.
Do not debug a stopped instance. Delete the stack and create it again.

## 5. Credential smoke test

```
aws sts get-caller-identity
```

A JSON blob with an account number and an assumed-role ARN means you are authenticated.
`ExpiredToken` or `InvalidClientTokenId` means the block in `~/.aws/credentials` is stale.
Copy it again.

## 6. Continuous integration

CI is already configured. When you push, `.github/workflows/ci.yml` runs the service
tests and builds the container image. GitHub disables workflows on a fresh fork, so
enable them from the Actions tab if it asks.
