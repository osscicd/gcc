node {
    def BASE_GCC_VERSION = params.GCC_VERSION.split('-', 2)[0]
    def BASE_LINUX_DISTRIBUTION = params.GCC_VERSION.split('-', 2)[1]
    def BASE_GCC_MAJOR_VERSION = BASE_GCC_VERSION.split('\\.')[0]

    try {
        wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [
            [password: params.LINUX_ROOT_PASSWORD, var: 'LINUX_ROOT_PASSWORD'],
            [password: params.LINUX_IP_ADDRESS, var: 'LINUX_IP_ADDRESS']
        ]]) {
            stage('Checkout') {
                dir('ci-cd') {
                    checkout scm
                }

                dir('gcc') {
                    if (params.GIT_CREDENTIALS_ID == "OFF") {
                        git(branch: "${params.GIT_BRANCH}", url: "${params.GIT_URL}")
                    } else {
                        git(credentialsId: "${params.GIT_CREDENTIALS_ID}", branch: "${params.GIT_BRANCH}", url: "${params.GIT_URL}")
                    }
                }

                sh """
                    cd ci-cd
                    git branch
                    git tag
                    git switch --detach "${params.GCC_VERSION}"
                    git rev-parse "${params.GCC_VERSION}"
                    git reset --hard HEAD
                    git rev-parse HEAD
                    git clean -fdx
                    git describe --tags --exact-match

                    cd ../gcc
                    git branch
                    git tag
                    git switch --detach "${params.GCC_VERSION}"
                    git rev-parse "${params.GCC_VERSION}"
                    git reset --hard HEAD
                    git rev-parse HEAD
                    git clean -fdx
                    git describe --tags --exact-match
                """
            }

            stage('Upload') {
                echo "Uploading the file build-gcc-${BASE_GCC_MAJOR_VERSION}-on-${BASE_LINUX_DISTRIBUTION}.build.sh to ${LINUX_IP_ADDRESS} in /root/${BASE_LINUX_DISTRIBUTION}/gcc-${BASE_GCC_VERSION}/"
                script {
                    withEnv([
                        "BASE_LINUX_DISTRIBUTION=${BASE_LINUX_DISTRIBUTION}",
                        "BASE_GCC_VERSION=${BASE_GCC_VERSION}",
                        "BASE_GCC_MAJOR_VERSION=${BASE_GCC_MAJOR_VERSION}"
                    ]) {
                        load "ci-cd/stage.upload.script.groovy"
                    }
                }
            }

            stage('Configure') {
                sh """
                    sshpass -p "${LINUX_ROOT_PASSWORD}" ssh -p ${params.LINUX_SSH_PORT} root@${LINUX_IP_ADDRESS} "bash /root/${BASE_LINUX_DISTRIBUTION}/gcc-${BASE_GCC_VERSION}/build-gcc-${BASE_GCC_MAJOR_VERSION}-on-${BASE_LINUX_DISTRIBUTION}.cicd.sh gcc_configure ${BASE_GCC_VERSION} ${BASE_LINUX_DISTRIBUTION}"
                """
            }

            stage('Make') {
                sh """
                    sshpass -p "${LINUX_ROOT_PASSWORD}" ssh -p ${params.LINUX_SSH_PORT} root@${LINUX_IP_ADDRESS} "bash /root/${BASE_LINUX_DISTRIBUTION}/gcc-${BASE_GCC_VERSION}/build-gcc-${BASE_GCC_MAJOR_VERSION}-on-${BASE_LINUX_DISTRIBUTION}.cicd.sh gcc_make ${BASE_GCC_VERSION} ${BASE_LINUX_DISTRIBUTION}"
                """
            }

            stage('MakeTest') {
                sh """
                    sshpass -p "${LINUX_ROOT_PASSWORD}" ssh -p ${params.LINUX_SSH_PORT} root@${LINUX_IP_ADDRESS} "bash /root/${BASE_LINUX_DISTRIBUTION}/gcc-${BASE_GCC_VERSION}/build-gcc-${BASE_GCC_MAJOR_VERSION}-on-${BASE_LINUX_DISTRIBUTION}.cicd.sh gcc_make_test ${BASE_GCC_VERSION} ${BASE_LINUX_DISTRIBUTION}"
                """
            }

            stage('MakeInstall') {
                sh """
                    sshpass -p "${LINUX_ROOT_PASSWORD}" ssh -p ${params.LINUX_SSH_PORT} root@${LINUX_IP_ADDRESS} "bash /root/${BASE_LINUX_DISTRIBUTION}/gcc-${BASE_GCC_VERSION}/build-gcc-${BASE_GCC_MAJOR_VERSION}-on-${BASE_LINUX_DISTRIBUTION}.cicd.sh gcc_make_install ${BASE_GCC_VERSION} ${BASE_LINUX_DISTRIBUTION}"
                """
            }

            stage('Cleanup') {
                echo 'Cleaning up temporary files...'
                lock(resource: "${NODE_NAME}") {
                    sh """
                        sshpass -p "${LINUX_ROOT_PASSWORD}" ssh -p ${params.LINUX_SSH_PORT} root@${LINUX_IP_ADDRESS} "docker system prune -f"
                    """
                }
            }
        }
    } catch (err) {
        echo "Error: ${err}"
        currentBuild.result = 'FAILURE'
    } finally {
        echo 'Sending build notification...'
        sh """
            rm -rf ci-cd*
            rm -rf gcc*
        """
    }
}
