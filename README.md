# How to Run the Project

Follow the steps below to set up and run the project using Docker:

## Steps

1. **Navigate to the Deploy Directory**

   cd Deploy/



2. **Add the .env File into the Deploy directory.**

   Make sure the .env file is in the Deploy directory.



3. **Run Docker Compose**

   docker compose up -d --build



4. **Access Swagger UI**

   http://localhost:8082

   https://b2b.algorithmity.com


5. **Access Grafana UI**

   http://localhost:3333

   http://b2b.algorithmity.com:3333


6. **ElasticSearch**

   http://localhost:9200/_cluster/health?pretty

   http://localhost:9200/_cluster/stats?pretty

   http://localhost:9200/_stats?pretty


7. **Zipkin**

   http://localhost:9411/zipkin/


8. **Prometheus**

   http://localhost:9090/targets


9. **cAdvisor**

   http://localhost:8080/containers/