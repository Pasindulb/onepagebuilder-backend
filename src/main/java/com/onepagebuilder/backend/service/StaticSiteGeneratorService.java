package com.onepagebuilder.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.Map;

@Service
public class StaticSiteGeneratorService {

    private final TemplateEngine templateEngine;
    private final ObjectMapper objectMapper;

    public StaticSiteGeneratorService() {
        this.objectMapper = new ObjectMapper();
        
        // Configure Thymeleaf for string templates
        StringTemplateResolver templateResolver = new StringTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
    }

    @SuppressWarnings("unchecked")
    public String generateStaticHTML(String projectName, String publishedConfig) {
        try {
            JsonNode config = objectMapper.readTree(publishedConfig);
            
            // Convert JsonNodes to Maps so OGNL can access properties
            Map<String, Object> navbar = config.has("navbar") && !config.get("navbar").isNull() 
                ? objectMapper.convertValue(config.get("navbar"), Map.class) 
                : null;
            Map<String, Object> hero = config.has("hero") && !config.get("hero").isNull()
                ? objectMapper.convertValue(config.get("hero"), Map.class)
                : null;

            // Build style strings in Java to avoid complex Thymeleaf expressions
            if (navbar != null) {
                navbar.put("computedStyle", buildNavbarStyle(navbar));
            }
            if (hero != null) {
                hero.put("computedStyle", buildHeroStyle(hero));
            }

            Context context = new Context();
            context.setVariable("projectName", projectName);
            context.setVariable("navbar", navbar);
            context.setVariable("hero", hero);

            String template = buildHTMLTemplate();
            return templateEngine.process(template, context);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate static HTML", e);
        }
    }

    private String buildNavbarStyle(Map<String, Object> navbar) {
        StringBuilder style = new StringBuilder();
        
        String height = navbar.get("height") != null ? navbar.get("height").toString() : "64px";
        style.append("height: ").append(height).append("; ");
        
        if (navbar.get("backgroundColor") != null) {
            style.append("background-color: ").append(navbar.get("backgroundColor")).append("; ");
        }
        if (navbar.get("textColor") != null) {
            style.append("color: ").append(navbar.get("textColor")).append("; ");
        }
        if (navbar.get("fontFamily") != null) {
            style.append("font-family: ").append(navbar.get("fontFamily")).append("; ");
        }
        if (navbar.get("fontSize") != null) {
            style.append("font-size: ").append(navbar.get("fontSize")).append("; ");
        }
        
        return style.toString();
    }

    private String buildHeroStyle(Map<String, Object> hero) {
        StringBuilder style = new StringBuilder();
        
        if (hero.get("backgroundColor") != null) {
            style.append("background-color: ").append(hero.get("backgroundColor")).append("; ");
        }
        if (hero.get("backgroundImage") != null) {
            style.append("background-image: url(").append(hero.get("backgroundImage")).append("); ");
            style.append("background-size: cover; background-position: center; ");
        }
        
        String minHeight = hero.get("minHeight") != null ? hero.get("minHeight").toString() : "500px";
        style.append("min-height: ").append(minHeight).append("; ");
        
        if (hero.get("fontFamily") != null) {
            style.append("font-family: ").append(hero.get("fontFamily")).append("; ");
        }
        
        return style.toString();
    }

    private String buildHTMLTemplate() {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title th:text="${projectName}">Website</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        html {
            scroll-behavior: smooth;
        }
    </style>
</head>
<body>
    <!-- Navbar -->
    <nav th:if="${navbar}" 
         th:attr="style=${navbar.computedStyle}"
         th:class="${navbar.position == 'fixed' ? 'sticky top-0 z-50' : 'relative'}"
         class="w-full shadow-md">
        <div class="max-w-7xl mx-auto px-4 h-full flex items-center justify-between">
            <!-- Brand -->
            <div class="flex items-center gap-3">
                <img th:if="${navbar.logo}" th:src="${navbar.logo}" alt="Logo" class="h-8 w-8 object-contain"/>
                <span class="text-xl font-bold" th:text="${navbar.brandName}">Brand</span>
            </div>
            
            <!-- Nav Items -->
            <div class="flex items-center gap-1 flex-1 px-8">
                <!-- Left aligned items -->
                <div class="flex gap-6">
                    <a th:each="item : ${navbar.navItems}" 
                       th:if="${item.alignment == null or item.alignment == 'left'}"
                       th:href="${item.link}" 
                       th:text="${item.title}"
                       class="hover:opacity-80 transition-opacity">
                    </a>
                </div>
                
                <!-- Center aligned items -->
                <div class="flex gap-6 mx-auto">
                    <a th:each="item : ${navbar.navItems}" 
                       th:if="${item.alignment == 'center'}"
                       th:href="${item.link}" 
                       th:text="${item.title}"
                       class="hover:opacity-80 transition-opacity">
                    </a>
                </div>
                
                <!-- Right aligned items -->
                <div class="flex gap-6 ml-auto">
                    <a th:each="item : ${navbar.navItems}" 
                       th:if="${item.alignment == 'right'}"
                       th:href="${item.link}" 
                       th:text="${item.title}"
                       class="hover:opacity-80 transition-opacity">
                    </a>
                </div>
            </div>
        </div>
    </nav>

    <!-- Hero Section -->
    <section th:if="${hero}"
             class="w-full relative flex items-center justify-center"
             th:attr="style=${hero.computedStyle}">
        
        <!-- Overlay if background image exists -->
        <div th:if="${hero.backgroundImage}" class="absolute inset-0 bg-black opacity-40"></div>
        
        <div class="relative z-10 max-w-6xl mx-auto px-8 py-16"
             th:class="${hero.textAlignment == 'center' ? 'text-center' : 'text-left'}"
             th:attr="style='color: ' + ${hero.textColor}">
            
            <!-- Heading -->
            <h1 class="font-bold mb-6 leading-tight"
                th:text="${hero.heading}"
                th:attr="style='font-size: ' + (${hero.fontSize} != null ? ${hero.fontSize} : '48px')">
            </h1>
            
            <!-- Subheading -->
            <p th:if="${hero.subheading}"
               th:text="${hero.subheading}"
               class="text-lg mb-8 opacity-90 max-w-2xl"
               th:attr="style=${hero.textAlignment == 'center' ? 'margin-left: auto; margin-right: auto;' : ''}">
            </p>
            
            <!-- Buttons -->
            <div th:if="${hero.buttons != null and #lists.size(hero.buttons) > 0}"
                 class="flex gap-4"
                 th:class="${hero.textAlignment == 'center' ? 'justify-center' : 'justify-start'}">
                <a th:each="button : ${hero.buttons}"
                   th:href="${button.link}"
                   th:text="${button.text}"
                   class="px-8 py-3 rounded-lg font-semibold transition-all duration-300"
                   th:class="${button.variant == 'primary' ? 'bg-white text-gray-900 hover:bg-gray-100 shadow-lg hover:shadow-xl' : 'border-2 border-white text-white hover:bg-white hover:text-gray-900'}">
                </a>
            </div>
        </div>
    </section>

    <!-- Footer -->
    <footer class="bg-gray-900 text-white py-8 text-center">
        <p class="text-sm">© 2025 <span th:text="${projectName}">Website</span>. Built with OnePageBuilder.</p>
    </footer>
</body>
</html>
                """;
    }
}
