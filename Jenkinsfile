pipeline {
    agent any

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
    }

    stages {
        stage('Verify prediction backend') {
            parallel {
                stage('Agri catalog') {
                    steps {
                        dir('services/agri-catalog-service') {
                            bat 'mvn -B test'
                        }
                    }
                }
                stage('File service') {
                    steps {
                        dir('services/file-service') {
                            bat 'mvn -B test'
                        }
                    }
                }
                stage('Diagnosis runtime') {
                    steps {
                        dir('services/rice-disease-diagnosis-service') {
                            bat 'mvn -B test'
                        }
                    }
                }
                stage('Gateway') {
                    steps {
                        dir('services/api-gateway') {
                            bat 'mvn -B test'
                        }
                    }
                }
            }
        }

        stage('Build frontend') {
            steps {
                dir('apps/web/nongthinh') {
                    bat 'npm ci'
                    bat 'npm run build'
                }
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'services/**/target/surefire-reports/*.xml'
            archiveArtifacts allowEmptyArchive: true, artifacts: 'apps/web/nongthinh/dist/**'
        }
    }
}
