pipeline {
    agent any
    options {
        skipDefaultCheckout(true)
        disableConcurrentBuilds()
        timeout(time: 20, unit: 'MINUTES')
        timestamps()
    }
    parameters {
        booleanParam(name: 'SIMULAR_FALHA_TESTE', defaultValue: false,
                     description: 'Somente laboratório: injeta um teste falhando no workspace descartável.')
    }
    environment {
        JAVA_HOME = '/opt/java/jdk17'
        PATH = "/opt/java/jdk17/bin:/usr/share/maven/bin:${env.PATH}"
    }
    stages {
        stage('Fontes identificados') {
            steps {
                deleteDir()
                sh 'cp -R /source/. .'
                sh 'python3 labs/operacao/verificar_snapshot.py'
            }
        }
        stage('Core — build e testes Maven') {
            steps {
                script {
                    if (params.SIMULAR_FALHA_TESTE) {
                        sh 'python3 labs/operacao/injetar_falha.py'
                        sh 'mvn -B -Dtest=PipelineBloqueioTest test'
                    } else {
                        sh 'java -version && mvn -version && mvn -B clean verify'
                    }
                }
            }
        }
        stage('Gateway — testes Python') {
            steps {
                sh 'python3 -m venv .venv-ci'
                sh '.venv-ci/bin/pip install --disable-pip-version-check -r labs/iot/requirements.txt'
                sh '.venv-ci/bin/python labs/operacao/ci_python.py'
                sh '.venv-ci/bin/python -m compileall -q labs/ia labs/iot'
            }
        }
        stage('Aprovar artefato') {
            steps {
                sh 'python3 labs/operacao/aprovar_artefato.py'
                archiveArtifacts artifacts: 'aprovado/**', fingerprint: true
            }
        }
    }
    post {
        always {
            junit testResults: 'target/surefire-reports/TEST-*.xml,reports/python-junit.xml', allowEmptyResults: true
            archiveArtifacts artifacts: 'target/surefire-reports/**,target/site/jacoco/**,reports/**,source-manifest.json', allowEmptyArchive: true
        }
    }
}
