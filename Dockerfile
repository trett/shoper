FROM adoptopenjdk/openjdk11:alpine-slim
ADD target/universal/shoper.tgz /opt/app
WORKDIR /opt/app
RUN chmod +x /opt/app/shoper/bin/shoper
RUN apk add --no-cache bash
CMD ["/opt/app/shoper/bin/shoper"]