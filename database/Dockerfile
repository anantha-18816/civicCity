FROM postgis/postgis:16-3.4
COPY schema.sql /docker-entrypoint-initdb.d/01-schema.sql
COPY seed.sql /docker-entrypoint-initdb.d/02-seed.sql