#!groovy
def config = null
pipeline {
    agent {label 'ecs_main'}
    stages{
        stage('Get Service Configuration') {
            // when {
            //     branch "*"
            //     tag "*"
            // }
            steps {
                script{
                    config = readYaml file: "config.yml"
                    echo config.toString()
                }
                echo "${WORKSPACE}"
                sh "docker run --rm -v ${WORKSPACE}:/tmp golang:alpine ls -al /tmp"
            }
        }
        stage ('Build Docker Container') {
            when {
                branch "*"
                not {
                    tag "*"
                }
            }
            steps {
                echo config.toString()
                echo "Building the docker container"
                sh "docker build --pull -t ${config['app_name']}:${GIT_COMMIT} ."
            }
        }
        stage ('Push Docker Container to Registry') {
            when {
                branch "master"
                not {
                    tag "*"
                }
            }
            steps {
                echo config.toString()
                echo "Pushing the docker container"
                //sh "docker push -t ${config['app_name']}:${GIT_COMMIT} ."
            }
        }
        stage ('Auto Deploy to Development') {
            when {
                branch "master"
                not {
                    tag "*"
                }
            }
            steps {
                echo config.toString()
                echo "Deploying to development"
            }
        }
        stage ('Deploy to Production') {
            when {
                tag "*"
            }
            steps {
                echo config.toString()
                echo "Deploying to Production!"
            }
        }
    }
}
