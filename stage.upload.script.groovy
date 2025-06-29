script {
    def LINUX_DISTRIBUTION = env.BASE_LINUX_DISTRIBUTION
    def GCC_VERSION = env.BASE_GCC_VERSION
    def GCC_MAJOR_VERSION = env.BASE_GCC_MAJOR_VERSION
    def tarCommand = "../../src/gcc/gcc-${GCC_VERSION}.tar.gz root@${LINUX_IP_ADDRESS}:/root/${LINUX_DISTRIBUTION}/gcc-${GCC_VERSION}/gcc-${GCC_VERSION}.tar.gz"
    def scpCommand = "sshpass -p '${LINUX_ROOT_PASSWORD}' scp -P ${params.LINUX_SSH_PORT}"

    sh """
        bash<<'EOF'
            set -x
            mkdir -p ~/.ssh
            chmod 700 ~/.ssh
            ssh-keyscan -H ${LINUX_IP_ADDRESS} >> ~/.ssh/known_hosts 2>/dev/null
            sshpass -p '${LINUX_ROOT_PASSWORD}' ssh -p ${params.LINUX_SSH_PORT} root@${LINUX_IP_ADDRESS} "mkdir -p /root/${LINUX_DISTRIBUTION}/gcc-${GCC_VERSION}/"
            set +x
EOF
    """

    sh """
        bash<<'EOF'
            set -x
            sed -i -e "s/LINUX_DISTRIBUTION/${LINUX_DISTRIBUTION}/g" \
                   -e "s/GCC_VERSION/${GCC_VERSION}/g" \
                   -e "s/GCC_MAJOR_VERSION/${GCC_MAJOR_VERSION}/g" \
                   ci-cd/build-gcc-${GCC_MAJOR_VERSION}-on-${LINUX_DISTRIBUTION}.cicd.sh

            sed -i -e "s/LINUX_DISTRIBUTION/${LINUX_DISTRIBUTION}/g" \
                   -e "s/GCC_VERSION/${GCC_VERSION}/g" \
                   -e "s/GCC_MAJOR_VERSION/${GCC_MAJOR_VERSION}/g" \
                   ci-cd/Dockerfile

            ${scpCommand} ci-cd/build-gcc-${GCC_MAJOR_VERSION}-on-${LINUX_DISTRIBUTION}.cicd.sh root@${LINUX_IP_ADDRESS}:/root/${LINUX_DISTRIBUTION}/gcc-${GCC_VERSION}/build-gcc-${GCC_MAJOR_VERSION}-on-${LINUX_DISTRIBUTION}.cicd.sh
            ${scpCommand} ci-cd/Dockerfile root@${LINUX_IP_ADDRESS}:/root/${LINUX_DISTRIBUTION}/gcc-${GCC_VERSION}/Dockerfile
            ${scpCommand} gcc/build-gcc-${GCC_MAJOR_VERSION}-on-${LINUX_DISTRIBUTION}.build.sh root@${LINUX_IP_ADDRESS}:/root/${LINUX_DISTRIBUTION}/gcc-${GCC_VERSION}/build-gcc-${GCC_MAJOR_VERSION}-on-${LINUX_DISTRIBUTION}.build.sh
            ${scpCommand} ${tarCommand}
            set +x
EOF
    """
}
