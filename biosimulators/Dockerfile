FROM python:3.9-slim-bullseye

# Install OpenJDK 11
RUN apt-get update && \
    apt-get install -y --no-install-recommends openjdk-11-jre-headless && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy and install Python dependencies
COPY biosimulators/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy the dependencies folder and the compiled Java engine
COPY target/dependency /app/lib
COPY target/sbscl-2.1.1.jar /app/sbscl.jar

# Copy the Python CLI and Entrypoint script
COPY biosimulators/main.py .
COPY biosimulators/entrypoint.sh .

# Make the entrypoint script executable
RUN chmod +x /app/entrypoint.sh

ENTRYPOINT ["/app/entrypoint.sh"]