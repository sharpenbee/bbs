package cms.repository.chatgroup;

import cms.dto.QueryResult;
import cms.model.chatgroup.ChatGroup;
import cms.model.chatgroup.ChatGroupMember;
import cms.model.chatgroup.ChatGroupMessage;
import cms.model.chatgroup.MessageReadStatus;
import cms.repository.besa.DAO;

import java.util.List;

public interface ChatGroupRepository extends DAO<ChatGroup> {

	ChatGroup findById(String groupId);
	
	QueryResult<ChatGroup> findChatGroupList(int firstIndex, int maxResult);
	
	QueryResult<ChatGroup> findChatGroupListByStatus(Integer status, int firstIndex, int maxResult);
	
	QueryResult<ChatGroup> findUserChatGroups(Long userId, int firstIndex, int maxResult);
	
	void saveChatGroup(ChatGroup chatGroup);
	
	void updateChatGroup(ChatGroup chatGroup);
	
	void deleteChatGroup(String groupId);
	
	void dissolveChatGroup(String groupId);
	
	ChatGroupMember findGroupMember(String groupId, Long userId);
	
	List<ChatGroupMember> findGroupMembers(String groupId);
	
	QueryResult<ChatGroupMember> findGroupMembersPage(String groupId, int firstIndex, int maxResult);
	
	void saveGroupMember(ChatGroupMember member);
	
	void deleteGroupMember(String groupId, Long userId);
	
	void updateGroupMember(ChatGroupMember member);
	
	Long countGroupMembers(String groupId);
	
	ChatGroupMessage findMessageById(String messageId);
	
	QueryResult<ChatGroupMessage> findGroupMessages(String groupId, int firstIndex, int maxResult);
	
	QueryResult<ChatGroupMessage> findGroupMessagesAfterTime(String groupId, Long timeFormat, int firstIndex, int maxResult);
	
	void saveMessage(ChatGroupMessage message);
	
	void deleteMessage(String messageId);
	
	Long countGroupMessages(String groupId);
	
	MessageReadStatus findReadStatus(String messageId, Long userId);
	
	void saveReadStatus(MessageReadStatus readStatus);
	
	void markMessageAsRead(String messageId, Long userId);
	
	void markAllMessagesAsRead(String groupId, Long userId);
	
	Long countUnreadMessages(String groupId, Long userId);
	
	Long countAllUnreadMessages(Long userId);
	
	ChatGroupMessage findLastMessage(String groupId);
}
