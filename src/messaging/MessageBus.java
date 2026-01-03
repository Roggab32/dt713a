package messaging;

import java.util.Map;
import java.util.concurrent.*;

public class MessageBus {
    private static MessageBus instance;
    private final Map<String, BlockingQueue<Message>> agentMailboxes;
    
    private MessageBus() {
        agentMailboxes = new ConcurrentHashMap<>();
    }
    
    public static synchronized MessageBus getInstance() {
        if (instance == null) {
            instance = new MessageBus();
        }
        return instance;
    }
    
    public void sendMessage(String recipientId, Message message) {
        agentMailboxes.computeIfAbsent(recipientId, k -> new LinkedBlockingQueue<>())
                     .offer(message);
    }
    
    public Message receiveMessage(String agentId, long timeoutMs) throws InterruptedException {
        BlockingQueue<Message> queue = agentMailboxes.get(agentId);
        if (queue == null) return null;
        return queue.poll(timeoutMs, TimeUnit.MILLISECONDS);
    }
}
