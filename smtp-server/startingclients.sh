#!/usr/bin/env bash

# run "chmod u+x path/to/file/startingclients.sh" to make the script runnable

if (( $# != 1 ))
then
    >&2 echo "Usage: ./startingclients.sh <number_of_clients>"
else
  for ((i=1; i <= $1; i++))
  do
    java -jar SMTPClient.jar localhost 1234 &
  done
fi

