FROM ubuntu:16.04
MAINTAINER egorkurakov <egor@live.ru>

RUN apt update &&\
    apt-get install -y postgresql git

RUN mkdir -p ./postgresql &&\
    chown -R postgres:postgres ./postgresql

USER postgres

#JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/park_db;=;=;PGDATABASE=park_db

RUN service postgresql start &&\
    bash -l -c "echo \$(psql -tc 'SHOW config_file') > ./postgresql/pgconfig.env" &&\
    psql -c "CREATE ROLE park_user WITH SUPERUSER LOGIN ENCRYPTED PASSWORD 'park_user_admin_pass';" &&\
    psql -c "CREATE DATABASE park_db;" &&\
    psql -c "GRANT ALL ON DATABASE park_db TO park_user;" &&\
    service postgresql stop

USER root

RUN apt-get install -y openjdk-8-jdk-headless
RUN apt-get install -y maven

RUN cat ./config/postgres.conf >> $(cat ./postgresql/pgconfig.env) &&\
    chown postgres:postgres $(cat ./postgresql/pgconfig.env)

ENV PGUSER=park_user PGPASSWORD=park_user_admin_pass PGHOST=127.0.0.1 PGPORT=5432 PGDATABASE=park_db
ENV PARK_DB_ROOT=/var/www/park_db

EXPOSE 5432
VOLUME  ["/etc/postgresql", "/var/log/postgresql", "/var/lib/postgresql"]

RUN mkdir -p $PARK_DB_ROOT
COPY . $PARK_DB_ROOT
WORKDIR $PARK_DB_ROOT

RUN mvn package

EXPOSE 5000

CMD service postgresql start && java -jar $PARK_DB_ROOT/target/DB-1.1-SNAPSHOT.jar