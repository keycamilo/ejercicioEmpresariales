from openjdk:21
copy "./target/liga-futbol-api-1.0.0.jar.original" "app.jar"
expose 8088
ENTRYPOINT [ "java", "-jar", "app.jar" ]