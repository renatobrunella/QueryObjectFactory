pipeline {
    agent {
        docker {
            image 'maven:3.6.3'
            args '-u root'
        }

    }
    stages {
        stage('build') {
            steps {
                sh 'mvn package'
            }
        }
    }
}
