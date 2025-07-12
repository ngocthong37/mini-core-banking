package com.bank.accountservice.repository;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface IEmailServiceRepository {
    String sendMail(String to, String[] cc, String subject, Map<String, Object> model);
}
