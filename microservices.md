## Microservices Deployment

oc new-project cloudnativeapps

### Inventory

Database: 

oc project cloudnativeapps && \
oc new-app --name inventory-database -e POSTGRESQL_USER=inventory -e POSTGRESQL_PASSWORD=mysecretpassword -e POSTGRESQL_DATABASE=inventory registry.redhat.io/rhel9/postgresql-15

App:

mvn clean compile package -DskipTests  -P native  -f ./inventory-service

oc label deployment/inventory app.kubernetes.io/part-of=inventory --overwrite && \
oc label deployment/inventory-database app.kubernetes.io/part-of=inventory app.openshift.io/runtime=postgresql --overwrite && \
oc annotate deployment/inventory app.openshift.io/connects-to=inventory-database --overwrite && \
oc annotate deployment/inventory app.openshift.io/vcs-ref=ocp-4.14 --overwrite

### Catalog

Database:

oc new-app --name catalog-database -e POSTGRESQL_USER=catalog -e POSTGRESQL_PASSWORD=mysecretpassword -e POSTGRESQL_DATABASE=catalog registry.redhat.io/rhel9/postgresql-15

App:

mvn clean compile package -P native  -DskipTests -f ./catalog-service-quarkus

oc label deployment/catalog app.kubernetes.io/part-of=catalog app.openshift.io/runtime=quarkus --overwrite && \
oc label deployment/catalog-database app.kubernetes.io/part-of=catalog app.openshift.io/runtime=postgresql --overwrite && \
oc annotate deployment/catalog app.openshift.io/connects-to=inventory,catalog-database --overwrite && \
oc annotate deployment/catalog app.openshift.io/vcs-uri=https://github.com/RedHat-Middleware-Workshops/cloud-native-workshop-v2m4-labs.git --overwrite && \
oc annotate deployment/catalog app.openshift.io/vcs-ref=ocp-4.14 --overwrite

### Shopping Cart

Infinispan:

oc new-app quay.io/openshiftlabs/ccn-infinispan:12.0.0.Final-1 --name=datagrid-service -e USER=user -e PASS=pass

App:

mvn clean package -DskipTests  -P native  -f ./cart-service

oc label deployment/cart app.kubernetes.io/part-of=cart --overwrite && \
oc label deployment/datagrid-service app.kubernetes.io/part-of=cart --overwrite && \
oc annotate deployment/cart app.openshift.io/connects-to=datagrid-service --overwrite 

### Orders

Mongo db:

oc new-app -n cloudnativeapps  --docker-image quay.io/openshiftlabs/ccn-mongo:4.0 --name=order-database

App:

mvn clean package -DskipTests  -P native  -f ./order-service

oc label deployment/order app.kubernetes.io/part-of=orders --overwrite && \
oc label deployment/order-database app.kubernetes.io/part-of=orders  app.openshift.io/runtime=mongodb  --overwrite && \
oc annotate deployment/order app.openshift.io/connects-to=order-database --overwrite 

### Web-ui

cd ./coolstore-ui && npm install --save-dev nodeshift

npm run nodeshift && oc expose svc/coolstore-ui && \
oc label dc/coolstore-ui app.kubernetes.io/part-of=coolstore --overwrite && \
oc annotate dc/coolstore-ui app.openshift.io/connects-to=cart,catalog,inventory,order --overwrite && \
oc annotate dc/coolstore-ui app.openshift.io/vcs-uri=https://github.com/RedHat-Middleware-Workshops/cloud-native-workshop-v2m4-labs.git --overwrite && \
oc annotate dc/coolstore-ui app.openshift.io/vcs-ref=ocp-4.14 --overwrite