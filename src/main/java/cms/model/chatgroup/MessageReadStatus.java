package cms.model.chatgroup;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name="messagereadstatus_0",indexes = {
		@Index(name="messagereadstatus_1_idx", columnList="messageId,userId"),
		@Index(name="messagereadstatus_2_idx", columnList="groupId,userId,readTimeFormat")
})
public class MessageReadStatus extends MessageReadStatusEntity implements Serializable{
	@Serial
    private static final long serialVersionUID = 1L;
}
