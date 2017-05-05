package eu.bcvsolutions.idm.core.notification.api.dto;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import org.springframework.hateoas.core.Relation;

/**
 * Notification template dto
 *
 * @author Peter Sourek
 */
@Relation(collectionRelation = "notificationTemplates")
public class IdmNotificationTemplateDto extends AbstractDto {

    private static final long serialVersionUID = 1L;

    private String name;
    private String code;
    private String subject;
    private String bodyHtml;
    private String bodyText;
    private String parameter;
    private boolean unmodifiable = false;
    private String module;

    public IdmNotificationTemplateDto(IdmNotificationTemplateDto template) {
        super(template);
        name = template.getName();
        code = template.getCode();
        subject = template.getSubject();
        bodyHtml = template.getBodyHtml();
        bodyText = template.getBodyText();
        parameter = template.getParameter();
        unmodifiable = template.isUnmodifiable();
        module = template.getModule();
    }

    public IdmNotificationTemplateDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBodyHtml() {
        return bodyHtml;
    }

    public void setBodyHtml(String bodyHtml) {
        this.bodyHtml = bodyHtml;
    }

    public String getBodyText() {
        return bodyText;
    }

    public void setBodyText(String bodyText) {
        this.bodyText = bodyText;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public boolean isUnmodifiable() {
        return unmodifiable;
    }

    public void setUnmodifiable(boolean unmodifiable) {
        this.unmodifiable = unmodifiable;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

}
