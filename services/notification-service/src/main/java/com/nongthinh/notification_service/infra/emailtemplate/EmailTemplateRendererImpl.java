package com.nongthinh.notification_service.infra.emailtemplate;

import com.nongthinh.notification_service.application.port.out.EmailTemplateRenderer;
import com.nongthinh.notification_service.common.exception.ErrorCode;
import com.nongthinh.notification_service.domain.emailtemplate.EmailTemplateVariable;
import com.nongthinh.notification_service.domain.emailtemplate.RenderedEmailContent;
import com.nongthinh.notification_service.domain.exception.BusinessException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Component;

@Component
public class EmailTemplateRendererImpl implements EmailTemplateRenderer {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_]+)\\s*\\}\\}");

    @Override
    public Set<String> extractPlaceholders(String... contents) {
        Set<String> placeholders = new HashSet<>();

        for (String content : contents) {
            if (content == null || content.isBlank()) {
                continue;
            }

            Matcher matcher = PLACEHOLDER_PATTERN.matcher(content);
            while (matcher.find()) {
                placeholders.add(matcher.group(1));
            }
        }

        return placeholders;
    }

    @Override
    public void validatePlaceholders(
            String subject,
            String htmlContent,
            String textContent,
            List<EmailTemplateVariable> allowedVariables) {
        Set<String> allowedNames = allowedVariables.stream()
                .map(EmailTemplateVariable::getVariableName)
                .collect(Collectors.toSet());

        Set<String> usedPlaceholders = extractPlaceholders(subject, htmlContent, textContent);
        Set<String> invalidPlaceholders = usedPlaceholders.stream()
                .filter(name -> !allowedNames.contains(name))
                .collect(Collectors.toSet());

        if (!invalidPlaceholders.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.EMAIL_TEMPLATE_INVALID_PLACEHOLDER,
                    "Invalid placeholders: " + String.join(", ", invalidPlaceholders));
        }
    }

    @Override
    public void validateRequiredVariables(
            Map<String, ?> variables,
            List<EmailTemplateVariable> allowedVariables) {
        Map<String, ?> safeVariables = variables != null ? variables : Map.of();

        List<String> missingRequired = allowedVariables.stream()
                .filter(EmailTemplateVariable::isRequired)
                .map(EmailTemplateVariable::getVariableName)
                .filter(name -> !hasValue(safeVariables.get(name)))
                .toList();

        if (!missingRequired.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.EMAIL_TEMPLATE_MISSING_REQUIRED_VARIABLE,
                    "Missing required variables: " + String.join(", ", missingRequired));
        }
    }

    @Override
    public RenderedEmailContent render(
            String subject,
            String htmlContent,
            String textContent,
            Map<String, ?> variables) {
        Map<String, String> stringVariables = toStringMap(variables);

        return new RenderedEmailContent(
                substitute(subject, stringVariables),
                substitute(htmlContent, stringVariables),
                substitute(textContent, stringVariables));
    }

    private static boolean hasValue(Object value) {
        if (value == null) {
            return false;
        }

        if (value instanceof String stringValue) {
            return !stringValue.isBlank();
        }

        return true;
    }

    private static Map<String, String> toStringMap(Map<String, ?> variables) {
        Map<String, String> result = new HashMap<>();

        if (variables == null) {
            return result;
        }

        variables.forEach((key, value) -> result.put(key, value == null ? "" : String.valueOf(value)));
        return result;
    }

    private static String substitute(String content, Map<String, String> variables) {
        if (content == null) {
            return null;
        }

        StringSubstitutor substitutor = new StringSubstitutor(variables);
        substitutor.setVariablePrefix("{{");
        substitutor.setVariableSuffix("}}");
        substitutor.setEnableSubstitutionInVariables(false);
        return substitutor.replace(content);
    }
}
