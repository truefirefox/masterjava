package ru.javaops.masterjava.service.mail;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by val on 2017-05-11.
 */
@Data
@AllArgsConstructor
public class MailData implements Serializable {
    private static final long serialVersionUID = 1L;
    private String users;
    private String subject;
    private String body;
    private HashMap<String, byte[]> attaches;
}