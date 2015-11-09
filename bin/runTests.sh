#!/bin/bash

echo "Starting tests.."
mkdir testOutputs

PACKAGE_NAME=$1
NUM_TESTS=$2

number=0
echo "Starting testing $PACKAGE_NAME"
while [ $number -lt $NUM_TESTS ]; do
     monkeyrunner test/${PACKAGE_NAME}_test.py
     number=$((number + 1))
     echo "Test $number is completed"
     sleep 1
done

echo "Finished tests"
mkdir testOutputs/${packageName}
adb pull /data/data/${packageName}/files testOutputs/${packageName}
echo "Copied trace files"
sleep 1
