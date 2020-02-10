pipeline {
    agent {
        docker {
            image 'maven:3.6.3-jdk-8'
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
