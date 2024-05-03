.PHONY: chmod-gradlew format format-check test cover-report quality-check checkup gen-gpg-key clean-gpg-keys \
publish-github publish-space publish-maven publish clean

chmod-gradlew: # Give permission to execute gradlew.
	git update-index --chmod=+x gradlew

format: # Format code with spotless.
	chmod 777 -R scripts/ && ./scripts/format.sh

format-check: # 🔬 Check code format with spotless.
	chmod 777 -R scripts/ && ./scripts/format.sh

test: # 🩺 Run all tests.
	chmod 777 -R scripts/ && ./scripts/test.sh

cover-report: # Generate code coverage report.
	chmod 777 -R scripts/ && ./scripts/test.sh

quality-check: # 🔬 Check code quality with sonar.
	chmod 777 -R scripts/ && ./scripts/quality-check.sh

checkup: format test quality-check  # Code format, test and quality check.

gen-gpg-key: # Generate gpg key.
	chmod 777 -R scripts/ && ./scripts/gen-gpg-key.sh

clean-gpg-keys: # Clean all gpg keys.
	chmod 777 -R scripts/ && ./scripts/clean-gpg-keys.sh

publish-github-packages: checkup # 🚀 Publish to GitHub Packages.
	chmod 777 -R scripts/ && ./scripts/publish-github-packages.sh

publish-space-packages: checkup # 🚀 Publish to Space Packages.
	chmod 777 -R scripts/ && ./scripts/publish-space-packages.sh

publish-maven: checkup # 🚀 Publish to Maven.
	chmod 777 -R scripts/ && ./scripts/publish-maven.sh

publish: checkup # 🚀 Publish to Space Packages, GitHub Packages and Maven.
	chmod 777 -R scripts/ && ./scripts/publish-github.sh && ./scripts/publish-space.sh && ./scripts/publish-maven.sh

clean: # Clean all.
	chmod 777 -R scripts/ && ./scripts/clean.sh
