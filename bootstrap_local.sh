#!/bin/sh
read -s -p "Enter Artifactory Username: " artifactoryUsername
read -s -p "Enter Artifactory Password: " artifactoryPassword

echo "Create new projects"
oc new-project sampleapp
oc new-project sampleapp-qa
oc new-project sampleapp-prod

echo "Add docker secrets so that project can interact with docker registry"
oc secrets new-dockercfg ca-docker-reg --docker-server=isl-dsdc.ca.com:5000 --docker-username=$artifactoryUsername --docker-password=$artifactoryPassword --docker-email=kalni03@ca.com -n sampleapp
oc secrets new-dockercfg ca-docker-reg --docker-server=isl-dsdc.ca.com:5000 --docker-username=$artifactoryUsername --docker-password=$artifactoryPassword --docker-email=kalni03@ca.com -n sampleapp-prod

echo "Add necessary role bindong so that jenkins can run the build and deployment configs"
oc policy add-role-to-user edit system:serviceaccount:ci:default -n ci
oc policy add-role-to-user edit system:serviceaccount:ci:default -n sampleapp
oc policy add-role-to-user edit system:serviceaccount:ci:default -n sampleapp-qa
oc policy add-role-to-user edit system:serviceaccount:ci:default -n sampleapp-prod
oc policy add-role-to-user edit system:serviceaccount:sampleapp-qa:default -n sampleapp

echo "Create blank new app structure in the sample app qa and prod projects"
oc new-app -f template_service_app_v2.json -p OSE_PRIVATE_REG=`oc get svc docker-registry -n default -o jsonpath='{.spec.portalIP}'`:5000,APP_IMAGE_TAG=latest,APP_PROJECT_NAME=sampleapp -n sampleapp
oc new-app -f template_service_webapp_v2.json -p APP_SERVER_HOST=`oc get svc sample-app -o jsonpath='{.spec.portalIP}' -n sampleapp`,OSE_PRIVATE_REG=`oc get svc docker-registry -n default -o jsonpath='{.spec.portalIP}'`:5000,APP_IMAGE_TAG=latest,APP_PROJECT_NAME=sampleapp -n sampleapp

oc new-app -f template_service_app_qa_v2.json -p APP_IMAGE_TAG=qa -n sampleapp-qa
oc new-app -f template_service_webapp_qa_v2.json -p APP_SERVER_HOST=`oc get svc sample-app -o jsonpath='{.spec.portalIP}' -n sampleapp-qa`,APP_IMAGE_TAG=qa -n sampleapp-qa

oc new-app -f template_service_app_v2.json -p OSE_PRIVATE_REG=`oc get svc docker-registry -n default -o jsonpath='{.spec.portalIP}'`:5000,APP_IMAGE_TAG=prod,APP_PROJECT_NAME=sampleapp-prod -n sampleapp-prod
oc new-app -f template_service_webapp_v2.json -p APP_SERVER_HOST=`oc get svc sample-app -o jsonpath='{.spec.portalIP}' -n sampleapp-prod`,OSE_PRIVATE_REG=`oc get svc docker-registry -n default -o jsonpath='{.spec.portalIP}'`:5000,APP_IMAGE_TAG=prod,APP_PROJECT_NAME=sampleapp-prod -n sampleapp-prod
