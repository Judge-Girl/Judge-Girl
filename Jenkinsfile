pipeline {
    agent any
    stages {
        stage('Commons') {
            steps {
                build job: "/Judge-K8S.commons", wait: true
            }
        }
        stage('Problem-Service') {
            steps {
                build job: "/Judge-K8S.Problem-Service", wait: true
            }
        }
        
        stage('Submission-Service') {
            steps {
                build job: "/Judge-K8S.Submission-Service", wait: true
            }
        }
        
    }
}
