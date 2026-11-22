#!/bin/bash

# Start the Java Gateway Server using ONLY the assembled fat JAR
java -cp "/app/sbscl.jar" org.simulator.biosimulators.BioSimulatorsGateway &
JAVA_PID=$!

# Trap Docker termination signals to gracefully shut down the Java gateway
trap "kill -TERM $JAVA_PID 2>/dev/null" SIGTERM SIGINT

# Give the Java server 2 seconds to initialize
sleep 2

# Execute the Python CLI, passing along any arguments provided to Docker
python /app/main.py "$@"
PYTHON_EXIT_CODE=$?

# Clean up the background Java process
kill -TERM $JAVA_PID 2>/dev/null
wait $JAVA_PID 2>/dev/null

# Exit container with the Python CLI's true exit code
exit $PYTHON_EXIT_CODE