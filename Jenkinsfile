pipeline {
    agent {
        docker {
            image 'maven:3.6.3-jdk-8'
            args '-u root -v $MAVEN_HOME:/root/.m2'
            reuseNode true
        }

    }
    stages {
        stage('build') {
            steps {
                sh 'printenv'
                sh 'mvn clean package'
            }
        }
        stage('install') {
            steps {
                sh 'mvn install'
            }
        }
    }
}
