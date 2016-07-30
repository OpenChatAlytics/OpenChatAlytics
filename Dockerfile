FROM maven:3.3.3-jdk-8

ENV CHATALYTICSDIR /opt/chatalytics
ENV DATABASEDIR /mnt/

RUN mkdir -p ${CHATALYTICSDIR}
RUN mkdir -p ${DATABASEDIR}

COPY . ${CHATALYTICSDIR}/code
WORKDIR ${CHATALYTICSDIR}/code

# Build ChatAlytics

RUN mvn clean package -Dmaven.test.skip=true

# Copy all the necessary things
RUN cp web/target/chatalytics-web-0.3-with-dependencies.jar ${CHATALYTICSDIR}
RUN cp compute/target/chatalytics-compute-0.3-with-dependencies.jar ${CHATALYTICSDIR}
RUN cp -r config ${CHATALYTICSDIR}
RUN cp -r bin ${CHATALYTICSDIR}
WORKDIR ${CHATALYTICSDIR}
RUN rm -rf code

# Run ChatAlytics
CMD ./bin/start-web-compute.sh chatalytics-local.yaml
