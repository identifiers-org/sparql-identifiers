# This Makefile helps on building the production container image

container_name = identifiersorg/sparql-identifiers
tag_version = $(shell cat VERSION)

release: set_next_development_version
	@echo "<===|DEVOPS|===> [RELEASE] Software Release, preparing next development version"
	@git add pom.xml
	@git commit -am "Next project development version prepared"
	@git push

sync_project_version:
	@echo "<===|DEVOPS|===> [SYNC] Synchronizing project version to version '${tag_version}'"
	@mvn versions:set -DnewVersion=${tag_version} -DgenerateBackupPoms=false

set_next_development_version:
	@echo "<===|DEVOPS|===> [SYNC] Setting the new development version, current ${tag_version}"
	@mvn versions:set -DnewVersion=$(shell ./increment_version.sh -p ${tag_version})-SNAPSHOT

container_production_build:
	@echo "<===|DEVOPS|===> [BUILD] Production container $(container_name):$(tag_version)"
	@docker build -t $(container_name):$(tag_version) -t $(container_name):latest .

container_production_push: container_production_build
	@echo "<===|DEVOPS|===> [PUBLISH]> Production container $(container_name):$(tag_version)"
	@docker push $(container_name):$(tag_version)
	@docker push $(container_name):latest

dev_container_build: clean container_production_build
	@echo "<===|DEVOPS|===> [DEV] Preparing local container"

clean:
	@echo "<===|DEVOPS|===> [CLEAN] Cleaning the space"
	@mvn clean > /dev/null

.PHONY: all clean app_structure container_production_build container_production_push dev_container_build deploy release sync_project_version set_next_development_version
