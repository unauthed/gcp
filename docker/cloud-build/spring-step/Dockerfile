FROM gcr.io/cloud-builders/gcloud

LABEL Description="This image is used to build Spring Boot artifacts with the Google Maven App Engine plugin."
LABEL Maintainer="GDG Bristol <gdg@unauthed.io>"
LABEL GDG="Bristol"

ARG MAVEN_VERSION=3.5.4
ARG USER_HOME_DIR="/root"
ARG SHA256=ce50b1c91364cb77efe3776f756a6d92b76d9038b0a0782f7d53acf1e997a14d
ARG BASE_URL=https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries

RUN apt-get update -qqy && apt-get install -qqy rpm curl default-jdk \
  && mkdir -p /usr/share/maven /usr/share/maven/ref \
  && curl -fsSL -o /tmp/apache-maven.tar.gz ${BASE_URL}/apache-maven-$MAVEN_VERSION-bin.tar.gz \
  && echo "${SHA256}  /tmp/apache-maven.tar.gz" | sha256sum -c - \
  && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
  && rm -f /tmp/apache-maven.tar.gz \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn \
  # clean up build packages
  && apt-get remove -qqy --purge curl \
  && rm /var/lib/apt/lists/*_*

ENV M2_HOME /usr/share/maven

# transitively resolve all maven dependencies
ADD mvn-deps.txt /builder/mvn-deps.txt
ADD resolve-mvn-deps.sh /builder/resolve-mvn-deps.sh

RUN /builder/resolve-mvn-deps.sh

ENTRYPOINT ["mvn"]

