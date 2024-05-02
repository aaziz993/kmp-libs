#!/bin/bash

echo Spotless code format

./gradlew spotlessApply --no-configuration-cache
