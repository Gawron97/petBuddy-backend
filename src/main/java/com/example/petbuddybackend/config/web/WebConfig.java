package com.example.petbuddybackend.config.web;

import com.example.petbuddybackend.utils.conversion.converter.ZonedDateTimeConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new ZonedDateTimeConverter());
    }

}
