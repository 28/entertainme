#!/usr/bin/env bash
f=$1
shift
java -jar eme.jar -${f} $@
