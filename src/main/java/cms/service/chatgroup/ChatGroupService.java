package cms.service.chatgroup;

import cms.dto.PageView;
import cms.model.chatgroup.ChatGroup;
import cms.model.chatgroup.ChatGroupMember;
import cms.model.chatgroup.ChatGroupMessage;

import java.util.List;
import java.util.Map;

public interface ChatGroupService {

    Map<String,Object> createGroup(String groupName, String description, List<Long> memberUserIds, String fileServerAddress);

    Map<String,Object> getGroupList(int page, String fileServerAddress);

    Map<String,Object> getGroupDetail(String groupId, String fileServerAddress);

    void addMembers(String groupId, List<Long> memberUserIds);

    void removeMember(String groupId, Long userId);

    ChatGroupMessage sendMessage(String groupId, String content, String fileServerAddress);

    Map<String,Object> getMessageList(String groupId, int page, String fileServerAddress);

    void markMessageAsRead(String messageId);

    void markAllMessagesAsRead(String groupId);

    Long getUnreadCount(String groupId);

    Long getAllUnreadCount();

    Map<String,Object> getMemberList(String groupId, int page, String fileServerAddress);

    void dissolveGroup(String groupId);

    PageView<ChatGroup> getAdminGroupList(int page, Integer status);

    ChatGroup getAdminGroupDetail(String groupId);

    void adminDissolveGroup(String groupId);

    void adminDeleteGroup(String groupId);
}
