#!/bin/bash

if [[ $(which sbt) == "" ]]; then
    echo "Installing sbt"
    echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
    sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
    sudo apt-get update
    sudo apt-get install sbt
fi

sbt compile
sbt "run-main app.App $1 $2 $2"
