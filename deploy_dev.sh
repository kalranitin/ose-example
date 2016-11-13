#!/bin/sh
oc login --insecure-skip-tls-verify=true -u nitin -p nitin
docker login -u nitin -p `oc whoami -t` `oc get svc docker-registry -n default -o jsonpath='{.spec.portalIP}'`:5000

docker pull isl-dsdc.ca.com:5001/saas-devops/ose-example/sample-app:latest
docker pull isl-dsdc.ca.com:5001/saas-devops/ose-example/sample-webapp:latest

docker tag isl-dsdc.ca.com:5001/saas-devops/ose-example/sample-app:latest `oc get svc docker-registry -n default -o jsonpath='{.spec.portalIP}'`:5000/sampleapp/sample-app:latest
docker tag isl-dsdc.ca.com:5001/saas-devops/ose-example/sample-webapp:latest `oc get svc docker-registry -n default -o jsonpath='{.spec.portalIP}'`:5000/sampleapp/sample-webapp:latest

docker push `oc get svc docker-registry -n default -o jsonpath='{.spec.portalIP}'`:5000/sampleapp/sample-app:latest
docker push `oc get svc docker-registry -n default -o jsonpath='{.spec.portalIP}'`:5000/sampleapp/sample-webapp:latest

docker rmi `oc get svc docker-registry -n default -o jsonpath='{.spec.portalIP}'`:5000/sampleapp/sample-app:latest
docker rmi `oc get svc docker-registry -n default -o jsonpath='{.spec.portalIP}'`:5000/sampleapp/sample-webapp:latest
docker rmi isl-dsdc.ca.com:5001/saas-devops/ose-example/sample-app:latest
docker rmi isl-dsdc.ca.com:5001/saas-devops/ose-example/sample-webapp:latest