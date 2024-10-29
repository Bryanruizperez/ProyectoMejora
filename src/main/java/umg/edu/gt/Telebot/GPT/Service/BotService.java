package umg.edu.gt.Telebot.GPT.Service;


import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import umg.edu.gt.Telebot.GPT.Model.Client;
import umg.edu.gt.Telebot.GPT.Repository.ClientRepository;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Service
public class BotService {

    private final String BOT_TOKEN = "7822338733:AAH3RJF87rr4QmkqRvjpIlUiZYhHS8zBTaQ"; // Reemplaza con tu token
    private final String TELEGRAM_API_URL = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage";

    private final ClientRepository clientRepository;

    public BotService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    private Map<Long, Boolean> askingName = new HashMap<>();
    private Map<Long, String> userNames = new HashMap<>();

    public void sendTelegramMessage(Long chatId, String message) {
        RestTemplate restTemplate = new RestTemplate();
        String url = TELEGRAM_API_URL + "?chat_id=" + chatId + "&text=" + message;
        restTemplate.getForObject(url, String.class);
    }

    public void setUserName(Long chatId, String name) {
        userNames.put(chatId, name);
    }

    public String getUserName(Long chatId, String s) {
        return userNames.getOrDefault(chatId, "Aún no me has dicho tu nombre.");
    }

    public void setAskingName(Long chatId, boolean asking) {
        askingName.put(chatId, asking);
    }

    public boolean isAskingName(Long chatId) {
        return askingName.getOrDefault(chatId, false);
    }

    @Cacheable(value = "clients", key = "#chatId")
    public Client getClientById(Long chatId) throws SQLException {
        return clientRepository.getById(chatId);
    }
    @CacheEvict(value = "clients", key = "#chatId")
    public void setClient(Long chatId, Client client) {
        try {
            clientRepository.add(client.getName(), chatId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @CachePut(value = "clients", key = "#client.clientId")
    public Client addClient(Client client) throws SQLException {
        clientRepository.add(client.getName(), client.getClientId());
        return client;
    }

    public void handleUpdate(Map<String, Object> update) throws SQLException {
        if (update.containsKey("message")) {
            Map<String, Object> message = (Map<String, Object>) update.get("message");
            Map<String, Object> chat = (Map<String, Object>) message.get("chat");
            long chatId = ((Number) chat.get("id")).longValue();
            String text = (String) message.get("text");

            Client client = getClientById(chatId);

            if (client != null) {
                sendTelegramMessage(chatId, "¡Hola " + client.getName() + ", en qué te puedo ayudar hoy?");
            } else {
                if (text.equalsIgnoreCase("/start")) {
                    sendTelegramMessage(chatId, "¡Bienvenido! ¿Cómo te llamas?");
                    setAskingName(chatId, true);
                } else if (isAskingName(chatId)) {
                    setUserName(chatId, text);
                    Client newClient = new Client(chatId, text);
                    addClient(newClient);

                    sendTelegramMessage(chatId, "¡Hola " + newClient.getName() + ", en qué te puedo ayudar hoy?");
                    setAskingName(chatId, false);
                } else {
                    String response = getUserName(chatId, "!Hola" + client.getName() + "!");
                    sendTelegramMessage(chatId, response);
                }
            }
        } else {
            System.out.println("La actualización no contiene un mensaje válido.");
        }
    }
}