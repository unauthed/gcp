# PubSub GDG Bristol Talk

## What you'll need

- Java v8 - [Oracle Download](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- Maven v3.5 - [Apache Download](https://maven.apache.org/)
- Google Cloud SDK v212 - [Google Download](https://cloud.google.com/sdk/)

## Set up Google Cloud Pub/Sub environment

The GCP project ID is auto-configured from the `GOOGLE_CLOUD_PROJECT` environment variable.

You will need a topic and a subscription to send and receive messages from Google Cloud Pub/Sub. You can create them in the `https://console.cloud.google.com/cloudpubsub`.

## Authentication

Your application must be authenticated via the **GOOGLE_APPLICATION_CREDENTIALS** environment variable.

If you have the **Google Cloud SDK** installed, you can log in with your user account using the `gcloud auth application-default login` command.

---

## Build and Run

```
mvn clean package -DskipTests
mvn spring-boot:run -DskipTests

browse http://localhost:8080

curl -d "message=test1" http://localhost:8080/publishMessage
curl -d "message=test2" http://localhost:8080/proxyMessage
```

## Package and Deploy

```
mvn clean install

mvn package -Pdeb -DskipTests

mvn package -Pdocker -DskipTests
mvn docker:push -Pdocker -DskipTests

mvn appengine:deploy -Pgcp -DskipTests
gcloud app logs tail -s default
gcloud app describe
gcloud app browse
browse https://console.cloud.google.com/appengine

mvn clean install -Pdeb,docker,gcp
```

---

## Tips and Tricks

Or reminders for the show and tell, in case we are getting nervous.

### gcloud working with multiple configurations

```
gcloud config configurations list
gcloud config set disable_usage_reporting true
gcloud config set disable_color false
gcloud config set app/stop_previous_version true
gcloud config configurations list

gcloud config configurations create my-project1
gcloud init
gcloud config list

gcloud config configurations create my-project2
gcloud init
gcloud config list

gcloud config configurations activate my-project2
gcloud config configurations describe my-project2
gcloud config configurations activate my-project1
gcloud config configurations describe

gcloud config configurations list
gcloud config configurations delete my-project2
gcloud config configurations list
```

### gcloud working with ssh / scp

Every time you add or remove an instance, you must re-run `config-ssh`

```
gcloud compute config-ssh
grep -i google ~/.ssh/config

ssh my-instance.us-central1-a.my-project1
```

### Spring Boot health and metrics endpoints

The standard Spring Boot actuator is enabled for this demonstration.

```
browse http://localhost:9001/actuator
browse http://localhost:9001/actuator/metrics/http.server.requests
```

If there is time we can show how Spring Boot integrates with Prometheus, ZipKin and Grafana.

```
docker-compose up -d

mvn clean spring-boot:run -Ptrace
browse http://localhost:9090
browse http://localhost:9080
browse http://localhost:9411
browse http://localhost:3000

docker-compose down
```
