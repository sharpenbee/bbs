package cms.model.chatgroup;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public class MessageReadStatusEntity implements Serializable{
	@Serial
    private static final long serialVersionUID = 1L;
	
	@Id @Column(length=36)
	protected String id;
	
	@Column(length=36)
	protected String messageId;
	
	@Column(length=36)
	protected String groupId;
	
	protected Long userId;
	
	@Column(columnDefinition = "DATETIME")
    protected LocalDateTime readTime = LocalDateTime.now();
	
	protected Long readTimeFormat;
}
