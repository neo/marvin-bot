#!/bin/sh -xe

export TERM=xterm
gosu postgres pg_ctl -D $PGDATA start


cd marvin-source

 ./gradlew test &&
    ./gradlew assemble &&
    cp -v build/libs/marvin-*.jar ../marvin-release/ &&
    cp -v manifest.yml ../marvin-release/ &&
    date "+%Y%m%d%H%M%S" > ../marvin-release/release-tag
