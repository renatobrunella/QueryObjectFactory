pipeline {
    agent {
        docker {
            image 'maven:3.6.3-jdk-8'
            args '-u root -v mavenVolume:/root/.m2'
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
                sh 'mvn install -DskipTests'
            }
        }
        stage('sign') {
            steps {
                sh 'mvn package gpg:sign -Possrh -DskipTests'
            }
        }
    }
}
