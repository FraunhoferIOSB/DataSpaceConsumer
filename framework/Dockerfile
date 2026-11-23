FROM eclipse-temurin:21-jre-alpine@sha256:5d7689f47d670aca766b9503a70bccc2d73b4eb9221ae8f714ae958313c4ce70
WORKDIR /app

RUN mkdir -p /app/extensions

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the pre-built jar
COPY --chown=spring:spring framework/target/*.jar app.jar
# Copy extensions if available
COPY --chown=spring:spring framework/extensions* /app/extensions

# Expose default Spring Boot port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
