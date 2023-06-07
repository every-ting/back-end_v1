package com.ting.ting.util;

import com.ting.ting.dto.idealPhoto.MixedImageResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class IdealPhotoManager {

    @Value("${machine-learning.server-url}")
    String mlServerUrl;

    private final WebClient webClient = WebClient.create();

    public MixedImageResponse mixIdealPhotos(String imageUrl1, String imageUrl2) {
        String imageKey1 = extractImageKeyFromUrl(imageUrl1);
        String imageKey2 = extractImageKeyFromUrl(imageUrl2);

        return webClient.get()
                .uri(mlServerUrl + "/mixTwoImages"+"/{imageKey1}/{imageKey2}/{ratio}", imageKey1, imageKey2, 0.5)
                .retrieve()
                .bodyToFlux(MixedImageResponse.class)
                .blockFirst();
    }

    public String extractImageKeyFromUrl(String url) {
        String[] parts = url.split("/");
        String filenameWithExtension = parts[parts.length - 1];
        String[] filenameParts = filenameWithExtension.split("\\.");
        return filenameParts[0];
    }
}
