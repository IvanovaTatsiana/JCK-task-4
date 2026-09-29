# Step 1: Use official Tomcat 10 image configured with Java 21 environment
FROM tomcat:10.1-jdk21-temurin

# Step 2: Clear native default Tomcat webapps context path registries
RUN rm -rf /usr/local/tomcat/webapps/*

# Step 3: Map and deploy our compiled application archive directly into ROOT directory
COPY target/shop-mvc.war /usr/local/tomcat/webapps/ROOT.war

# Step 4: Expose native transmission ports
EXPOSE 8080

# Step 5: Start servlet routing engine pipeline routines
CMD ["catalina.sh", "run"]