publish-space: # Publish to Space Packages
	chmod 777 -R scripts/ && cd ./scripts && ./publish-space.sh
publish-github: # Publish to Github Packages
	chmod 777 -R scripts/ && cd ./scripts && ./publish-github.sh
publish-maven: # Publish to Maven Central
	chmod 777 -R scripts/ && cd ./scripts && ./publish-maven.sh
publish: # Publish to Space Packages, Github Packages and Maven Central
	cd ./scripts && ./publish.sh
clean: # Clean all
	./gradlew clean
