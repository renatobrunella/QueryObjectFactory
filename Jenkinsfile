pipeline {
    agent {
        docker {
            image 'maven:3.6.3-jdk-8'
            args '-u root -v mavenVolume:/root/.m2 -v gpgKeyVolume:/root/.gnupg'
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
                sh 'export GPG_TTY=/dev/pts/1 && mvn package gpg:sign -Possrh -DskipTests'
            }
        }
    }
}
