#!/bin/sh -xe

export TERM=xterm
gosu postgres pg_ctl -D $PGDATA start


cd marvin-source

 ./gradlew test &&
    ./gradlew assemble &&
    cp -v build/libs/marvin-*.jar ../marvin-release/ &&
    env &&
    echo ${BUILD_ID}_${BUILD_NAME}_${BUILD_JOB_NAME} > ../marvin-release/release-tag
