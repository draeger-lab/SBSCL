package org.simulator.biosimulators;

import py4j.GatewayServer;

public class BioSimulatorsGateway {

    public BioSimulatorsGateway() {
    }

    public static void main(String[] args) {
        BioSimulatorsGateway app = new BioSimulatorsGateway();
        // Starts the server on the default port (25333)
        GatewayServer server = new GatewayServer(app);
        server.start();
        System.out.println("SBSCL Gateway Server Started");
    }
}
