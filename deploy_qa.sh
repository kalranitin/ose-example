#!/bin/sh

docker login -u nitin -e nitin.kalra@ca.com -p `oc whoami -t` `oc get svc docker-registry -n default -o jsonpath='{.spec.portalIP}'`:5000
docker pull isl-dsdc.ca.com:5001/saas-devops/ose-example/sample-app:latest
docker tag isl-dsdc.ca.com:5001/saas-devops/ose-example/sample-app:latest `oc get svc docker-registry -n default -o jsonpath='{.spec.portalIP}'`:5000/sampleapp/sample-app:latest
docker push `oc get svc docker-registry -n default -o jsonpath='{.spec.portalIP}'`:5000/sampleapp/sample-app:latest