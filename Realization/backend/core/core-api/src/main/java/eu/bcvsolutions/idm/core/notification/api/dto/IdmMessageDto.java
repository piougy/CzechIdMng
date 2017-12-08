package eu.bcvsolutions.idm.core.notification.api.dto;

import java.util.HashMap;
import java.util.Map;

import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonProperty;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;

/**
 * Notification message dto
 *
 * @author Peter Sourek
 * @author Radek Tomiška
 */
@Relation(collectionRelation = "messages")
public class IdmMessageDto extends AbstractDto {

    private static final long serialVersionUID = 1L;

    public static final NotificationLevel DEFAULT_LEVEL = NotificationLevel.INFO;

    @JsonProperty("level")
    private NotificationLevel level = DEFAULT_LEVEL;

    @JsonProperty("model")
    private ResultModel model;

    @JsonProperty("template")
    private IdmNotificationTemplateDto template;

    private String subject;
    private String textMessage;
    private String htmlMessage;
    private transient Map<String, Object> parameters;

    public IdmMessageDto(IdmMessageDto other) {
        super(other);
        //
        level = other.getLevel();
        subject = other.getSubject();
        textMessage = other.getTextMessage();
        htmlMessage = other.getHtmlMessage();
        model = other.getModel();
        template = other.getTemplate();
        parameters = other.getParameters() == null ? new HashMap<>() : new HashMap<>(other.getParameters());
    }

    private IdmMessageDto(Builder builder) {  
    	this.parameters = new HashMap<>();
    	//
        if (builder.model != null) {
        	// model - the lowest priority - template and manually given messages has the higher priority 
        	this.model = builder.model;        	
        	this.textMessage = builder.model.getMessage();
        	this.htmlMessage = builder.model.getMessage();
        	this.subject = builder.model.getStatusEnum();    
        	if (builder.model.getParameters() != null) {
        		this.parameters.putAll(builder.model.getParameters());
        	}
        	if (model.getStatus() != null) {
        		this.level = NotificationLevel.getLevel(model.getStatus());
        	}
        }
        if (builder.template != null) {
        	template = builder.template;
        	this.textMessage = builder.template.getBodyText();
        	this.htmlMessage = builder.template.getBodyHtml();
        	this.subject = builder.template.getSubject();
        }
        if (builder.subject != null) {
        	this.subject = builder.subject;
        }
    	if (builder.textMessage != null) {
        	this.textMessage = builder.textMessage;
        }
    	if (builder.htmlMessage != null) {
    		this.htmlMessage = builder.htmlMessage;
    	}
    	if (builder.level != null) {
    		this.level = builder.level;
    	}
    	if (builder.parameters != null) {
    		// higher priority than model parameters
    		this.getParameters().putAll(builder.parameters);
    	}
    	// make sure html is filled, if text is filled
    	if (this.htmlMessage == null) {
    		this.htmlMessage = this.textMessage;
    	}
    }

    public IdmMessageDto() {
    }

    public NotificationLevel getLevel() {
        return level;
    }

    public void setLevel(NotificationLevel level) {
        this.level = level;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTextMessage() {
        return textMessage;
    }

    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    public String getHtmlMessage() {
        return htmlMessage;
    }

    public void setHtmlMessage(String htmlMessage) {
        this.htmlMessage = htmlMessage;
    }

    public ResultModel getModel() {
        return model;
    }

    public void setModel(ResultModel model) {
        this.model = model;
    }

    public IdmNotificationTemplateDto getTemplate() {
        return template;
    }

    public void setTemplate(IdmNotificationTemplateDto template) {
        this.template = template;
    }

    public Map<String, Object> getParameters() {
    	if (parameters == null) {
    		parameters = new HashMap<>();
    	}
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    /**
     * Simple {@link IdmMessageDto} builder.
     *
     * @author Radek Tomiška
     */
    public static class Builder {

        private NotificationLevel level;
        private String subject;
        private String textMessage;
        private String htmlMessage;
        private ResultModel model;
        private IdmNotificationTemplateDto template;
        private Map<String, Object> parameters;

        public Builder() {
            // Default constructor
        }

        public Builder(NotificationLevel level) {
            this.level = level;
        }

        public Builder setLevel(NotificationLevel level) {
            this.level = level;
            return this;
        }

        public Builder setSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder setTextMessage(String textMessage) {
            this.textMessage = textMessage;
            return this;
        }

        public Builder setHtmlMessage(String htmlMessage) {
            this.htmlMessage = htmlMessage;
            return this;
        }

        public Builder setParameters(Map<String, Object> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder addParameter(String key, Object value) {
            if (this.parameters == null) {
                this.parameters = new HashMap<>();
            }
            this.parameters.put(key, value);
            return this;
        }

        public Builder setTemplate(IdmNotificationTemplateDto template) {
            this.template = template;
            return this;
        }

        /**
         * Sets all messages (text, html ...)
         *
         * @param message Both text and html message
         * @return This builder
         */
        public Builder setMessage(String message) {
            this.textMessage = message;
            this.htmlMessage = message;
            return this;
        }

        public Builder setModel(ResultModel model) {
            this.model = model;
            return this;
        }

        public IdmMessageDto build() {
            return new IdmMessageDto(this);
        }
    }

}
