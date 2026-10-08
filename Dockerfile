# =============================================================================
#  BYTEBOOKS API — imagen de la aplicación
#
#  Build en dos etapas: la primera compila con Maven, la segunda sólo lleva
#  el JRE y el jar. La imagen final no incluye Maven ni el código fuente.
#
#  Construir:  docker build -t bytebooks-api .
# =============================================================================


# -----------------------------------------------------------------------------
#  ETAPA 1 — compilación
# -----------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# Las dependencias se descargan en una capa aparte: mientras el pom.xml no
# cambie, Docker la reutiliza y el build siguiente es mucho más rápido.
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B


# -----------------------------------------------------------------------------
#  ETAPA 2 — ejecución
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jre

WORKDIR /app

# Usuario sin privilegios: si alguien logra ejecutar código en el contenedor,
# no lo hace como root.
RUN groupadd -r bytebooks && useradd -r -g bytebooks bytebooks

COPY --from=build /build/target/*.jar app.jar
RUN chown bytebooks:bytebooks app.jar

USER bytebooks

EXPOSE 8080

# Techo de memoria explícito, no un porcentaje del host.
#
# Railway factura la RAM que el proceso tiene tomada, y sin techo la JVM se
# reserva una fracción de toda la memoria que ve en la máquina. El recolector
# no devuelve lo que ya no usa, así que el proceso se queda en el máximo que
# alcanzó alguna vez y eso es lo que se paga todo el mes.
#
# 256 MB de heap alcanzan: las portadas llegan de a 2 MB y la importación desde
# Google Books trae páginas chicas. SerialGC evita los hilos y estructuras que
# G1 mantiene, que en un contenedor de un solo core no compensan, y
# TieredStopAtLevel=1 deja sólo el compilador rápido, que ocupa menos memoria y
# alcanza para el tráfico de este sitio.
ENTRYPOINT ["java", "-Xmx256m", "-Xss512k", "-XX:MaxMetaspaceSize=128m", "-XX:ReservedCodeCacheSize=64m", "-XX:MaxDirectMemorySize=64m", "-XX:+UseSerialGC", "-XX:TieredStopAtLevel=1", "-jar", "/app/app.jar"]
