chmod-gradlew:
	git update-index --chmod=+x gradlew

publish-space: # Publish to Space Packages
	chmod 777 -R scripts/ && cd ./scripts && ./publish-space.sh

publish-github: # Publish to Github Packages
	chmod 777 -R scripts/ && cd ./scripts && ./publish-github.sh

publish-maven: # Publish to Maven Central
	chmod 777 -R scripts/ && cd ./scripts && ./publish-maven.sh

publish: # Publish to Space Packages, Github Packages and Maven Central
	cd ./scripts && ./publish.sh

format:
	gradlew spotlessApply --no-configuration-cache

format-check:
	gradlew spotlessCheck --no-configuration-cache

clean: # Clean all
	gradlew clean
