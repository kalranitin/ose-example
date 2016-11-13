#!/bin/sh

oc delete all --all -n sampleapp
oc delete all --all -n sampleapp-qa
oc delete all --all -n sampleapp-prod
oc delete project sampleapp
oc delete project sampleapp-qa
oc delete project sampleapp-prod
