/*
 * Pipeline del backend IngresoUniversitarioTPI135 2026S.
 *
 * Requisitos en el agente Jenkins:
 *   - JDK 21 (o el mvnw lo resuelve via toolchain del agente)
 *   - Docker disponible (build de imágenes + docker compose)
 *   - Credenciales Jenkins (Manage Jenkins > Credentials), tipo "Secret text":
 *       ingreso-db-password        -> contraseña de PostgreSQL
 *       ingreso-keystore-password  -> contraseña del keystore SSL de Liberty
 *       sonarqube-token            -> token de análisis de SonarQube
 *
 * No se asumen rutas absolutas del desarrollador ni secretos en texto plano:
 * todo lo sensible viene de Jenkins credentials; los nombres de variables
 * (PGSERVER/PGPORT/PGDBNAME/PGUSER/PGPASSWORD) son los mismos que usa
 * server.xml y docker-compose.yml.
 */

pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    parameters {
        string(name: 'FRONTEND_JOB_NAME', defaultValue: 'NuevoIngresoWeb-Frontend',
               description: 'Nombre del job Jenkins del frontend a disparar como downstream. Si aun no existe, el pipeline no falla.')
        string(name: 'SONAR_HOST_URL', defaultValue: 'http://localhost:9000',
               description: 'URL del servidor SonarQube Community.')
        string(name: 'DB_NAME', defaultValue: 'ingreso_tpi135', description: 'Nombre de la base de datos PostgreSQL.')
        string(name: 'DB_USER', defaultValue: 'ingreso_user', description: 'Usuario de PostgreSQL.')
        string(name: 'LIBERTY_BASE_IMAGE', defaultValue: 'ingresouniversitariotpi135-base:26.0.0.2',
               description: 'Tag de la imagen base de Open Liberty (Dockerfile) sobre la que se arma la imagen de despliegue.')
    }

    environment {
        WAR_NAME = 'IngresoUniversitarioTPI135-1.0-SNAPSHOT.war'
        HEALTHCHECK_URL = 'http://localhost:9080/ingreso/resources/v1/areas'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build + Unit + Integration Tests') {
            steps {
                script {
                    mvnExec('clean verify -Pintegracion')
                }
            }
            post {
                always {
                    junit testResults: '**/target/surefire-reports/*.xml,**/target/failsafe-reports/*.xml', allowEmptyResults: true
                    archiveArtifacts artifacts: "target/${WAR_NAME}", fingerprint: true, allowEmptyArchive: true
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withCredentials([string(credentialsId: 'sonarqube-token', variable: 'SONAR_TOKEN')]) {
                    script {
                        // %VAR% solo lo expande cmd.exe; en sh hay que usar $VAR. Sin esta
                        // rama, en un agente Linux se mandaría el literal "%SONAR_TOKEN%" a Maven.
                        String tokenRef = isUnix() ? '$SONAR_TOKEN' : '%SONAR_TOKEN%'
                        mvnExec("sonar:sonar -Dsonar.host.url=${params.SONAR_HOST_URL} -Dsonar.token=${tokenRef}")
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                // Requiere que el servidor SonarQube tenga configurado el webhook hacia este Jenkins.
                // Si no hay Quality Gate Jenkins plugin / webhook configurado, este stage falla por timeout:
                // en ese caso, ejecutar el análisis manualmente y revisar el resultado en el dashboard de Sonar.
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Build base Liberty image') {
            steps {
                script {
                    dockerExec("build -t ${params.LIBERTY_BASE_IMAGE} .")
                }
            }
        }

        stage('Build deploy image') {
            steps {
                withEnv(["LIBERTY_BASE_IMAGE=${params.LIBERTY_BASE_IMAGE}"]) {
                    script {
                        composeExec('build ingreso-app')
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                withCredentials([
                    string(credentialsId: 'ingreso-db-password', variable: 'DB_PASSWORD'),
                    string(credentialsId: 'ingreso-keystore-password', variable: 'KEYSTORE_PASSWORD')
                ]) {
                    withEnv([
                        "DB_NAME=${params.DB_NAME}",
                        "DB_USER=${params.DB_USER}",
                        "LIBERTY_BASE_IMAGE=${params.LIBERTY_BASE_IMAGE}"
                    ]) {
                        script {
                            composeExec('up -d')
                        }
                    }
                }
            }
        }

        stage('Post-deploy health check') {
            steps {
                script {
                    healthCheck(env.HEALTHCHECK_URL, 20, 3)
                }
            }
        }

        stage('Trigger frontend') {
            steps {
                script {
                    try {
                        build job: params.FRONTEND_JOB_NAME, wait: false, propagate: false
                        echo "Disparado downstream job '${params.FRONTEND_JOB_NAME}'."
                    } catch (Exception e) {
                        echo "No se pudo disparar el job frontend '${params.FRONTEND_JOB_NAME}' (¿aun no existe?). " +
                             "No se falla el pipeline del backend por esto. Detalle: ${e.getMessage()}"
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Helpers: compatibles con agentes Windows y Unix sin asumir rutas absolutas.
// ---------------------------------------------------------------------------

void mvnExec(String goals) {
    if (isUnix()) {
        sh "./mvnw ${goals}"
    } else {
        bat "mvnw.cmd ${goals}"
    }
}

void dockerExec(String args) {
    if (isUnix()) {
        sh "docker ${args}"
    } else {
        bat "docker ${args}"
    }
}

void composeExec(String args) {
    if (isUnix()) {
        sh "docker compose ${args}"
    } else {
        bat "docker compose ${args}"
    }
}

void healthCheck(String url, int maxIntentos, int pausaSegundos) {
    for (int intento = 1; intento <= maxIntentos; intento++) {
        int code = isUnix()
            ? sh(script: "curl -s -o /dev/null -w '%{http_code}' ${url}", returnStdout: true).trim().toInteger()
            : bat(script: "@curl -s -o NUL -w \"%%{http_code}\" ${url}", returnStdout: true).trim().toInteger()

        echo "Healthcheck intento ${intento}/${maxIntentos} -> HTTP ${code}"
        if (code == 200) {
            echo 'Backend desplegado y respondiendo correctamente.'
            return
        }
        sleep(time: pausaSegundos, unit: 'SECONDS')
    }
    error "El backend no respondió HTTP 200 en ${url} tras ${maxIntentos * pausaSegundos} segundos."
}
