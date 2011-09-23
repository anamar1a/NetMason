#!/bin/bash

cd netmason2

mkdir gathered
mkdir gathered/strategists
mkdir gathered/fitness
mkdir gathered/evaluations
mkdir gathered/runs

echo "Moving"

cp *strategists*.txt gathered/strategists
cp *evaluations*.txt gathered/evaluations
cp *run*.txt gathered/runs
cp *fitness*.txt gathered/fitness

tar -czvf gathered.tgz gathered/

cd ..