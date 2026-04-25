package cms.model.chatgroup;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name="chatgroupmember_0",indexes = {
		@Index(name="chatgroupmember_1_idx", columnList="groupId,userId"),
		@Index(name="chatgroupmember_2_idx", columnList="userId,status")
})
public class ChatGroupMember extends ChatGroupMemberEntity implements Serializable{
	@Serial
    private static final long serialVersionUID = 1L;
}
