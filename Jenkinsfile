pipeline {
    agent any
    stages {
        stage('Prepare Files') {
            steps {
                sh "cp -r ../../judge-girl-secrets/* ./"
            }
        }
        stage('Test') {
            steps {
                sh "mvn -Dmaven.test.failure.ignore=true clean package"
            }
        }
    }
    post {
      success {
        withCredentials([string(credentialsId: 'bdf163db-6a48-4c76-955b-5cc9759925bf', variable: 'TOKEN')]) {
          sh '''
          curl -XPOST -H "Authorization: token $TOKEN" https://api.github.com/repos/Judge-Girl/Judge-Girl/statuses/$(git rev-parse HEAD) -d "{
                \\"state\\": \\"success\\",
                \\"target_url\\": \\"${BUILD_URL}\\",
                \\"description\\": \\"The build has succeeded!\\"
              }"
              '''
        }
      }

      failure {
        withCredentials([string(credentialsId: 'bdf163db-6a48-4c76-955b-5cc9759925bf', variable: 'TOKEN')]) {
          sh '''
          curl -XPOST -H "Authorization: token $TOKEN" https://api.github.com/repos/Judge-Girl/Judge-Girl/statuses/$(git rev-parse HEAD) -d "{
                \\"state\\": \\"failure\\",
                \\"target_url\\": \\"${BUILD_URL}\\",
                \\"description\\": \\"The build failed!\\"
              }"
          '''
        }
      }
    }
}
