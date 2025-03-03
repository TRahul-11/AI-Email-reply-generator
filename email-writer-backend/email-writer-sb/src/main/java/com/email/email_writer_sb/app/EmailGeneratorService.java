package com.email.email_writer_sb.app;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class EmailGeneratorService {
	
	private final WebClient webClient;
	

	  
	  public EmailGeneratorService(WebClient.Builder webClientBuilder) {
		super();
		this.webClient = webClientBuilder.build();
	}

 
	 public String generateEmailReply(EmailRequest emailRequest) {
		 
//	      Build the prompt
		  String prompt  = buildPrompt(emailRequest);
		  
//		  Craft a Request
		  Map<String , Object> requestBody = Map.of(
				  "contents", new Object[] {
						  Map.of("parts", new Object[] {
								  Map.of("text" , prompt)
					 })
				  }
				 );
//		  Do request and get Response
		  
		  String response = webClient.post()
				   .uri("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=AIzaSyB7UOneAVZ5YIsYzLg3WjkJFcmmyuKdLN4")
				   .header("Content-Type", "application/json")
				   .bodyValue(requestBody)
				   .retrieve()
				   .bodyToMono(String.class)
				   .block();
		  
		  
//	Extract Response and return
		  
		  return extractResponseContent(response);
		  
				  
		 
	 }

	private String extractResponseContent(String response) {
		
		 try {
			 ObjectMapper mapper = new ObjectMapper();
			 JsonNode rootNode = mapper.readTree(response);
			 return rootNode.path("candidates")
					 .get(0)
					 .path("content")
					 .path("parts")
					 .get(0)
					 .path("text")
					 .asText();
			
		} catch (Exception e) {
			return "Error in Processing " + e.getMessage(); 
		}
		  
	}

	private String buildPrompt(EmailRequest emailRequest) {
		
		StringBuilder prompt = new StringBuilder();
		prompt.append("Generate a professional Email Reply for the following Email content. please Don't Generate a subject Line ");
		if(emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()) {
			prompt.append("Use a ").append(emailRequest.getTone()).append(" tone");
		}
		prompt.append("\nOriginal email : \n").append(emailRequest.getEmailContent());
		
		return prompt.toString();
	}
	
}
