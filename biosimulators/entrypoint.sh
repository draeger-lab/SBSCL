#!/bin/bash

# Start the Java Gateway Server in the background. 
# We include the main jar AND all dependency jars in the classpath.
java -cp "/app/sbscl.jar:/app/lib/*" org.simulator.biosimulators.BioSimulatorsGateway &

# Give the Java server 2 seconds to initialize
sleep 2

# Execute the Python CLI, passing along any arguments provided to Docker
python /app/main.py "$@"