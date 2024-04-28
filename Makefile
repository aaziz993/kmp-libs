.PHONY: chmod-gradlew format format-check quality-check publish-github publish-space publish-maven clean

chmod-gradlew: # Give permission to execute gradlew
	git update-index --chmod=+x gradlew

format: # Format code with spotless
	chmod 777 -R scripts/ && ./scripts/format.sh

format-check: # Check code format with spotless
	chmod 777 -R scripts/ && ./scripts/format.sh

quality-check: # Check code quality with sonar
	chmod 777 -R scripts/ && ./scripts/quality-check.sh

publish-github: # Publish to Github Packages
	chmod 777 -R scripts/ && ./scripts/publish-github.sh

publish-space: # Publish to Space Packages
	chmod 777 -R scripts/ && ./scripts/publish-space.sh

publish-maven: # Publish to Maven Central
	chmod 777 -R scripts/ && ./scripts/publish-maven.sh

publish: # Publish to Space Packages, Github Packages and Maven Central
	./scripts/publish.sh

test:
	chmod 777 -R scripts/ && ./scripts/test.sh
clean: # Clean all
	chmod 777 -R scripts/ && ./scripts/clean.sh
