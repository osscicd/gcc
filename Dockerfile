FROM LINUX_DISTRIBUTION:gcc

WORKDIR /root/
COPY --chown=root:root "gcc-GCC_VERSION.tar.gz" \
                       "build-gcc-GCC_MAJOR_VERSION-on-LINUX_DISTRIBUTION.build.sh" ./
