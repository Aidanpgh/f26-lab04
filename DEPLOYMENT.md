# Deployment Evidence

Fill this in as you go. Paste real output, not descriptions of output. A TA reads this
file with you at recitation.

## 1. Deployed URL and instance id

<!-- The ServiceUrl and InstanceId outputs. Paste both here every time
describe-stacks prints them, for the healthy deploy and for scenario 2. Both
change on every recreate, and you will need them for curls and sessions. -->

**Healthy deploy** (stack `lab04-service`):

```
InstanceId  i-075cb02dde5549693
ServiceUrl  http://ec2-54-146-198-231.compute-1.amazonaws.com:8080
```

**Scenario 2 deploy** (`params-scenario2.json`, PortOverride=9090):

```
InstanceId  i-0c4fbc0756b4539ab
ServiceUrl  http://ec2-54-208-41-71.compute-1.amazonaws.com:8080
```

**Healthy redeploy after the fix** (`params-healthy.json`):

```
InstanceId  i-0b0062613879ba9c3
ServiceUrl  http://ec2-3-91-29-75.compute-1.amazonaws.com:8080
```

## 2. External health check

Run the check from your own machine, not from the instance. Paste the command and the
response.

```
$ curl http://ec2-54-146-198-231.compute-1.amazonaws.com:8080/api/health
{"status":"ok"}
```

## 3. What the template created

Three or four sentences, your own words. What compute, what network access, and what
glue made the service start.

The template created one small virtual machine (a t3.micro EC2 instance running
Amazon Linux 2023) to run the service. It also created a security group, which is a
firewall that lets the internet reach the instance on port 8080 (the service) and port
22 (SSH), and blocks everything else inbound. The glue is a startup script (UserData)
that runs once when the instance boots: it installs Docker, pulls the course's
lab04-service image, and runs it with port 8080 forwarded from the host into the
container. The stack outputs the public URL and the instance id so we can curl the
service and open a shell on it.

## 4. Scenario 2 diagnosis

**The failing curl** (command and output):

```
curl http://ec2-54-160-135-197.compute-1.amazonaws.com:8080/api/health
curl: (28) Failed to connect to ec2-54-160-135-197.compute-1.amazonaws.com port 8080 after 75008 ms: Couldn't connect to server
```

**The log line that told you what was wrong:**

```
$ sudo docker ps            (PORTS column)
0.0.0.0:8080->8080/tcp, :::8080->8080/tcp

$ sudo docker logs lab04-service
lab04-service listening on 9090
```

**What was wrong, and the fix you applied:**

The app inside the container was listening on port 9090, but Docker was forwarding
host port 8080 to container port 8080, so requests to 8080 reached a port nobody was
listening on and the connection failed. `docker ps` shows the mapping is
`8080->8080`, and `docker logs` shows `listening on 9090` instead of the healthy
`listening on 8080`. The cause was `PortOverride=9090` in
`infra/params-scenario2.json`, which the template passes into the container as
`PORT=9090`. The fix was to delete the stack and create it again with
`infra/params-healthy.json`, where `PortOverride` is empty, so `PORT` falls back to
`ServicePort` (8080) and matches the forwarding.

**The healthy curl after the fix:**

```
$ curl http://ec2-3-91-29-75.compute-1.amazonaws.com:8080/api/health
{"status":"ok"}
```

## 5. Teardown proof

Paste the delete output, or describe the console evidence that the resources are gone.

```
$ aws cloudformation delete-stack --stack-name lab04-service
$ aws cloudformation wait stack-delete-complete --stack-name lab04-service
$ aws cloudformation describe-stacks --stack-name lab04-service

An error occurred (ValidationError) when calling the DescribeStacks operation: Stack with id lab04-service does not exist
```
