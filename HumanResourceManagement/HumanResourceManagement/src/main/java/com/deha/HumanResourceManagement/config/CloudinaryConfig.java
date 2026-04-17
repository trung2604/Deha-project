package com.deha.HumanResourceManagement.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;
import java.util.regex.Pattern;

@Configuration
public class CloudinaryConfig {
    private static final Pattern CLOUD_NAME_PATTERN = Pattern.compile("^[a-z0-9_-]+$");

    @Value("${cloudinary.cloud_name}")
    private String cloudName;
    @Value("${cloudinary.api_key}")
    private String apiKey;
    @Value("${cloudinary.api_secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        String normalizedCloudName = validateCloudName(cloudName);
        String normalizedApiKey = requireNonBlank(apiKey, "CLOUDINARY_API_KEY");
        String normalizedApiSecret = requireNonBlank(apiSecret, "CLOUDINARY_API_SECRET");

        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", normalizedCloudName,
                "api_key", normalizedApiKey,
                "api_secret", normalizedApiSecret,
                "secure", true
        ));
    }

    private String validateCloudName(String value) {
        String normalized = requireNonBlank(value, "CLOUDINARY_CLOUD_NAME");
        if (!CLOUD_NAME_PATTERN.matcher(normalized).matches()) {
            throw new IllegalStateException(
                    "Invalid CLOUDINARY_CLOUD_NAME: '" + normalized + "'. Cloud name must be lowercase and match [a-z0-9_-]."
            );
        }
        return normalized;
    }

    private String requireNonBlank(String value, String envName) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalStateException("Missing required environment variable: " + envName);
        }
        return normalized;
    }
}