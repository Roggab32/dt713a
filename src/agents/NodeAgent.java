package agents;

import messaging.Message;

public interface NodeAgent {
    void start();
    void stop();
    void onMessage(Message message);
    String getAgentId();
    boolean isRunning();
}
