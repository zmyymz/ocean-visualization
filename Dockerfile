FROM openjdk:8-jdk-slim
FROM conda/miniconda3
FROM osgeo/gdal

LABEL maintainer=csu

RUN conda install netCDF4==1.3.1

COPY . .

COPY target/*.jar /app.jar

ENTRYPOINT ["java","-jar","/app.jar"]