#!/usr/bin/env bash

GCC_BASE_DIR="/root"
GCC_PKG_DIR="$GCC_BASE_DIR/binaries/gcc"
mkdir -p "$GCC_PKG_DIR"

exec_docker(){
    local command="$1"
    docker exec build-gcc-GCC_VERSION-in-docker-on-LINUX_DISTRIBUTION bash -c "cd $GCC_BASE_DIR/ && $command"
}

gcc_configure(){
    cd "$GCC_BASE_DIR/LINUX_DISTRIBUTION/gcc-GCC_VERSION/" || exit 1
    docker build --no-cache=true \
                 -t LINUX_DISTRIBUTION:build-gcc-GCC_VERSION \
                 -f ./Dockerfile .

    docker create --name build-gcc-GCC_VERSION-in-docker-on-LINUX_DISTRIBUTION \
                  -v "$GCC_PKG_DIR":"$GCC_PKG_DIR" \
                  LINUX_DISTRIBUTION:build-gcc-GCC_VERSION \
                  bash -c "sleep infinity"

    docker start build-gcc-GCC_VERSION-in-docker-on-LINUX_DISTRIBUTION

    exec_docker "bash build-gcc-GCC_MAJOR_VERSION-on-LINUX_DISTRIBUTION.build.sh gcc_configure $1 $2"
}

gcc_make(){
    exec_docker "bash build-gcc-GCC_MAJOR_VERSION-on-LINUX_DISTRIBUTION.build.sh gcc_make $1 $2"
}

gcc_make_test(){
    exec_docker "bash build-gcc-GCC_MAJOR_VERSION-on-LINUX_DISTRIBUTION.build.sh gcc_make_test $1 $2"
}

gcc_make_install(){
    exec_docker "bash build-gcc-GCC_MAJOR_VERSION-on-LINUX_DISTRIBUTION.build.sh gcc_make_install $1 $2"
    docker stop build-gcc-GCC_VERSION-in-docker-on-LINUX_DISTRIBUTION
    docker rm build-gcc-GCC_VERSION-in-docker-on-LINUX_DISTRIBUTION
    docker rmi LINUX_DISTRIBUTION:build-gcc-GCC_VERSION
    rm -fr "$GCC_BASE_DIR/LINUX_DISTRIBUTION/gcc-GCC_VERSION/"
}

case $1 in
    gcc_configure) gcc_configure $2 $3;;
    gcc_make) gcc_make $2 $3;;
    gcc_make_test) gcc_make_test $2 $3;;
    gcc_make_install) gcc_make_install $2 $3;;
esac
