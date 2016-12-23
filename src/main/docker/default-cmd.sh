#!/bin/sh
set -eo pipefail

DEFAULT_ADDRESS="localhost:3306"
DEFAULT_name="faf_test"
DEFAULT_USERNAME="root"
DEFAULT_PASSWORD="banana"

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

"
    echo "Aborting"
    exit 1
fi

exec "$@"
