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
        
        stage('Student-Adapter-Service') {
            steps {
                build job: "/Judge-K8S.Student-Adapter-Service", wait: true
            }
        }
        
        stage('Streaming-Service') {
            steps {
                build job: "/Judge-K8S.Streaming-Service", wait: true
            }
        }
        
        stage('Judge-K8S-Web') {
            steps {
                build job: "/Judge-K8S-Web", wait: true
            }
        }
    }
}
