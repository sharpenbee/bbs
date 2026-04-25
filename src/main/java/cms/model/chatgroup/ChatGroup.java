package cms.model.chatgroup;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name="chatgroup_0",indexes = {
		@Index(name="chatgroup_1_idx", columnList="ownerId,createTimeFormat"),
		@Index(name="chatgroup_2_idx", columnList="status,createTimeFormat")
})
public class ChatGroup extends ChatGroupEntity implements Serializable{
	@Serial
    private static final long serialVersionUID = 1L;
}
