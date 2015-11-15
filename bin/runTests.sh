#!/bin/bash

echo "Starting tests.."
mkdir testOutputs

PACKAGE_NAME=$1
TEST_ID=$2
NUM_TESTS=$3

number=0
echo "Starting testing $PACKAGE_NAME"
while [ $number -lt $NUM_TESTS ]; do
     monkeyrunner test/${PACKAGE_NAME}_test${TEST_ID}.py
     number=$((number + 1))
     echo "Test $number is completed"
     sleep 1
done

echo "Finished tests"
mkdir testOutputs/$1
adb pull /data/data/$1/files testOutputs/$1
echo "Copied trace files"
sleep 1
