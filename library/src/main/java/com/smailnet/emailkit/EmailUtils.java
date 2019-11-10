package com.smailnet.emailkit;

import android.text.TextUtils;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

import java.util.Properties;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;

class EmailUtils {

    /**
     * 获取Session
     * @param config
     * @return
     */
    static Session getSession(EmailKit.Config config) {
        //获取配置对象
        String smtpHost = config.getSmtpHost();
        String imapHost = config.getImapHost();
        String smtpPort = String.valueOf(config.getSmtpPort());
        String imapPort = String.valueOf(config.getImapPort());

        //配置
        Properties properties = new Properties();
        if (!TextUtils.isEmpty(smtpHost) && !TextUtils.isEmpty(smtpPort)) {
            properties.put("mail.smtp.auth", true);
            properties.put("mail.smtp.host", smtpHost);
            properties.put("mail.smtp.port", smtpPort);
            properties.put("mail.smtp.ssl.enable", config.isSMTPSSL());
            if (config.getType() == EmailKit.MailType.OUTLOOK)
                properties.put("mail.smtp.starttls.enable", true);
        }
        if (!TextUtils.isEmpty(imapHost) && !TextUtils.isEmpty(imapPort)) {
            properties.put("mail.imap.auth", true);
            properties.put("mail.imap.host", imapHost);
            properties.put("mail.imap.port", imapPort);
            properties.put("mail.imap.ssl.enable", config.isIMAPSSL());
        }

        //返回值
        if (ObjectManager.getSession() == null) {
            Session session = Session.getInstance(properties);
            ObjectManager.setSession(session);
            return session;
        } else {
            return ObjectManager.getSession();
        }
    }

    /**
     * 获取Transport
     * @param config
     * @return
     * @throws MessagingException
     */
    static Transport getTransport(EmailKit.Config config) throws MessagingException {
        if (ObjectManager.getTransport() == null || !ObjectManager.getTransport().isConnected()) {
            Session session = getSession(config);
            Transport transport = session.getTransport("smtp");
            transport.connect(config.getSmtpHost(), config.getAccount(), config.getPassword());
            ObjectManager.setTransport(transport);
            return transport;
        } else {
            return ObjectManager.getTransport();
        }
    }

    /**
     * 获取IMAPStore
     * @param config
     * @return
     * @throws MessagingException
     */
    static IMAPStore getStore(EmailKit.Config config) throws MessagingException {
        if (ObjectManager.getStore() == null || !ObjectManager.getStore().isConnected()) {
            Session session = getSession(config);
            IMAPStore store = (IMAPStore) session.getStore("imap");
            store.connect(config.getImapHost(), config.getAccount(), config.getPassword());
            ObjectManager.setStore(store);
            return store;
        } else {
            return ObjectManager.getStore();
        }
    }

    /**
     * 获取IMAPFolder
     * @param folderName
     * @param store
     * @param config
     * @return
     * @throws MessagingException
     */
    static IMAPFolder getFolder(String folderName, IMAPStore store, EmailKit.Config config) throws MessagingException {
        IMAPFolder folder = (IMAPFolder) store.getFolder(folderName);
        if (config.getType() == EmailKit.MailType.$163 || config.getType() == EmailKit.MailType.$126) {
            folder.doCommand(protocol -> {
                protocol.id("FUTONG");
                return null;
            });
        }
        folder.open(Folder.READ_WRITE);
        return folder;
    }

}