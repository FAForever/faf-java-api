#!/bin/sh
set -eo pipefail

if [ -z "${DATABASE_ADDRESS}" ] \
      || [ -z "${DATABASE_NAME}" ] \
      || [ -z "${DATABASE_USERNAME}" ] \
      || [ -z "${DATABASE_PASSWORD}" ] \
      || [ -z "${JWT_SECRET}" ]; then
  echo "One of the following environment variables have not been set:

  DATABASE_ADDRESS
  DATABASE_NAME
  DATABASE_USERNAME
  DATABASE_PASSWORD
  JWT_SECRET

Please specify them before starting the container, preferably in a environment file.
"
    exit 1
fi

exec "$@"
