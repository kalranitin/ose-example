# Introduction
This example project is created to help bring your applications into OpenShift.

OpenShift 3 allows you to deploy your application to the cloud and the great thing is it doesn't matter if your cloud is public, private, or even hybrid. Typically, the PaaS platform (OpenShift in this case) will provide a set of predefined runtimes that a developer can use to deploy an application on top of. This developer does not need to worry about the infrastructure, the runtime setup, or the configuration, he/she would just need to focus on their application, and what runtime to use. The PaaS platform will take care of sewing it all together and running it.

As OpenShift 3 relies on containers, the runtimes will be base images that provide the underlying foundation to deploy an application (provided by the user) on top of. The containers also need to be highly configurable so there is no need to provide a single image for every use case. Instead, a different configuration provided to the image will make the runtime work as desired.

To understand detailed level architecture components please read the documentation provided [here](https://docs.openshift.com/enterprise/3.1/architecture/core_concepts/index.html).


## What is a template
The official OpenShift 3 [documentation](https://docs.openshift.com/enterprise/3.1/architecture/core_concepts/templates.html_) states:

```
A template describes a set of objects that can be parameterized and processed to produce a list of objects for creation by OpenShift. The objects to create can include anything that users have permission to create within a project, for example services, build configurations, and deployment configurations. A template may also define a set of labels to apply to every object defined in the template.
```

This means that typically in a template we will have:
* A set of resources that will be created as part of "creating/deploying" the template
* A set of values for the parameters defined in the template
* A set of labels to describe the generated resources 

A template will be defined in JSON or YAML format, and will be loaded into OpenShift for user instantiation, also known as application creation.

The templates can have global visibility scope (visible for every OpenShift project) or project visibility scope (visible only for a specific project).

### Benefits of using templates
A template provides developers with an easy way to create all the necessary OpenShift resources for their application to work. This allows a developer to quickly deploy an application without having to understand all of the internals of the OpenShift 3 platform.
* As a PaaS provider you have better control on what is being created and can make better usage of your resources.
* As a PaaS provider you can define different Service Level Agreements in templates, defining the amount of host resources (cpu, memory) each and every container can consume.


## Our Sample Application
For this example we are going spin up an Openshift environment in a "Vagrant" box and will be compiling/building (create Docker images) and deploying the sample application
using certain templates. Once we are satisfied with the deployment and have verified the application, we would replicate only the deployment procedure
again using templates where in the main difference would be that the porduction deployment would not do the build but it would refer to the image created earlier! Hence shocasing the entire lifecycle of CI and CD process.

The architecture of the sample application is as follows:
* An embedded jetty server based application tier (written in Java and a pom project) running on port 8081
* An embedded jetty server based we application tier (written in Java and Angular JS) runing on port 8080. 
* There is also a proxy server running within the web tier which would proxy all api request to the app tier while serve the web pages from it's own resources.



## Design of our template
The first thing we will need to do is design the contents of our template. Each of the content specified below are known as __objects__ in the template. The best approach we've found so far is to think of a template as a set of objects of resources with the following structure (from bottom up):
* __Abstractions:__ Additional resources needed for our application, like networking, storage, security,…
* __OpenShift Images:__ Base images we will be using for our containers.
* __Builds:__ Generate an image from source code (application source or Dockerfile source).
* __Images:__ Images produced by the builds.
* __Deployments:__ What images will be deployed and how.


### __Abstractions__
This layer defines all of the additional resources needed for our application to run, like networking, storage, security etc

#### Service:
A [service](https://docs.openshift.com/enterprise/3.1/architecture/core_concepts/pods_and_services.html#services) serves as an internal load balancer. 
It identifies a set of replicated pods in order to proxy the connections it receives to them. 
Backing pods can be added to or removed from a service arbitrarily while the service remains consistently available, enabling anything that depends on the service to refer to it at a consistent internal address.

Services are assigned an IP address and port pair that, when accessed, proxy to an appropriate backing pod. A service uses a label selector to find all the containers running that provide a certain network service on a certain port.

#### Route:
An OpenShift [route](https://docs.openshift.com/enterprise/3.1/dev_guide/routes.html) exposes a service at a host name, like samplewebapp-dev.com, so that external clients can reach it by name

#### PersistentVolumeClaim:
You can make a request for storage resources using a [PersistentVolumeClaim](https://docs.openshift.com/enterprise/3.1/dev_guide/persistent_volumes.html) object; the claim is paired with a volume that generally matches your request.

#### ServiceAccount:

[Service accounts](https://docs.openshift.com/enterprise/3.1/dev_guide/service_accounts.html) provide a flexible way to control API access without sharing a regular user’s credentials.

#### Secret:
A [secret](https://docs.openshift.com/enterprise/3.0/dev_guide/secrets.html) provides a mechanism to hold sensitive information such as passwords, OpenShift client config files, dockercfg files, etc. Secrets decouple sensitive content from the pods that use it and can be mounted into containers using a volume plug-in or used by the system to perform actions on behalf of a pod.


In our example, we will need a set of services abstracting the deployments. 

```
      {
         "kind":"Service",
         "apiVersion":"v1",
         "metadata":{
            "name":"${APP_SERVICE_NAME}", 		(1)
            "creationTimestamp":null
         },
         "spec":{
            "ports":[
               {
                  "name":"sample-webapp-http",
                  "protocol":"TCP",
                  "port":5432,					(2)
                  "targetPort":8080,			(3)
                  "nodePort":0
               }
            ],
            "selector":{						(4)
               "name":"${APP_SERVICE_NAME}"
            },
            "portalIP":"",
            "type":"ClusterIP",
            "sessionAffinity":"None"
         },
         "status":{
            "loadBalancer":{

            }
         }
      }
```
1. Name of the service. In this case it is coming in from a parameter defined in the template.
2. Port where the service will be listening
3. Port in the pod to route the network traffic to
4. Label selector for determining which pods will be target for this service.

Also, we want our application to be publicly available, so we expose the service providing HTTP access to the frontend component of the application as a route:
```
      {
         "kind":"Route",
         "apiVersion":"v1",
         "metadata":{
            "name":"route-edge",				(1)
            "creationTimestamp":null
         },
         "spec":{
            "host":"samplewebapp-dev.com",		(2)
            "to":{								(3)
               "kind":"Service",
               "name":"${APP_SERVICE_NAME}"
            },
            "port":{
               "targetPort":"sample-webapp-http"
            }
         },
         "status":{

         }
      }
```
1. Name of the route
2. DNS name used to access our application. This DNS name needs to resolve to the ip address of the [OpenShift router](https://docs.openshift.com/enterprise/3.1/architecture/core_concepts/routes.html#routers).
3. Defines that this is a route to a service with the specified name.



### __OpenShift Images__
In this first layer, we will need to define all the “base” images we will be using for our containers. These images typically will not be part of the template, but they need to be identified.

#### ImageStream
An image stream presents a single virtual view of related images, as it may contain images from:
* Its own image repository in OpenShift’s integrated Docker Registry
* Other image streams
* Docker image repositories from external registries.
OpenShift stores complete metadata about each image (e.g., command, entrypoint, environment variables, etc.). Images in OpenShift are immutable.

#### ImageStreamImage
An ImageStreamImage is used to reference or retrieve an image for a given image stream and image name. It uses the following convention for its name: \<image stream name\>@\<name\>

#### ImageStreamTag
An ImageStreamTag is used to reference or retrieve an image for a given image stream and tag. It uses the following convention for its name: \<image stream name\>:\<tag\>


In our sample application, we will be using 2 base images:
```
      {
         "kind":"ImageStream",
         "apiVersion":"v1",
         "metadata":{
            "name":"kalranitin-java",			(1)
            "creationTimestamp":null
         },
         "spec":{								(2)
            "dockerImageRepository":"kalranitin/java"
         },
         "status":{
            "dockerImageRepository":""			(3)
         }
      },
      {
         "kind":"ImageStream",
         "apiVersion":"v1",
         "metadata":{
            "name":"sample-webapp",
            "creationTimestamp":null
         },
         "spec":{

         },
         "status":{
            "dockerImageRepository":""
         }
      }
```
1. Name of the ImageStream. This ImageStream will be created in the current project.
2. ImageStream Specifications
3. Docker Repository backing this image stream.

### __Builds__
This layer defines all the builds we will require for our application. A build is the process of transforming input parameters into a resulting object. Most often, the process is used to transform source code into a runnable image.

#### BuildConfig
A [BuildConfig](https://docs.openshift.com/enterprise/3.1/dev_guide/builds.html#defining-a-buildconfig) object is the definition of the entire build process.

A build configuration consists of the following key parts:
* A source description (Where is your source code?)
* A strategy for building (How to build your image?)
	* _Source-To-Image_: Transform your application into a runnable docker image, using a S2I image for building and running your application.
	* _Docker_: Your Dockerfile will be built into an image. This image will contain both, the runtime and the application already built.
	* _Custom_: You provide the building method in a Docker image.
* An output description (Where to place the built image?)
* A list of triggers (When and Why will the source be built?)

In most of the cases we would be using the "_Docker_" strategy where in the Dockerfile would specify on how to build the image. 
As a result of the build process, for every build OpenShift will create a new version of the image, that we will always be tagged as latest. 
For our example here is the config:

```
      {
         "kind":"BuildConfig",
         "apiVersion":"v1",
         "metadata":{
            "name":"sample-webappbuild",			(1)
            "creationTimestamp":null,
            "labels":{
               "name":"sample-webappbuild"			(2)
            }
         },
         "spec":{
            "triggers":[
               {
                  "type":"github",					(3)
                  "github":{
                     "secret":"secret101"
                  }
               },
               {
                  "type":"generic",					(4)
                  "generic":{
                     "secret":"secret101"
                  }
               },
               {
                  "type":"imageChange",				(5)
                  "imageChange":{

                  }
               },
               {
                  "type":"ConfigChange"				(6)
               }
            ],
            "source":{								(7-3)
               "type":"Git",						(8-4)
               "git":{
                  "uri":"git@github-isl-01.ca.com:kalni03/ose-example.git",	(9-5)
				  "ref": "master"					(10-6)
               },
			   "contextDir":"",						(11-7)
               "sourceSecret":{
                  "name":"scmsecret"
               }
            },
            "strategy":{							(12-8)
               "type":"Docker",						(13-9)
               "dockerStrategy":{
                  "dockerfilePath":"assignment-webapp/Dockerfile",	(14)
                  "env":[							(15)
                     {
                        "name":"EXAMPLE",
                        "value":"sample-webapp"
                     }
                  ]
               }
            },
            "output":{								(16-11)
               "to":{
                  "kind":"ImageStreamTag",
                  "name":"sample-webapp:latest"
               }
            },
            "resources":{

            }
         },
         "status":{
            "lastVersion":0
         }
      }
```

1. This is the name that will identify this BuildConfig
2. These are the labels that will be set for this BuildConfig
3. This defines that a change generated via a GitHub webhook trigger (if the source code is changed) will trigger a build.
4. This defines that a change generated via a Generic webhook trigger will trigger a build.
5. This defines that an Image Change will trigger a build. This will trigger a build if the builder image changes or is updated
6. This defines that any Configuration change would trigger the build.
7. This section defines where is the source for the build.
8. It defines it is source located in a Git repository.
9. In this URI.
10. And using this tag/branch. This value is optional and defaults to “master” if not provided.
11. And this subdirectory from the repository. This value is optional and defaults to the root directory of the repository.
12. This defines which build strategy to use.
13. Source=Docker.
14. The Dockerfile path within the project. If left blank it would take the root path.
15. Environment variables needed to build the image.
16. Defines where to leave the generated image if the build succeeds. It is placing it in our current project.



### __Deployments__
This layer defines the core of our applications. It defines what will be running in OpenShift.

#### DeploymentConfig
A [DeploymentConfig](https://docs.openshift.com/enterprise/3.1/architecture/core_concepts/deployments.html#deployments-and-deployment-configurations) is a definition of what will be deployed and running on OpenShift.

A deployment configuration consists of the following key parts:
* A replication controller template which describes the application to be deployed. (What will be deployed?)
* The default replica count for the deployment. (How many instances will be deployed and running?)
* A deployment strategy which will be used to execute the deployment. (How it will be deployed?)
* A set of triggers which cause deployments to be created automatically. (When and Why will it be deployed?)

```
      {
         "kind":"DeploymentConfig",
         "apiVersion":"v1",
         "metadata":{
            "name":"sample-webapp-deployment",			(1)
            "creationTimestamp":null
         },
         "spec":{										(2)
            "strategy":{
               "type":"Rolling",						(3)
               "rollingParams":{
                  "updatePeriodSeconds":1,
                  "intervalSeconds":1,
                  "timeoutSeconds":120
               },
               "resources":{

               }
            },
            "triggers":[								(4)
               {
                  "type":"ImageChange",					(5)
                  "imageChangeParams":{
                     "automatic":true,
                     "containerNames":[
                        "sample-webapp-container"
                     ],
                     "from":{
                        "kind":"ImageStreamTag",
                        "namespace":"sampleapp",
                        "name":"sample-webapp:promote"
                     },
                     "lastTriggeredImage":""
                  }
               },
               {
                  "type":"ConfigChange"					(6)
               }
            ],
            "replicas":1,								(7)
            "selector":{
               "name":"${APP_SERVICE_NAME}"				(8)
            },
            "template":{								(9)
               "metadata":{
                  "creationTimestamp":null,
                  "labels":{							(10)
                     "name":"${APP_SERVICE_NAME}"		(11)
                  }
               },
               "spec":{									(12)
                  "containers":[
                     {
                        "name":"sample-webapp-container",	(13)
                        "image":"172.30.118.161:5000/sampleapp/sample-webapp:promote",	(14)
                        "ports":[							(15)
                           {
                              "containerPort":8080,
                              "protocol":"TCP"
                           }
                        ],
                        "env":[								(16)
                           {
                              "name":"SAMPLE_APP_VERSION",
                              "value":"${SAMPLE_APP_VERSION}"
                           },
                           {
                              "name":"APP_SERVER_HOST",
                              "value":"${APP_SERVER_HOST}"
                           },
                           {
                              "name":"APP_SERVER_PORT",
                              "value":"${APP_SERVER_PORT}"
                           }
                        ],
                        "resources":{

                        },
                        "terminationMessagePath":"/dev/termination-log",
                        "imagePullPolicy":"IfNotPresent",					(17)
                        "capabilities":{

                        },
                        "securityContext":{					(18)
                           "capabilities":{

                           },
                           "privileged":false
                        }
                     }
                  ],
                  "restartPolicy":"Always",
                  "dnsPolicy":"ClusterFirst",
                  "serviceAccount":""
               }
            }
         },
         "status":{

         }
      }
```

1. This is the name that will identify this DeploymentConfig
2. Specification for the DeploymentConfig. Everything inside this section describes the DeploymentConfig configuration
3. Strategy to use when deploying a new version of the application in case it is triggered. As defined in triggers
4. The triggers that will dictate on what conditions to create a new deployment. (Deploy a new version of the pod)
5. Create a new deployment when the latest image tag is updated.
6. Create a new deployment when there is a configuration change for this Resource.
7. Number of instances that should be created for this component/deployment.
8. This should be the same as name (1).
9. The template defines what will be deployed as part of this deployment (the pod)
10. The labels to apply for the resources contained in the template (pod)
11. Name of the pod. Every pod instance created will have this name as prefix.
12. Defines the configuration (contents) of the pod.
13. The name of the container.
14. The name of the image to use.
15. The ports that the container exposes
16. A set of environment variables to pass to this container
17. What should do when deploying. As we will be building the image, we need to always pull on new deployments. Note that if the image tag is latest, it will always pull the image by default, otherwise it will default to “IfNotPresent”.
18. SecurityContextContraint to use for the container

# How to get the environment up and running?

#### Pre-Requisuite
Make sure you have [Git](https://git-scm.com/downloads), [Virtualbox](https://www.virtualbox.org/wiki/Downloads) and [Vagrant](https://www.vagrantup.com/downloads.html) installed in your machine. 

The minimum spec needed would be atleast 6 gigs of ram and 10 gigs of free space.

You should have the access to the CA github [repo](https://github-isl-01.ca.com/kalni03/ose-example)

1. Clone the repository. 
   Go to the command prompt and run:

  ```
  git clone https://github-isl-01.ca.com/kalni03/ose-example.git
  ```
  
2. Bring up the Vagrant Image and SSH into it

  ```
  cd ose-example/
  vagrant up
  vagrant ssh
  ```

3. Generate a key pair

  ```
  ssh-keygen
  ```

4. Provide the public key to the repository owner (you can fork it as well and do it yourself if needed) as that needs to go into the deploy keys within git hub so that openshift can pull the source code.
5. You can login to the openshift console via the browser as well using the URL as https://https://10.2.2.2:8443 with login credentials as admin/admin
6. Create a new openshift dev project and provide the private key to the secrets.
  ```
  oc new-project sampleapp
  oc project sampleapp
  oc secrets new scmsecret ssh-privatekey=$HOME/.ssh/id_rsa
  oc secrets add serviceaccount/builder secrets/scmsecret
  ```

6. Navigate to https://github-isl-01.ca.com/kalni03/ose-example/blob/master/template_service_app_dev.json template page. If you have forked the repository then navigate accordingly.
7. Click on the "Raw" button located on the webpage. This would give the raw view of the template file. 
8. Copy the URL. It would be something line: https://github-isl-01.ca.com/kalni03/ose-example/blob/master/template_service_app_dev.json?token=AAAB2IdwWaBsG0lQBgxzsu7QwQ8-XAJkks5XBacrwA%3D%3D
9. Run the following command which would create the app server pod and a service.

  ```
  oc new-app -f \<**Copied URL at step 8**\>
  ```

  You can view the build logs by running:
  ```
  oc logs -f bc/sample-appbuild
  ```

10. Once the build is completed, get the clusterIP from the service which would be used as the input to spin up the web app.
  ```
  oc get svc

  It would give a result as:

  NAME         CLUSTER-IP       EXTERNAL-IP   PORT(S)    SELECTOR          AGE
  sample-app   __172.30.152.133__   <none>        5433/TCP   name=sample-app   1m
  ```

11. Copy the CLUSTER_IP value and follow steps 6,7,8 for the URL https://github-isl-01.ca.com/kalni03/ose-example/blob/master/template_service_webapp_dev.json
12. RUN the new app command and provide the CLUSTER IP in this case
  ```
  oc new-app -f template_service_webapp_dev.json -p APP_SERVER_PORT=\<__PORT FOR APP SERVER__\>,APP_SERVER_HOST=\<__CLUSTER IP OF APP SERVER__\>
  ```

13. Check the logs by running:
  ```
  oc logs -f bc/sample-webappbuild
  ```

14. Get the cluster IP for webapp as we may have to open a tunnel since this is running in a vagrant environment.
  ```
  oc get svc

  in a new terminal window (not inside vagrant) run
  vagrant ssh -- -L 9999:<__substitute with Web app CLUSTER IP__>:5432
  ```

15. Navigate to http://127.0.0.1:9999/


# To run the build again
oc start-build sample-appbuild

oc start-build sample-webappbuild

# Tag the image stream
oc project sampleapp

oc get is
oc describe is sample-app
oc tag --insecure 172.30.118.161:5000/sampleapp/sample-app sampleapp/sample-app:promote

oc describe is sample-webapp
oc tag --insecure 172.30.118.161:5000/sampleapp/sample-webapp sampleapp/sample-webapp:promote

# Run Production build
oc new-project sampleapp-prod
oc project sampleapp-prod
oc new-app -f template_service_prod.json
-- Wait for deployment to complete
oc get svc

oc new-app -f template_service_webapp_prod.json -p APP_SERVER_PORT=5435,APP_SERVER_HOST=172.30.82.82
oc get svc
vagrant ssh -- -L 9998:172.30.187.209:5434
http://127.0.0.1:9998/
oc rollback sample-app-deployment
oc rollback sample-webapp-deployment
