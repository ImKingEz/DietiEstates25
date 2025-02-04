package dietiestates25ui.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dietiestates25ui.dto.LoginResponse;
import dietiestates25ui.dto.UtenteDTO;
import dietiestates25ui.model.Utente;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class UtenteService {
    private static final String BASE_URL = "http://localhost:8080/api/users";


    public HttpResponse<String> registraUtente(Utente user) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String jsonBody =
                "{\"nome\":\"" + user.getNome() + "\"," +
                        "\"cognome\":\"" + user.getCognome() + "\",";
        if (user.getCitta() != null && !user.getCitta().isEmpty()) {
            jsonBody += "\"citta\":\"" + user.getCitta() + "\",";
        }
        jsonBody +=
                "\"email\":\"" + user.getEmail() + "\"," +
                        "\"password\":\"" + user.getPassword() + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(BASE_URL + "/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response;
    }

    public String loginUtente(Utente user) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(BASE_URL + "/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"email\":\"" + user.getEmail() + "\",\"password\":\"" + user.getPassword() + "\"}"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            ObjectMapper objectMapper = new ObjectMapper();
            LoginResponse loginResponse = objectMapper.readValue(response.body(), LoginResponse.class);
            return loginResponse.getToken();
        } else {
            throw new Exception("Login failed : (" +  response.statusCode() + ")");
        }
    }

    public HttpResponse<String> updateUtente(Utente user, String token) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonBody;
        if (user.getCitta() != null) {
            jsonBody = objectMapper.writeValueAsString(
                    Map.of("nome", user.getNome(), "cognome", user.getCognome(), "citta", user.getCitta()));
        } else {
            jsonBody = objectMapper.writeValueAsString(
                    Map.of("nome", user.getNome(), "cognome", user.getCognome()));
        }
        System.out.println("jsonBody: {}" + jsonBody); // aggiungi log qui
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(BASE_URL + "/update"))
                .header("Content-Type", "application/json")
                .header("Authorization","Bearer " + token)
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        System.out.println("Request URL: {}" + request.uri()); // aggiungi log qui
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Response Status: {}" + response.statusCode());
        System.out.println("Response Body: {}" + response.body());
        return response;
    }

    public UtenteDTO getUtenteDetails(String token) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(BASE_URL + "/me"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(response.body(), UtenteDTO.class);
        } else {
            throw new Exception("Failed to get user details: " + response.statusCode());
        }
    }
}