package umg.edu.gt.Telebot.GPT.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import umg.edu.gt.Telebot.GPT.Model.ChatMessage;
import umg.edu.gt.Telebot.GPT.Model.Client;
import umg.edu.gt.Telebot.GPT.Model.ErrorLog;
import umg.edu.gt.Telebot.GPT.Repository.ChatMessageRepository;
import umg.edu.gt.Telebot.GPT.Repository.ClientRepository;
import umg.edu.gt.Telebot.GPT.Repository.ErrorLogRepository;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class BotService {

    private final String BOT_TOKEN = "7822338733:AAH3RJF87rr4QmkqRvjpIlUiZYhHS8zBTaQ"; // Reemplaza con tu token
    private final String TELEGRAM_API_URL = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage";
    private final String OPENAI_API_URL = "";
    private final String OPENAI_API_KEY = "";

    private final ClientRepository clientRepository;
    private final SendNotification sendNotification;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ErrorLogRepository errorLogRepository;

    public void logError(ChatMessage chatMessage, Exception e) {
        ErrorLog errorLog = new ErrorLog();
        errorLog.setChatMessage(chatMessage);
        errorLog.setErrorMessage(e.getMessage());
        errorLog.setStackTrace(getStackTraceAsString(e));
        errorLog.setTimestamp(LocalDateTime.now());
        errorLogRepository.save(errorLog);
    }

    private String getStackTraceAsString(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }

    @Autowired
    public BotService(ClientRepository clientRepository, SendNotification sendNotification) {
        this.clientRepository = clientRepository;
        this.sendNotification = sendNotification;
        sendNotification.notifyCacheInitialization();
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

    public String getUserName(Long chatId) {
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
        System.out.println("Consultando a la base de datos para chatId: " + chatId);
        return clientRepository.getById(chatId);
    }

    @CacheEvict(value = "clients", key = "#chatId")
    public void setClient(Long chatId, Client client) {
        try {
            clientRepository.add(client.getName(), chatId);
            sendNotification.notifyCacheEviction(chatId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @CachePut(value = "clients", key = "#client.clientId")
    public Client addClient(Client client) throws SQLException {
        clientRepository.add(client.getName(), client.getClientId());
        sendNotification.notifyClientCached(client);
        return client;
    }

    public String getChatGptResponse(String message) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(OPENAI_API_KEY);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model","gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role","user","content", message)
                ));
        requestBody.put("max_tokens", 150);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(OPENAI_API_URL, request, Map.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> responseBody = response.getBody();
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String,Object> messageContent = (Map<String, Object>) choices.get(0).get("message");
                return (String) messageContent.get("content");
            }
        }
        return "Lo siento, no pude obtener una respuesta en este momento.";
    }

    public void handleUpdate(Map<String, Object> update) throws SQLException {
        ChatMessage chatMessage = null;
        try {
            if (update.containsKey("message")) {
                Map<String, Object> message = (Map<String, Object>) update.get("message");
                Map<String, Object> chat = (Map<String, Object>) message.get("chat");
                long chatId = ((Number) chat.get("id")).longValue();
                String text = (String) message.get("text");

                chatMessage = new ChatMessage();
                chatMessage.setChatId(chatId);
                chatMessage.setMessage(text);
                chatMessage.setTimestamp(LocalDateTime.now());
                chatMessageRepository.save(chatMessage);

                Client client = getClientById(chatId);

                if (client != null) {
                    sendNotification.notifyCacheHit(chatId);
                    sendTelegramMessage(chatId, "¡Hola " + client.getName() + ", en qué te puedo ayudar hoy?");
                } else {
                    if (text.equalsIgnoreCase("/start")) {
                        sendTelegramMessage(chatId, "¡Bienvenido! ¿Cómo te llamas?");
                        setAskingName(chatId, true);
                    } else if (isAskingName(chatId)) {
                        setUserName(chatId, text);
                        clientRepository.add(text, chatId);

                        Client newClient = clientRepository.getById(chatId);
                        sendTelegramMessage(chatId, "¡Hola " + newClient.getName() + ", en qué te puedo ayudar hoy?");
                        setAskingName(chatId, false);
                        System.out.println("nombre guardado: " + newClient.getName());
                    } else {
                        String response = getChatGptResponse(text);
                        sendTelegramMessage(chatId, response);
                        System.out.println("Respuesta enviada: " + response);
                    }
                }
            } else {
                System.out.println("La actualización no contiene un mensaje válido.");
            }
        } catch (Exception e) {
            if (chatMessage != null) {
                logError(chatMessage, e);
            } else {
                System.err.println("Error sin mensaje de chat asociado: " + e.getMessage());
            }
            e.printStackTrace();
        }

       }
    }