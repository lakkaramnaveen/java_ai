package com.thenaveen.demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.openai.audio.speech.SpeechModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class JavaController {

    private final ChatClient client;
    private final SpeechModel speechModel;
    private final ImageModel imageModel;
    private final VectorStore vectorStore;

    private final static String DIR_IN = "Users/naveenkumarlakkaram/files/in";
    private final static String DIR_OUT = "Users/naveenkumarlakkaram/files/in";

    public JavaController(ChatClient.Builder builder, SpeechModel speechModel, ImageModel imageModel, VectorStore vectorStore){
        this.client = builder.defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory(), "default", 10)).build();
        this.speechModel = speechModel;
        this.imageModel = imageModel;
        this.vectorStore = vectorStore;
    }

    @GetMapping
    public String getString(@RequestParam(defaultValue = "what is the meaning of Life") String message){
        return client.prompt().user(message).call().content();
    }

    @GetMapping("/response")
    public ChatResponse getResponse(@RequestParam(defaultValue = "what is the meaning of Life") String message){
        return client.prompt().user(message).call().chatResponse();
    }

    @GetMapping("/entity")
    public MyResponse getEntity(@RequestParam(defaultValue = "what is the meaning of Life") String message){
        return client.prompt().user(message).call().entity(MyResponse.class);
    }

    record MyResponse(String generationTokens, String promptTokens, String totalTokens, String text){ }

    @GetMapping("/rag")
    public String getResponseFromOurData(@RequestParam(defaultValue = "Airspeeds") String message){
        // so called rag
        return client.prompt().user(message).advisors(new QuestionAnswerAdvisor(vectorStore)).call().content();
    }

    @GetMapping("/template")
    public String getResponseFromTemplate(@RequestParam String type, @RequestParam String topic){
        return client.prompt().user(u -> u.text("Create a {type} about {topic}, limit to 100 words").param("type", type).param("topic", topic)).call().content();
    }

    @GetMapping("/conversation")
    public String getConversation(@RequestParam(defaultValue = "what is the meaning of life?") String message, @RequestParam(defaultValue = "default") String convId){
        return client.prompt().user(message).advisors(as -> as.param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, convId)).call().content();
    }
}
