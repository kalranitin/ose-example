FROM kalranitin/maven:latest

MAINTAINER Nitin Kalra <kalni03@ca.com>

USER root
WORKDIR /tmp

ADD assignment/ assignment/
WORKDIR /tmp/assignment

RUN mvn clean && mvn package

RUN cp target/assignment-1.0.jar /opt/app.jar

WORKDIR /opt
RUN rm -rf /tmp/assignment

COPY assignment/entrypoint.sh entrypoint.sh

COPY assignment/filebeat.yml /etc/filebeat/filebeat.yml
COPY filebeat.repo /etc/yum.repos.d/filebeat.repo
RUN chown root:root /etc/filebeat/filebeat.yml && rpm --import https://packages.elastic.co/GPG-KEY-elasticsearch && yum -y install which initscripts filebeat

ENV PATH=/opt/:$PATH
ENTRYPOINT ["entrypoint.sh"]
# CMD ["java", "-jar", "app.jar"]
