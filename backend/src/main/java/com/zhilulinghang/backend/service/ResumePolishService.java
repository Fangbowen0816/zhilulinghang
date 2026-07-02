package com.zhilulinghang.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhilulinghang.backend.config.DeepSeekProperties;
import com.zhilulinghang.backend.dto.ResumePolishRequest;
import com.zhilulinghang.backend.dto.ResumePolishResponse;
import com.zhilulinghang.backend.exception.AiServiceException;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class ResumePolishService {
    private static final int TITLE_LIMIT = 80;
    private static final int NAME_LIMIT = 50;
    private static final int CONTACT_LIMIT = 100;
    private static final int TARGET_LIMIT = 100;
    private static final int EDUCATION_LIMIT = 2000;
    private static final int EXPERIENCE_LIMIT = 3000;
    private static final int SKILLS_LIMIT = 1000;
    private static final int AWARDS_LIMIT = 2000;
    private static final int SELF_EVALUATION_LIMIT = 1000;
    private static final int SUMMARY_LIMIT = 500;

    private final DeepSeekProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient.Builder restClientBuilder;

    public ResumePolishService(DeepSeekProperties properties, ObjectMapper objectMapper, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClientBuilder = restClientBuilder;
    }

    public ResumePolishResponse polish(ResumePolishRequest request) {
        validateRequest(request);
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new AiServiceException("DeepSeek API Key 未配置");
        }

        Map<String, Object> payload = Map.of(
                "model", properties.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt()),
                        Map.of("role", "user", "content", userPrompt(request))
                ),
                "response_format", Map.of("type", "json_object"),
                "temperature", 0.2,
                "thinking", Map.of("type", "disabled"),
                "stream", false
        );

        try {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()));
            requestFactory.setReadTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()));

            RestClient restClient = restClientBuilder
                    .baseUrl(properties.getBaseUrl())
                    .requestFactory(requestFactory)
                    .build();

            String responseBody = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> headers.setBearerAuth(properties.getApiKey()))
                    .body(payload)
                    .retrieve()
                    .body(String.class);

            String content = extractAssistantContent(responseBody);
            ResumePolishResponse response = parsePolishResult(content);
            validateResponse(response, request);
            return response;
        } catch (RestClientResponseException exception) {
            throw new AiServiceException(resolveDeepSeekError(exception), exception);
        } catch (ResourceAccessException exception) {
            throw new AiServiceException("AI 润色请求超时或网络不可达，请检查网络、代理或 DeepSeek 服务状态", exception);
        } catch (AiServiceException exception) {
            throw exception;
        } catch (com.fasterxml.jackson.core.JsonProcessingException exception) {
            throw new AiServiceException("AI 返回内容格式异常，已尝试提取 JSON 但仍未成功，请稍后重试或补充更具体的润色目标", exception);
        } catch (Exception exception) {
            throw new AiServiceException("AI 润色服务暂时不可用", exception);
        }
    }

    private void validateRequest(ResumePolishRequest request) {
        if (request == null || !hasAnyText(request.getName(), request.getTargetPosition(), request.getEducation(), request.getExperience(), request.getSkills(), request.getAwards(), request.getSelfEvaluation())) {
            throw new IllegalArgumentException("请先填写简历内容，再使用 AI 润色");
        }
        checkLength("简历名称", request.getTitle(), TITLE_LIMIT);
        checkLength("姓名", request.getName(), NAME_LIMIT);
        checkLength("手机号", request.getPhone(), CONTACT_LIMIT);
        checkLength("邮箱", request.getEmail(), CONTACT_LIMIT);
        checkLength("求职意向", request.getTargetPosition(), TARGET_LIMIT);
        checkLength("教育经历", request.getEducation(), EDUCATION_LIMIT);
        checkLength("项目经历", request.getExperience(), EXPERIENCE_LIMIT);
        checkLength("技能", request.getSkills(), SKILLS_LIMIT);
        checkLength("奖项证书", request.getAwards(), AWARDS_LIMIT);
        checkLength("自我评价", request.getSelfEvaluation(), SELF_EVALUATION_LIMIT);
        checkLength("润色目标", request.getGoal(), SUMMARY_LIMIT);
    }

    private String extractAssistantContent(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        if (!content.isTextual() || !StringUtils.hasText(content.asText())) {
            throw new AiServiceException("AI 未返回有效润色内容");
        }
        return stripCodeFence(content.asText());
    }

    private ResumePolishResponse parsePolishResult(String content) throws Exception {
        String json = extractJsonObject(stripCodeFence(content));
        JsonNode root = objectMapper.readTree(json);
        if (root.isTextual() && StringUtils.hasText(root.asText())) {
            root = objectMapper.readTree(extractJsonObject(stripCodeFence(root.asText())));
        }
        if (root.has("resume") && root.get("resume").isObject()) {
            root = root.get("resume");
        } else if (root.has("result") && root.get("result").isObject()) {
            root = root.get("result");
        } else if (root.has("data") && root.get("data").isObject()) {
            root = root.get("data");
        }

        ResumePolishResponse response = new ResumePolishResponse();
        response.setTitle(readText(root, "title"));
        response.setName(readText(root, "name"));
        response.setPhone(readText(root, "phone"));
        response.setEmail(readText(root, "email"));
        response.setTargetPosition(readText(root, "targetPosition", "target_position"));
        response.setEducation(readText(root, "education"));
        response.setExperience(readText(root, "experience", "projectExperience", "project_experience"));
        response.setSkills(readText(root, "skills"));
        response.setAwards(readText(root, "awards"));
        response.setSelfEvaluation(readText(root, "selfEvaluation", "self_evaluation"));
        response.setSummary(readText(root, "summary"));
        return response;
    }

    private String resolveDeepSeekError(RestClientResponseException exception) {
        String remoteMessage = extractRemoteMessage(exception.getResponseBodyAsString());
        return switch (exception.getStatusCode().value()) {
            case 400 -> "AI 请求参数不正确，请检查模型名称、输出格式或请求内容" + suffix(remoteMessage);
            case 401 -> "DeepSeek API Key 无效或未授权，请检查 deepseek.api-key 配置" + suffix(remoteMessage);
            case 403 -> "DeepSeek API 权限不足，请检查账号权限或模型访问权限" + suffix(remoteMessage);
            case 404 -> "DeepSeek 接口地址或模型不存在，请检查 deepseek.base-url 和 deepseek.model 配置" + suffix(remoteMessage);
            case 429 -> "DeepSeek 请求过于频繁或额度不足，请稍后重试" + suffix(remoteMessage);
            default -> {
                if (exception.getStatusCode().is5xxServerError()) {
                    yield "DeepSeek 服务端异常，请稍后重试" + suffix(remoteMessage);
                }
                yield "AI 润色调用失败，HTTP 状态码：" + exception.getStatusCode().value() + suffix(remoteMessage);
            }
        };
    }

    private String extractRemoteMessage(String body) {
        if (!StringUtils.hasText(body)) {
            return "";
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode message = root.path("error").path("message");
            if (message.isTextual() && StringUtils.hasText(message.asText())) {
                return message.asText();
            }
            JsonNode plainMessage = root.path("message");
            if (plainMessage.isTextual() && StringUtils.hasText(plainMessage.asText())) {
                return plainMessage.asText();
            }
        } catch (Exception ignored) {
            return body.length() > 160 ? body.substring(0, 160) : body;
        }
        return "";
    }

    private String suffix(String message) {
        return StringUtils.hasText(message) ? "：" + message : "";
    }

    private void validateResponse(ResumePolishResponse response, ResumePolishRequest request) {
        if (response == null) {
            throw new AiServiceException("AI 返回内容格式不正确");
        }
        response.setTitle(defaultIfBlank(trim(response.getTitle()), trim(request.getTitle())));
        response.setName(defaultIfBlank(trim(response.getName()), trim(request.getName())));
        response.setPhone(defaultIfBlank(trim(response.getPhone()), trim(request.getPhone())));
        response.setEmail(defaultIfBlank(trim(response.getEmail()), trim(request.getEmail())));
        response.setTargetPosition(trim(response.getTargetPosition()));
        response.setEducation(trim(response.getEducation()));
        response.setExperience(trim(response.getExperience()));
        response.setSkills(trim(response.getSkills()));
        response.setAwards(trim(response.getAwards()));
        response.setSelfEvaluation(trim(response.getSelfEvaluation()));
        response.setSummary(trim(response.getSummary()));

        if (!hasAnyText(response.getTargetPosition(), response.getEducation(), response.getExperience(), response.getSkills(), response.getAwards(), response.getSelfEvaluation())) {
            throw new AiServiceException("AI 返回内容为空，请稍后重试");
        }
        checkLength("润色后简历名称", response.getTitle(), TITLE_LIMIT);
        checkLength("润色后姓名", response.getName(), NAME_LIMIT);
        checkLength("润色后手机号", response.getPhone(), CONTACT_LIMIT);
        checkLength("润色后邮箱", response.getEmail(), CONTACT_LIMIT);
        checkLength("润色后求职意向", response.getTargetPosition(), TARGET_LIMIT);
        checkLength("润色后教育经历", response.getEducation(), EDUCATION_LIMIT);
        checkLength("润色后项目经历", response.getExperience(), EXPERIENCE_LIMIT);
        checkLength("润色后技能", response.getSkills(), SKILLS_LIMIT);
        checkLength("润色后奖项证书", response.getAwards(), AWARDS_LIMIT);
        checkLength("润色后自我评价", response.getSelfEvaluation(), SELF_EVALUATION_LIMIT);
        checkLength("润色说明", response.getSummary(), SUMMARY_LIMIT);
    }

    private String systemPrompt() {
        return """
                你是高校就业指导场景中的中文简历润色助手。
                你的任务是基于学生提供的真实经历，优化表达、结构和专业度。
                禁止编造学校、项目、奖项、公司、技术栈、成果数字或任何学生未提供的信息。
                如果信息不足，只能改写已有信息，不得补充不存在的经历。
                语言应简洁、正式、适合校招简历，尽量突出职责、技术能力和结果表达。
                输出必须是 JSON，不要输出 Markdown，不要添加解释性段落。
                JSON 字段必须包含 title、name、phone、email、targetPosition、education、experience、skills、awards、selfEvaluation、summary。
                只输出一个 JSON 对象，不能使用 ```json 代码块，不能在 JSON 前后添加任何文字。
                字段名必须使用上面给出的英文 camelCase，所有字段值必须是字符串；没有内容时输出空字符串。
                对 phone、email、name 这类事实字段，只允许保留或规范格式，不得生成新内容。
                """;
    }

    private String userPrompt(ResumePolishRequest request) {
        return """
                学生原始简历：
                简历名称：%s
                姓名：%s
                手机号：%s
                邮箱：%s
                求职意向：%s
                教育经历：%s
                项目经历：%s
                技能：%s
                奖项证书：%s
                自我评价：%s

                润色要求/目标：
                %s

                请严格按以下 JSON 结构返回，不要添加任何额外文本：
                {
                  "title": "",
                  "name": "",
                  "phone": "",
                  "email": "",
                  "targetPosition": "",
                  "education": "",
                  "experience": "",
                  "skills": "",
                  "awards": "",
                  "selfEvaluation": "",
                  "summary": ""
                }
                """.formatted(
                blankToPlaceholder(request.getTitle()),
                blankToPlaceholder(request.getName()),
                blankToPlaceholder(request.getPhone()),
                blankToPlaceholder(request.getEmail()),
                blankToPlaceholder(request.getTargetPosition()),
                blankToPlaceholder(request.getEducation()),
                blankToPlaceholder(request.getExperience()),
                blankToPlaceholder(request.getSkills()),
                blankToPlaceholder(request.getAwards()),
                blankToPlaceholder(request.getSelfEvaluation()),
                blankToPlaceholder(request.getGoal())
        );
    }

    private boolean hasAnyText(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return true;
            }
        }
        return false;
    }

    private void checkLength(String label, String value, int limit) {
        if (value != null && value.length() > limit) {
            throw new IllegalArgumentException(label + "过长，最多 " + limit + " 个字符");
        }
    }

    private String stripCodeFence(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(?:json)?\\s*", "");
            trimmed = trimmed.replaceFirst("\\s*```$", "");
        }
        return trimmed.trim();
    }

    private String extractJsonObject(String content) {
        String trimmed = content == null ? "" : content.trim();
        if (!StringUtils.hasText(trimmed)) {
            throw new AiServiceException("AI 未返回有效 JSON 内容");
        }

        int start = trimmed.indexOf('{');
        if (start < 0) {
            throw new AiServiceException("AI 返回内容中没有 JSON 对象");
        }

        boolean inString = false;
        boolean escaped = false;
        int depth = 0;
        for (int i = start; i < trimmed.length(); i++) {
            char ch = trimmed.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (ch == '\\' && inString) {
                escaped = true;
                continue;
            }
            if (ch == '"') {
                inString = !inString;
                continue;
            }
            if (inString) {
                continue;
            }
            if (ch == '{') {
                depth++;
            } else if (ch == '}') {
                depth--;
                if (depth == 0) {
                    return trimmed.substring(start, i + 1);
                }
            }
        }
        throw new AiServiceException("AI 返回 JSON 对象不完整");
    }

    private String readText(JsonNode node, String... names) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return "";
        }
        for (String name : names) {
            JsonNode value = node.path(name);
            if (value.isTextual()) {
                return value.asText();
            }
            if (value.isNumber() || value.isBoolean()) {
                return value.asText();
            }
            if (value.isArray()) {
                StringBuilder builder = new StringBuilder();
                for (JsonNode item : value) {
                    if (item.isTextual() || item.isNumber() || item.isBoolean()) {
                        if (!builder.isEmpty()) {
                            builder.append("\n");
                        }
                        builder.append(item.asText());
                    }
                }
                if (!builder.isEmpty()) {
                    return builder.toString();
                }
            }
        }
        return "";
    }

    private String blankToPlaceholder(String value) {
        return StringUtils.hasText(value) ? value.trim() : "未填写";
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }
}
