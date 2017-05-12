package ru.javaops.masterjava.service.mail;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Created by val on 2017-05-11.
 */
@Data
@AllArgsConstructor
public class MailData implements Serializable {
    private static final long serialVersionUID = 1L;
    private HashSet<Addressee> usersTo;
    private String subject;
    private String body;
}