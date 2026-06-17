// Jenkins CI/CD pipeline for the ID Card Manager.
//
// Job setup (one-time, in Jenkins): create a *Pipeline* job →
//   "Pipeline script from SCM" → Git → this repo → Script Path: Jenkinsfile.
// The pollSCM trigger below then checks Git every 5 minutes and builds on changes.
//
// Prerequisites on the Jenkins agent: JDK 25, Git, Ansible, SSH access to the web
// server (port 2222), and the "Email Extension" plugin configured with an SMTP server.
pipeline {
    agent any

    triggers {
        // Poll the Git repository every 5 minutes.
        pollSCM('H/5 * * * *')
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw -B -ntp clean package -DskipTests'
            }
        }

        stage('Test (SQLite)') {
            steps {
                // Tests run against the SQLite test database (production uses MySQL).
                sh './mvnw -B -ntp test'
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Deploy (Ansible)') {
            // Only reached when Build and Test succeeded.
            steps {
                dir('ansible') {
                    sh 'ansible-playbook deploy.yml'
                }
            }
        }
    }

    post {
        failure {
            // On build/test error: notify the developer who committed (culprits)
            // and CC the instructor.
            emailext(
                subject: "❌ BUILD FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """\
The build failed.

Job:     ${env.JOB_NAME}
Build:   #${env.BUILD_NUMBER}
URL:     ${env.BUILD_URL}
Branch:  ${env.GIT_BRANCH}
Commit:  ${env.GIT_COMMIT}

See the full console log at: ${env.BUILD_URL}console
""",
                // Recipients = the developer(s) who committed the breaking change.
                recipientProviders: [culprits(), developers(), requestor()],
                // CC the instructor on every failure.
                cc: 'srengty@gmail.com',
                attachLog: true
            )
        }
        fixed {
            emailext(
                subject: "✅ BUILD RECOVERED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: "The pipeline is green again: ${env.BUILD_URL}",
                recipientProviders: [culprits(), developers(), requestor()],
                cc: 'srengty@gmail.com'
            )
        }
    }
}
