ARG MAVEN_BASE=maven:3.9.11-eclipse-temurin-17
ARG JENKINS_BASE=jenkins/jenkins:lts-jdk21
FROM ${MAVEN_BASE} AS ferramentas
FROM ${JENKINS_BASE}
USER root
COPY --from=ferramentas /opt/java/openjdk /opt/java/jdk17
COPY --from=ferramentas /usr/share/maven /usr/share/maven
RUN apt-get update && apt-get install -y --no-install-recommends python3 python3-venv && rm -rf /var/lib/apt/lists/*
ENV PATH="/usr/share/maven/bin:${PATH}"
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
USER jenkins
RUN jenkins-plugin-cli --latest false --plugin-file /usr/share/jenkins/ref/plugins.txt
COPY init.groovy.d/ /usr/share/jenkins/ref/init.groovy.d/
