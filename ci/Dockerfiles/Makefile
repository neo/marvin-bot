BASE_VERSION=1.0.1
MARVIN_VERSION=0.1.1

.PHONY: build push base build-base push-base marvin build-marvin push-marvin

default: build push

build: build-base build-marvin
push: push-base push-marvin

base: build-base push-base

build-base: Dockerfile.base
	docker build -t pivotalsg/marvin-base:$(BASE_VERSION) -f Dockerfile.base .

push-base:
	docker push pivotalsg/marvin-base:$(BASE_VERSION)

marvin: build-marvin push-marvin

build-marvin: Dockerfile
	docker build -t pivotalsg/marvin:$(MARVIN_VERSION) -f Dockerfile ../../

push-marvin:
	docker push pivotalsg/marvin:$(MARVIN_VERSION)
