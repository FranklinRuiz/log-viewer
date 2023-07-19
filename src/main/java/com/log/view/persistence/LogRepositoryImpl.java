package com.log.view.persistence;

import com.log.view.application.dto.LogResponseDto;
import com.log.view.infrastructure.DataReader;
import com.log.view.infrastructure.model.LogModel;
import com.log.view.persistence.mapper.LogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LogRepositoryImpl implements LogRepository {

    private final DataReader dataReader;

    @Value("${data.pagination.pageSize}")
    private int pageSize;

    @Override
    public List<String> allComponents() {
        return dataReader.deploymentTextReader();
    }

    @Override
    public List<LogResponseDto> logsFilter(String traceId, String message, int pageNumber) {
        List<LogModel> logModels = dataReader.jsonLogReader();
        if (!traceId.trim().isEmpty() || !message.trim().isEmpty()) {

            List<String> filters = filterMessageLog(message);
            List<String> excludes = excludeMessageLog(message);

            logModels = logModels.stream()
                    .filter(log -> traceId.trim().isEmpty() || log.getTraceId().equals(traceId.trim()))
                    .filter(log -> message.isEmpty()
                            || (filters.stream().allMatch(filter -> log.getMessage().contains(filter))
                            && excludes.stream().noneMatch(exclude -> log.getMessage().contains(exclude))))
                    .collect(Collectors.toList());
        }
        logModels.sort(Comparator.comparing(LogModel::getTimestamp));

        pageNumber = (logModels.size() / pageSize) - pageNumber;

        List<LogModel> page = logModels.stream()
                .skip(pageNumber * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
        return page.stream().map(LogMapper.INSTANCE::map).collect(Collectors.toList());
    }

    private List<String> filterMessageLog(String message) {
        List<String> messageQuotesExtracted = new ArrayList<>();
        Pattern quotesPattern = Pattern.compile("\"(.*?)\"");
        Matcher quotesMatcher = quotesPattern.matcher(message);
        while (quotesMatcher.find()) {
            messageQuotesExtracted.add(quotesMatcher.group(1));
        }

        messageQuotesExtracted.removeIf(excludeMessageLog(message)::contains);

        if (messageQuotesExtracted.isEmpty()) {
            messageQuotesExtracted.add("");
        }

        return messageQuotesExtracted;
    }

    private List<String> excludeMessageLog(String message) {
        List<String> excludedMessages = new ArrayList<>();
        Pattern hyphenQuotesPattern = Pattern.compile("-\"(.*?)\"");
        Matcher hyphenQuotesMatcher = hyphenQuotesPattern.matcher(message);
        while (hyphenQuotesMatcher.find()) {
            excludedMessages.add(hyphenQuotesMatcher.group(1));
        }
        return excludedMessages;
    }

}
