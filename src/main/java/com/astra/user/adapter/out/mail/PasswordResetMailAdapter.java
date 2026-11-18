package com.astra.user.adapter.out.mail;

import com.astra.user.application.port.out.SendPasswordResetEmailPort;
import com.astra.user.domain.PasswordResetToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
class PasswordResetMailAdapter implements SendPasswordResetEmailPort {

    private final JavaMailSender mailSender;
    private final String from;
    private final String replyTo;
    private final String frontendUrl;

    PasswordResetMailAdapter(JavaMailSender mailSender, @Value("${astra.mail.from}") String from,
                             @Value("${astra.mail.reply-to:}") String replyTo,
                             @Value("${astra.frontend-url}") String frontendUrl) {
        this.mailSender = mailSender;
        this.from = from;
        this.replyTo = replyTo;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void sendPasswordReset(String email, String rawToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        if (!replyTo.isBlank()) {
            message.setReplyTo(replyTo);
        }
        message.setSubject("Redefina sua senha do Astra");
        message.setText("""
                Recebemos um pedido para redefinir a senha da sua conta no Astra.

                Para criar uma nova senha, abra o link abaixo. Ele vale por %d minutos e só pode ser usado uma vez:

                %s

                Se não foi você, ignore este e-mail. Sua senha continua a mesma.
                """.formatted(PasswordResetToken.LIFETIME.toMinutes(), frontendUrl + "/reset-password#token=" + rawToken));
        mailSender.send(message);
    }
}
