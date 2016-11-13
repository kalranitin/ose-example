FROM kalranitin/maven:latest

MAINTAINER Nitin Kalra <kalni03@ca.com>

USER root
WORKDIR /tmp

ADD assignment-webapp/ assignment-webapp/
WORKDIR /tmp/assignment-webapp

RUN mvn clean && mvn package

RUN cp target/assignment-webapp-1.0.jar /opt/app.jar

WORKDIR /opt
RUN rm -rf /tmp/assignment-webapp

COPY assignment-webapp/entrypoint.sh entrypoint.sh

COPY assignment-webapp/filebeat.yml /etc/filebeat/filebeat.yml
COPY filebeat.repo /etc/yum.repos.d/filebeat.repo
RUN chown root:root /etc/filebeat/filebeat.yml && rpm --import https://packages.elastic.co/GPG-KEY-elasticsearch && yum -y install which initscripts filebeat

ENV PATH=/opt/:$PATH
ENTRYPOINT ["entrypoint.sh"]
# CMD ["java", "-jar", "app.jar"]
