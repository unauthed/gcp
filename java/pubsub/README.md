# PubSub GDG Bristol Talk [![Build Status](https://travis-ci.com/unauthed/gdg-bristol.svg?branch=master)](https://travis-ci.com/unauthed/gdg-bristol)

## What you'll need

- Java v8 - [Oracle Download](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- Maven v3.5 - [Apache Download](https://maven.apache.org/)
- Google Cloud SDK v212 - [Google Download](https://cloud.google.com/sdk/)

## Set up Google Cloud PubSub environment

The GCP project ID is auto-configured from the `GOOGLE_CLOUD_PROJECT` environment variable.

You will need a topic and a subscription to send and receive messages from Google Cloud PubSub. You can create them in the `https://console.cloud.google.com/cloudpubsub` or use the *gcloud*  example below.

```
gcloud projects create gdg-bristol-pubsub-poc-1 --name="PubSub POC" --organization=[TODO] --folder=[TODO] --labels=gdg=bristol --set-as-default
gcloud info
export GOOGLE_CLOUD_PROJECT=`gcloud config list --format 'value(core.project)'`

gcloud services list
gcloud services enable appengine.googleapis.com pubsub.googleapis.com stackdriver.googleapis.com logging.googleapis.com
gcloud services enable storage-api.googleapis.com chat.googleapis.com
gcloud services list --enabled

gcloud pubsub topics create testTopic
gcloud pubsub subscriptions create testSubscription --topic testTopic
gcloud pubsub topics publish testTopic --message "Testing messaging"

gcloud pubsub subscriptions delete testSubscription
gcloud pubsub topics delete testTopic
```

## Authentication

Your application must be authenticated via the **GOOGLE_APPLICATION_CREDENTIALS** environment variable.

If you have the **Google Cloud SDK** installed and access to a web browser, you can log in with your user account using the *gcloud auth* command.

```
gcloud auth application-default login
gcloud auth list
```

---

## Build and Run

```
mvn clean package -DskipTests
mvn spring-boot:run -DskipTests

browse http://localhost:8080

curl -i -d "message=test1" http://localhost:8080/publishMessage
```

## Package and Deploy

```
mvn clean install

mvn package -Pdeb -DskipTests
mvn package -Prpm -DskipTests

mvn package -Pdocker -DskipTests
mvn docker:push -Pdocker -DskipTests

mvn appengine:deploy -Pgcp -DskipTests
gcloud app logs tail -s default
gcloud app describe
gcloud app browse
browse https://console.cloud.google.com/appengine
```

---

## Tips and Tricks

Or reminders for the show and tell, in case we are getting all nervous.

### gcloud working with multiple configurations

```
gcloud config configurations list
gcloud config set disable_usage_reporting true
gcloud config set disable_color false
gcloud config set app/stop_previous_version true
gcloud config configurations list

gcloud config configurations create my-project1
mkdir my-project1 && cd my-project1 && gcloud init
gcloud config list

gcloud config configurations create my-project2
mkdir my-project2 && cd my-project2 && gcloud init
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

### gcloud runtime configuration (beta)

```
gcloud beta runtime-config configs create pubsub_development
gcloud beta runtime-config configs list
gcloud beta runtime-config configs variables set pubsub.proxyEndpoint http://localhost:9090/echo --config-name pubsub_development
gcloud beta runtime-config configs variables list --config-name=pubsub_development
gcloud beta runtime-config configs variables describe pubsub.proxyEndpoint --config-name=pubsub_development

mvn spring-boot:run -DskipTests -Dspring-boot.run.profiles=development
curl -i -d "message=test3" http://localhost:8080/proxyMessage

gcloud beta runtime-config configs variables set pubsub.proxyEndpoint https://us-central1-gdg-bristol.cloudfunctions.net/pubsub-http-poc --config-name pubsub_development
curl -X POST http://localhost:8080/actuator/refresh
curl -i -d "message=test4" http://localhost:8080/proxyMessage
```

## gcloud builder

```
gcloud builds submit java/pubsub --config=./cloudbuild.yaml
```

### Spring Boot health and metrics endpoints

The standard Spring Boot actuator is enabled for this demonstration.

```
browse http://localhost:8080/actuator
browse http://localhost:8080/actuator/metrics/http.server.requests
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
