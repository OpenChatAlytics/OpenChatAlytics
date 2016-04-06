# DOCKER-VERSION  1.3.2
# Dockerfile that sets up an image for running the compute and web server for ChatAlytics

FROM java:8

ENV CHATALYTICSDIR /opt/chatalytics
ENV DATABASEDIR /mnt/

RUN apt-get update
RUN DEBIAN_FRONTEND=noninteractive apt-get -y install software-properties-common
RUN apt-get install -y sudo && \
    apt-get install -y wget && \
    apt-get install -y vim && \
    apt-get --no-install-recommends install -y maven && \
    wget -q -O - https://deb.nodesource.com/setup | sudo bash - && \
    apt-get install -y git build-essential nodejs && \
    rm -rf /var/lib/apt/lists/* && \
    npm install -g coffee-script

RUN echo "#!/bin/sh\nexit 0" > /usr/sbin/policy-rc.d

RUN mkdir -p ${CHATALYTICSDIR}
RUN mkdir -p ${DATABASEDIR}

COPY . ${CHATALYTICSDIR}
WORKDIR ${CHATALYTICSDIR}

# Build ChatAlytics

RUN mvn clean package

RUN cp web/target/chatalytics-web-0.3-with-dependencies.jar ${CHATALYTICSDIR}
RUN cp compute/target/chatalytics-compute-0.3-with-dependencies.jar ${CHATALYTICSDIR}

# Run ChatAlytics
CMD ./bin/start-web-compute.sh
