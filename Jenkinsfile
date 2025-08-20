pipeline {
  agent any

  tools {
    jdk 'jdk21'
    maven 'maven3'
  }

  environment {
    APP_NAME        = 'order-service'


    NEXUS_RELEASES  = 'http://localhost:8081/repository/maven-releases/'
    NEXUS_SNAPSHOTS = 'http://localhost:8081/repository/maven-snapshots/'

    DOCKER_REGISTRY = 'docker.io/pranav4329'
    IMAGE           = "${env.DOCKER_REGISTRY}/${env.APP_NAME}:${env.BUILD_NUMBER}"
  }

  options {
    skipDefaultCheckout(false)
    timestamps()
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build & Test') {
      steps {
        sh 'mvn -B clean package -DskipTests=false'
      }
      post {
        always {
          junit 'target/surefire-reports/*.xml'
        }
      }
    }

    stage('Publish JAR to Nexus') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'nexus-creds', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASS')]) {
          writeFile file: 'jenkins-settings.xml', text: """
<settings xmlns='http://maven.apache.org/SETTINGS/1.0.0'
          xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'
          xsi:schemaLocation='http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd'>
  <servers>
    <server>
      <id>nexus-releases</id>
      <username>${NEXUS_USER}</username>
      <password>${NEXUS_PASS}</password>
    </server>
    <server>
      <id>nexus-snapshots</id>
      <username>${NEXUS_USER}</username>
      <password>${NEXUS_PASS}</password>
    </server>
  </servers>
</settings>
"""
          sh 'mvn -B -s jenkins-settings.xml deploy'
        }
      }
    }

    stage('Build Docker Image') {
      steps {
        sh "docker build -t ${IMAGE} ."
      }
    }

    stage('Push Docker Image') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'docker-reg-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
          sh """
            echo "${DOCKER_PASS}" | docker login -u "${DOCKER_USER}" --password-stdin
            docker push ${IMAGE}
          """
        }
      }
    }

    stage('Deploy Locally') {
      when { branch 'main' }
      steps {
        sh """
          docker rm -f ${APP_NAME} || true
          docker run -d --name ${APP_NAME} -p 8083:8083 \
            -e SPRING_PROFILES_ACTIVE=prod \
            ${IMAGE}
        """
      }
    }
  }

  post {
    always {
      cleanWs()
    }
  }
}
