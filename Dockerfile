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
    wget -q -O - https://deb.nodesource.com/setup | sudo bash - && \
    apt-get install -y git build-essential nodejs && \
    rm -rf /var/lib/apt/lists/* && \
    npm install -g coffee-script

RUN echo "#!/bin/sh\nexit 0" > /usr/sbin/policy-rc.d

RUN mkdir -p ${CHATALYTICSDIR}
RUN mkdir -p ${DATABASEDIR}

# Copy jars and resources
COPY web/target/chatalytics-web-0.3-with-dependencies.jar ${CHATALYTICSDIR}
COPY compute/target/chatalytics-compute-0.3-with-dependencies.jar ${CHATALYTICSDIR}
COPY bin/start-web-compute.sh ${CHATALYTICSDIR}

# Copy configs
COPY config ${CHATALYTICSDIR}/config

WORKDIR ${CHATALYTICSDIR}

# Run ChatAlytics
CMD ./start-web-compute.sh
