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
public class ChatGroupMemberEntity implements Serializable{
	@Serial
    private static final long serialVersionUID = 1L;
	
	@Id @Column(length=36)
	protected String id;
	
	@Column(length=36)
	protected String groupId;
	
	protected Long userId;
	
	@Column(length=50)
	protected String userName;
	
	@Column(length=50)
	protected String nickname;
	
	@Column(length=50)
	protected String avatarName;
	
	@Column(columnDefinition = "DATETIME")
    protected LocalDateTime joinTime = LocalDateTime.now();
	
	protected Long joinTimeFormat;
	
	protected Integer role = 10;
	
	protected Integer status = 10;
	
	@Transient
	protected String avatarPath;
	
	@Transient
	protected String account;
}
