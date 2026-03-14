FROM debian:12

# Dependencias del sistema
RUN apt-get update && apt-get install -y --no-install-recommends \
    wget \
    unzip \
    ca-certificates \
 && rm -rf /var/lib/apt/lists/*

# ── JDK 21 Oracle ────────────────────────────────────────────────────────────
RUN wget -q https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.tar.gz \
         -O /tmp/jdk21.tar.gz \
 && mkdir -p /opt/jdk-21 \
 && tar -xzf /tmp/jdk21.tar.gz -C /opt/jdk-21 --strip-components=1 \
 && rm /tmp/jdk21.tar.gz

ENV JAVA_HOME=/opt/jdk-21
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# ── OpenLiberty 26.0.0.2 jakartaee10 ─────────────────────────────────────────
RUN wget -q https://public.dhe.ibm.com/ibmdl/export/pub/software/openliberty/runtime/release/26.0.0.2/openliberty-jakartaee10-26.0.0.2.zip \
         -O /tmp/liberty.zip \
 && unzip -q /tmp/liberty.zip -d /opt \
 && rm /tmp/liberty.zip

ENV LIBERTY_HOME=/opt/wlp
ENV PATH="${LIBERTY_HOME}/bin:${PATH}"

# ── Driver JDBC PostgreSQL 42.7.7 en recursos compartidos de Liberty ──────────
RUN mkdir -p /opt/wlp/usr/shared/resources \
 && wget -q https://jdbc.postgresql.org/download/postgresql-42.7.7.jar \
         -O /opt/wlp/usr/shared/resources/postgresql-42.7.7.jar

# ── Crear servidor Liberty y directorios necesarios ─────────────────────────
# apps/ queda vacío: el WAR se inyecta en tiempo de test con Testcontainers
RUN /opt/wlp/bin/server create tpi135_2026 \
 && mkdir -p /opt/wlp/usr/servers/tpi135_2026/apps \
             /opt/wlp/usr/servers/tpi135_2026/dropins \
             /opt/wlp/usr/servers/tpi135_2026/resources/security

# ── Configuración del servidor ─────────────────────────────────────────────────────
COPY server.xml /opt/wlp/usr/servers/tpi135_2026/server.xml

EXPOSE 9080 9443

# Genera el keystore SSL si no existe y arranca Liberty
CMD ["/bin/bash", "-c", \
  "KEYSTORE=${LIBERTY_HOME}/usr/servers/tpi135_2026/resources/security/keystore.p12; \
   if [ -n \"${KEYSTORE_PASSWORD}\" ] && [ ! -f \"${KEYSTORE}\" ]; then \
     echo '[init] Generando keystore SSL...'; \
     ${JAVA_HOME}/bin/keytool -genkeypair \
       -alias liberty -keyalg RSA -keysize 2048 -validity 3650 \
       -storetype PKCS12 -keystore ${KEYSTORE} \
       -storepass ${KEYSTORE_PASSWORD} \
       -dname 'CN=ingreso-universitario,OU=TPI135,O=UES,C=SV'; \
   fi && exec ${LIBERTY_HOME}/bin/server run tpi135_2026"]
