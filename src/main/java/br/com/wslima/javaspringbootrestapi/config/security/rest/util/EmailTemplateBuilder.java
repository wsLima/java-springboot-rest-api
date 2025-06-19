package br.com.wslima.javaspringbootrestapi.config.security.rest.util;

public class EmailTemplateBuilder {

    public static String buildPasswordResetEmail(String name, String resetUrl) {
        return "<html>" +
                "<body>" +
                "<h3>Olá " + name + ",</h3>" +
                "<p>Recebemos uma solicitação para redefinir sua senha.</p>" +
                "<p>Clique no link abaixo para criar uma nova senha:</p>" +
                "<p><a href=\"" + resetUrl + "\">Redefinir senha</a></p>" +
                "<p>Se você não solicitou, pode ignorar este e-mail.</p>" +
                "<br><p>Equipe de Suporte</p>" +
                "</body>" +
                "</html>";
    }
}