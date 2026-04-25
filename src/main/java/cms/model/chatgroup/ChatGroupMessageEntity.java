package cms.model.chatgroup;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public class ChatGroupMessageEntity implements Serializable{
	@Serial
    private static final long serialVersionUID = 1L;
	
	@Id @Column(length=36)
	protected String id;
	
	@Column(length=36)
	protected String groupId;
	
	protected Long senderId;
	
	@Column(length=50)
	protected String senderName;
	
	@Column(length=50)
	protected String senderNickname;
	
	@Column(length=50)
	protected String senderAvatarName;
	
	@Lob
	protected String content;
	
	@Column(columnDefinition = "DATETIME")
    protected LocalDateTime sendTime = LocalDateTime.now();
	
	protected Long sendTimeFormat;
	
	protected Integer status = 10;
	
	@Transient
	protected String senderAvatarPath;
	
	@Transient
	protected Boolean isRead;
}
