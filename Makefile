.PHONY: chmod-gradlew publish-github publish-space publish-maven format format-check clean

chmod-gradlew:
	git update-index --chmod=+x gradlew

publish-github: # Publish to Github Packages
	chmod 777 -R scripts/ && ./scripts/publish-github.sh

publish-space: # Publish to Space Packages
	chmod 777 -R scripts/ && ./scripts/publish-space.sh


publish-maven: # Publish to Maven Central
	chmod 777 -R scripts/ && ./scripts/publish-maven.sh

publish: # Publish to Space Packages, Github Packages and Maven Central
	./scripts/publish.sh

format:
	gradlew clean spotlessApply --no-configuration-cache

format-check:
	gradlew clean spotlessCheck --no-configuration-cache

clean: # Clean all
	gradlew clean
