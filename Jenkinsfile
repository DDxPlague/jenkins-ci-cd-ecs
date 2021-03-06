#!groovy
def config = null
pipeline {
    // only keep the last three build results
    // options {
    //     buildDiscarder(logRotator(numToKeepStr: '3'))
    // }
    agent {label 'ecs_main'}
    stages{
        // always get the service configuration
        stage('Get Service Configuration') {
            steps {
                script{
                    config = readYaml file: "config.yml"
                    echo "ENVIRONMENT CONFIG:"
                    echo config.toString()
                    sh "env"
                }
            }
        }
        // build the docker container on any branch
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
                sh "docker build --pull -t ${config['app_name']}:${config['app_version_number']} ."
            }
        }
        // push the docker container only when building the master branch
        stage ('Push Docker Container to Registry') {
            when {
                branch "master"
                not {
                    tag "*"
                }
            }
            steps {
                echo config.toString()

                sh "aws sts assume-role --role-arn arn:aws:iam::${config['aws_account_id']}:role/${config['build_role_name']} --role-session-name ${GIT_COMMIT} > build-credentials.json"
                echo "Logging in to Amazon ECR and pushing container..."
                sh """
                export AWS_DEFAULT_REGION=${config['aws_region']}
                export AWS_ACCESS_KEY_ID=`/home/jenkins/jq -r '.Credentials.AccessKeyId' build-credentials.json`
                export AWS_SECRET_ACCESS_KEY=`/home/jenkins/jq -r '.Credentials.SecretAccessKey' build-credentials.json`
                export AWS_SESSION_TOKEN=`/home/jenkins/jq -r '.Credentials.SessionToken' build-credentials.json`
                eval \$(aws ecr get-login --region ${config['aws_region']} --no-include-email)
                docker push ${config['app_name']}:${config['app_version_number']}
                """
            }
        }
        // auto deploy to the development environment only when building the master branch
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
                // script {
                //     def environment = "dev"
                //     def description = "Deploying my branch"
                //     def ref = "0.0.8"
                //     def deployURL = "https://api.github.com/repos/redventures/${config['app_name']}/deployments"
                //     def deployBody = '{"ref": "' + ref +'","environment": "' + environment  +'","description": "' + description + '"}'
                //     echo deployBody.toString()
                //     // Create new Deployment using the GitHub Deployment API
                //     def response = httpRequest authentication: 'github_service_account', httpMode: 'POST', requestBody: deployBody, responseHandle: 'STRING', url: deployURL
                //     echo response.toString()
                //     if(response.status != 201) {
                //         error("Deployment API Create Failed: " + response.status + "   " + response.toString())
                //     }
                // }
            }
        }
        // deploy to production only when building a tag
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
