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
//                sh 'export GPG_TTY=`ps -p 1 | tail -n 1 | awk \'{ print "/dev/" $2 }\'`; printenv; mvn -X package gpg:sign -Possrh -DskipTests'
                sh 'export GPG_TTY=/dev/console; mvn package gpg:sign -Possrh -DskipTests'
            }
        }
    }
}
