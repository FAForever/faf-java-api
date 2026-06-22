#!/usr/bin/env sh
# Build the current branch into a Docker image and push it as a tag.
# Refuses to run on develop/main/master or in a detached HEAD state.
#
# Builds for linux/amd64 (x86_64) by default so the image runs on the real
# servers even when building from an arm64 machine (Apple Silicon).
#
# The Dockerfile expects a prebuilt jar in build/libs/, so we run the Gradle
# bootJar task first (skip with SKIP_BUILD=1 if you already built it).
#
# Usage: ./push-branch-image.sh
# Env:   IMAGE       override the destination repo (default: faforever/faf-java-api)
#        PLATFORM    override the target platform (default: linux/amd64)
#        SKIP_BUILD  set to 1 to reuse an existing build/libs jar
set -eu

IMAGE="${IMAGE:-faforever/faf-java-api}"
PLATFORM="${PLATFORM:-linux/amd64}"

branch=$(git rev-parse --abbrev-ref HEAD)

case "$branch" in
  develop|main|master|HEAD)
    echo "Refusing to push '$branch' as a Docker tag." >&2
    exit 1
    ;;
esac

# Docker tags must match [A-Za-z0-9_][A-Za-z0-9_.-]{0,127}.
# Replace anything else (most commonly '/' from branch prefixes) with '-'.
tag=$(printf '%s' "$branch" | sed 's/[^A-Za-z0-9_.-]/-/g')

if [ "${SKIP_BUILD:-0}" != "1" ]; then
  echo "Building jar with ./gradlew bootJar"
  ./gradlew bootJar -x test
fi

echo "Building $IMAGE:$tag from branch $branch for $PLATFORM"
# buildx with --push builds and pushes in one step. --provenance=false keeps
# the registry tag pointing at a plain image manifest rather than a manifest
# list, which some deployment tooling expects.
docker buildx build \
  --platform "$PLATFORM" \
  --provenance=false \
  -t "$IMAGE:$tag" \
  --push \
  .

echo "Done: $IMAGE:$tag"
