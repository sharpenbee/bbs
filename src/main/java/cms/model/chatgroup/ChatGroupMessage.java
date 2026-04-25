package cms.model.chatgroup;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name="chatgroupmessage_0",indexes = {
		@Index(name="chatgroupmessage_1_idx", columnList="groupId,sendTimeFormat"),
		@Index(name="chatgroupmessage_2_idx", columnList="senderId,sendTimeFormat")
})
public class ChatGroupMessage extends ChatGroupMessageEntity implements Serializable{
	@Serial
    private static final long serialVersionUID = 1L;
}
