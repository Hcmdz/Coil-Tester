#!/bin/sh
APP_NAME="Gradle"
APP_BASE_NAME=gradlew
DEFAULT_JVM_OPTS="-Xmx64m -Xms64m"
MAX_FD="maximum"
cygwin=false
msys=false
darwin=false
nonstop=false

case "`uname`" in
  CYGWIN* ) cygwin=true ;;
  Darwin* ) darwin=true ;;
  MSYS* | MINGW* ) msys=true ;;
esac

# Resolve JAVACMD
if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

# Resolve APP_HOME
APP_HOME=$(cd "`dirname "$0"`" && pwd)

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

exec "$JAVACMD" $DEFAULT_JVM_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"