package umg.edu.gt.Telebot.GPT.Model;

import java.io.Serializable;

// Clase interna que representa la entidad Client
public class Client implements Serializable {
    private Long clientId;
    private String name;

    public Client(Long clientId, String name) {
        this.clientId = clientId;
        this.name = name;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Client{" +
                "clientId=" + clientId +
                ", name='" + name + '\'' +
                '}';
    }
}
