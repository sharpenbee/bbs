package cms.service.chatgroup.impl;

import cms.config.BusinessException;
import cms.dto.PageView;
import cms.dto.QueryResult;
import cms.dto.user.AccessUser;
import cms.model.chatgroup.ChatGroup;
import cms.model.chatgroup.ChatGroupMember;
import cms.model.chatgroup.ChatGroupMessage;
import cms.model.user.User;
import cms.repository.chatgroup.ChatGroupRepository;
import cms.repository.setting.SettingRepository;
import cms.repository.user.UserRepository;
import cms.service.chatgroup.ChatGroupService;
import cms.utils.AccessUserThreadLocal;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ChatGroupServiceImpl implements ChatGroupService {

    @Resource
    ChatGroupRepository chatGroupRepository;
    @Resource
    SettingRepository settingRepository;
    @Resource
    UserRepository userRepository;

    private AccessUser getCurrentUser() {
        AccessUser accessUser = AccessUserThreadLocal.get();
        if (accessUser == null || accessUser.getId() == null) {
            throw new BusinessException(Map.of("error", "用户未登录"));
        }
        return accessUser;
    }

    @Override
    @Transactional
    public Map<String, Object> createGroup(String groupName, String description, List<Long> memberUserIds, String fileServerAddress) {
        AccessUser currentUser = getCurrentUser();
        Long ownerId = currentUser.getId();
        String ownerName = currentUser.getUserName();

        if (groupName == null || groupName.trim().isEmpty()) {
            throw new BusinessException(Map.of("groupName", "群聊名称不能为空"));
        }

        ChatGroup chatGroup = new ChatGroup();
        chatGroup.setId(UUID.randomUUID().toString());
        chatGroup.setGroupName(groupName);
        chatGroup.setDescription(description);
        chatGroup.setOwnerId(ownerId);
        chatGroup.setOwnerName(ownerName);
        chatGroup.setStatus(10);
        chatGroupRepository.saveChatGroup(chatGroup);

        User owner = userRepository.findUserById(ownerId);
        ChatGroupMember ownerMember = new ChatGroupMember();
        ownerMember.setId(UUID.randomUUID().toString());
        ownerMember.setGroupId(chatGroup.getId());
        ownerMember.setUserId(ownerId);
        ownerMember.setUserName(ownerName);
        ownerMember.setNickname(owner != null ? owner.getNickname() : ownerName);
        ownerMember.setAvatarName(owner != null ? owner.getAvatarName() : null);
        ownerMember.setRole(30);
        ownerMember.setStatus(10);
        chatGroupRepository.saveGroupMember(ownerMember);

        if (memberUserIds != null && !memberUserIds.isEmpty()) {
            for (Long userId : memberUserIds) {
                if (!userId.equals(ownerId)) {
                    User user = userRepository.findUserById(userId);
                    if (user != null && user.getState() == 1) {
                        ChatGroupMember member = new ChatGroupMember();
                        member.setId(UUID.randomUUID().toString());
                        member.setGroupId(chatGroup.getId());
                        member.setUserId(userId);
                        member.setUserName(user.getUserName());
                        member.setNickname(user.getNickname());
                        member.setAvatarName(user.getAvatarName());
                        member.setRole(10);
                        member.setStatus(10);
                        chatGroupRepository.saveGroupMember(member);
                    }
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("groupId", chatGroup.getId());
        result.put("groupName", chatGroup.getGroupName());
        result.put("ownerId", chatGroup.getOwnerId());
        result.put("memberCount", chatGroupRepository.countGroupMembers(chatGroup.getId()));
        return result;
    }

    @Override
    public Map<String, Object> getGroupList(int page, String fileServerAddress) {
        AccessUser currentUser = getCurrentUser();
        Long userId = currentUser.getId();

        Map<String, Object> returnValue = new HashMap<>();
        PageView<ChatGroup> pageView = new PageView<>(settingRepository.findSystemSetting_cache().getBackstagePageNumber(), page, 20);
        int firstIndex = (page - 1) * pageView.getMaxresult();

        QueryResult<ChatGroup> qr = chatGroupRepository.findUserChatGroups(userId, firstIndex, pageView.getMaxresult());
        if (qr != null && qr.getResultlist() != null) {
            for (ChatGroup group : qr.getResultlist()) {
                group.setMemberCount(chatGroupRepository.countGroupMembers(group.getId()).intValue());
                group.setUnreadCount(chatGroupRepository.countUnreadMessages(group.getId(), userId).intValue());
                
                ChatGroupMessage lastMessage = chatGroupRepository.findLastMessage(group.getId());
                if (lastMessage != null) {
                    group.setLastMessage(lastMessage.getContent());
                    group.setLastMessageTimeFormat(lastMessage.getSendTimeFormat());
                }
            }
        }
        pageView.setQueryResult(qr);
        returnValue.put("pageView", pageView);
        return returnValue;
    }

    @Override
    public Map<String, Object> getGroupDetail(String groupId, String fileServerAddress) {
        AccessUser currentUser = getCurrentUser();
        Long userId = currentUser.getId();

        ChatGroup chatGroup = chatGroupRepository.findById(groupId);
        if (chatGroup == null) {
            throw new BusinessException(Map.of("error", "群聊不存在"));
        }

        if (chatGroup.getStatus() != 10) {
            throw new BusinessException(Map.of("error", "该群聊已解散"));
        }

        ChatGroupMember member = chatGroupRepository.findGroupMember(groupId, userId);
        if (member == null || member.getStatus() != 10) {
            throw new BusinessException(Map.of("error", "您不是该群聊成员"));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("groupId", chatGroup.getId());
        result.put("groupName", chatGroup.getGroupName());
        result.put("description", chatGroup.getDescription());
        result.put("ownerId", chatGroup.getOwnerId());
        result.put("ownerName", chatGroup.getOwnerName());
        result.put("memberCount", chatGroupRepository.countGroupMembers(groupId).intValue());
        result.put("unreadCount", chatGroupRepository.countUnreadMessages(groupId, userId).intValue());
        result.put("myRole", member.getRole());
        return result;
    }

    @Override
    @Transactional
    public void addMembers(String groupId, List<Long> memberUserIds) {
        AccessUser currentUser = getCurrentUser();
        Long userId = currentUser.getId();

        ChatGroup chatGroup = chatGroupRepository.findById(groupId);
        if (chatGroup == null) {
            throw new BusinessException(Map.of("error", "群聊不存在"));
        }

        if (chatGroup.getStatus() != 10) {
            throw new BusinessException(Map.of("error", "该群聊已解散"));
        }

        ChatGroupMember operator = chatGroupRepository.findGroupMember(groupId, userId);
        if (operator == null || operator.getStatus() != 10) {
            throw new BusinessException(Map.of("error", "您不是该群聊成员"));
        }

        if (operator.getRole() < 20) {
            throw new BusinessException(Map.of("error", "只有群主或管理员才能拉人进群"));
        }

        if (memberUserIds != null && !memberUserIds.isEmpty()) {
            for (Long targetUserId : memberUserIds) {
                ChatGroupMember existingMember = chatGroupRepository.findGroupMember(groupId, targetUserId);
                if (existingMember == null) {
                    User user = userRepository.findUserById(targetUserId);
                    if (user != null && user.getState() == 1) {
                        ChatGroupMember newMember = new ChatGroupMember();
                        newMember.setId(UUID.randomUUID().toString());
                        newMember.setGroupId(groupId);
                        newMember.setUserId(targetUserId);
                        newMember.setUserName(user.getUserName());
                        newMember.setNickname(user.getNickname());
                        newMember.setAvatarName(user.getAvatarName());
                        newMember.setRole(10);
                        newMember.setStatus(10);
                        chatGroupRepository.saveGroupMember(newMember);
                    }
                } else if (existingMember.getStatus() != 10) {
                    existingMember.setStatus(10);
                    chatGroupRepository.updateGroupMember(existingMember);
                }
            }
        }
    }

    @Override
    @Transactional
    public void removeMember(String groupId, Long userId) {
        AccessUser currentUser = getCurrentUser();
        Long operatorId = currentUser.getId();

        ChatGroup chatGroup = chatGroupRepository.findById(groupId);
        if (chatGroup == null) {
            throw new BusinessException(Map.of("error", "群聊不存在"));
        }

        ChatGroupMember operator = chatGroupRepository.findGroupMember(groupId, operatorId);
        if (operator == null || operator.getStatus() != 10) {
            throw new BusinessException(Map.of("error", "您不是该群聊成员"));
        }

        if (operatorId.equals(userId)) {
            chatGroupRepository.deleteGroupMember(groupId, userId);
        } else {
            if (operator.getRole() < 20) {
                throw new BusinessException(Map.of("error", "只有群主或管理员才能移除成员"));
            }

            ChatGroupMember targetMember = chatGroupRepository.findGroupMember(groupId, userId);
            if (targetMember != null && targetMember.getRole() >= operator.getRole()) {
                throw new BusinessException(Map.of("error", "无权操作该成员"));
            }

            chatGroupRepository.deleteGroupMember(groupId, userId);
        }
    }

    @Override
    @Transactional
    public ChatGroupMessage sendMessage(String groupId, String content, String fileServerAddress) {
        AccessUser currentUser = getCurrentUser();
        Long userId = currentUser.getId();
        String userName = currentUser.getUserName();

        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(Map.of("content", "消息内容不能为空"));
        }

        ChatGroup chatGroup = chatGroupRepository.findById(groupId);
        if (chatGroup == null) {
            throw new BusinessException(Map.of("error", "群聊不存在"));
        }

        if (chatGroup.getStatus() != 10) {
            throw new BusinessException(Map.of("error", "该群聊已解散"));
        }

        ChatGroupMember member = chatGroupRepository.findGroupMember(groupId, userId);
        if (member == null || member.getStatus() != 10) {
            throw new BusinessException(Map.of("error", "您不是该群聊成员"));
        }

        User user = userRepository.findUserById(userId);
        ChatGroupMessage message = new ChatGroupMessage();
        message.setId(UUID.randomUUID().toString());
        message.setGroupId(groupId);
        message.setSenderId(userId);
        message.setSenderName(userName);
        message.setSenderNickname(user != null ? user.getNickname() : userName);
        message.setSenderAvatarName(user != null ? user.getAvatarName() : null);
        message.setContent(content);
        message.setStatus(10);
        chatGroupRepository.saveMessage(message);

        if (user != null && user.getAvatarName() != null && !user.getAvatarName().trim().isEmpty()) {
            message.setSenderAvatarPath(fileServerAddress + user.getAvatarPath());
        }
        return message;
    }

    @Override
    public Map<String, Object> getMessageList(String groupId, int page, String fileServerAddress) {
        AccessUser currentUser = getCurrentUser();
        Long userId = currentUser.getId();

        ChatGroup chatGroup = chatGroupRepository.findById(groupId);
        if (chatGroup == null) {
            throw new BusinessException(Map.of("error", "群聊不存在"));
        }

        ChatGroupMember member = chatGroupRepository.findGroupMember(groupId, userId);
        if (member == null || member.getStatus() != 10) {
            throw new BusinessException(Map.of("error", "您不是该群聊成员"));
        }

        Map<String, Object> returnValue = new HashMap<>();
        PageView<ChatGroupMessage> pageView = new PageView<>(settingRepository.findSystemSetting_cache().getBackstagePageNumber(), page, 50);
        int firstIndex = (page - 1) * pageView.getMaxresult();

        QueryResult<ChatGroupMessage> qr = chatGroupRepository.findGroupMessages(groupId, firstIndex, pageView.getMaxresult());
        if (qr != null && qr.getResultlist() != null) {
            for (ChatGroupMessage message : qr.getResultlist()) {
                User sender = userRepository.findUserById(message.getSenderId());
                if (sender != null && sender.getAvatarName() != null && !sender.getAvatarName().trim().isEmpty()) {
                    message.setSenderAvatarPath(fileServerAddress + sender.getAvatarPath());
                }
                message.setIsRead(chatGroupRepository.findReadStatus(message.getId(), userId) != null);
            }
            Collections.reverse(qr.getResultlist());
        }
        pageView.setQueryResult(qr);
        returnValue.put("pageView", pageView);
        returnValue.put("unreadCount", chatGroupRepository.countUnreadMessages(groupId, userId));
        return returnValue;
    }

    @Override
    @Transactional
    public void markMessageAsRead(String messageId) {
        AccessUser currentUser = getCurrentUser();
        Long userId = currentUser.getId();
        chatGroupRepository.markMessageAsRead(messageId, userId);
    }

    @Override
    @Transactional
    public void markAllMessagesAsRead(String groupId) {
        AccessUser currentUser = getCurrentUser();
        Long userId = currentUser.getId();
        chatGroupRepository.markAllMessagesAsRead(groupId, userId);
    }

    @Override
    public Long getUnreadCount(String groupId) {
        AccessUser currentUser = getCurrentUser();
        Long userId = currentUser.getId();
        return chatGroupRepository.countUnreadMessages(groupId, userId);
    }

    @Override
    public Long getAllUnreadCount() {
        AccessUser currentUser = getCurrentUser();
        Long userId = currentUser.getId();
        return chatGroupRepository.countAllUnreadMessages(userId);
    }

    @Override
    public Map<String, Object> getMemberList(String groupId, int page, String fileServerAddress) {
        AccessUser currentUser = getCurrentUser();
        Long userId = currentUser.getId();

        ChatGroup chatGroup = chatGroupRepository.findById(groupId);
        if (chatGroup == null) {
            throw new BusinessException(Map.of("error", "群聊不存在"));
        }

        ChatGroupMember member = chatGroupRepository.findGroupMember(groupId, userId);
        if (member == null || member.getStatus() != 10) {
            throw new BusinessException(Map.of("error", "您不是该群聊成员"));
        }

        Map<String, Object> returnValue = new HashMap<>();
        PageView<ChatGroupMember> pageView = new PageView<>(settingRepository.findSystemSetting_cache().getBackstagePageNumber(), page, 20);
        int firstIndex = (page - 1) * pageView.getMaxresult();

        QueryResult<ChatGroupMember> qr = chatGroupRepository.findGroupMembersPage(groupId, firstIndex, pageView.getMaxresult());
        if (qr != null && qr.getResultlist() != null) {
            for (ChatGroupMember m : qr.getResultlist()) {
                User user = userRepository.findUserById(m.getUserId());
                if (user != null) {
                    m.setAccount(user.getAccount());
                    if (user.getAvatarName() != null && !user.getAvatarName().trim().isEmpty()) {
                        m.setAvatarPath(fileServerAddress + user.getAvatarPath());
                    }
                }
            }
        }
        pageView.setQueryResult(qr);
        returnValue.put("pageView", pageView);
        return returnValue;
    }

    @Override
    @Transactional
    public void dissolveGroup(String groupId) {
        AccessUser currentUser = getCurrentUser();
        Long userId = currentUser.getId();

        ChatGroup chatGroup = chatGroupRepository.findById(groupId);
        if (chatGroup == null) {
            throw new BusinessException(Map.of("error", "群聊不存在"));
        }

        if (!chatGroup.getOwnerId().equals(userId)) {
            throw new BusinessException(Map.of("error", "只有群主才能解散群聊"));
        }

        chatGroupRepository.dissolveGroup(groupId);
    }

    @Override
    public PageView<ChatGroup> getAdminGroupList(int page, Integer status) {
        PageView<ChatGroup> pageView = new PageView<>(settingRepository.findSystemSetting_cache().getBackstagePageNumber(), page, 20);
        int firstIndex = (page - 1) * pageView.getMaxresult();

        QueryResult<ChatGroup> qr;
        if (status != null) {
            qr = chatGroupRepository.findChatGroupListByStatus(status, firstIndex, pageView.getMaxresult());
        } else {
            qr = chatGroupRepository.findChatGroupList(firstIndex, pageView.getMaxresult());
        }

        if (qr != null && qr.getResultlist() != null) {
            for (ChatGroup group : qr.getResultlist()) {
                group.setMemberCount(chatGroupRepository.countGroupMembers(group.getId()).intValue());
                group.setUnreadCount(chatGroupRepository.countGroupMessages(group.getId()).intValue());
            }
        }
        pageView.setQueryResult(qr);
        return pageView;
    }

    @Override
    public ChatGroup getAdminGroupDetail(String groupId) {
        ChatGroup chatGroup = chatGroupRepository.findById(groupId);
        if (chatGroup != null) {
            chatGroup.setMemberCount(chatGroupRepository.countGroupMembers(groupId).intValue());
            chatGroup.setUnreadCount(chatGroupRepository.countGroupMessages(groupId).intValue());
        }
        return chatGroup;
    }

    @Override
    @Transactional
    public void adminDissolveGroup(String groupId) {
        ChatGroup chatGroup = chatGroupRepository.findById(groupId);
        if (chatGroup == null) {
            throw new BusinessException(Map.of("error", "群聊不存在"));
        }
        chatGroupRepository.dissolveGroup(groupId);
    }

    @Override
    @Transactional
    public void adminDeleteGroup(String groupId) {
        ChatGroup chatGroup = chatGroupRepository.findById(groupId);
        if (chatGroup == null) {
            throw new BusinessException(Map.of("error", "群聊不存在"));
        }
        chatGroupRepository.deleteChatGroup(groupId);
    }
}
