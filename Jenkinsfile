pipeline {
    agent {
        docker {
            image 'maven:3.6.3-jdk-8'
            args '-u root'
            reuseNode true
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
