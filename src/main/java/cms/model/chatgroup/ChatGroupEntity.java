package cms.model.chatgroup;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public class ChatGroupEntity implements Serializable{
	@Serial
    private static final long serialVersionUID = 1L;
	
	@Id @Column(length=36)
	protected String id;
	
	@Column(length=100)
	protected String groupName;
	
	protected Long ownerId;
	
	@Column(length=50)
	protected String ownerName;
	
	@Column(length=50)
	protected String avatarName;
	
	@Column(length=500)
	protected String description;
	
	@Column(columnDefinition = "DATETIME")
    protected LocalDateTime createTime = LocalDateTime.now();
	
	protected Long createTimeFormat;
	
	protected Integer status = 10;
	
	@Transient
	protected String avatarPath;
	
	@Transient
	protected Integer memberCount;
	
	@Transient
	protected String lastMessage;
	
	@Transient
	protected Long lastMessageTimeFormat;
	
	@Transient
	protected Integer unreadCount;
}
