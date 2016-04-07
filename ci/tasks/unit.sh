#!/bin/sh -xe

export TERM=vt100
gosu postgres pg_ctl -D $PGDATA start


cd marvin-source

./gradlew test
