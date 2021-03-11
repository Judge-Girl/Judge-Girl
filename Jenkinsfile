pipeline {
    agent any

    stages {
        stage('Prepare Files') {
            sh "cp -r ../../judge-girl-secrets/* ./"
        }
        stage('Test') {
            steps {
                sh "mvn -Dmaven.test.failure.ignore=true clean package"
            }
        }
    }
}
