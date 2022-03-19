FROM openjdk:8-jdk-slim
FROM python:3.7

LABEL maintainer=csu

RUN pip install -i https://pypi.doubanio.com/simple/ netCDF4==1.3.1
RUN pip install -i https://pypi.doubanio.com/simple/ numpy==1.21.5

COPY target/*.jar /app.jar

ENTRYPOINT ["java","-jar","/app.jar"]